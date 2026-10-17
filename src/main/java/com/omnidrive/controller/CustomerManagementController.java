package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.model.Customer;
import com.omnidrive.model.Dealership;
import com.omnidrive.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controlador para la administración de clientes del concesionario OmniDrive.
 * Permite registrar nuevos clientes, actualizar datos personales y filtrar en tiempo real.
 */
public class CustomerManagementController {

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, String> colId;

    @FXML
    private TableColumn<Customer, String> colFullName;

    @FXML
    private TableColumn<Customer, String> colPhone;

    @FXML
    private TableColumn<Customer, String> colEmail;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnClear;

    private final ObservableList<Customer> masterCustomerList = FXCollections.observableArrayList();
    private Customer selectedCustomer;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelection();
        setupSearchListener();
        loadCustomers();
        resetForm();
    }

    private void setupTableColumns() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        }
        if (colFullName != null) {
            colFullName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFullName()));
        }
        if (colPhone != null) {
            colPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        }
        if (colEmail != null) {
            colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        }
    }

    private void setupTableSelection() {
        if (customerTable != null) {
            customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    selectCustomer(newV);
                }
            });
        }
    }

    private void setupSearchListener() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilter());
        }
    }

    public void loadCustomers() {
        Dealership dealership = AppContext.getDealership();
        if (dealership == null) {
            return;
        }

        masterCustomerList.clear();
        masterCustomerList.addAll(dealership.getAllCustomers());
        applyFilter();
    }

    public void applyFilter() {
        if (customerTable == null) {
            return;
        }

        String filterText = txtSearch != null ? txtSearch.getText() : null;
        if (filterText == null || filterText.trim().isEmpty()) {
            customerTable.setItems(FXCollections.observableArrayList(masterCustomerList));
            return;
        }

        String query = filterText.trim().toLowerCase();
        List<Customer> filtered = masterCustomerList.stream()
                .filter(c -> c.getId().toLowerCase().contains(query)
                        || c.getFullName().toLowerCase().contains(query)
                        || c.getEmail().toLowerCase().contains(query))
                .toList();

        customerTable.setItems(FXCollections.observableArrayList(filtered));
    }

    public void selectCustomer(Customer c) {
        this.selectedCustomer = c;
        if (c == null) {
            resetForm();
            return;
        }

        if (txtId != null) {
            txtId.setText(c.getId());
            txtId.setDisable(true);
        }
        if (txtFirstName != null) {
            txtFirstName.setText(c.getFirstName());
        }
        if (txtLastName != null) {
            txtLastName.setText(c.getLastName());
        }
        if (txtPhone != null) {
            txtPhone.setText(c.getPhone());
        }
        if (txtEmail != null) {
            txtEmail.setText(c.getEmail());
        }

        if (btnRegister != null) {
            btnRegister.setDisable(true);
        }
        if (btnUpdate != null) {
            btnUpdate.setDisable(false);
        }
    }

    public void resetForm() {
        this.selectedCustomer = null;
        if (txtId != null) {
            txtId.clear();
            txtId.setDisable(false);
        }
        if (txtFirstName != null) {
            txtFirstName.clear();
        }
        if (txtLastName != null) {
            txtLastName.clear();
        }
        if (txtPhone != null) {
            txtPhone.clear();
        }
        if (txtEmail != null) {
            txtEmail.clear();
        }

        if (btnRegister != null) {
            btnRegister.setDisable(false);
        }
        if (btnUpdate != null) {
            btnUpdate.setDisable(true);
        }
        if (customerTable != null && customerTable.getSelectionModel() != null) {
            customerTable.getSelectionModel().clearSelection();
        }
    }

    public boolean processRegisterCustomer() {
        String id = txtId != null ? txtId.getText() : "";
        String firstName = txtFirstName != null ? txtFirstName.getText() : "";
        String lastName = txtLastName != null ? txtLastName.getText() : "";
        String phone = txtPhone != null ? txtPhone.getText() : "";
        String email = txtEmail != null ? txtEmail.getText() : "";

        try {
            Customer newCustomer = new Customer(id, firstName, lastName, phone, email);
            AppContext.getDealership().registerCustomer(newCustomer);
            AppContext.save();

            loadCustomers();
            resetForm();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Registrar Cliente", e.getMessage());
            return false;
        }
    }

    public boolean processUpdateCustomer() {
        if (selectedCustomer == null) {
            return false;
        }

        String id = selectedCustomer.getId();
        String firstName = txtFirstName != null ? txtFirstName.getText() : "";
        String lastName = txtLastName != null ? txtLastName.getText() : "";
        String phone = txtPhone != null ? txtPhone.getText() : "";
        String email = txtEmail != null ? txtEmail.getText() : "";

        try {
            AppContext.getDealership().updateCustomer(id, firstName, lastName, phone, email);
            AppContext.save();

            loadCustomers();
            selectCustomer(AppContext.getDealership().findCustomerById(id));
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Actualizar Cliente", e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleRegister() {
        if (processRegisterCustomer()) {
            AlertUtil.showInfo("Registro Exitoso", "El cliente ha sido registrado exitosamente en OmniDrive.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (processUpdateCustomer()) {
            AlertUtil.showInfo("Actualización Exitosa", "Los datos personales del cliente fueron actualizados.");
        }
    }

    @FXML
    private void handleClear() {
        resetForm();
    }

    // Getters y Setters para pruebas unitarias e integración

    public TableView<Customer> getCustomerTable() {
        return customerTable;
    }

    public void setCustomerTable(TableView<Customer> customerTable) {
        this.customerTable = customerTable;
    }

    public TableColumn<Customer, String> getColId() {
        return colId;
    }

    public void setColId(TableColumn<Customer, String> colId) {
        this.colId = colId;
    }

    public TableColumn<Customer, String> getColFullName() {
        return colFullName;
    }

    public void setColFullName(TableColumn<Customer, String> colFullName) {
        this.colFullName = colFullName;
    }

    public TableColumn<Customer, String> getColPhone() {
        return colPhone;
    }

    public void setColPhone(TableColumn<Customer, String> colPhone) {
        this.colPhone = colPhone;
    }

    public TableColumn<Customer, String> getColEmail() {
        return colEmail;
    }

    public void setColEmail(TableColumn<Customer, String> colEmail) {
        this.colEmail = colEmail;
    }

    public TextField getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(TextField txtSearch) {
        this.txtSearch = txtSearch;
    }

    public TextField getTxtId() {
        return txtId;
    }

    public void setTxtId(TextField txtId) {
        this.txtId = txtId;
    }

    public TextField getTxtFirstName() {
        return txtFirstName;
    }

    public void setTxtFirstName(TextField txtFirstName) {
        this.txtFirstName = txtFirstName;
    }

    public TextField getTxtLastName() {
        return txtLastName;
    }

    public void setTxtLastName(TextField txtLastName) {
        this.txtLastName = txtLastName;
    }

    public TextField getTxtPhone() {
        return txtPhone;
    }

    public void setTxtPhone(TextField txtPhone) {
        this.txtPhone = txtPhone;
    }

    public TextField getTxtEmail() {
        return txtEmail;
    }

    public void setTxtEmail(TextField txtEmail) {
        this.txtEmail = txtEmail;
    }

    public Button getBtnRegister() {
        return btnRegister;
    }

    public void setBtnRegister(Button btnRegister) {
        this.btnRegister = btnRegister;
    }

    public Button getBtnUpdate() {
        return btnUpdate;
    }

    public void setBtnUpdate(Button btnUpdate) {
        this.btnUpdate = btnUpdate;
    }

    public Button getBtnClear() {
        return btnClear;
    }

    public void setBtnClear(Button btnClear) {
        this.btnClear = btnClear;
    }
}
