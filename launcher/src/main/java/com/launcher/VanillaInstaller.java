package com.launcher;

import com.launcher.service.VersionInstaller;
import java.io.File;
import java.io.IOException;

/**
 * Strategy implementation for Vanilla Minecraft versions.
 */
public class VanillaInstaller implements VersionInstaller {
    private final VersionManager versionManager;

    public VanillaInstaller(File workDir) {
        this.versionManager = new VersionManager(workDir);
    }

    // Alternative constructor if we want to share the manager
    public VanillaInstaller(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    @Override
    public String install(String mcVersion, String loaderVersion) throws IOException {
        // Vanilla only needs game version
        System.out.println("Installing/Verifying Vanilla version " + mcVersion);
        versionManager.downloadVersionIndex(mcVersion);
        return mcVersion;
    }
}
