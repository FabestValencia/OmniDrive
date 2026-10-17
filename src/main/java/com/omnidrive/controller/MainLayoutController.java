package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Administrator;
import com.omnidrive.model.User;
import com.omnidrive.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador de la estructura principal (Shell con Sidebar y Topbar) de OmniDrive.
 * Aplica el patrón MVC y adapta la navegación según el rol del usuario autenticado.
 */
public class MainLayoutController {

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnVehicles;

    @FXML
    private Button btnCustomers;

    @FXML
    private Button btnTransactions;

    @FXML
    private Button btnEmployees;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    @FXML
    private Label lblSectionTitle;

    @FXML
    private StackPane contentArea;

    private Runnable onLogoutRequested;

    @FXML
    public void initialize() {
        refreshUserSession();
        showDashboard();
    }

    /**
     * Sincroniza la vista con el usuario actualmente autenticado en AppContext.
     */
    public void refreshUserSession() {
        User user = AppContext.getCurrentUser();
        if (user == null) {
            return;
        }

        if (lblUserName != null) {
            lblUserName.setText(user.getFullName());
        }

        boolean isAdmin = user instanceof Administrator;

        if (lblUserRole != null) {
            lblUserRole.setText(isAdmin ? "Administrador" : "Empleado");
        }

        // RF-05 y RF-06: Configuración de visibilidad según rol
        configureNodeVisibility(btnEmployees, isAdmin);
        configureNodeVisibility(btnReports, isAdmin);
        configureNodeVisibility(btnTransactions, !isAdmin);

        configureNodeVisibility(btnVehicles, true);
        configureNodeVisibility(btnCustomers, true);
        configureNodeVisibility(btnDashboard, true);
    }

    private void configureNodeVisibility(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    @FXML
    public void showDashboard() {
        updateActiveState(btnDashboard, "Panel Principal");
        loadSubView("/com/omnidrive/view/DashboardView.fxml");
    }

    @FXML
    public void showVehicles() {
        updateActiveState(btnVehicles, "Inventario de Vehículos");
        loadSubView("/com/omnidrive/view/VehicleManagementView.fxml");
    }

    @FXML
    public void showCustomers() {
        updateActiveState(btnCustomers, "Gestión de Clientes");
        loadSubView("/com/omnidrive/view/CustomerManagementView.fxml");
    }

    @FXML
    public void showTransactions() {
        updateActiveState(btnTransactions, "Operaciones Comerciales");
        loadSubView("/com/omnidrive/view/TransactionView.fxml");
    }

    @FXML
    public void showEmployees() {
        updateActiveState(btnEmployees, "Gestión de Empleados");
        loadSubView("/com/omnidrive/view/EmployeeManagementView.fxml");
    }

    @FXML
    public void showReports() {
        updateActiveState(btnReports, "Reportes Gerenciales");
        loadSubView("/com/omnidrive/view/ReportView.fxml");
    }

    @FXML
    public void handleLogout() {
        AppContext.logout();
        if (onLogoutRequested != null) {
            onLogoutRequested.run();
        }
    }

    /**
     * Reemplaza el contenido visual de la zona principal.
     */
    public void setContent(Node node) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            if (node != null) {
                contentArea.getChildren().add(node);
            }
        }
    }

    /**
     * Carga dinámicamente una sub-vista FXML en el área de contenido.
     */
    public void loadSubView(String fxmlPath) {
        if (contentArea == null) {
            return;
        }

        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            // Placeholder temporal si aún no existe la vista hija (permite desacople incremental)
            Label placeholder = new Label("Vista en preparación: " + fxmlPath);
            placeholder.getStyleClass().add("subtitle-text");
            setContent(placeholder);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            setContent(view);
        } catch (IOException e) {
            AlertUtil.showError("Error de Carga", "No se pudo cargar la vista seleccionada: " + e.getMessage());
        }
    }

    private void updateActiveState(Button activeButton, String title) {
        if (lblSectionTitle != null) {
            lblSectionTitle.setText(title);
        }

        List<Button> buttons = Arrays.asList(btnDashboard, btnVehicles, btnCustomers, btnTransactions, btnEmployees, btnReports);
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }

        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    // Getters y Setters para pruebas unitarias e integración

    public void setOnLogoutRequested(Runnable onLogoutRequested) {
        this.onLogoutRequested = onLogoutRequested;
    }

    public Button getBtnDashboard() {
        return btnDashboard;
    }

    public void setBtnDashboard(Button btnDashboard) {
        this.btnDashboard = btnDashboard;
    }

    public Button getBtnVehicles() {
        return btnVehicles;
    }

    public void setBtnVehicles(Button btnVehicles) {
        this.btnVehicles = btnVehicles;
    }

    public Button getBtnCustomers() {
        return btnCustomers;
    }

    public void setBtnCustomers(Button btnCustomers) {
        this.btnCustomers = btnCustomers;
    }

    public Button getBtnTransactions() {
        return btnTransactions;
    }

    public void setBtnTransactions(Button btnTransactions) {
        this.btnTransactions = btnTransactions;
    }

    public Button getBtnEmployees() {
        return btnEmployees;
    }

    public void setBtnEmployees(Button btnEmployees) {
        this.btnEmployees = btnEmployees;
    }

    public Button getBtnReports() {
        return btnReports;
    }

    public void setBtnReports(Button btnReports) {
        this.btnReports = btnReports;
    }

    public Button getBtnLogout() {
        return btnLogout;
    }

    public void setBtnLogout(Button btnLogout) {
        this.btnLogout = btnLogout;
    }

    public Label getLblUserName() {
        return lblUserName;
    }

    public void setLblUserName(Label lblUserName) {
        this.lblUserName = lblUserName;
    }

    public Label getLblUserRole() {
        return lblUserRole;
    }

    public void setLblUserRole(Label lblUserRole) {
        this.lblUserRole = lblUserRole;
    }

    public Label getLblSectionTitle() {
        return lblSectionTitle;
    }

    public void setLblSectionTitle(Label lblSectionTitle) {
        this.lblSectionTitle = lblSectionTitle;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
}
