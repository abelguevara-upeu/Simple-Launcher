package com.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

public class AssetManager {
    private final File assetsDir;
    private final Gson gson;

    public AssetManager(File workDir) {
        this.assetsDir = new File(workDir, "assets");
        this.gson = new Gson();
    }

    public void downloadAssets(Version version) throws IOException {
        if (version.assetIndex == null)
            return;

        System.out.println("Downloading assets for " + version.assetIndex.id);

        // Download asset index
        File indexFile = new File(assetsDir, "indexes/" + version.assetIndex.id + ".json");
        indexFile.getParentFile().mkdirs();

        if (!indexFile.exists()) {
            downloadFile(version.assetIndex.url, indexFile);
        }

        // Read asset index
        String jsonContent = new String(Files.readAllBytes(indexFile.toPath()));
        AssetIndex index = gson.fromJson(jsonContent, AssetIndex.class);

        System.out.println("Verifying " + index.objects.size() + " assets...");

        // Download assets with threads
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 download threads
        int[] count = { 0 };

        for (Map.Entry<String, AssetObject> entry : index.objects.entrySet()) {
            executor.submit(() -> {
                try {
                    AssetObject obj = entry.getValue();
                    String hashHead = obj.hash.substring(0, 2);
                    File objectFile = new File(assetsDir, "objects/" + hashHead + "/" + obj.hash);

                    if (!objectFile.exists()) {
                        String url = "https://resources.download.minecraft.net/" + hashHead + "/" + obj.hash;
                        objectFile.getParentFile().mkdirs();
                        downloadFile(url, objectFile);
                        System.out.print(".");
                    }
                    count[0]++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Downloaded " + count[0] + " assets");
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Class for mapping assets to their objects
    private static class AssetIndex {
        public Map<String, AssetObject> objects;
    }

    private static class AssetObject {
        public String hash;
        public long size;
    }
}
