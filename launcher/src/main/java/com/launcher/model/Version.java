package com.launcher.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a specific version of Minecraft (e.g., 1.20.1, 1.8.9).
 */
public class Version {
    private String id;
    private String inheritsFrom;
    private String mainClass;
    private String minecraftArguments; // Legacy < 1.13
    private Arguments arguments; // Modern >= 1.13
    private List<Library> libraries;
    private AssetIndex assetIndex;
    private Downloads downloads;
    private String type; // release, snapshot, old_beta, etc.
    private String time;
    private String releaseTime;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public void setInheritsFrom(String inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }

    public AssetIndex getAssetIndex() {
        return assetIndex;
    }

    public void setAssetIndex(AssetIndex assetIndex) {
        this.assetIndex = assetIndex;
    }

    public Downloads getDownloads() {
        return downloads;
    }

    public void setDownloads(Downloads downloads) {
        this.downloads = downloads;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLegacy() {
        return minecraftArguments != null && (arguments == null);
    }

    public static class Arguments {
        private List<Object> game; // Can be String or Object(Rule)
        private List<Object> jvm;

        public List<Object> getGame() {
            return game;
        }

        public void setGame(List<Object> game) {
            this.game = game;
        }

        public List<Object> getJvm() {
            return jvm;
        }

        public void setJvm(List<Object> jvm) {
            this.jvm = jvm;
        }
    }

    public static class AssetIndex {
        private String id;
        private String sha1;
        private int size;
        private int totalSize;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Downloads {
        private Library.Artifact client;
        private Library.Artifact server;

        public Library.Artifact getClient() {
            return client;
        }

        public void setClient(Library.Artifact client) {
            this.client = client;
        }
    }
}
