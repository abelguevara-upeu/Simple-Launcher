package com.launcher.model;

import java.util.List;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Represents a library dependency for the game.
 * Follows the layout of the Minecraft version manifest libraries.
 */
public class Library {
    private String name;
    private String cancel; // Sometimes present to cancel previous libs
    private String url; // For custom Maven repositories
    private Downloads downloads;
    private List<Rule> rules;
    private Extract extract;
    private Object natives; // Can be a recursive map/object, usually Map<String, String>

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Downloads getDownloads() {
        return downloads;
    }

    public void setDownloads(Downloads downloads) {
        this.downloads = downloads;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public Object getNatives() {
        return natives;
    }

    public boolean appliesTo(String currentOsName, String currentOsArch) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }

        boolean allow = false; // Default behavior if rules exist is usually restrictive unless 'allow' is
                               // found?
        // Actually, logic is: iterate all rules. If a rule matches, apply its action.
        // Default (no rules) is allowed.
        // If rules exist, implicit start depends on the first rule?
        // Usually, the logic is: "First matching rule determines the result". Or "Last
        // matching rule"?
        // Mojang launcher logic: "The rules are processed in order. The first rule that
        // matches and has an action determines the result?"
        // Re-reading common logic (and GameLauncher implementation):
        // GameLauncher iterates ALL rules. `if (matches) { allow =
        // "allow".equals(action); }`
        // consistently overwrites `allow`. So it is "Last Matching Rule Wins".

        allow = false; // Start as disallowed?
        // Wait, if the first rule is "allow", then it allows.
        // Standard usually:
        // If no rules -> Allow.
        // If rules -> Check them. logic often implies "disallow unless allowed" OR
        // "allow unless disallowed".
        // Looking at GameLauncher line 156: `boolean allow = false;`
        // So default is FALSE if rules exist.

        for (Rule rule : rules) {
            if (rule.applies(currentOsName, currentOsArch)) {
                if ("allow".equals(rule.getAction())) {
                    allow = true;
                } else {
                    allow = false;
                }
            }
        }
        return allow;
    }

    public static class Downloads {
        private Artifact artifact;
        private Artifact classifiers; // For natives

        public Artifact getArtifact() {
            return artifact;
        }

        public void setArtifact(Artifact artifact) {
            this.artifact = artifact;
        }

        // Handling classifiers might be complex due to map structure in JSON?
        // Usually "classifiers": { "natives-osx": { ... } }
        // For now simplifying to simple Artifact, but keeping note.
    }

    public static class Artifact {
        private String path;
        private String sha1;
        private int size;
        private String url;

        public String getPath() {
            return path;
        }

        public String getSha1() {
            return sha1;
        }

        public int getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Rule {
        private String action; // "allow" or "disallow"
        private OS os;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public OS getOs() {
            return os;
        }

        public void setOs(OS os) {
            this.os = os;
        }

        public boolean applies(String currentOsName, String currentOsArch) {
            if (os == null)
                return true; // No OS restriction -> Applies to all
            return os.matches(currentOsName, currentOsArch);
        }
    }

    public static class OS {
        private String name;
        private String arch;
        private String version; // Regex for regex matching

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean matches(String currentOsName, String currentOsArch) {
            if (name != null && !name.equals(currentOsName)) {
                return false;
            }
            if (arch != null) {
                // simple arch check
                // if (this.osArch.equals("aarch64") && !arch.equals("arm64")) matches = false;
                // Translating GameLauncher logic:
                if (currentOsArch.equals("aarch64") && !arch.equals("arm64")) {
                    // logic is fuzzy in GameLauncher.
                    // It says: if system is aarch64 (M1 Mac), and rule requires something ELSE
                    // (wait, logically?)
                    // GameLauncher Line 173: `if (this.osArch.equals("aarch64") &&
                    // !arch.equals("arm64")) matches = false;`
                    // This implies: If system is aarch64, valid rule arch must be "arm64".
                    // If rule arch is "x86", it does not match.
                    // But if system is "x86_64", line 173 condition is false (first part false), so
                    // it continues?
                    // Wait, generic check: `if (arch != null && !arch.equals(currentOsArch))`?
                    // Verify logic.
                    return arch.equals("arm64") ? currentOsArch.equals("aarch64") : arch.equals(currentOsArch);
                }
            }
            return true;
        }
    }

    public static class Extract {
        private List<String> exclude;
        // defaults
    }
}
