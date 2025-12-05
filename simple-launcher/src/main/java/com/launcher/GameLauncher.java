package com.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.launcher.auth.OfflineAuthenticator;

public class GameLauncher {
    private final File workDir;
    private final String osName;

    public GameLauncher(File workDir) {
        this.workDir = workDir;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            this.osName = "windows";
        else if (os.contains("mac"))
            this.osName = "osx";
        else
            this.osName = "linux";
    }

    public void launch(Version version, OfflineAuthenticator.Session session) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        // Java executable
        command.add(System.getProperty("java.home") + "/bin/java");

        // Memory arguments (RAM)
        command.add("-Xmx2G"); // 2GB max

        // macOS specific argument (Fixes GLFW crash)
        if (this.osName.equals("osx")) {
            command.add("-XstartOnFirstThread");
        }

        // Classpath (Libraries + Game)
        command.add("-cp");
        command.add(buildClasspath(version));

        // Main class
        command.add(version.mainClass);

        // Game arguments (User, Version, Assets, etc)
        addGameArguments(command, version, session);

        // Execute
        System.out.println("Executing command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.inheritIO(); // Show output game in console
        Process process = pb.start();
        process.waitFor();
    }

    private String buildClasspath(Version version) {
        StringBuilder cp = new StringBuilder();
        String separator = System.getProperty("path.separator"); // ":" for Linux/Mac, ";" for Windows

        // Add libraries
        for (Library lib : version.libraries) {
            if (lib.downloads != null && lib.downloads.artifact != null) {
                // TODO: Handle classifiers rules like on LibraryManager
                File libFile = new File(workDir, "libraries/" + lib.downloads.artifact.path);
                cp.append(libFile.getAbsolutePath()).append(separator);
            }
        }

        // Add game JAR
        File clientJar = new File(workDir, "versions/" + version.id + "/" + version.id + ".jar");
        cp.append(clientJar.getAbsolutePath());

        return cp.toString();
    }

    private void addGameArguments(List<String> cmd, Version version, OfflineAuthenticator.Session session) {
        cmd.add("--username");
        cmd.add(session.username);
        cmd.add("--version");
        cmd.add(version.id);
        cmd.add("--gameDir");
        cmd.add(workDir.getAbsolutePath());
        cmd.add("--assetsDir");
        cmd.add(new File(workDir, "assets").getAbsolutePath());
        cmd.add("--assetIndex");
        cmd.add(version.assetIndex.id);
        cmd.add("--uuid");
        cmd.add(session.uuid);
        cmd.add("--accessToken");
        cmd.add(session.accessToken);
        cmd.add("--userType");
        cmd.add(session.userType);
    }
}
