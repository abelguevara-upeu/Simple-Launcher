package com.launcher;

import java.util.List;

public class Library {
    public String name;
    public String url; // For custom Maven repositories (Fabric)
    public Downloads downloads;
    public List<Rule> rules;

    public static class Downloads {
        public Artifact artifact;
    }

    public static class Artifact {
        public String path;
        public String sha1;
        public int size;
        public String url;
    }

    public static class Rule {
        public String action; // allow or disallow
        public OS os; // os name
    }

    public static class OS {
        public String name;
    }
}
