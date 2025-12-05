package com.launcher;

import java.util.List;

public class Version {
    public String id;
    public String inheritsFrom; // Important for Forge/Fabric
    public String mainClass;
    public String minecraftArguments; // For olds versions (<1.13)
    public Arguments arguments;
    public List<Library> libraries;
    public AssetIndex assetIndex;
    public Downloads downloads;

    public static class Arguments {
        public List<Object> game;
        public List<Object> jvm;
    }

    public static class AssetIndex {
        public String id;
        public String sha1;
        public int size;
        public int totalSize;
        public String url;
    }

    public static class Downloads {
        public Library.Artifact client;
        public Library.Artifact server;
    }
}
