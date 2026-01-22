package com.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.internal.LinkedTreeMap;
import com.launcher.auth.OfflineAuthenticator;
import com.launcher.model.Version;
import com.launcher.model.Library;

public class GameLauncher {
    private final File workDir;
    private final String osName;
    private final String osArch;

    public GameLauncher(File workDir) {
        this.workDir = workDir;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            this.osName = "windows";
        else if (os.contains("mac"))
            this.osName = "osx";
        else
            this.osName = "linux";

        this.osArch = System.getProperty("os.arch");
    }

    public void launch(Version version, OfflineAuthenticator.Session session, int ramMB)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        // Java executable
        String javaPath = System.getProperty("java.home") + "/bin/java";
        command.add(javaPath);

        // Prepare variables for substitution
        Map<String, String> variables = new HashMap<>();
        variables.put("natives_directory", new File(workDir, "natives").getAbsolutePath()); // Simplified
        variables.put("launcher_name", "SimpleLauncher");
        variables.put("launcher_version", "1.0");
        variables.put("auth_player_name", session.username);
        variables.put("version_name", version.getId());
        variables.put("game_directory", workDir.getAbsolutePath());
        variables.put("library_directory", new File(workDir, "libraries").getAbsolutePath());
        variables.put("classpath_separator", System.getProperty("path.separator"));
        variables.put("assets_root", new File(workDir, "assets").getAbsolutePath());
        variables.put("resolution_width", "854");
        variables.put("resolution_height", "480");
        variables.put("assets_index_name",
                version.getAssetIndex() != null ? version.getAssetIndex().getId() : "legacy");
        variables.put("auth_uuid", session.uuid);
        variables.put("auth_access_token", session.accessToken);
        variables.put("user_type", session.userType);
        variables.put("version_type", "release");

        // Quick Play & Auth Placeholders (Fixes 'Failed to Quick Play' errors)
        variables.put("clientid", "0");
        variables.put("auth_xuid", "0000000000000000");
        variables.put("quickPlayPath", new File(workDir, "quickPlay").getAbsolutePath());
        variables.put("quickPlaySingleplayer", "");
        variables.put("quickPlayMultiplayer", "");
        variables.put("quickPlayRealms", "");

        variables.put("classpath", buildClasspath(version));

        // Standard JVM Arguments (RAM, OS specifics) - Applied to ALL versions
        command.add("-Xmx" + ramMB + "M"); // Dynamic RAM

        if (this.osName.equals("osx")) {
            command.add("-XstartOnFirstThread");
            command.add("-Dfml.earlyprogresswindow=false");
        }

        // Modern Arguments (1.13+)
        if (version.getArguments() != null) {
            // JVM Arguments from version.json
            if (version.getArguments().getJvm() != null) {
                for (Object arg : version.getArguments().getJvm()) {
                    processArgument(command, arg, variables);
                }
            } else {
                // Default JVM args if missing in modern version
                command.add("-Djava.library.path=" + variables.get("natives_directory"));
                command.add("-cp");
                command.add(variables.get("classpath"));
            }

            // Add Main Class
            command.add(version.getMainClass());

            // Game Arguments
            if (version.getArguments().getGame() != null) {
                for (Object arg : version.getArguments().getGame()) {
                    processArgument(command, arg, variables);
                }
            }
        }
        // Legacy Arguments (<1.13)
        else {
            command.add("-Djava.library.path=" + variables.get("natives_directory"));
            command.add("-cp");
            command.add(variables.get("classpath"));

            command.add(version.getMainClass());

            // Parse minecraftArguments string
            if (version.getMinecraftArguments() != null) {
                String[] args = version.getMinecraftArguments().split(" ");
                for (String arg : args) {
                    command.add(replaceVariables(arg, variables));
                }
            }
        }

        // Execute
        System.out.println("Executing command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }

    private void processArgument(List<String> command, Object arg, Map<String, String> variables) {
        if (arg instanceof String) {
            command.add(replaceVariables((String) arg, variables));
        } else if (arg instanceof LinkedTreeMap) {
            // { "rules": [...], "value": ["--foo", "bar"] } or "value": "--foo"
            LinkedTreeMap<?, ?> map = (LinkedTreeMap<?, ?>) arg;

            // To check rules here, we would need to deserialize them to Library.Rule
            // objects
            // However, arguments in 'arguments' field are generic objects.
            // For now, implementing basic rule check locally for arguments
            // or we could map map.get("rules") -> List<Rule>

            // Note: Since 'arguments' in Version is now List<Object>, this remains tricky.
            // But previous code called local checkRules(map.get("rules"))
            // I will keep a simplified local checkRules for arguments OR adapt it.
            // The user wants patterns. Duplicate logic is bad.
            // But converting LinkedTreeMap to List<Rule> is annoying without Gson here.

            // Let's use the helper we are about to inject, or honestly,
            // KEEP checkRules BUT only for Arguments if strictly necessary?
            // Actually, I can construct a temporary Library or Rule object.

            if (checkRules(map.get("rules"))) {
                Object value = map.get("value");
                if (value instanceof String) {
                    command.add(replaceVariables((String) value, variables));
                } else if (value instanceof List) {
                    for (Object v : (List<?>) value) {
                        if (v instanceof String) {
                            command.add(replaceVariables((String) v, variables));
                        }
                    }
                }
            }
        }
    }

    private boolean checkRules(Object rulesObj) {
        if (rulesObj == null)
            return true;
        if (!(rulesObj instanceof List))
            return true; // Should be a list

        List<?> rules = (List<?>) rulesObj;
        boolean allow = false;

        for (Object ruleObj : rules) {
            if (ruleObj instanceof LinkedTreeMap) {
                LinkedTreeMap<?, ?> rule = (LinkedTreeMap<?, ?>) ruleObj;
                String action = (String) rule.get("action");
                boolean matches = true;

                if (rule.containsKey("os")) {
                    LinkedTreeMap<?, ?> osParams = (LinkedTreeMap<?, ?>) rule.get("os");
                    if (osParams.containsKey("name")) {
                        String name = (String) osParams.get("name");
                        if (!name.equals(this.osName))
                            matches = false;
                    }
                    if (osParams.containsKey("arch")) {
                        String arch = (String) osParams.get("arch");
                        if (this.osArch.equals("aarch64") && !arch.equals("arm64"))
                            matches = false;
                    }
                }

                if (rule.containsKey("features")) {
                    LinkedTreeMap<?, ?> features = (LinkedTreeMap<?, ?>) rule.get("features");
                    for (Object keyObj : features.keySet()) {
                        String key = (String) keyObj;
                        Boolean required = (Boolean) features.get(key); // Requirement (usually true)

                        boolean hasFeature = false; // Default state
                        if (key.equals("is_demo_user"))
                            hasFeature = false;
                        else if (key.equals("has_custom_resolution"))
                            hasFeature = true;

                        if (hasFeature != required) {
                            matches = false;
                        }
                    }
                }

                if (matches) {
                    allow = "allow".equals(action);
                }
            }
        }

        return allow;
    }

    private String replaceVariables(String arg, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            arg = arg.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return arg;
    }

    private String buildClasspath(Version version) {
        StringBuilder cp = new StringBuilder();
        String separator = System.getProperty("path.separator");
        java.util.Set<String> addedPaths = new java.util.HashSet<>();
        java.util.Set<String> addedArtifacts = new java.util.HashSet<>();

        // Add libraries
        if (version.getLibraries() != null) {
            for (Library lib : version.getLibraries()) {
                // Strict filter for 1.21+ log4j-slf4j-impl conflict
                // Version ID for NeoForge 1.21 is "neoforge-21.x.x", vanilla is "1.21.x"
                boolean isModern = version.getId().contains("1.21") || version.getId().contains("neoforge-21");

                if (isModern && lib.getName() != null && lib.getName().contains(":log4j-slf4j-impl:")) {
                    System.out.println(
                            "Skipping conflicting library: " + lib.getName() + " for version " + version.getId());
                    continue;
                }

                // Deduplication by Artifact ID (keep first/newest)
                if (lib.getName() != null) {
                    if (!lib.appliesTo(this.osName, this.osArch)) {
                        continue;
                    }

                    String[] parts = lib.getName().split(":");
                    if (parts.length >= 2) {
                        String key = parts[0] + ":" + parts[1];
                        // Important: Include classifier in key (e.g. natives) to avoid filtering
                        // natives
                        if (parts.length > 3) {
                            key += ":" + parts[3];
                        }

                        if (addedArtifacts.contains(key)) {
                            // System.out.println("Skipping duplicate library version: " + lib.name);
                            continue;
                        }
                        addedArtifacts.add(key);
                    }
                }

                File libFile = null;

                // Vanilla (downloads)
                if (lib.getDownloads() != null && lib.getDownloads().getArtifact() != null) {
                    libFile = new File(workDir, "libraries/" + lib.getDownloads().getArtifact().getPath());
                }
                // Fabric/Forge/NeoForge (Maven)
                else if (lib.getName() != null) {
                    String[] parts = lib.getName().split(":");
                    String group = parts[0].replace('.', '/');
                    String artifact = parts[1];
                    String libVersion = parts[2];
                    String path = group + "/" + artifact + "/" + libVersion + "/" + artifact + "-" + libVersion
                            + ".jar";
                    libFile = new File(workDir, "libraries/" + path);
                }

                if (libFile != null) {
                    String absPath = libFile.getAbsolutePath();
                    if (addedPaths.add(absPath)) { // Only add if not already present
                        if (cp.length() > 0)
                            cp.append(separator);
                        cp.append(absPath);
                    }
                }
            }
        }

        // Add game JAR
        // For NeoForge, the game data is provided by the libraries (client-srg, etc).
        // Adding the vanilla JAR causes a module conflict (_1._20._4 vs minecraft).
        if (!version.getId().toLowerCase().contains("neoforge") && !version.getId().toLowerCase().contains("forge")) {
            String jarId = version.getInheritsFrom() != null ? version.getInheritsFrom() : version.getId();
            File clientJar = new File(workDir, "versions/" + jarId + "/" + jarId + ".jar");
            String clientPath = clientJar.getAbsolutePath();
            if (addedPaths.add(clientPath)) {
                if (cp.length() > 0)
                    cp.append(separator);
                cp.append(clientPath);
            }
        }

        return cp.toString();
    }
}
