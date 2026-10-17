package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Aceptación e Integración GUI - Spec 006 (6 Escenarios Oficiales)")
class GuiAcceptanceIntegrationTest {

    private Employee activeEmployee;
    private Employee blockedEmployee;
    private Customer testCustomer;
    private Sedan sedanNew;

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit ya inicializado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));

        // Registrar empleado activo
        activeEmployee = new Employee("EMP-ACCEPT-01", "Roberto", "Gómez", "3001112233",
                "roberto@omnidrive.com", "rgomez", "EmpPass123*");
        AppContext.getDealership().registerEmployee(activeEmployee);

        // Registrar empleado bloqueado
        blockedEmployee = new Employee("EMP-BLOCKED-01", "Pedro", "Navaja", "3009998877",
                "pedro@omnidrive.com", "pnavaja", "BlockedPass123*");
        AppContext.getDealership().registerEmployee(blockedEmployee);
        AppContext.getDealership().blockEmployee(blockedEmployee.getId());

        // Registrar cliente
        testCustomer = new Customer("CUST-ACCEPT-01", "Diana", "Uribe", "3112223344", "duribe@historia.com");
        AppContext.getDealership().registerCustomer(testCustomer);

        // Registrar vehículo nuevo
        sedanNew = new Sedan("ACC-001", "Mazda 3", 2024, 6, 210.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 95_000_000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        AppContext.getDealership().registerVehicle(sedanNew);
    }

    @Test
    @DisplayName("Escenario 1: Autenticación exitosa y barra lateral adaptativa por rol (Admin vs Empleado)")
    void testScenario1_AuthenticationAndSidebarByRole() {
        LoginController loginController = new LoginController();
        AtomicBoolean loginSuccessCalled = new AtomicBoolean(false);
        loginController.setOnLoginSuccess(user -> loginSuccessCalled.set(true));

        // 1. Login exitoso como Administrador default
        boolean adminLoginOk = loginController.processLogin("admin", "Admin123*");
        assertTrue(adminLoginOk, "El Administrador root debe autenticarse exitosamente");
        assertTrue(loginSuccessCalled.get());
        assertTrue(AppContext.isAdmin());

        // Verificar vista y botones de MainLayout para Administrador
        MainLayoutController mainLayout = new MainLayoutController();
        Button btnEmployees = new Button();
        Button btnReports = new Button();
        Button btnTransactions = new Button();
        Label lblUserRole = new Label();
        Label lblUserName = new Label();

        mainLayout.setBtnEmployees(btnEmployees);
        mainLayout.setBtnReports(btnReports);
        mainLayout.setBtnTransactions(btnTransactions);
        mainLayout.setLblUserRole(lblUserRole);
        mainLayout.setLblUserName(lblUserName);

        mainLayout.refreshUserSession();

        assertTrue(btnEmployees.isVisible(), "Admin debe ver botón de Empleados");
        assertTrue(btnReports.isVisible(), "Admin debe ver botón de Reportes");
        assertFalse(btnTransactions.isVisible(), "Admin NO debe ver botón de Transacciones comerciales");
        assertEquals("Administrador", lblUserRole.getText());

        // 2. Conmutar sesión a Empleado activo
        AppContext.login(activeEmployee);
        mainLayout.refreshUserSession();

        assertFalse(btnEmployees.isVisible(), "Empleado NO debe ver botón de Empleados");
        assertFalse(btnReports.isVisible(), "Empleado NO debe ver botón de Reportes");
        assertTrue(btnTransactions.isVisible(), "Empleado SÍ debe ver botón de Transacciones comerciales");
        assertEquals("Empleado", lblUserRole.getText());
    }

    @Test
    @DisplayName("Escenario 2: Rechazo en login de empleado en estado bloqueado")
    void testScenario2_BlockedEmployeeLoginRejected() {
        LoginController loginController = new LoginController();

        boolean loginOk = loginController.processLogin("pnavaja", "BlockedPass123*");

        assertFalse(loginOk, "El empleado bloqueado no debe poder iniciar sesión");
        assertFalse(AppContext.isAuthenticated(), "No debe existir sesión activa tras intento bloqueado");
        assertNotNull(loginController.getLastErrorMessage());
        assertTrue(loginController.getLastErrorMessage().toLowerCase().contains("bloqueada") ||
                loginController.getLastErrorMessage().toLowerCase().contains("bloqueado"));
    }

    @Test
    @DisplayName("Escenario 3: Recuperación de clave con código temporal de 6 dígitos")
    void testScenario3_PasswordRecoveryFlow() {
        PasswordRecoveryController recoveryController = new PasswordRecoveryController();

        // Paso 1: Solicitar código para el empleado activo
        boolean requested = recoveryController.processCodeRequest("roberto@omnidrive.com", Instant.now());
        assertTrue(requested, "La solicitud de código para correo válido debe completarse");
        String code = recoveryController.getGeneratedCode();
        assertNotNull(code);
        assertEquals(6, code.length());

        // Paso 2: Restablecer contraseña con código generado
        boolean resetOk = recoveryController.processPasswordReset("roberto@omnidrive.com", code, "NewSecret2026*", Instant.now());
        assertTrue(resetOk, "El reseteo de clave con código correcto debe ser exitoso");

        // Validar que ahora se puede iniciar sesión con la nueva contraseña
        LoginController loginController = new LoginController();
        boolean loginWithOldPass = loginController.processLogin("rgomez", "EmpPass123*");
        assertFalse(loginWithOldPass, "La clave antigua ya no debe ser válida");

        boolean loginWithNewPass = loginController.processLogin("rgomez", "NewSecret2026*");
        assertTrue(loginWithNewPass, "El inicio de sesión debe triunfar con la nueva contraseña");
    }

    @Test
    @DisplayName("Escenario 4: Inventario con tabla, filtros en tiempo real y panel lateral de ficha técnica")
    void testScenario4_VehicleManagementInventoryAndTechnicalPanel() {
        VehicleManagementController controller = new VehicleManagementController();
        controller.setVehicleTable(new TableView<>());
        controller.setColPlate(new TableColumn<>());
        controller.setColBrand(new TableColumn<>());
        controller.setColType(new TableColumn<>());
        controller.setColModelYear(new TableColumn<>());
        controller.setColFuelType(new TableColumn<>());
        controller.setColCondition(new TableColumn<>());
        controller.setColStatus(new TableColumn<>());
        controller.setColBasePrice(new TableColumn<>());

        controller.setTxtSearch(new TextField());
        controller.setCmbConditionFilter(new ComboBox<>());
        controller.setCmbStatusFilter(new ComboBox<>());

        controller.setImgVehiclePhoto(new ImageView());
        controller.setLblDetailPlate(new Label());
        controller.setLblDetailBrand(new Label());
        controller.setLblDetailType(new Label());
        controller.setLblDetailStatus(new Label());
        controller.setLblDetailPrice(new Label());
        controller.setLblDetailTechnicalSpecs(new Label());
        controller.setDetailPanel(new VBox());
        controller.setBtnNewVehicle(new Button());

        controller.initialize();

        assertEquals(1, controller.getVehicleTable().getItems().size());

        // Seleccionar vehículo y verificar sincronización del panel lateral
        controller.getVehicleTable().getSelectionModel().select(sedanNew);
        assertEquals("ACC-001", controller.getLblDetailPlate().getText());
        assertTrue(controller.getLblDetailBrand().getText().contains("Mazda 3"));
        assertEquals("Sedán", controller.getLblDetailType().getText());
        assertTrue(controller.getLblDetailTechnicalSpecs().getText().contains("2024"));

        // Filtrar por búsqueda
        controller.getTxtSearch().setText("ACC-001");
        assertEquals(1, controller.getVehicleTable().getItems().size());

        controller.getTxtSearch().setText("INEXISTENTE");
        assertEquals(0, controller.getVehicleTable().getItems().size());
    }

    @Test
    @DisplayName("Escenario 5: Registro y bloqueo en compra de usado sin revisión técnica aprobada")
    void testScenario5_BuyUsedVehicleTechnicalReviewValidation() {
        TransactionController controller = new TransactionController();
        controller.setCmbBuyEmployee(new ComboBox<>());
        controller.setCmbBuyCustomer(new ComboBox<>());
        controller.setCmbBuyVehicleType(new ComboBox<>());
        controller.setTxtBuyPlate(new TextField());
        controller.setTxtBuyBrand(new TextField());
        controller.setTxtBuyYear(new TextField());
        controller.setTxtBuyCylinder(new TextField());
        controller.setCmbBuyFuel(new ComboBox<>());
        controller.setCmbBuyTransmission(new ComboBox<>());
        controller.setCmbBuyCondition(new ComboBox<>());
        controller.setChkBuyTechnicalReview(new CheckBox());
        controller.setCmbBuyInitialStatus(new ComboBox<>());
        controller.setTxtBuyAmount(new TextField());
        controller.setBtnProcessBuy(new Button());

        // Resto de controles
        controller.setCmbSaleEmployee(new ComboBox<>());
        controller.setCmbSaleCustomer(new ComboBox<>());
        controller.setCmbSaleVehicle(new ComboBox<>());
        controller.setTxtSaleAmount(new TextField());
        controller.setBtnProcessSale(new Button());
        controller.setCmbRentalEmployee(new ComboBox<>());
        controller.setCmbRentalCustomer(new ComboBox<>());
        controller.setCmbRentalVehicle(new ComboBox<>());
        controller.setDpRentalStart(new DatePicker());
        controller.setDpRentalEnd(new DatePicker());
        controller.setTxtRentalDays(new TextField());
        controller.setBtnProcessRental(new Button());
        controller.setCmbReturnVehicle(new ComboBox<>());
        controller.setBtnProcessReturn(new Button());
        controller.setHistoryTable(new TableView<>());
        controller.setColTxnId(new TableColumn<>());
        controller.setColTxnType(new TableColumn<>());
        controller.setColTxnDate(new TableColumn<>());
        controller.setColTxnEmployee(new TableColumn<>());
        controller.setColTxnCustomer(new TableColumn<>());
        controller.setColTxnVehicle(new TableColumn<>());
        controller.setColTxnAmount(new TableColumn<>());
        controller.initialize();

        controller.getCmbBuyEmployee().setValue(activeEmployee);
        controller.getCmbBuyCustomer().setValue(testCustomer);
        controller.getCmbBuyVehicleType().setValue("Sedán");
        controller.getTxtBuyPlate().setText("USD-999");
        controller.getTxtBuyBrand().setText("Chevrolet Aveo");
        controller.getTxtBuyYear().setText("2018");
        controller.getTxtBuyCylinder().setText("1500");
        controller.getCmbBuyFuel().setValue(FuelType.GASOLINE);
        controller.getCmbBuyTransmission().setValue(TransmissionType.MANUAL);
        controller.getCmbBuyCondition().setValue(VehicleCondition.USED);
        controller.getCmbBuyInitialStatus().setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        controller.getTxtBuyAmount().setText("25000000");

        // 1. Intento sin revisión técnica marcada -> DEBE SER RECHAZADA
        controller.getChkBuyTechnicalReview().setSelected(false);
        boolean buyWithoutReview = controller.processBuy();
        assertFalse(buyWithoutReview, "La compra de un vehículo usado sin revisión técnica aprobada debe ser rechazada");
        assertNull(AppContext.getDealership().findVehicleByPlate("USD-999"));

        // 2. Intento con revisión técnica aprobada -> DEBE SER ACEPTADA
        controller.getChkBuyTechnicalReview().setSelected(true);
        boolean buyWithReview = controller.processBuy();
        assertTrue(buyWithReview, "La compra con revisión técnica aprobada debe procesarse con éxito");
        assertNotNull(AppContext.getDealership().findVehicleByPlate("USD-999"));
    }

    @Test
    @DisplayName("Escenario 6: Generación y exportación de reporte gerencial con comisión del 2% y archivo TXT")
    void testScenario6_ReportGenerationCommissionAndTxtExport(@TempDir Path tempDir) throws Exception {
        // Registrar una venta de $100,000,000 realizada por el empleado activo
        AppContext.getDealership().sellVehicle(activeEmployee, testCustomer, sedanNew.getPlate(), 100_000_000.0, Instant.now());

        ReportController controller = new ReportController();
        controller.setCmbEmployee(new ComboBox<>());
        controller.setDpStartDate(new DatePicker());
        controller.setDpEndDate(new DatePicker());
        controller.setBtnGenerateReport(new Button());
        controller.setBtnExportTxt(new Button());
        controller.setLblPurchaseAmount(new Label());
        controller.setLblSaleAmount(new Label());
        controller.setLblRentalAmount(new Label());
        controller.setLblSalesCommission(new Label());
        controller.setLblGrandTotal(new Label());
        controller.setLblTotalTransactions(new Label());
        controller.setTblReportTransactions(new TableView<>());
        controller.setColTxId(new TableColumn<>());
        controller.setColTxType(new TableColumn<>());
        controller.setColTxDate(new TableColumn<>());
        controller.setColTxVehicle(new TableColumn<>());
        controller.setColTxCustomer(new TableColumn<>());
        controller.setColTxAmount(new TableColumn<>());
        controller.initialize();

        controller.getCmbEmployee().setValue(activeEmployee);
        controller.getDpStartDate().setValue(LocalDate.now().minusDays(1));
        controller.getDpEndDate().setValue(LocalDate.now().plusDays(1));

        controller.handleGenerateReport();

        assertNotNull(controller.getCurrentReport());
        assertEquals(1, controller.getCurrentReport().getSaleCount());
        assertEquals(100_000_000.0, controller.getCurrentReport().getTotalSaleAmount());
        assertEquals(2_000_000.0, controller.getCurrentReport().getSalesCommission(), 0.001); // 2% de 100M = 2M
        assertTrue(controller.getLblSalesCommission().getText().contains("2,000,000.00") ||
                controller.getLblSalesCommission().getText().contains("2000000.00"));

        // Exportar a TXT
        File exportFile = tempDir.resolve("reporte_gerencial_audit.txt").toFile();
        boolean exported = controller.exportReportToFile(exportFile);

        assertTrue(exported, "El archivo de reporte debe exportarse con éxito");
        assertTrue(exportFile.exists());
        String txt = Files.readString(exportFile.toPath());
        assertTrue(txt.contains("OMNIDRIVE"));
        assertTrue(txt.contains("Roberto Gómez"));
        assertTrue(txt.contains("EMP-ACCEPT-01"));
        assertTrue(txt.contains("2%"));
        assertTrue(txt.contains("2,000,000.00") || txt.contains("2000000.00"));
        assertTrue(txt.contains("ACC-001"));
    }
}
