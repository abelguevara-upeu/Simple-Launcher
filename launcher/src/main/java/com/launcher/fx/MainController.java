package com.launcher.fx;

import com.launcher.*;
import com.launcher.auth.OfflineAuthenticator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.awt.Desktop;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private Button btnPlayTab, btnModsTab, btnLaunch;
    @FXML
    private VBox viewPlay, viewMods;

    @FXML
    private TextField usernameField;
    @FXML
    private ComboBox<String> typeSelector;
    @FXML
    private ComboBox<String> versionSelector;
    @FXML
    private TextArea consoleArea;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> modsList;

    private File workDir;

    @FXML
    public void initialize() {
        setupWorkDir();
        setupRedirection();

        // Init Selectors
        typeSelector.setItems(FXCollections.observableArrayList("Vanilla", "Forge", "Fabric", "NeoForge"));
        typeSelector.getSelectionModel().select("Forge"); // Default
        onTypeChanged();

        usernameField.setText("NeoDev");

        // Init Tabs
        showPlayTab();
    }

    private void setupWorkDir() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            workDir = new File(System.getProperty("user.dir"), "minecraft-data");
        } else {
            workDir = new File(System.getenv("APPDATA"), ".simplelauncher");
        }
        if (!workDir.exists())
            workDir.mkdirs();
    }

    @FXML
    private void showPlayTab() {
        viewPlay.setVisible(true);
        viewMods.setVisible(false);
        updateTabStyle(btnPlayTab);
    }

    @FXML
    private void showModsTab() {
        viewPlay.setVisible(false);
        viewMods.setVisible(true);
        updateTabStyle(btnModsTab);
        loadModsList();
    }

    // Simple helper to highlight selected tab
    private void updateTabStyle(Button selected) {
        btnPlayTab.getStyleClass().remove("selected");
        btnModsTab.getStyleClass().remove("selected");
        // Add style if supported by CSS, or just leave as is
        // For now our CSS handles :hover, but we can add a pseudo class if needed.
    }

    @FXML
    private void onTypeChanged() {
        String type = typeSelector.getValue();
        List<String> versions = new ArrayList<>();

        switch (type) {
            case "Vanilla":
                versions.add("1.21.1");
                versions.add("1.20.4");
                versions.add("1.20.1");
                versions.add("1.16.5");
                break;
            case "Forge":
                versions.add("1.20.1 - 47.2.0");
                versions.add("1.16.5 - 36.2.39");
                break;
            case "Fabric":
                versions.add("1.21.1 - 0.16.5");
                break;
            case "NeoForge":
                versions.add("1.21.1 - 21.1.200");
                break;
        }
        versionSelector.setItems(FXCollections.observableArrayList(versions));
        versionSelector.getSelectionModel().selectFirst();
    }

    @FXML
    private void onLaunch() {
        btnLaunch.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        statusLabel.setText("Preparing...");

        String username = usernameField.getText();
        String type = typeSelector.getValue();
        String versionRaw = versionSelector.getValue();

        Task<Void> launchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                launchGameLogic(username, type, versionRaw);
                return null;
            }

            @Override
            protected void succeeded() {
                btnLaunch.setDisable(false);
                progressBar.setVisible(false);
                statusLabel.setText("Game Closed");
            }

            @Override
            protected void failed() {
                btnLaunch.setDisable(false);
                progressBar.setVisible(false);
                statusLabel.setText("Error Launching");
                getException().printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Launch Error");
                    alert.setContentText(getException().getMessage());
                    alert.showAndWait();
                });
            }
        };

        new Thread(launchTask).start();
    }

    private void launchGameLogic(String username, String type, String versionRaw) throws Exception {
        System.out.println("Starting launch for " + type + " " + versionRaw);

        OfflineAuthenticator.Session session = OfflineAuthenticator.login(username);

        VersionManager manager = new VersionManager(workDir);
        AssetManager assetManager = new AssetManager(workDir);
        GameLauncher launcher = new GameLauncher(workDir);

        String versionId = "";

        if (type.equals("NeoForge")) {
            NeoForgeManager neoMgr = new NeoForgeManager(workDir);
            // Parse "1.21.1 - 21.1.200"
            String[] parts = versionRaw.split(" - ");
            versionId = neoMgr.installNeoForge(parts[0], parts[1]);
        } else if (type.equals("Fabric")) {
            FabricManager fabricMgr = new FabricManager(workDir);
            String[] parts = versionRaw.split(" - ");
            versionId = fabricMgr.installFabric(parts[0], parts[1]);
        } else if (type.equals("Forge")) {
            ForgeManager forgeMgr = new ForgeManager(workDir);
            String[] parts = versionRaw.split(" - ");
            versionId = forgeMgr.installForge(parts[0], parts[1]);
        } else {
            // Vanilla
            versionId = versionRaw.split(" ")[0]; // Just the number
            manager.downloadVersionIndex(versionId);
        }

        updateMessage("Loading Version...");
        Version version = manager.loadVersion(versionId);

        updateMessage("Downloading Assets...");
        if (version.assetIndex != null)
            assetManager.downloadAssets(version);

        updateMessage("Downloading Libraries...");
        if (version.libraries != null) {
            com.launcher.LibraryManager libMgr = new com.launcher.LibraryManager(workDir);
            libMgr.downloadLibraries(version);
        }

        if (!type.equals("Forge") && !type.equals("NeoForge")) {
            manager.downloadGameJar(version);
        }

        updateMessage("Launching...");
        System.out.println("Launching game process...");
        launcher.launch(version, session);
    }

    private void updateMessage(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    @FXML
    private void openModsFolder() {
        try {
            File modsDir = new File(workDir, "mods");
            if (!modsDir.exists())
                modsDir.mkdirs();
            Desktop.getDesktop().open(modsDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModsList() {
        File modsDir = new File(workDir, "mods");
        if (modsDir.exists()) {
            String[] files = modsDir.list((d, name) -> name.endsWith(".jar"));
            if (files != null) {
                modsList.setItems(FXCollections.observableArrayList(files));
            }
        }
    }

    private void setupRedirection() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> consoleArea.appendText(String.valueOf((char) b)));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String s = new String(b, off, len);
                Platform.runLater(() -> consoleArea.appendText(s));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
