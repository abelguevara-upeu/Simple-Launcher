package com.launcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

public class VersionManager {
    private final File versionsDir;
    private final Gson gson;

    public VersionManager(File workdir) {
        this.versionsDir = new File(workdir, "versions");
        this.gson = new Gson();
    }

    public Version loadVersion(String versionId) throws IOException {
        File jsonFile = new File(versionsDir, versionId + "/" + versionId + ".json");

        if (!jsonFile.exists()) {
            throw new IOException("Version file does not exist: " + jsonFile);
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Version version = gson.fromJson(reader, Version.class);
            version.id = versionId;

            if (version.inheritsFrom != null && !version.inheritsFrom.isEmpty()) {
                System.out.println("Inheriting from " + versionId + " -> " + version.inheritsFrom);

                File parentFile = new File(versionsDir, version.inheritsFrom + "/" + version.inheritsFrom + ".json");
                if (!parentFile.exists()) {
                    System.out.println("Parent version " + version.inheritsFrom + " not found locally. Downloading...");
                    downloadVersionIndex(version.inheritsFrom);
                }

                Version parent = loadVersion(version.inheritsFrom);
                mergeVersions(version, parent);
            }

            return version;
        }
    }

    private void mergeVersions(Version child, Version parent) {
        // Merge libraries
        if (parent.libraries != null) {
            if (child.libraries == null) {
                child.libraries = parent.libraries;
            } else {
                child.libraries.addAll(parent.libraries);
            }

            // Merge basic arguments
            if (child.mainClass == null)
                child.mainClass = parent.mainClass;
            if (child.minecraftArguments == null)
                child.minecraftArguments = parent.minecraftArguments;
            // If child has not asset index, inherit assets
            if (child.assetIndex == null)
                child.assetIndex = parent.assetIndex;
        }

        // Merge modern arguments (game/jvm)
        if (parent.arguments != null) {
            if (child.arguments == null) {
                child.arguments = new Version.Arguments();
            }
            if (child.arguments.game == null) {
                child.arguments.game = parent.arguments.game;
            } else if (parent.arguments.game != null) {
                child.arguments.game.addAll(parent.arguments.game);
            }

            if (child.arguments.jvm == null) {
                child.arguments.jvm = parent.arguments.jvm;
            } else if (parent.arguments.jvm != null) {
                child.arguments.jvm.addAll(parent.arguments.jvm);
            }
        }
    }

    public void downloadVersionIndex(String versionId) throws IOException {
        System.out.println("Buscando versión " + versionId + " en Mojang...");

        // Download version manifest
        String manifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
        String manifestJson = downloadString(manifestUrl);

        // Search url for specific version
        Manifest manifest = gson.fromJson(manifestJson, Manifest.class);
        String versionUrl = null;

        for (Manifest.VersionEntry v : manifest.versions) {
            if (v.id.equals(versionId)) {
                versionUrl = v.url;
                break;
            }
        }

        if (versionUrl == null) {
            throw new IOException("Version " + versionId + " not found on Mojang");
        }

        // Download version JSON
        File versionFile = new File(versionsDir, versionId + "/" + versionId + ".json");
        versionFile.getParentFile().mkdirs(); // Create version directory if it doesn't exist

        downloadFile(versionUrl, versionFile);
        System.out.println("Version JSON descargada: " + versionFile);

    }

    private static class Manifest {
        public List<VersionEntry> versions;

        public static class VersionEntry {
            public String id;
            public String url;
        }
    }

    private String downloadString(String urlStr) throws IOException {
        try (Scanner s = new Scanner(new URL(urlStr).openStream(), "UTF-8")) {
            return s.useDelimiter("\\A").next();
        }
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void downloadGameJar(Version version) throws IOException {
        if (version.downloads == null || version.downloads.client == null)
            return;

        File jarFile = new File(versionsDir, version.id + "/" + version.id + ".jar");

        if (!jarFile.exists()) {
            System.out.println("Descargando JAR para " + version.id);
            downloadFile(version.downloads.client.url, jarFile);
        }
    }
}