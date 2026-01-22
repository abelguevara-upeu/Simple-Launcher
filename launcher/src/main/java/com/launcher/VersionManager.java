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
import com.launcher.model.Version;
import com.launcher.model.Library;

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
            version.setId(versionId);

            if (version.getInheritsFrom() != null && !version.getInheritsFrom().isEmpty()) {
                System.out.println("Inheriting from " + versionId + " -> " + version.getInheritsFrom());

                File parentFile = new File(versionsDir,
                        version.getInheritsFrom() + "/" + version.getInheritsFrom() + ".json");
                if (!parentFile.exists()) {
                    System.out.println(
                            "Parent version " + version.getInheritsFrom() + " not found locally. Downloading...");
                    downloadVersionIndex(version.getInheritsFrom());
                }

                Version parent = loadVersion(version.getInheritsFrom());
                mergeVersions(version, parent);
            }

            return version;
        }
    }

    private void mergeVersions(Version child, Version parent) {
        // Merge libraries
        if (parent.getLibraries() != null) {
            if (child.getLibraries() == null) {
                child.setLibraries(parent.getLibraries());
            } else {
                child.getLibraries().addAll(parent.getLibraries());
            }

            // Merge basic arguments
            if (child.getMainClass() == null)
                child.setMainClass(parent.getMainClass());
            if (child.getMinecraftArguments() == null)
                child.setMinecraftArguments(parent.getMinecraftArguments());
            // If child has not asset index, inherit assets
            if (child.getAssetIndex() == null)
                child.setAssetIndex(parent.getAssetIndex());
        }

        // Merge modern arguments (game/jvm)
        if (parent.getArguments() != null) {
            if (child.getArguments() == null) {
                child.setArguments(new Version.Arguments());
            }
            if (child.getArguments().getGame() == null) {
                child.getArguments().setGame(parent.getArguments().getGame());
            } else if (parent.getArguments().getGame() != null) {
                child.getArguments().getGame().addAll(parent.getArguments().getGame());
            }

            if (child.getArguments().getJvm() == null) {
                child.getArguments().setJvm(parent.getArguments().getJvm());
            } else if (parent.getArguments().getJvm() != null) {
                child.getArguments().getJvm().addAll(parent.getArguments().getJvm());
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
        if (version.getDownloads() == null || version.getDownloads().getClient() == null)
            return;

        File jarFile = new File(versionsDir, version.getId() + "/" + version.getId() + ".jar");

        if (!jarFile.exists()) {
            System.out.println("Descargando JAR para " + version.getId());
            downloadFile(version.getDownloads().getClient().getUrl(), jarFile);
        }
    }
}