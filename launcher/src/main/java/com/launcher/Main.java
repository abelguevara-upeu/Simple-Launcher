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
            // 3. Instalar y Cargar NeoForge (Mods)
            // Primero aseguramos que la versión base (Vanilla) exista
            String gameVersion = "1.21.1"; // Usando 1.21.1 para NeoForge moderno
            manager.downloadVersionIndex(gameVersion);

            // Ahora instalamos NeoForge
            // FabricManager fabricMgr = new FabricManager(workDir);
            // String versionId = fabricMgr.installFabric("1.20.1", "0.15.11");

            NeoForgeManager neoforgeMgr = new NeoForgeManager(workDir);
            // Version 21.1.200 (Required by Create Mod)
            String versionId = neoforgeMgr.installNeoForge(gameVersion, "21.1.200");

            // Paso B: Cargar en memoria (Cargará NeoForge + Vanilla combinados)
            Version version = manager.loadVersion(versionId);

            // Paso C: Descargar JAR del juego
            manager.downloadGameJar(version);

            System.out.println("--------------------------------------------------");
            System.out.println("¡ÉXITO! Versión cargada: " + version.id);
            System.out.println("Clase principal: " + version.mainClass);
            System.out.println("Librerías detectadas: " + (version.libraries != null ? version.libraries.size() : 0));
            // Debug arguments
            if (version.arguments != null) {
                System.out.println("JVM Args: " + (version.arguments.jvm != null ? version.arguments.jvm.size() : 0));
                System.out
                        .println("Game Args: " + (version.arguments.game != null ? version.arguments.game.size() : 0));
            }
            System.out.println("--------------------------------------------------");

            // 4. Descargar Librerías
            LibraryManager libManager = new LibraryManager(workDir);
            libManager.downloadLibraries(version);

            // 5. Descargar Assets
            AssetManager assetManager = new AssetManager(workDir);
            assetManager.downloadAssets(version);

            // 6. Autenticación Offline
            OfflineAuthenticator.Session session = OfflineAuthenticator.login("NeoDev");

            // 7. LANZAR EL JUEGO
            GameLauncher launcher = new GameLauncher(workDir);
            launcher.launch(version, session);

        } catch (Exception e) {
            System.err.println("Error crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
