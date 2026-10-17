package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.AuthenticationException;
import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.model.User;
import com.omnidrive.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

/**
 * Controlador de la vista de autenticación y acceso en OmniDrive (Patrón MVC).
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    private Consumer<User> onLoginSuccess;
    private Runnable onForgotPasswordRequested;
    private String lastErrorMessage;

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    public void setOnLoginSuccess(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public void setOnForgotPasswordRequested(Runnable onForgotPasswordRequested) {
        this.onForgotPasswordRequested = onForgotPasswordRequested;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Procesa la autenticación del usuario desacoplada de la interfaz directa.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return true si la autenticación fue exitosa; false en caso contrario.
     */
    public boolean processLogin(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            displayError("Por favor ingrese su nombre de usuario y contraseña.");
            return false;
        }

        try {
            User user = AppContext.getDealership().authenticate(username.trim(), password);
            AppContext.login(user);
            clearError();

            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
            return true;
        } catch (BlockedUserException e) {
            displayError(e.getMessage());
            AlertUtil.showError("Cuenta Bloqueada", e.getMessage());
            return false;
        } catch (AuthenticationException e) {
            displayError(e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField != null ? usernameField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        processLogin(username, password);
    }

    @FXML
    private void handleForgotPassword() {
        if (onForgotPasswordRequested != null) {
            onForgotPasswordRequested.run();
        }
    }

    private void displayError(String message) {
        this.lastErrorMessage = message;
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError() {
        this.lastErrorMessage = null;
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}
