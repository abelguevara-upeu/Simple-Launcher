# SimpleLauncher - Resumen del Proyecto

## Descripción General
Un **launcher de Minecraft personalizado** escrito en Java que permite descargar y ejecutar diferentes versiones de Minecraft, incluyendo soporte para mod loaders como Fabric, NeoForge y Forge.

## Stack Tecnológico
- **Lenguaje:** Java 21
- **Build System:** Maven
- **GUI:** Java Swing + FlatLaf (Dark/Light themes)
- **JSON Library:** Google Gson
- **Packaging:** Maven Shade Plugin (Fat JAR)
- **Package:** `com.launcher`

## Funcionalidades Actuales
1. **Vanilla Minecraft** - Descarga y ejecuta cualquier versión (1.20.4, 1.21.1, etc.)
2. **Fabric** - Instalador integrado (ej. 1.21.1).
3. **NeoForge** - Soporte para mod loader moderno (ej. 1.21.1).
4. **Forge** - Soporte para Forge legacy/moderno (ej. 1.20.1).
5. **Autenticación Offline** - Jugar sin cuenta de Microsoft.
6. **UI Gráfica** - Interfaz moderna con selector de versiones y consola en tiempo real.
7. **Gestión de Assets y Librerías** - Descarga automática y gestión de dependencias.
8. **Empaquetado** - Generación de JAR ejecutable independiente.

## Funcionalidades Pendientes (Por implementar 🚧)
1. **Autenticación Microsoft** - Login con cuenta real.
2. **Gestión de Mods** - Interfaz para agregar/quitar JARs de la carpeta `mods`.
3. **Múltiples Perfiles** - Guardar configuraciones por instancia.

## Estructura del Proyecto
```
SimpleLauncher/
├── .git/
├── .gitignore
├── PROJECT_SUMMARY.md
└── launcher/
    ├── .sdkmanrc          # Java 17 context
    ├── pom.xml            # Maven build config
    ├── minecraft-data/    # Datos del juego (creado al ejecutar)
    └── src/main/java/com/launcher/
        ├── Main.java           # Entry point
        ├── GameLauncher.java   # Lógica de lanzamiento
        ├── ForgeManager.java   # Instalador Forge
        ├── NeoForgeManager.java# Instalador NeoForge
        ├── FabricManager.java  # Instalador Fabric
        ├── VersionManager.java # Vanilla versions
        └── ui/
            └── LauncherUI.java # Interfaz Gráfica (Swing)
```

## Comandos Útiles

### Ejecutar desde Código
```bash
cd launcher
mvn clean compile exec:java -Dexec.mainClass="com.launcher.Main"
```

### Empaquetar (Build)
Genera un JAR ejecutable en `target/`.
```bash
cd launcher
mvn clean package
```

### Ejecutar JAR
```bash
java -jar target/simple-launcher-1.0-SNAPSHOT.jar
```

## Notas Técnicas
- **Forge/NeoForge:** Se utilizan los instaladores oficiales en modo `--installClient` para generar la estructura de librerías correcta.
- **Conflictos de Classpath:** El `GameLauncher` filtra el JAR de vanilla cuando se lanza Forge/NeoForge para evitar conflictos de módulos.
- **Directorios:** Todo se guarda en `minecraft-data` dentro del directorio de trabajo para portabilidad.
