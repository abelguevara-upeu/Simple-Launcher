package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LibraryManager {
    private final File librariesDir;
    private final String osName;

    public LibraryManager(File workDir) {
        this.librariesDir = new File(workDir, "libraries");

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            this.osName = "windows";
        } else if (os.contains("mac")) {
            this.osName = "osx";
        } else {
            this.osName = "linux";
        }
    }

    public void downloadLibraries(Version version) {
        System.out.println("Verified libraries for " + this.osName + "...");
        int count = 0;

        if (version.libraries == null)
            return;

        for (Library lib : version.libraries) {
            if (shouldDownload(lib)) {
                try {
                    downloadLibrary(lib);
                    count++;
                } catch (IOException e) {
                    System.err.println("Error downloading library: " + lib.name);
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Downloaded " + count + " libraries for " + this.osName);
    }

    private boolean shouldDownload(Library lib) {
        if (lib.rules == null)
            return true; // If no rules, download always

        boolean allow = false;
        for (Library.Rule rule : lib.rules) {
            if (rule.os == null) {
                allow = rule.action.equals("allow");

            } else if (rule.os.name.equals(this.osName)) {
                allow = rule.action.equals("allow");
            }
        }
        return allow;
    }

    private void downloadLibrary(Library lib) throws IOException {
        if (lib.downloads == null || lib.downloads.artifact == null)
            return;

        Library.Artifact artifact = lib.downloads.artifact;
        File libFile = new File(librariesDir, artifact.path);

        if (!libFile.exists()) {
            System.out.println("Downloading library: " + lib.name);
            libFile.getParentFile().mkdirs();

            try (InputStream in = new URL(artifact.url).openStream()) {
                Files.copy(in, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
