package com.omnidrive.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utilidad para visualización uniforme de cuadros de diálogo y alertas en JavaFX.
 */
public final class AlertUtil {

    private AlertUtil() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Muestra un diálogo modal de error con el título y contenido especificado.
     */
    public static void showError(String title, String message) {
        showDialog(Alert.AlertType.ERROR, title, null, message);
    }

    /**
     * Muestra un diálogo modal de advertencia.
     */
    public static void showWarning(String title, String message) {
        showDialog(Alert.AlertType.WARNING, title, null, message);
    }

    /**
     * Muestra un diálogo modal informativo.
     */
    public static void showInfo(String title, String message) {
        showDialog(Alert.AlertType.INFORMATION, title, null, message);
    }

    /**
     * Muestra un diálogo modal de confirmación y retorna si el usuario aceptó.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title != null ? title : "Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "¿Está seguro de realizar esta acción?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showDialog(Alert.AlertType type, String title, String header, String content) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> executeShowDialog(type, title, header, content));
        } else {
            executeShowDialog(type, title, header, content);
        }
    }

    private static void executeShowDialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title != null ? title : "OmniDrive");
        alert.setHeaderText(header);
        alert.setContentText(content != null ? content : "");
        alert.showAndWait();
    }
}
