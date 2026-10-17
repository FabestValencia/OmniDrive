package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.model.Customer;
import com.omnidrive.model.Employee;
import com.omnidrive.model.EmployeeReport;
import com.omnidrive.model.Transaction;
import com.omnidrive.model.Vehicle;
import com.omnidrive.util.AlertUtil;
import com.omnidrive.validation.InputValidator;
import com.omnidrive.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador para la generación y exportación de reportes gerenciales (MVC).
 * Permite filtrar por colaborador y rango de fechas, calcular comisiones por venta del 2%
 * y exportar desgloses auditados a archivos de texto plano (.txt).
 */
public class ReportController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    private ComboBox<Employee> cmbEmployee;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private Button btnGenerateReport;
    @FXML
    private Button btnExportTxt;

    @FXML
    private Label lblPurchaseAmount;
    @FXML
    private Label lblSaleAmount;
    @FXML
    private Label lblRentalAmount;
    @FXML
    private Label lblSalesCommission;
    @FXML
    private Label lblGrandTotal;
    @FXML
    private Label lblTotalTransactions;

    @FXML
    private TableView<Transaction> tblReportTransactions;
    @FXML
    private TableColumn<Transaction, String> colTxId;
    @FXML
    private TableColumn<Transaction, String> colTxType;
    @FXML
    private TableColumn<Transaction, String> colTxDate;
    @FXML
    private TableColumn<Transaction, String> colTxVehicle;
    @FXML
    private TableColumn<Transaction, String> colTxCustomer;
    @FXML
    private TableColumn<Transaction, String> colTxAmount;

    private EmployeeReport currentReport;

    @FXML
    public void initialize() {
        setupEmployeeComboBox();
        setupTableColumns();
        setupDefaultDates();
        resetMetricLabels();
    }

    private void setupEmployeeComboBox() {
        if (cmbEmployee == null) return;

        cmbEmployee.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee emp) {
                return emp == null ? "" : emp.getFullName() + " (" + emp.getId() + " - " + emp.getUsername() + ")";
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });

        if (AppContext.getDealership() != null) {
            List<Employee> employees = AppContext.getDealership().getAllEmployees();
            cmbEmployee.setItems(FXCollections.observableArrayList(employees));
            if (!employees.isEmpty()) {
                cmbEmployee.getSelectionModel().selectFirst();
            }
        }
    }

    private void setupTableColumns() {
        if (colTxId != null) {
            colTxId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        }
        if (colTxType != null) {
            colTxType.setCellValueFactory(cell -> {
                String typeStr = switch (cell.getValue().getType()) {
                    case PURCHASE -> "COMPRA";
                    case SALE -> "VENTA";
                    case RENTAL -> "ALQUILER";
                };
                return new SimpleStringProperty(typeStr);
            });
        }
        if (colTxDate != null) {
            colTxDate.setCellValueFactory(cell ->
                    new SimpleStringProperty(DATE_TIME_FORMATTER.format(cell.getValue().getTimestamp())));
        }
        if (colTxVehicle != null) {
            colTxVehicle.setCellValueFactory(cell -> {
                Vehicle v = cell.getValue().getVehicle();
                return new SimpleStringProperty(v != null ? v.getPlate() + " - " + v.getBrand() : "N/A");
            });
        }
        if (colTxCustomer != null) {
            colTxCustomer.setCellValueFactory(cell -> {
                Customer c = cell.getValue().getCustomer();
                return new SimpleStringProperty(c != null ? c.getFullName() + " (" + c.getId() + ")" : "N/A");
            });
        }
        if (colTxAmount != null) {
            colTxAmount.setCellValueFactory(cell ->
                    new SimpleStringProperty(String.format(Locale.US, "$%,.2f", cell.getValue().getTotalAmount())));
        }
    }

    private void setupDefaultDates() {
        if (dpStartDate != null && dpStartDate.getValue() == null) {
            dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        }
        if (dpEndDate != null && dpEndDate.getValue() == null) {
            dpEndDate.setValue(LocalDate.now());
        }
    }

    private void resetMetricLabels() {
        if (lblPurchaseAmount != null) lblPurchaseAmount.setText("$0.00 (0 ops)");
        if (lblSaleAmount != null) lblSaleAmount.setText("$0.00 (0 ops)");
        if (lblRentalAmount != null) lblRentalAmount.setText("$0.00 (0 ops)");
        if (lblSalesCommission != null) lblSalesCommission.setText("$0.00");
        if (lblGrandTotal != null) lblGrandTotal.setText("$0.00");
        if (lblTotalTransactions != null) lblTotalTransactions.setText("0");
    }

    @FXML
    public void handleGenerateReport() {
        Employee selectedEmployee = cmbEmployee != null ? cmbEmployee.getValue() : null;
        if (selectedEmployee == null) {
            AlertUtil.showWarning("Validación de Reporte", "Debe seleccionar un empleado para generar el reporte.");
            return;
        }

        LocalDate startDate = dpStartDate != null ? dpStartDate.getValue() : null;
        LocalDate endDate = dpEndDate != null ? dpEndDate.getValue() : null;

        ValidationResult dateRangeResult = InputValidator.validateDateRange(startDate, endDate);
        if (!dateRangeResult.isValid()) {
            AlertUtil.showWarning("Validación de Fechas", dateRangeResult.getFirstErrorMessage());
            currentReport = null;
            resetMetricLabels();
            if (tblReportTransactions != null) {
                tblReportTransactions.setItems(FXCollections.emptyObservableList());
            }
            return;
        }

        try {
            currentReport = AppContext.getDealership().generateEmployeeReport(selectedEmployee.getId(), startDate, endDate);
            updateViewWithReport(currentReport);
        } catch (OmniDriveException e) {
            currentReport = null;
            resetMetricLabels();
            if (tblReportTransactions != null) {
                tblReportTransactions.setItems(FXCollections.emptyObservableList());
            }
            AlertUtil.showError("Error al Generar Reporte", e.getMessage());
        }
    }

    private void updateViewWithReport(EmployeeReport report) {
        if (report == null) return;

        if (lblPurchaseAmount != null) {
            lblPurchaseAmount.setText(String.format(Locale.US, "$%,.2f (%d ops)",
                    report.getTotalPurchaseAmount(), report.getPurchaseCount()));
        }
        if (lblSaleAmount != null) {
            lblSaleAmount.setText(String.format(Locale.US, "$%,.2f (%d ops)",
                    report.getTotalSaleAmount(), report.getSaleCount()));
        }
        if (lblRentalAmount != null) {
            lblRentalAmount.setText(String.format(Locale.US, "$%,.2f (%d ops)",
                    report.getTotalRentalAmount(), report.getRentalCount()));
        }
        if (lblSalesCommission != null) {
            lblSalesCommission.setText(String.format(Locale.US, "$%,.2f", report.getSalesCommission()));
        }
        if (lblGrandTotal != null) {
            lblGrandTotal.setText(String.format(Locale.US, "$%,.2f", report.getGrandTotalAmount()));
        }
        if (lblTotalTransactions != null) {
            lblTotalTransactions.setText(String.valueOf(report.getTotalTransactionsCount()));
        }

        if (tblReportTransactions != null) {
            tblReportTransactions.setItems(FXCollections.observableArrayList(report.getTransactions()));
        }
    }

    @FXML
    public void handleExportTxt() {
        if (currentReport == null) {
            AlertUtil.showWarning("Exportación no disponible", "Primero debe generar un reporte para poder exportarlo.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Reporte Gerencial a TXT");
        String defaultFileName = String.format("Reporte_%s_%s_%s.txt",
                currentReport.getEmployee().getUsername(),
                DATE_FORMATTER.format(LocalDate.now()),
                System.currentTimeMillis() % 10000);
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto (*.txt)", "*.txt"));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            boolean exported = exportReportToFile(file);
            if (exported) {
                AlertUtil.showInfo("Exportación Exitosa",
                        "El reporte gerencial ha sido exportado exitosamente a:\n" + file.getAbsolutePath());
            }
        }
    }

    public boolean exportReportToFile(File file) {
        if (currentReport == null || file == null) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                       OMNIDRIVE - REPORTE GERENCIAL DE RENDIMIENTO                     \n");
        sb.append("========================================================================================\n\n");

        sb.append("DATOS DEL COLABORADOR:\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("  Nombre Completo : %s\n", currentReport.getEmployee().getFullName()));
        sb.append(String.format("  Identificación  : %s\n", currentReport.getEmployee().getId()));
        sb.append(String.format("  Usuario         : %s\n", currentReport.getEmployee().getUsername()));
        sb.append(String.format("  Correo Electrón.: %s\n", currentReport.getEmployee().getEmail()));
        sb.append(String.format("  Teléfono        : %s\n", currentReport.getEmployee().getPhone()));
        sb.append(String.format("  Estado Cuenta   : %s\n\n", currentReport.getEmployee().getStatus()));

        sb.append("PARÁMETROS DEL REPORTE:\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("  Rango Inicio    : %s\n", DATE_TIME_FORMATTER.format(currentReport.getStartInstant())));
        sb.append(String.format("  Rango Fin       : %s\n", DATE_TIME_FORMATTER.format(currentReport.getEndInstant())));
        sb.append(String.format("  Generado el     : %s\n\n", DATE_TIME_FORMATTER.format(currentReport.getGeneratedAt())));

        sb.append("RESUMEN EJECUTIVO Y COMISIONES:\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(Locale.US, "  Total Compras           : %3d operaciones  |  Total: $%,15.2f\n",
                currentReport.getPurchaseCount(), currentReport.getTotalPurchaseAmount()));
        sb.append(String.format(Locale.US, "  Total Ventas            : %3d operaciones  |  Total: $%,15.2f\n",
                currentReport.getSaleCount(), currentReport.getTotalSaleAmount()));
        sb.append(String.format(Locale.US, "  Total Alquileres        : %3d operaciones  |  Total: $%,15.2f\n",
                currentReport.getRentalCount(), currentReport.getTotalRentalAmount()));
        sb.append(String.format(Locale.US, "  Comisión por Ventas (2%%):                   Total: $%,15.2f\n",
                currentReport.getSalesCommission()));
        sb.append("  --------------------------------------------------------------------------------------\n");
        sb.append(String.format(Locale.US, "  Gran Total Consolidado  : %3d transacciones|  Monto: $%,15.2f\n\n",
                currentReport.getTotalTransactionsCount(), currentReport.getGrandTotalAmount()));

        sb.append("DETALLE DE TRANSACCIONES AUDITADAS:\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s | %-19s | %-10s | %-10s | %-20s | %14s\n",
                "ID TX", "FECHA/HORA", "TIPO", "PLACA", "CLIENTE", "MONTO ($)"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Transaction tx : currentReport.getTransactions()) {
            String vehicleInfo = tx.getVehicle() != null ? tx.getVehicle().getPlate() : "N/A";
            String customerInfo = tx.getCustomer() != null ? tx.getCustomer().getFullName() : "N/A";
            if (customerInfo.length() > 20) {
                customerInfo = customerInfo.substring(0, 17) + "...";
            }
            sb.append(String.format(Locale.US, "%-12s | %-19s | %-10s | %-10s | %-20s | %14.2f\n",
                    tx.getId(),
                    DATE_TIME_FORMATTER.format(tx.getTimestamp()),
                    tx.getType().name(),
                    vehicleInfo,
                    customerInfo,
                    tx.getTotalAmount()));
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("                     FIN DEL REPORTE - SISTEMA OMNIDRIVE (CONFIDENCIAL)                 \n");
        sb.append("========================================================================================\n");

        try {
            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            AlertUtil.showError("Error de Exportación", "No se pudo escribir el archivo: " + e.getMessage());
            return false;
        }
    }

    // Getters y Setters para pruebas unitarias y binding FXML
    public EmployeeReport getCurrentReport() {
        return currentReport;
    }

    public ComboBox<Employee> getCmbEmployee() {
        return cmbEmployee;
    }

    public void setCmbEmployee(ComboBox<Employee> cmbEmployee) {
        this.cmbEmployee = cmbEmployee;
    }

    public DatePicker getDpStartDate() {
        return dpStartDate;
    }

    public void setDpStartDate(DatePicker dpStartDate) {
        this.dpStartDate = dpStartDate;
    }

    public DatePicker getDpEndDate() {
        return dpEndDate;
    }

    public void setDpEndDate(DatePicker dpEndDate) {
        this.dpEndDate = dpEndDate;
    }

    public Button getBtnGenerateReport() {
        return btnGenerateReport;
    }

    public void setBtnGenerateReport(Button btnGenerateReport) {
        this.btnGenerateReport = btnGenerateReport;
    }

    public Button getBtnExportTxt() {
        return btnExportTxt;
    }

    public void setBtnExportTxt(Button btnExportTxt) {
        this.btnExportTxt = btnExportTxt;
    }

    public Label getLblPurchaseAmount() {
        return lblPurchaseAmount;
    }

    public void setLblPurchaseAmount(Label lblPurchaseAmount) {
        this.lblPurchaseAmount = lblPurchaseAmount;
    }

    public Label getLblSaleAmount() {
        return lblSaleAmount;
    }

    public void setLblSaleAmount(Label lblSaleAmount) {
        this.lblSaleAmount = lblSaleAmount;
    }

    public Label getLblRentalAmount() {
        return lblRentalAmount;
    }

    public void setLblRentalAmount(Label lblRentalAmount) {
        this.lblRentalAmount = lblRentalAmount;
    }

    public Label getLblSalesCommission() {
        return lblSalesCommission;
    }

    public void setLblSalesCommission(Label lblSalesCommission) {
        this.lblSalesCommission = lblSalesCommission;
    }

    public Label getLblGrandTotal() {
        return lblGrandTotal;
    }

    public void setLblGrandTotal(Label lblGrandTotal) {
        this.lblGrandTotal = lblGrandTotal;
    }

    public Label getLblTotalTransactions() {
        return lblTotalTransactions;
    }

    public void setLblTotalTransactions(Label lblTotalTransactions) {
        this.lblTotalTransactions = lblTotalTransactions;
    }

    public TableView<Transaction> getTblReportTransactions() {
        return tblReportTransactions;
    }

    public void setTblReportTransactions(TableView<Transaction> tblReportTransactions) {
        this.tblReportTransactions = tblReportTransactions;
    }

    public TableColumn<Transaction, String> getColTxId() {
        return colTxId;
    }

    public void setColTxId(TableColumn<Transaction, String> colTxId) {
        this.colTxId = colTxId;
    }

    public TableColumn<Transaction, String> getColTxType() {
        return colTxType;
    }

    public void setColTxType(TableColumn<Transaction, String> colTxType) {
        this.colTxType = colTxType;
    }

    public TableColumn<Transaction, String> getColTxDate() {
        return colTxDate;
    }

    public void setColTxDate(TableColumn<Transaction, String> colTxDate) {
        this.colTxDate = colTxDate;
    }

    public TableColumn<Transaction, String> getColTxVehicle() {
        return colTxVehicle;
    }

    public void setColTxVehicle(TableColumn<Transaction, String> colTxVehicle) {
        this.colTxVehicle = colTxVehicle;
    }

    public TableColumn<Transaction, String> getColTxCustomer() {
        return colTxCustomer;
    }

    public void setColTxCustomer(TableColumn<Transaction, String> colTxCustomer) {
        this.colTxCustomer = colTxCustomer;
    }

    public TableColumn<Transaction, String> getColTxAmount() {
        return colTxAmount;
    }

    public void setColTxAmount(TableColumn<Transaction, String> colTxAmount) {
        this.colTxAmount = colTxAmount;
    }
}
