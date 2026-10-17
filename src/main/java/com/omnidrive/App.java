package com.omnidrive;

import com.omnidrive.context.AppContext;
import com.omnidrive.controller.LoginController;
import com.omnidrive.controller.MainLayoutController;
import com.omnidrive.controller.PasswordRecoveryController;
import com.omnidrive.exception.PersistenceException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Clase principal de la aplicación OmniDrive en JavaFX (Patrón MVC).
 * Orquesta el arranque, la inicialización del contexto de persistencia y el ciclo de vida de las vistas.
 */
public class App extends Application {

    public static final String DEFAULT_STORAGE_PATH = "data/dealership.dat";

    @Override
    public void init() {
        if (AppContext.getDealership() == null) {
            Path storage = Paths.get(DEFAULT_STORAGE_PATH);
            AppContext.init(storage);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("OmniDrive - Concesionario Integral");
        showLoginView(primaryStage);
    }

    public void showLoginView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnidrive/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setOnLoginSuccess(user -> showMainLayout(stage));
            controller.setOnForgotPasswordRequested(() -> showPasswordRecoveryModal(stage));

            Scene scene = new Scene(root, 480, 560);
            stage.setScene(scene);
            stage.setMinWidth(440);
            stage.setMinHeight(520);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la pantalla de inicio de sesión: " + e.getMessage(), e);
        }
    }

    public void showMainLayout(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnidrive/view/MainLayoutView.fxml"));
            Parent root = loader.load();

            MainLayoutController controller = loader.getController();
            controller.setOnLogoutRequested(() -> showLoginView(stage));

            Scene scene = new Scene(root, 1100, 720);
            stage.setScene(scene);
            stage.setMinWidth(960);
            stage.setMinHeight(640);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la interfaz principal: " + e.getMessage(), e);
        }
    }

    public void showPasswordRecoveryModal(Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnidrive/view/PasswordRecoveryDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Recuperación de Contraseña - OmniDrive");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (ownerStage != null) {
                dialogStage.initOwner(ownerStage);
            }

            PasswordRecoveryController controller = loader.getController();
            controller.setOnSuccessCallback(dialogStage::close);
            controller.setOnCancelCallback(dialogStage::close);

            Scene scene = new Scene(root, 450, 480);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el modal de recuperación: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            AppContext.save();
        } catch (PersistenceException ignored) {
            // Ignorado en cierre de aplicación
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
