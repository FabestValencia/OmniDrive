package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.AuthenticationException;
import com.omnidrive.exception.InvalidRecoveryCodeException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.exception.PersistenceException;
import com.omnidrive.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Instant;

/**
 * Controlador para el asistente modal de recuperación y restablecimiento de contraseña (RF-08).
 */
public class PasswordRecoveryController {

    @FXML
    private TextField identifierField;

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private VBox step1Box;

    @FXML
    private VBox step2Box;

    @FXML
    private Button requestButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button cancelButton;

    private String generatedCode;
    private String lastErrorMessage;
    private Runnable onSuccessCallback;
    private Runnable onCancelCallback;

    @FXML
    public void initialize() {
        if (step2Box != null) {
            step2Box.setVisible(false);
            step2Box.setManaged(false);
        }
        if (messageLabel != null) {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setOnSuccessCallback(Runnable onSuccessCallback) {
        this.onSuccessCallback = onSuccessCallback;
    }

    public void setOnCancelCallback(Runnable onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    /**
     * Procesa la solicitud del código de recuperación (Paso 1).
     */
    public boolean processCodeRequest(String identifier, Instant now) {
        if (identifier == null || identifier.trim().isEmpty()) {
            displayError("Por favor ingrese su nombre de usuario o correo electrónico.");
            return false;
        }

        try {
            Instant requestTime = now != null ? now : Instant.now();
            this.generatedCode = AppContext.getDealership().requestPasswordRecovery(identifier.trim(), requestTime);
            clearError();

            if (step1Box != null && step2Box != null) {
                step1Box.setVisible(false);
                step1Box.setManaged(false);
                step2Box.setVisible(true);
                step2Box.setManaged(true);
            }
            return true;
        } catch (InvalidUserDataException e) {
            displayError(e.getMessage());
            return false;
        }
    }

    /**
     * Procesa el cambio de clave con el código recibido (Paso 2).
     */
    public boolean processPasswordReset(String identifier, String code, String newPassword, Instant now) {
        if (identifier == null || identifier.trim().isEmpty() ||
            code == null || code.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty()) {
            displayError("Todos los campos son obligatorios.");
            return false;
        }

        try {
            Instant resetTime = now != null ? now : Instant.now();
            AppContext.getDealership().resetPasswordWithCode(identifier.trim(), code.trim(), newPassword, resetTime);
            AppContext.save();
            clearError();

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            return true;
        } catch (AuthenticationException | InvalidUserDataException | InvalidRecoveryCodeException | PersistenceException e) {
            displayError(e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleRequestCode() {
        String identifier = identifierField != null ? identifierField.getText() : "";
        boolean success = processCodeRequest(identifier, Instant.now());
        if (success) {
            AlertUtil.showInfo("Código Enviado",
                    "Se ha enviado un código de verificación de 6 dígitos al correo registrado. Es válido por 15 minutos.");
        }
    }

    @FXML
    private void handleResetPassword() {
        String identifier = identifierField != null ? identifierField.getText() : "";
        String code = codeField != null ? codeField.getText() : "";
        String pass1 = newPasswordField != null ? newPasswordField.getText() : "";
        String pass2 = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (!pass1.equals(pass2)) {
            displayError("Las contraseñas no coinciden. Por favor verifique.");
            return;
        }

        boolean success = processPasswordReset(identifier, code, pass1, Instant.now());
        if (success) {
            AlertUtil.showInfo("Contraseña Restablecida",
                    "Tu contraseña ha sido actualizada exitosamente. Ya puedes iniciar sesión con tu nueva credencial.");
            closeDialog();
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
        closeDialog();
    }

    private void closeDialog() {
        if (cancelButton != null && cancelButton.getScene() != null && cancelButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void displayError(String message) {
        this.lastErrorMessage = message;
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }

    private void clearError() {
        this.lastErrorMessage = null;
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }
    }
}
