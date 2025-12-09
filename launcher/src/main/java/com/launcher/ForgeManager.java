package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ForgeManager {
    private final File versionsDir;
    private final File librariesDir;

    public ForgeManager(File workDir) {
        this.versionsDir = new File(workDir, "versions");
        this.librariesDir = new File(workDir, "libraries");
    }

    public String installForge(String gameVersion, String forgeVersion) throws IOException {
        String fullVersion = gameVersion + "-" + forgeVersion;
        // Standard Forge version ID format: 1.20.1-forge-47.2.0
        String versionId = gameVersion + "-forge-" + forgeVersion;

        File forgeVersionDir = new File(versionsDir, versionId);
        File forgeJson = new File(forgeVersionDir, versionId + ".json");
        File clientJar = new File(librariesDir,
                "net/minecraftforge/forge/" + fullVersion + "/forge-" + fullVersion + "-client.jar");

        if (forgeJson.exists()) {
            System.out.println("Forge " + versionId + " appears to be installed.");
            return versionId;
        }

        System.out.println("Forge " + versionId + " not found. Running installer...");

        String installerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + fullVersion + "/forge-"
                + fullVersion + "-installer.jar";
        File installerFile = new File(this.versionsDir.getParentFile(), "forge-installer.jar");

        downloadFile(installerUrl, installerFile);

        File profilesFile = new File(this.versionsDir.getParentFile(), "launcher_profiles.json");
        if (!profilesFile.exists()) {
            Files.write(profilesFile.toPath(), "{\"profiles\":{}}".getBytes());
        }

        ProcessBuilder pb = new ProcessBuilder(
                System.getProperty("java.home") + "/bin/java",
                "-jar",
                installerFile.getAbsolutePath(),
                "--installClient",
                this.versionsDir.getParentFile().getAbsolutePath());
        pb.directory(this.versionsDir.getParentFile());
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            System.out.println("Executing Forge Installer JAR: " + installerFile.getAbsolutePath());
            Process process = pb.start();
            int code = process.waitFor();

            if (code != 0) {
                System.err.println("Forge installer exited with code " + code);
                throw new IOException("Forge installer failed with exit code: " + code);
            }
            System.out.println("Forge Installer finished successfully.");

            installerFile.delete();

            return versionId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during installation", e);
        }
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        System.out.println("Downloading: " + urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
