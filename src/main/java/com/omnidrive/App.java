package com.omnidrive;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("OmniDrive - Aplicación JavaFX");

        Label titleLabel = new Label("¡Bienvenido a OmniDrive!");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a202c;");

        Label infoLabel = new Label("Proyecto base JavaFX configurado con Maven correctamente.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        Label counterLabel = new Label("Clics: 0");
        counterLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");

        Button actionButton = new Button("Presionar aquí");
        actionButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px; -fx-cursor: hand;");

        final int[] count = {0};
        actionButton.setOnAction(event -> {
            count[0]++;
            counterLabel.setText("Clics: " + count[0]);
        });

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f7fafc;");
        root.getChildren().addAll(titleLabel, infoLabel, actionButton, counterLabel);

        Scene scene = new Scene(root, 480, 320);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
