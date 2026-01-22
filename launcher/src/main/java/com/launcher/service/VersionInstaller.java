package com.launcher.service;

import java.io.IOException;

/**
 * Strategy interface for installing/setting up a Minecraft version.
 */
public interface VersionInstaller {
    /**
     * Installs or prepares the version.
     * 
     * @param mcVersion     The base Minecraft version (e.g. "1.20.1")
     * @param loaderVersion The loader version (e.g. "47.2.0" for Forge), or
     *                      null/empty for Vanilla.
     * @return The installed version ID (e.g. "1.20.1-forge-47.2.0").
     * @throws IOException If installation fails.
     */
    String install(String mcVersion, String loaderVersion) throws IOException;
}
