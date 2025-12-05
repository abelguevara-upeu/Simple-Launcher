package com.launcher;

import java.io.File;
import com.launcher.auth.OfflineAuthenticator;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Simple Launcher...");

        // 1. Configurar directorio de trabajo
        File workDir = new File("minecraft-data");
        if (!workDir.exists()) {
            workDir.mkdirs();
        }
        System.out.println("Directorio de trabajo: " + workDir.getAbsolutePath());

        // 2. Inicializar VersionManager
        VersionManager manager = new VersionManager(workDir);

        try {
            // 3. Descargar y Cargar la versión 1.20.1
            String versionId = "1.20.1";

            // Paso A: Descargar JSON
            manager.downloadVersionIndex(versionId);

            // Paso B: Cargar en memoria
            Version version = manager.loadVersion(versionId);

            // Paso C: Descargar JAR del juego
            manager.downloadGameJar(version);

            System.out.println("--------------------------------------------------");
            System.out.println("¡ÉXITO! Versión cargada: " + version.id);
            System.out.println("Clase principal: " + version.mainClass);
            System.out.println("Librerías detectadas: " + (version.libraries != null ? version.libraries.size() : 0));
            System.out.println("--------------------------------------------------");

            // 4. Descargar Librerías
            LibraryManager libManager = new LibraryManager(workDir);
            libManager.downloadLibraries(version);

            // 5. Descargar Assets
            AssetManager assetManager = new AssetManager(workDir);
            assetManager.downloadAssets(version);

            // 6. Autenticación Offline
            OfflineAuthenticator.Session session = OfflineAuthenticator.login("SteveDev");

            // 7. LANZAR EL JUEGO
            GameLauncher launcher = new GameLauncher(workDir);
            launcher.launch(version, session);

        } catch (Exception e) {
            System.err.println("Error crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
