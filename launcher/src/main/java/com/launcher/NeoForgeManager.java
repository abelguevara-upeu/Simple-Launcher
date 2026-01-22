package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.launcher.service.VersionInstaller;

public class NeoForgeManager implements VersionInstaller {
    private final File versionsDir;
    private final File librariesDir;

    public NeoForgeManager(File workDir) {
        this.versionsDir = new File(workDir, "versions");
        this.librariesDir = new File(workDir, "libraries");
    }

    @Override
    public String install(String gameVersion, String neoforgeVersion) throws IOException {
        String versionId = "neoforge-" + neoforgeVersion;
        File neoforgeVersionDir = new File(versionsDir, versionId);
        File neoforgeJson = new File(neoforgeVersionDir, versionId + ".json");

        // Check if already installed (we use the existence of the client jar as a
        // marker,
        // as well as the JSON which the installer creates)
        // NeoForge 20.4+ uses a split client jar structure.
        File clientJar = new File(librariesDir,
                "net/neoforged/neoforge/" + neoforgeVersion + "/neoforge-" + neoforgeVersion + "-client.jar");

        if (neoforgeJson.exists() && clientJar.exists()) {
            System.out.println("NeoForge " + versionId + " appears to be installed.");
            return versionId;
        }

        System.out.println("NeoForge " + versionId + " not found. Running installer...");

        // 1. Download Installer
        String installerUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/" + neoforgeVersion
                + "/neoforge-" + neoforgeVersion + "-installer.jar";
        File installerFile = new File(this.versionsDir.getParentFile(), "installer.jar");
        // Save in root temporarily or define a cache. workDir is
        // versionsDir.getParent()

        downloadFile(installerUrl, installerFile);

        // 2. Ensure launcher_profiles.json exists (required by installer)
        File profilesFile = new File(this.versionsDir.getParentFile(), "launcher_profiles.json");
        if (!profilesFile.exists()) {
            Files.write(profilesFile.toPath(), "{\"profiles\":{}}".getBytes());
        }

        // 3. Run Installer
        // java -jar installer.jar --installClient [workDir]
        ProcessBuilder pb = new ProcessBuilder(
                System.getProperty("java.home") + "/bin/java",
                "-jar",
                installerFile.getAbsolutePath(),
                "--installClient",
                this.versionsDir.getParentFile().getAbsolutePath());
        pb.directory(this.versionsDir.getParentFile());
        pb.inheritIO();

        try {
            System.out.println("Executing NeoForge Installer. This may take a while...");
            Process process = pb.start();
            int code = process.waitFor();
            if (code != 0) {
                throw new IOException("NeoForge installer failed with exit code: " + code);
            }
            System.out.println("NeoForge Installer finished successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during installation", e);
        } finally {
            installerFile.delete(); // Cleanup
        }

        return versionId;
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
