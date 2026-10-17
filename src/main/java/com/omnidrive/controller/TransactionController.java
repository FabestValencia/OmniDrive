package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import com.omnidrive.util.AlertUtil;
import com.omnidrive.validation.InputValidator;
import com.omnidrive.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para las operaciones comerciales del concesionario OmniDrive (MVC).
 * Gestiona pestañas independientes de Compra, Venta, Alquiler, Devolución e Historial auditable.
 */
public class TransactionController {

    // Compra
    @FXML
    private ComboBox<Employee> cmbBuyEmployee;
    @FXML
    private ComboBox<Customer> cmbBuyCustomer;
    @FXML
    private ComboBox<String> cmbBuyVehicleType;
    @FXML
    private TextField txtBuyPlate;
    @FXML
    private TextField txtBuyBrand;
    @FXML
    private TextField txtBuyYear;
    @FXML
    private TextField txtBuyCylinder;
    @FXML
    private ComboBox<FuelType> cmbBuyFuel;
    @FXML
    private ComboBox<TransmissionType> cmbBuyTransmission;
    @FXML
    private ComboBox<VehicleCondition> cmbBuyCondition;
    @FXML
    private CheckBox chkBuyTechnicalReview;
    @FXML
    private ComboBox<VehicleStatus> cmbBuyInitialStatus;
    @FXML
    private TextField txtBuyAmount;
    @FXML
    private Button btnProcessBuy;

    // Venta
    @FXML
    private ComboBox<Employee> cmbSaleEmployee;
    @FXML
    private ComboBox<Customer> cmbSaleCustomer;
    @FXML
    private ComboBox<Vehicle> cmbSaleVehicle;
    @FXML
    private TextField txtSaleAmount;
    @FXML
    private Button btnProcessSale;

    // Alquiler
    @FXML
    private ComboBox<Employee> cmbRentalEmployee;
    @FXML
    private ComboBox<Customer> cmbRentalCustomer;
    @FXML
    private ComboBox<Vehicle> cmbRentalVehicle;
    @FXML
    private DatePicker dpRentalStart;
    @FXML
    private DatePicker dpRentalEnd;
    @FXML
    private TextField txtRentalDays;
    @FXML
    private Button btnProcessRental;

    // Devolución
    @FXML
    private ComboBox<Vehicle> cmbReturnVehicle;
    @FXML
    private Button btnProcessReturn;

    // Historial
    @FXML
    private TableView<Transaction> historyTable;
    @FXML
    private TableColumn<Transaction, String> colTxnId;
    @FXML
    private TableColumn<Transaction, String> colTxnType;
    @FXML
    private TableColumn<Transaction, String> colTxnDate;
    @FXML
    private TableColumn<Transaction, String> colTxnEmployee;
    @FXML
    private TableColumn<Transaction, String> colTxnCustomer;
    @FXML
    private TableColumn<Transaction, String> colTxnVehicle;
    @FXML
    private TableColumn<Transaction, String> colTxnAmount;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    @FXML
    public void initialize() {
        setupHistoryColumns();
        setupStringConverters();
        populateCombos();
        loadHistoryTable();
        attachErrorClearingListeners();
    }

    private void setupHistoryColumns() {
        if (colTxnId != null) {
            colTxnId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        }
        if (colTxnType != null) {
            colTxnType.setCellValueFactory(cell -> new SimpleStringProperty(formatTxnType(cell.getValue().getType())));
        }
        if (colTxnDate != null) {
            colTxnDate.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getTimestamp() != null ? DATE_FORMATTER.format(cell.getValue().getTimestamp()) : "N/A"
            ));
        }
        if (colTxnEmployee != null) {
            colTxnEmployee.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getEmployee() != null ? cell.getValue().getEmployee().getFullName() : "N/A"
            ));
        }
        if (colTxnCustomer != null) {
            colTxnCustomer.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getCustomer() != null ? cell.getValue().getCustomer().getFullName() : "N/A"
            ));
        }
        if (colTxnVehicle != null) {
            colTxnVehicle.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getVehicle() != null ? cell.getValue().getVehicle().getPlate() + " (" + cell.getValue().getVehicle().getBrand() + ")" : "N/A"
            ));
        }
        if (colTxnAmount != null) {
            colTxnAmount.setCellValueFactory(cell -> new SimpleStringProperty(
                    String.format("$ %,.0f", cell.getValue().getTotalAmount())
            ));
        }
    }

    private String formatTxnType(TransactionType type) {
        if (type == null) return "N/A";
        return switch (type) {
            case PURCHASE -> "Compra";
            case SALE -> "Venta";
            case RENTAL -> "Alquiler";
        };
    }

    private void setupStringConverters() {
        StringConverter<Employee> employeeConverter = new StringConverter<>() {
            @Override
            public String toString(Employee e) {
                return e != null ? e.getFullName() + " (" + e.getId() + ")" : "";
            }
            @Override
            public Employee fromString(String string) { return null; }
        };

        StringConverter<Customer> customerConverter = new StringConverter<>() {
            @Override
            public String toString(Customer c) {
                return c != null ? c.getFullName() + " (" + c.getId() + ")" : "";
            }
            @Override
            public Customer fromString(String string) { return null; }
        };

        StringConverter<Vehicle> vehicleConverter = new StringConverter<>() {
            @Override
            public String toString(Vehicle v) {
                return v != null ? v.getPlate() + " - " + v.getBrand() + " ($ " + String.format("%,.0f", v.getBasePrice()) + ")" : "";
            }
            @Override
            public Vehicle fromString(String string) { return null; }
        };

        if (cmbBuyEmployee != null) cmbBuyEmployee.setConverter(employeeConverter);
        if (cmbSaleEmployee != null) cmbSaleEmployee.setConverter(employeeConverter);
        if (cmbRentalEmployee != null) cmbRentalEmployee.setConverter(employeeConverter);

        if (cmbBuyCustomer != null) cmbBuyCustomer.setConverter(customerConverter);
        if (cmbSaleCustomer != null) cmbSaleCustomer.setConverter(customerConverter);
        if (cmbRentalCustomer != null) cmbRentalCustomer.setConverter(customerConverter);

        if (cmbSaleVehicle != null) cmbSaleVehicle.setConverter(vehicleConverter);
        if (cmbRentalVehicle != null) cmbRentalVehicle.setConverter(vehicleConverter);
        if (cmbReturnVehicle != null) cmbReturnVehicle.setConverter(vehicleConverter);
    }

    public void populateCombos() {
        Dealership dealership = AppContext.getDealership();
        if (dealership == null) {
            return;
        }

        List<Employee> activeEmployees = dealership.getAllEmployees().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .toList();

        List<Customer> allCustomers = dealership.getAllCustomers();

        List<Vehicle> forSale = dealership.getAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE_FOR_SALE)
                .toList();

        List<Vehicle> forRent = dealership.getAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE_FOR_RENT)
                .toList();

        List<Vehicle> rentedVehicles = dealership.getAllVehicles().stream()
                .filter(v -> v.getStatus() == VehicleStatus.RENTED)
                .toList();

        // Asignación de listas
        setComboItems(cmbBuyEmployee, activeEmployees);
        setComboItems(cmbSaleEmployee, activeEmployees);
        setComboItems(cmbRentalEmployee, activeEmployees);

        setComboItems(cmbBuyCustomer, allCustomers);
        setComboItems(cmbSaleCustomer, allCustomers);
        setComboItems(cmbRentalCustomer, allCustomers);

        setComboItems(cmbSaleVehicle, forSale);
        setComboItems(cmbRentalVehicle, forRent);
        setComboItems(cmbReturnVehicle, rentedVehicles);

        // Preselección del empleado logueado
        if (AppContext.getCurrentUser() instanceof Employee activeEmp) {
            if (cmbBuyEmployee != null) cmbBuyEmployee.setValue(activeEmp);
            if (cmbSaleEmployee != null) cmbSaleEmployee.setValue(activeEmp);
            if (cmbRentalEmployee != null) cmbRentalEmployee.setValue(activeEmp);
        }

        // Combos específicos de Compra
        if (cmbBuyVehicleType != null && cmbBuyVehicleType.getItems().isEmpty()) {
            cmbBuyVehicleType.setItems(FXCollections.observableArrayList(
                    "Sedán", "Deportivo", "Camioneta / SUV", "Pick Up", "Van", "Bus", "Camión", "Moto"
            ));
            cmbBuyVehicleType.setValue("Sedán");
        }
        if (cmbBuyFuel != null && cmbBuyFuel.getItems().isEmpty()) {
            cmbBuyFuel.setItems(FXCollections.observableArrayList(FuelType.values()));
            cmbBuyFuel.setValue(FuelType.GASOLINE);
        }
        if (cmbBuyTransmission != null && cmbBuyTransmission.getItems().isEmpty()) {
            cmbBuyTransmission.setItems(FXCollections.observableArrayList(TransmissionType.values()));
            cmbBuyTransmission.setValue(TransmissionType.MANUAL);
        }
        if (cmbBuyCondition != null && cmbBuyCondition.getItems().isEmpty()) {
            cmbBuyCondition.setItems(FXCollections.observableArrayList(VehicleCondition.values()));
            cmbBuyCondition.setValue(VehicleCondition.USED);
        }
        if (cmbBuyInitialStatus != null && cmbBuyInitialStatus.getItems().isEmpty()) {
            cmbBuyInitialStatus.setItems(FXCollections.observableArrayList(
                    VehicleStatus.AVAILABLE_FOR_SALE, VehicleStatus.AVAILABLE_FOR_RENT
            ));
            cmbBuyInitialStatus.setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        }
    }

    private <T> void setComboItems(ComboBox<T> combo, List<T> items) {
        if (combo != null) {
            combo.setItems(FXCollections.observableArrayList(items));
            if (!items.isEmpty() && combo.getValue() == null) {
                combo.setValue(items.get(0));
            }
        }
    }

    public void loadHistoryTable() {
        Dealership dealership = AppContext.getDealership();
        if (dealership != null && historyTable != null) {
            historyTable.setItems(FXCollections.observableArrayList(dealership.getAllTransactions()));
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

    private void attachErrorClearingListeners() {
        TextField[] fields = {txtBuyPlate, txtBuyBrand, txtBuyYear, txtBuyCylinder, txtBuyAmount, txtSaleAmount, txtRentalDays};
        for (TextField tf : fields) {
            if (tf != null) {
                tf.textProperty().addListener((obs, oldV, newV) -> setFieldError(tf, false));
            }
        }
    }

    // --- ACCIONES DE NEGOCIO ---

    public boolean processBuy() {
        Employee employee = cmbBuyEmployee != null ? cmbBuyEmployee.getValue() : null;
        Customer customer = cmbBuyCustomer != null ? cmbBuyCustomer.getValue() : null;

        List<String> errors = new ArrayList<>();

        String plate = txtBuyPlate != null ? txtBuyPlate.getText().trim() : "";
        ValidationResult resPlate = InputValidator.validatePlate(plate);
        setFieldError(txtBuyPlate, !resPlate.isValid());
        if (!resPlate.isValid()) errors.add(resPlate.getFirstErrorMessage());

        String brand = txtBuyBrand != null ? txtBuyBrand.getText().trim() : "";
        ValidationResult resBrand = InputValidator.validateName(brand, "Marca");
        setFieldError(txtBuyBrand, !resBrand.isValid());
        if (!resBrand.isValid()) errors.add(resBrand.getFirstErrorMessage());

        String yearStr = txtBuyYear != null ? txtBuyYear.getText().trim() : "";
        ValidationResult resYear = InputValidator.parsePositiveInt(yearStr, "Año");
        int year = 0;
        if (resYear.isValid()) {
            year = Integer.parseInt(yearStr);
            ValidationResult resYearRange = InputValidator.validateModelYear(year);
            if (!resYearRange.isValid()) resYear = resYearRange;
        }
        setFieldError(txtBuyYear, !resYear.isValid());
        if (!resYear.isValid()) errors.add(resYear.getFirstErrorMessage());

        String cylinderStr = txtBuyCylinder != null ? txtBuyCylinder.getText().trim() : "";
        ValidationResult resCylinder = InputValidator.parsePositiveDouble(cylinderStr, "Cilindraje");
        setFieldError(txtBuyCylinder, !resCylinder.isValid());
        if (!resCylinder.isValid()) errors.add(resCylinder.getFirstErrorMessage());

        String amountStr = txtBuyAmount != null ? txtBuyAmount.getText().trim() : "";
        ValidationResult resAmount = InputValidator.parsePositiveDouble(amountStr, "Monto de Compra");
        setFieldError(txtBuyAmount, !resAmount.isValid());
        if (!resAmount.isValid()) errors.add(resAmount.getFirstErrorMessage());

        if (!errors.isEmpty()) {
            AlertUtil.showError("Datos de Compra Inválidos", String.join("\n", errors));
            return false;
        }

        FuelType fuel = cmbBuyFuel != null ? cmbBuyFuel.getValue() : FuelType.GASOLINE;
        TransmissionType transmission = cmbBuyTransmission != null ? cmbBuyTransmission.getValue() : TransmissionType.MANUAL;
        VehicleCondition condition = cmbBuyCondition != null ? cmbBuyCondition.getValue() : VehicleCondition.USED;
        boolean technicalReview = chkBuyTechnicalReview != null && chkBuyTechnicalReview.isSelected();
        VehicleStatus initialStatus = cmbBuyInitialStatus != null ? cmbBuyInitialStatus.getValue() : VehicleStatus.AVAILABLE_FOR_SALE;

        try {
            double cylinder = Double.parseDouble(cylinderStr.replace(",", "."));
            double amount = Double.parseDouble(amountStr.replace(",", "."));

            // Se crea el vehículo comprado
            Vehicle vehicle = new Sedan(plate, brand, year, 5, 180.0, cylinder, fuel, transmission,
                    condition, initialStatus, amount, null, technicalReview, null, null,
                    5, 4, 2, true, false, true, 400.0, false, false, false, false);

            AppContext.getDealership().buyVehicle(employee, customer, vehicle, amount, initialStatus, Instant.now());
            AppContext.save();

            populateCombos();
            loadHistoryTable();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Operación de Compra Rechazada", e.getMessage());
            return false;
        }
    }

    public boolean processSale() {
        Employee employee = cmbSaleEmployee != null ? cmbSaleEmployee.getValue() : null;
        Customer customer = cmbSaleCustomer != null ? cmbSaleCustomer.getValue() : null;
        Vehicle vehicle = cmbSaleVehicle != null ? cmbSaleVehicle.getValue() : null;
        String amountStr = txtSaleAmount != null ? txtSaleAmount.getText().trim() : "";

        if (vehicle == null) {
            AlertUtil.showError("Vehículo No Seleccionado", "Debe seleccionar un vehículo disponible para la venta.");
            return false;
        }

        double amount;
        if (!amountStr.isBlank()) {
            ValidationResult resAmount = InputValidator.parsePositiveDouble(amountStr, "Monto de Venta");
            setFieldError(txtSaleAmount, !resAmount.isValid());
            if (!resAmount.isValid()) {
                AlertUtil.showError("Formato Inválido", resAmount.getFirstErrorMessage());
                return false;
            }
            amount = Double.parseDouble(amountStr.replace(",", "."));
        } else {
            amount = vehicle.getBasePrice();
            setFieldError(txtSaleAmount, false);
        }

        try {
            AppContext.getDealership().sellVehicle(employee, customer, vehicle.getPlate(), amount, Instant.now());
            AppContext.save();

            populateCombos();
            loadHistoryTable();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Operación de Venta Rechazada", e.getMessage());
            return false;
        }
    }

    public boolean processRental() {
        Employee employee = cmbRentalEmployee != null ? cmbRentalEmployee.getValue() : null;
        Customer customer = cmbRentalCustomer != null ? cmbRentalCustomer.getValue() : null;
        Vehicle vehicle = cmbRentalVehicle != null ? cmbRentalVehicle.getValue() : null;
        LocalDate start = dpRentalStart != null ? dpRentalStart.getValue() : null;
        LocalDate end = dpRentalEnd != null ? dpRentalEnd.getValue() : null;
        String daysStr = txtRentalDays != null ? txtRentalDays.getText().trim() : "";

        if (vehicle == null) {
            AlertUtil.showError("Vehículo No Seleccionado", "Debe seleccionar un vehículo disponible para alquiler.");
            return false;
        }

        int rentalDays = 1;
        if (!daysStr.isBlank()) {
            ValidationResult resDays = InputValidator.parsePositiveInt(daysStr, "Días de Alquiler");
            setFieldError(txtRentalDays, !resDays.isValid());
            if (!resDays.isValid()) {
                AlertUtil.showError("Formato Inválido", resDays.getFirstErrorMessage());
                return false;
            }
            rentalDays = Integer.parseInt(daysStr);
        } else {
            setFieldError(txtRentalDays, false);
        }

        if (start != null && end != null) {
            ValidationResult resRange = InputValidator.validateDateRange(start, end);
            if (!resRange.isValid()) {
                AlertUtil.showError("Rango de Fechas Inválido", resRange.getFirstErrorMessage());
                return false;
            }
        } else if (start == null) {
            start = LocalDate.now();
            if (end == null) {
                end = start.plusDays(rentalDays);
            }
        } else {
            end = start.plusDays(rentalDays);
        }

        try {
            AppContext.getDealership().rentVehicle(employee, customer, vehicle.getPlate(), start, end, rentalDays, Instant.now());
            AppContext.save();

            populateCombos();
            loadHistoryTable();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Operación de Alquiler Rechazada", e.getMessage());
            return false;
        }
    }

    public boolean processReturn() {
        Vehicle vehicle = cmbReturnVehicle != null ? cmbReturnVehicle.getValue() : null;
        if (vehicle == null) {
            AlertUtil.showError("Vehículo No Seleccionado", "Debe seleccionar un vehículo actualmente alquilado.");
            return false;
        }

        try {
            AppContext.getDealership().returnRentedVehicle(vehicle.getPlate());
            AppContext.save();

            populateCombos();
            loadHistoryTable();
            return true;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error en Devolución", e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleProcessBuy() {
        if (processBuy()) {
            AlertUtil.showInfo("Compra Registrada", "El vehículo fue comprado e ingresado al inventario.");
        }
    }

    @FXML
    private void handleProcessSale() {
        if (processSale()) {
            AlertUtil.showInfo("Venta Registrada", "La venta fue procesada y el vehículo conmutó a estado Vendido.");
        }
    }

    @FXML
    private void handleProcessRental() {
        if (processRental()) {
            AlertUtil.showInfo("Alquiler Registrado", "El alquiler fue registrado y el vehículo conmutó a estado Alquilado.");
        }
    }

    @FXML
    private void handleProcessReturn() {
        if (processReturn()) {
            AlertUtil.showInfo("Devolución Exitosa", "El vehículo ha retornado a Disponible para Alquiler.");
        }
    }

    // Getters y Setters para pruebas unitarias e inyección

    public ComboBox<Employee> getCmbBuyEmployee() { return cmbBuyEmployee; }
    public void setCmbBuyEmployee(ComboBox<Employee> cmbBuyEmployee) { this.cmbBuyEmployee = cmbBuyEmployee; }

    public ComboBox<Customer> getCmbBuyCustomer() { return cmbBuyCustomer; }
    public void setCmbBuyCustomer(ComboBox<Customer> cmbBuyCustomer) { this.cmbBuyCustomer = cmbBuyCustomer; }

    public ComboBox<String> getCmbBuyVehicleType() { return cmbBuyVehicleType; }
    public void setCmbBuyVehicleType(ComboBox<String> cmbBuyVehicleType) { this.cmbBuyVehicleType = cmbBuyVehicleType; }

    public TextField getTxtBuyPlate() { return txtBuyPlate; }
    public void setTxtBuyPlate(TextField txtBuyPlate) { this.txtBuyPlate = txtBuyPlate; }

    public TextField getTxtBuyBrand() { return txtBuyBrand; }
    public void setTxtBuyBrand(TextField txtBuyBrand) { this.txtBuyBrand = txtBuyBrand; }

    public TextField getTxtBuyYear() { return txtBuyYear; }
    public void setTxtBuyYear(TextField txtBuyYear) { this.txtBuyYear = txtBuyYear; }

    public TextField getTxtBuyCylinder() { return txtBuyCylinder; }
    public void setTxtBuyCylinder(TextField txtBuyCylinder) { this.txtBuyCylinder = txtBuyCylinder; }

    public ComboBox<FuelType> getCmbBuyFuel() { return cmbBuyFuel; }
    public void setCmbBuyFuel(ComboBox<FuelType> cmbBuyFuel) { this.cmbBuyFuel = cmbBuyFuel; }

    public ComboBox<TransmissionType> getCmbBuyTransmission() { return cmbBuyTransmission; }
    public void setCmbBuyTransmission(ComboBox<TransmissionType> cmbBuyTransmission) { this.cmbBuyTransmission = cmbBuyTransmission; }

    public ComboBox<VehicleCondition> getCmbBuyCondition() { return cmbBuyCondition; }
    public void setCmbBuyCondition(ComboBox<VehicleCondition> cmbBuyCondition) { this.cmbBuyCondition = cmbBuyCondition; }

    public CheckBox getChkBuyTechnicalReview() { return chkBuyTechnicalReview; }
    public void setChkBuyTechnicalReview(CheckBox chkBuyTechnicalReview) { this.chkBuyTechnicalReview = chkBuyTechnicalReview; }

    public ComboBox<VehicleStatus> getCmbBuyInitialStatus() { return cmbBuyInitialStatus; }
    public void setCmbBuyInitialStatus(ComboBox<VehicleStatus> cmbBuyInitialStatus) { this.cmbBuyInitialStatus = cmbBuyInitialStatus; }

    public TextField getTxtBuyAmount() { return txtBuyAmount; }
    public void setTxtBuyAmount(TextField txtBuyAmount) { this.txtBuyAmount = txtBuyAmount; }

    public Button getBtnProcessBuy() { return btnProcessBuy; }
    public void setBtnProcessBuy(Button btnProcessBuy) { this.btnProcessBuy = btnProcessBuy; }

    public ComboBox<Employee> getCmbSaleEmployee() { return cmbSaleEmployee; }
    public void setCmbSaleEmployee(ComboBox<Employee> cmbSaleEmployee) { this.cmbSaleEmployee = cmbSaleEmployee; }

    public ComboBox<Customer> getCmbSaleCustomer() { return cmbSaleCustomer; }
    public void setCmbSaleCustomer(ComboBox<Customer> cmbSaleCustomer) { this.cmbSaleCustomer = cmbSaleCustomer; }

    public ComboBox<Vehicle> getCmbSaleVehicle() { return cmbSaleVehicle; }
    public void setCmbSaleVehicle(ComboBox<Vehicle> cmbSaleVehicle) { this.cmbSaleVehicle = cmbSaleVehicle; }

    public TextField getTxtSaleAmount() { return txtSaleAmount; }
    public void setTxtSaleAmount(TextField txtSaleAmount) { this.txtSaleAmount = txtSaleAmount; }

    public Button getBtnProcessSale() { return btnProcessSale; }
    public void setBtnProcessSale(Button btnProcessSale) { this.btnProcessSale = btnProcessSale; }

    public ComboBox<Employee> getCmbRentalEmployee() { return cmbRentalEmployee; }
    public void setCmbRentalEmployee(ComboBox<Employee> cmbRentalEmployee) { this.cmbRentalEmployee = cmbRentalEmployee; }

    public ComboBox<Customer> getCmbRentalCustomer() { return cmbRentalCustomer; }
    public void setCmbRentalCustomer(ComboBox<Customer> cmbRentalCustomer) { this.cmbRentalCustomer = cmbRentalCustomer; }

    public ComboBox<Vehicle> getCmbRentalVehicle() { return cmbRentalVehicle; }
    public void setCmbRentalVehicle(ComboBox<Vehicle> cmbRentalVehicle) { this.cmbRentalVehicle = cmbRentalVehicle; }

    public DatePicker getDpRentalStart() { return dpRentalStart; }
    public void setDpRentalStart(DatePicker dpRentalStart) { this.dpRentalStart = dpRentalStart; }

    public DatePicker getDpRentalEnd() { return dpRentalEnd; }
    public void setDpRentalEnd(DatePicker dpRentalEnd) { this.dpRentalEnd = dpRentalEnd; }

    public TextField getTxtRentalDays() { return txtRentalDays; }
    public void setTxtRentalDays(TextField txtRentalDays) { this.txtRentalDays = txtRentalDays; }

    public Button getBtnProcessRental() { return btnProcessRental; }
    public void setBtnProcessRental(Button btnProcessRental) { this.btnProcessRental = btnProcessRental; }

    public ComboBox<Vehicle> getCmbReturnVehicle() { return cmbReturnVehicle; }
    public void setCmbReturnVehicle(ComboBox<Vehicle> cmbReturnVehicle) { this.cmbReturnVehicle = cmbReturnVehicle; }

    public Button getBtnProcessReturn() { return btnProcessReturn; }
    public void setBtnProcessReturn(Button btnProcessReturn) { this.btnProcessReturn = btnProcessReturn; }

    public TableView<Transaction> getHistoryTable() { return historyTable; }
    public void setHistoryTable(TableView<Transaction> historyTable) { this.historyTable = historyTable; }

    public TableColumn<Transaction, String> getColTxnId() { return colTxnId; }
    public void setColTxnId(TableColumn<Transaction, String> colTxnId) { this.colTxnId = colTxnId; }

    public TableColumn<Transaction, String> getColTxnType() { return colTxnType; }
    public void setColTxnType(TableColumn<Transaction, String> colTxnType) { this.colTxnType = colTxnType; }

    public TableColumn<Transaction, String> getColTxnDate() { return colTxnDate; }
    public void setColTxnDate(TableColumn<Transaction, String> colTxnDate) { this.colTxnDate = colTxnDate; }

    public TableColumn<Transaction, String> getColTxnEmployee() { return colTxnEmployee; }
    public void setColTxnEmployee(TableColumn<Transaction, String> colTxnEmployee) { this.colTxnEmployee = colTxnEmployee; }

    public TableColumn<Transaction, String> getColTxnCustomer() { return colTxnCustomer; }
    public void setColTxnCustomer(TableColumn<Transaction, String> colTxnCustomer) { this.colTxnCustomer = colTxnCustomer; }

    public TableColumn<Transaction, String> getColTxnVehicle() { return colTxnVehicle; }
    public void setColTxnVehicle(TableColumn<Transaction, String> colTxnVehicle) { this.colTxnVehicle = colTxnVehicle; }

    public TableColumn<Transaction, String> getColTxnAmount() { return colTxnAmount; }
    public void setColTxnAmount(TableColumn<Transaction, String> colTxnAmount) { this.colTxnAmount = colTxnAmount; }
}
