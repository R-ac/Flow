package com.trashsailors.game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Trash Sailors Co-Op - FXML JavaFX Game");

        // Try loading FXML with multiple fallback resource paths for different IDE classpath setups
        URL fxmlLocation = getClass().getResource("/com/trashsailors/view/GameView.fxml");
        if (fxmlLocation == null) {
            fxmlLocation = getClass().getResource("../view/GameView.fxml");
        }
        if (fxmlLocation == null) {
            fxmlLocation = Main.class.getClassLoader().getResource("com/trashsailors/view/GameView.fxml");
        }

        if (fxmlLocation == null) {
            throw new IllegalStateException("Could not find GameView.fxml in classpath resources!");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
