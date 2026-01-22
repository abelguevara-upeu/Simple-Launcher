package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.launcher.service.VersionInstaller;

public class FabricManager implements VersionInstaller {
    private final File versionsDir;

    public FabricManager(File workDir) {
        this.versionsDir = new File(workDir, "versions");
    }

    @Override
    public String install(String gameVersion, String loaderVersion) throws IOException {
        String versionId = "fabric-loader-" + loaderVersion + "-" + gameVersion;
        System.out.println("Installing Fabric Loader " + versionId);

        // URL magic of Fabric
        String url = "https://meta.fabricmc.net/v2/versions/loader/" + gameVersion + "/" + loaderVersion
                + "/profile/json";

        File versionFile = new File(versionsDir, versionId + "/" + versionId + ".json");
        versionFile.getParentFile().mkdirs();
        if (!versionFile.exists()) {
            System.out.println("Downloading Fabric Loader " + versionId);
            downloadFile(url, versionFile);
        } else {
            System.out.println("Fabric Loader " + versionId + " already installed");
        }

        return versionId;
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
