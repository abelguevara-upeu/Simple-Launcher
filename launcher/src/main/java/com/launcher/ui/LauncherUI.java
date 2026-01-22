package com.launcher.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.launcher.AssetManager;
import com.launcher.FabricManager;
import com.launcher.ForgeManager;
import com.launcher.GameLauncher;
import com.launcher.NeoForgeManager;
import com.launcher.model.Version;
import com.launcher.VersionManager;
import com.launcher.auth.OfflineAuthenticator;
import com.launcher.service.VersionInstaller;
import com.launcher.VanillaInstaller;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class LauncherUI extends JFrame {

    private JTextField usernameField;
    private JComboBox<String> versionSelector;
    private JButton playButton;
    private JTextArea consoleArea;
    private JProgressBar progressBar;

    private final File workDir;

    public LauncherUI() {
        // Setup working directory
        // Maintain existing path logic
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            workDir = new File(System.getProperty("user.dir"), "minecraft-data");
        } else {
            workDir = new File(System.getenv("APPDATA"), ".simplelauncher");
        }
        if (!workDir.exists())
            workDir.mkdirs();

        initUI();
    }

    private void initUI() {
        setTitle("Simple Launcher");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Simple Launcher");
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f).deriveFont(Font.BOLD));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Controls + Console)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlsPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        controlsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField("NeoDev");
        controlsPanel.add(usernameField);

        controlsPanel.add(new JLabel("Version:"));
        String[] versions = { "NeoForge 1.21.1 (Create Mod)", "Forge 1.20.1", "Fabric 1.21.1", "Vanilla 1.21.1",
                "Vanilla 1.20.4" };
        versionSelector = new JComboBox<>(versions);
        controlsPanel.add(versionSelector);

        centerPanel.add(controlsPanel, BorderLayout.NORTH);

        // Console
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Console Output"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Footer (Play Button)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playButton = new JButton("PLAY");
        playButton.setFont(playButton.getFont().deriveFont(Font.BOLD, 18f));
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.addActionListener(e -> onPlay());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        footerPanel.add(playButton, BorderLayout.CENTER);
        footerPanel.add(progressBar, BorderLayout.SOUTH);

        add(footerPanel, BorderLayout.SOUTH);

        // Redirect System.out/err
        redirectSystemStreams();
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateConsole(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateConsole(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void onPlay() {
        playButton.setEnabled(false);
        usernameField.setEnabled(false);
        versionSelector.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Launching...");

        String username = usernameField.getText();
        String selection = (String) versionSelector.getSelectedItem();

        new Thread(() -> {
            try {
                launchGame(username, selection);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error launching game:\n" + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    playButton.setEnabled(true);
                    usernameField.setEnabled(true);
                    versionSelector.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Ready");
                });
            }
        }).start();
    }

    private void launchGame(String username, String selection) throws Exception {
        System.out.println("Starting launch sequence for: " + selection);
        System.out.println("Working directory: " + workDir.getAbsolutePath());

        // Authenticate

        OfflineAuthenticator.Session session = OfflineAuthenticator.login(username);
        System.out.println("Logged in as " + session.username);

        VersionManager manager = new VersionManager(workDir);
        AssetManager assetManager = new AssetManager(workDir);
        GameLauncher launcher = new GameLauncher(workDir);

        String versionId = "";

        if (selection.contains("NeoForge")) {
            VersionInstaller installer = new NeoForgeManager(workDir);
            versionId = installer.install("1.21.1", "21.1.200");
        } else if (selection.contains("Fabric")) {
            VersionInstaller installer = new FabricManager(workDir);
            versionId = installer.install("1.21.1", "0.16.5");
        } else if (selection.contains("Forge")) {
            VersionInstaller installer = new ForgeManager(workDir);
            versionId = installer.install("1.20.1", "47.2.0");
        } else if (selection.contains("1.21.1")) {
            VersionInstaller installer = new VanillaInstaller(workDir);
            versionId = installer.install("1.21.1", null);
        } else if (selection.contains("1.20.4")) {
            VersionInstaller installer = new VanillaInstaller(workDir);
            versionId = installer.install("1.20.4", null);
        }

        System.out.println("Loading version: " + versionId);
        Version version = manager.loadVersion(versionId);

        // Merging is handled automatically by VersionManager.loadVersion()

        System.out.println("Downloading assets...");
        if (version.getAssetIndex() != null) {
            assetManager.downloadAssets(version);
        }

        System.out.println("Downloading libraries...");
        if (version.getLibraries() != null) {
            com.launcher.LibraryManager libMgr = new com.launcher.LibraryManager(workDir);
            libMgr.downloadLibraries(version);
        }

        // Download Client Jar
        if (!versionId.contains("neoforge") && !versionId.contains("forge")) {
            manager.downloadGameJar(version);
        }

        System.out.println("Launching game...");
        launcher.launch(version, session, 4096);
        System.out.println("Game process exited.");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new LauncherUI().setVisible(true);
        });
    }
}
