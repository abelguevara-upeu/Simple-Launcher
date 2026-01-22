package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.launcher.model.Library;
import com.launcher.model.Version;

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

        if (version.getLibraries() == null)
            return;

        for (Library lib : version.getLibraries()) {
            if (shouldDownload(lib)) {
                try {
                    downloadLibrary(lib);
                    count++;
                } catch (IOException e) {
                    System.err.println("Error downloading library: " + lib.getName());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Downloaded " + count + " libraries for " + this.osName);
    }

    private boolean shouldDownload(Library lib) {
        // Use the model's logic
        return lib.appliesTo(this.osName, System.getProperty("os.arch"));
    }

    private void downloadLibrary(Library lib) throws IOException {
        File libFile = null;
        String url = null;

        // Vanilla Libraries
        if (lib.getDownloads() != null && lib.getDownloads().getArtifact() != null) {
            libFile = new File(librariesDir, lib.getDownloads().getArtifact().getPath());
            url = lib.getDownloads().getArtifact().getUrl();
        }
        // Fabric/Forge Libraries (Maven Central)
        else if (lib.getName() != null) {
            String[] parts = lib.getName().split(":");
            String group = parts[0].replace('.', '/');
            String artifact = parts[1];
            String version = parts[2];

            String path = group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".jar";
            libFile = new File(librariesDir, path);

            // If JSON has a custom URL, use it, otherwise use Maven Central
            String baseUrl = (lib.getUrl() != null) ? lib.getUrl() : "https://repo1.maven.org/maven2/";
            url = baseUrl + path;
        }

        if (libFile != null && !libFile.exists()) {
            System.out.println("Downloading library: " + lib.getName());
            libFile.getParentFile().mkdirs();

            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
