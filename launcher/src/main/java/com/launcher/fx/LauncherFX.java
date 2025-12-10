package com.launcher.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LauncherFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.valueOf("#1e1e1e"));

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Simple Launcher FX");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(600);

        // Setup stage closing
        stage.setOnCloseRequest(e -> System.exit(0));

        stage.show();
    }

    public static void launch(String[] args) {
        Application.launch(args);
    }
}
