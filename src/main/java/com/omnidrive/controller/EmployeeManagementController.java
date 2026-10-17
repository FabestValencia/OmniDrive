package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.model.Dealership;
import com.omnidrive.model.Employee;
import com.omnidrive.util.AlertUtil;
import com.omnidrive.validation.InputValidator;
import com.omnidrive.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Controlador para la administración de colaboradores (empleados) del concesionario (Solo Administrador).
 * Permite listar, registrar, actualizar datos personales y bloquear permanentemente cuentas.
 */
public class EmployeeManagementController {

    @FXML
    private TableView<Employee> employeeTable;

    @FXML
    private TableColumn<Employee, String> colId;

    @FXML
    private TableColumn<Employee, String> colFullName;

    @FXML
    private TableColumn<Employee, String> colPhone;

    @FXML
    private TableColumn<Employee, String> colEmail;

    @FXML
    private TableColumn<Employee, String> colUsername;

    @FXML
    private TableColumn<Employee, String> colStatus;

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
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnBlock;

    @FXML
    private Button btnClear;

    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private Employee selectedEmployee;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelection();
        setupSearchListener();
        loadEmployees();
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
        if (colUsername != null) {
            colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().isBlocked() ? "Bloqueado" : "Activo"
            ));
        }
    }

    private void setupTableSelection() {
        if (employeeTable != null) {
            employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectEmployee(newSelection);
                }
            });
        }
    }

    private void setupSearchListener() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        }
    }

    public void loadEmployees() {
        Dealership dealership = AppContext.getDealership();
        if (dealership == null) {
            return;
        }

        masterEmployeeList.clear();
        masterEmployeeList.addAll(dealership.getAllEmployees());
        applyFilter();
    }

    public void applyFilter() {
        if (employeeTable == null) {
            return;
        }

        String filterText = txtSearch != null ? txtSearch.getText() : null;
        if (filterText == null || filterText.trim().isEmpty()) {
            employeeTable.setItems(FXCollections.observableArrayList(masterEmployeeList));
            return;
        }

        String query = filterText.trim().toLowerCase();
        List<Employee> filtered = masterEmployeeList.stream()
                .filter(e -> e.getId().toLowerCase().contains(query)
                        || e.getFullName().toLowerCase().contains(query)
                        || e.getUsername().toLowerCase().contains(query))
                .toList();

        employeeTable.setItems(FXCollections.observableArrayList(filtered));
    }

    public void selectEmployee(Employee emp) {
        this.selectedEmployee = emp;
        if (emp == null) {
            resetForm();
            return;
        }

        if (txtId != null) {
            txtId.setText(emp.getId());
            txtId.setDisable(true);
        }
        if (txtFirstName != null) {
            txtFirstName.setText(emp.getFirstName());
        }
        if (txtLastName != null) {
            txtLastName.setText(emp.getLastName());
        }
        if (txtPhone != null) {
            txtPhone.setText(emp.getPhone());
        }
        if (txtEmail != null) {
            txtEmail.setText(emp.getEmail());
        }
        if (txtUsername != null) {
            txtUsername.setText(emp.getUsername());
            txtUsername.setDisable(true);
        }
        if (txtPassword != null) {
            txtPassword.clear();
            txtPassword.setDisable(true);
        }

        if (btnRegister != null) {
            btnRegister.setDisable(true);
        }
        if (btnUpdate != null) {
            btnUpdate.setDisable(false);
        }
        if (btnBlock != null) {
            btnBlock.setDisable(emp.isBlocked());
            btnBlock.setText(emp.isBlocked() ? "Cuenta Bloqueada" : "Bloquear Empleado");
        }
    }

    private void setFieldError(TextField field, boolean hasError) {
        if (field == null) return;
        if (hasError) {
            if (!field.getStyleClass().contains("text-field-error")) {
                field.getStyleClass().add("text-field-error");
            }
        } else {
            field.getStyleClass().remove("text-field-error");
        }
    }

    private void clearFieldErrors() {
        setFieldError(txtId, false);
        setFieldError(txtFirstName, false);
        setFieldError(txtLastName, false);
        setFieldError(txtPhone, false);
        setFieldError(txtEmail, false);
        setFieldError(txtUsername, false);
        setFieldError(txtPassword, false);
    }

    public void resetForm() {
        this.selectedEmployee = null;
        clearFieldErrors();
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
        if (txtUsername != null) {
            txtUsername.clear();
            txtUsername.setDisable(false);
        }
        if (txtPassword != null) {
            txtPassword.clear();
            txtPassword.setDisable(false);
        }

        if (btnRegister != null) {
            btnRegister.setDisable(false);
        }
        if (btnUpdate != null) {
            btnUpdate.setDisable(true);
        }
        if (btnBlock != null) {
            btnBlock.setDisable(true);
            btnBlock.setText("Bloquear Empleado");
        }
        if (employeeTable != null && employeeTable.getSelectionModel() != null) {
            employeeTable.getSelectionModel().clearSelection();
        }
    }

    public boolean processRegisterEmployee() {
        String id = txtId != null ? txtId.getText() : "";
        String firstName = txtFirstName != null ? txtFirstName.getText() : "";
        String lastName = txtLastName != null ? txtLastName.getText() : "";
        String phone = txtPhone != null ? txtPhone.getText() : "";
        String email = txtEmail != null ? txtEmail.getText() : "";
        String username = txtUsername != null ? txtUsername.getText() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";

        ValidationResult idRes = InputValidator.validateId(id);
        ValidationResult fnRes = InputValidator.validateName(firstName, "Nombres");
        ValidationResult lnRes = InputValidator.validateName(lastName, "Apellidos");
        ValidationResult phRes = InputValidator.validatePhone(phone);
        ValidationResult emRes = InputValidator.validateEmail(email);
        boolean usernameEmpty = username == null || username.trim().isEmpty();
        boolean passwordEmpty = password == null || password.trim().isEmpty();

        setFieldError(txtId, idRes.isInvalid());
        setFieldError(txtFirstName, fnRes.isInvalid());
        setFieldError(txtLastName, lnRes.isInvalid());
        setFieldError(txtPhone, phRes.isInvalid());
        setFieldError(txtEmail, emRes.isInvalid());
        setFieldError(txtUsername, usernameEmpty);
        setFieldError(txtPassword, passwordEmpty);

        if (idRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", idRes.getFirstErrorMessage());
            return false;
        }
        if (fnRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", fnRes.getFirstErrorMessage());
            return false;
        }
        if (lnRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", lnRes.getFirstErrorMessage());
            return false;
        }
        if (phRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", phRes.getFirstErrorMessage());
            return false;
        }
        if (emRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", emRes.getFirstErrorMessage());
            return false;
        }
        if (usernameEmpty) {
            AlertUtil.showWarning("Validación de Empleado", "El nombre de usuario no puede estar vacío.");
            return false;
        }
        if (passwordEmpty) {
            AlertUtil.showWarning("Validación de Empleado", "La contraseña no puede estar vacía.");
            return false;
        }

        try {
            Employee newEmployee = new Employee(id, firstName, lastName, phone, email, username, password);
            AppContext.getDealership().registerEmployee(newEmployee);
            AppContext.save();

            loadEmployees();
            resetForm();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Registrar Empleado", e.getMessage());
            return false;
        }
    }

    public boolean processUpdateEmployee() {
        if (selectedEmployee == null) {
            return false;
        }

        String id = selectedEmployee.getId();
        String firstName = txtFirstName != null ? txtFirstName.getText() : "";
        String lastName = txtLastName != null ? txtLastName.getText() : "";
        String phone = txtPhone != null ? txtPhone.getText() : "";
        String email = txtEmail != null ? txtEmail.getText() : "";

        ValidationResult fnRes = InputValidator.validateName(firstName, "Nombres");
        ValidationResult lnRes = InputValidator.validateName(lastName, "Apellidos");
        ValidationResult phRes = InputValidator.validatePhone(phone);
        ValidationResult emRes = InputValidator.validateEmail(email);

        setFieldError(txtFirstName, fnRes.isInvalid());
        setFieldError(txtLastName, lnRes.isInvalid());
        setFieldError(txtPhone, phRes.isInvalid());
        setFieldError(txtEmail, emRes.isInvalid());

        if (fnRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", fnRes.getFirstErrorMessage());
            return false;
        }
        if (lnRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", lnRes.getFirstErrorMessage());
            return false;
        }
        if (phRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", phRes.getFirstErrorMessage());
            return false;
        }
        if (emRes.isInvalid()) {
            AlertUtil.showWarning("Validación de Empleado", emRes.getFirstErrorMessage());
            return false;
        }

        try {
            AppContext.getDealership().updateEmployee(id, firstName, lastName, phone, email);
            AppContext.save();

            loadEmployees();
            selectEmployee(AppContext.getDealership().findEmployeeById(id));
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Actualizar Empleado", e.getMessage());
            return false;
        }
    }

    public boolean processBlockEmployee(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        try {
            AppContext.getDealership().blockEmployee(id);
            AppContext.save();

            loadEmployees();
            Employee blocked = AppContext.getDealership().findEmployeeById(id);
            selectEmployee(blocked);
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Bloquear Empleado", e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleRegister() {
        if (processRegisterEmployee()) {
            AlertUtil.showInfo("Registro Exitoso", "El empleado ha sido registrado y habilitado en el sistema.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (processUpdateEmployee()) {
            AlertUtil.showInfo("Actualización Exitosa", "Los datos personales del empleado fueron actualizados.");
        }
    }

    @FXML
    private void handleBlock() {
        if (selectedEmployee == null) {
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Confirmación de Bloqueo Permanente",
                "¿Está seguro de bloquear permanentemente al empleado '" + selectedEmployee.getFullName() + "'?\n\nEsta acción es irreversible y le impedirá acceder u operar en el sistema."
        );

        if (confirmed) {
            if (processBlockEmployee(selectedEmployee.getId())) {
                AlertUtil.showWarning("Empleado Bloqueado", "La cuenta ha sido bloqueada permanentemente.");
            }
        }
    }

    @FXML
    private void handleClear() {
        resetForm();
    }

    // Getters y Setters para inyección y pruebas

    public TableView<Employee> getEmployeeTable() {
        return employeeTable;
    }

    public void setEmployeeTable(TableView<Employee> employeeTable) {
        this.employeeTable = employeeTable;
    }

    public TableColumn<Employee, String> getColId() {
        return colId;
    }

    public void setColId(TableColumn<Employee, String> colId) {
        this.colId = colId;
    }

    public TableColumn<Employee, String> getColFullName() {
        return colFullName;
    }

    public void setColFullName(TableColumn<Employee, String> colFullName) {
        this.colFullName = colFullName;
    }

    public TableColumn<Employee, String> getColPhone() {
        return colPhone;
    }

    public void setColPhone(TableColumn<Employee, String> colPhone) {
        this.colPhone = colPhone;
    }

    public TableColumn<Employee, String> getColEmail() {
        return colEmail;
    }

    public void setColEmail(TableColumn<Employee, String> colEmail) {
        this.colEmail = colEmail;
    }

    public TableColumn<Employee, String> getColUsername() {
        return colUsername;
    }

    public void setColUsername(TableColumn<Employee, String> colUsername) {
        this.colUsername = colUsername;
    }

    public TableColumn<Employee, String> getColStatus() {
        return colStatus;
    }

    public void setColStatus(TableColumn<Employee, String> colStatus) {
        this.colStatus = colStatus;
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

    public TextField getTxtUsername() {
        return txtUsername;
    }

    public void setTxtUsername(TextField txtUsername) {
        this.txtUsername = txtUsername;
    }

    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    public void setTxtPassword(PasswordField txtPassword) {
        this.txtPassword = txtPassword;
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

    public Button getBtnBlock() {
        return btnBlock;
    }

    public void setBtnBlock(Button btnBlock) {
        this.btnBlock = btnBlock;
    }

    public Button getBtnClear() {
        return btnClear;
    }

    public void setBtnClear(Button btnClear) {
        this.btnClear = btnClear;
    }
}
