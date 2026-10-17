package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Administrator;
import com.omnidrive.model.Dealership;
import com.omnidrive.model.User;
import com.omnidrive.model.enums.EmployeeStatus;
import com.omnidrive.model.enums.VehicleStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controlador de la vista del Panel Principal (Dashboard) con indicadores rápidos del concesionario.
 */
public class DashboardController {

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblUserGreeting;

    @FXML
    private Label lblAvailableVehicles;

    @FXML
    private Label lblTotalCustomers;

    @FXML
    private Label lblTotalTransactions;

    @FXML
    private Label lblActiveEmployees;

    @FXML
    private Label lblRentedVehicles;

    @FXML
    private VBox cardEmployees;

    @FXML
    public void initialize() {
        refreshMetrics();
    }

    /**
     * Recalcula y actualiza los indicadores clave de rendimiento (KPIs) en la interfaz gráfica.
     */
    public void refreshMetrics() {
        Dealership dealership = AppContext.getDealership();
        User user = AppContext.getCurrentUser();

        if (user != null) {
            if (lblWelcome != null) {
                lblWelcome.setText("¡Bienvenido al Sistema OmniDrive!");
            }
            if (lblUserGreeting != null) {
                lblUserGreeting.setText("Hola, " + user.getFullName() + ". Panel de control y métricas del concesionario.");
            }

            boolean isAdmin = user instanceof Administrator;
            if (cardEmployees != null) {
                cardEmployees.setVisible(isAdmin);
                cardEmployees.setManaged(isAdmin);
            }
        }

        if (dealership == null) {
            return;
        }

        long availableCount = dealership.getAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE_FOR_SALE || v.getStatus() == VehicleStatus.AVAILABLE_FOR_RENT)
                .count();

        long rentedCount = dealership.getAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.RENTED)
                .count();

        int customerCount = dealership.getCustomerCount();
        int transactionCount = dealership.getAllTransactions().size();

        long activeEmployeeCount = dealership.getAllEmployees().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .count();

        if (lblAvailableVehicles != null) {
            lblAvailableVehicles.setText(String.valueOf(availableCount));
        }
        if (lblTotalCustomers != null) {
            lblTotalCustomers.setText(String.valueOf(customerCount));
        }
        if (lblTotalTransactions != null) {
            lblTotalTransactions.setText(String.valueOf(transactionCount));
        }
        if (lblActiveEmployees != null) {
            lblActiveEmployees.setText(String.valueOf(activeEmployeeCount));
        }
        if (lblRentedVehicles != null) {
            lblRentedVehicles.setText(String.valueOf(rentedCount));
        }
    }

    // Getters y Setters para pruebas unitarias e integración

    public Label getLblWelcome() {
        return lblWelcome;
    }

    public void setLblWelcome(Label lblWelcome) {
        this.lblWelcome = lblWelcome;
    }

    public Label getLblUserGreeting() {
        return lblUserGreeting;
    }

    public void setLblUserGreeting(Label lblUserGreeting) {
        this.lblUserGreeting = lblUserGreeting;
    }

    public Label getLblAvailableVehicles() {
        return lblAvailableVehicles;
    }

    public void setLblAvailableVehicles(Label lblAvailableVehicles) {
        this.lblAvailableVehicles = lblAvailableVehicles;
    }

    public Label getLblTotalCustomers() {
        return lblTotalCustomers;
    }

    public void setLblTotalCustomers(Label lblTotalCustomers) {
        this.lblTotalCustomers = lblTotalCustomers;
    }

    public Label getLblTotalTransactions() {
        return lblTotalTransactions;
    }

    public void setLblTotalTransactions(Label lblTotalTransactions) {
        this.lblTotalTransactions = lblTotalTransactions;
    }

    public Label getLblActiveEmployees() {
        return lblActiveEmployees;
    }

    public void setLblActiveEmployees(Label lblActiveEmployees) {
        this.lblActiveEmployees = lblActiveEmployees;
    }

    public Label getLblRentedVehicles() {
        return lblRentedVehicles;
    }

    public void setLblRentedVehicles(Label lblRentedVehicles) {
        this.lblRentedVehicles = lblRentedVehicles;
    }

    public VBox getCardEmployees() {
        return cardEmployees;
    }

    public void setCardEmployees(VBox cardEmployees) {
        this.cardEmployees = cardEmployees;
    }
}
