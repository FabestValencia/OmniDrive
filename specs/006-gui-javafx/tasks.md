# Tareas de Implementación: Interfaz Gráfica de Usuario en JavaFX

* **Feature:** `006-gui-javafx`
* **Metodología:** TDD estricto y diseño modular de granularidad fina
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas Granulares (T1 a T13)

- [x] **T1. Contexto de aplicación (`AppContext`) y utilidades visuales (`AlertUtil`)**
  * **RFs asociados:** RF-01, RF-03, RF-12
  * **Archivos:** `AppContext.java`, `AlertUtil.java`, `AppContextTest.java`
  * **Hecho cuando:** `AppContext` gestiona el estado persistido del concesionario, el usuario con sesión activa y la persistencia automática ante mutaciones. `AlertUtil` despliega alertas estandarizadas de información, advertencia y error en español.

- [x] **T2. Sistema de diseño corporativo 'Light Clean' (`styles.css`) y recursos visuales**
  * **RFs asociados:** RF-02
  * **Archivos:** `styles.css`, imagen de silueta por defecto `default_vehicle.png`
  * **Hecho cuando:** Define la paleta corporativa completa (fondo `#f8fafc`, zafiro `#1e40af`, tarjetas blancas, bordes `#e2e8f0`, badges de estado y micro-interacciones hover/focus).

- [x] **T3. Pantalla de Autenticación (`LoginView.fxml`, `LoginController`)**
  * **RFs asociados:** RF-04, RF-07, RF-12
  * **Archivos:** `LoginView.fxml`, `LoginController.java`, `LoginControllerTest.java`
  * **Hecho cuando:** Permite autenticar usuarios, valida campos vacíos, bloquea el acceso con alerta visual ante empleados bloqueados (`BlockedUserException`) y transiciona a la vista principal.

- [x] **T4. Asistente modal de recuperación de contraseñas (`PasswordRecoveryDialog.fxml`, `PasswordRecoveryController`)**
  * **RFs asociados:** RF-04, RF-08, RF-12
  * **Archivos:** `PasswordRecoveryDialog.fxml`, `PasswordRecoveryController.java`, `PasswordRecoveryControllerTest.java`
  * **Hecho cuando:** Gestiona la solicitud de código de 6 dígitos al correo, valida la expiración de 15 minutos y realiza el reseteo seguro de la clave.

- [x] **T5. Shell principal con Sidebar interactivo por rol (`MainLayoutView.fxml`, `MainLayoutController`)**
  * **RFs asociados:** RF-05, RF-06
  * **Archivos:** `MainLayoutView.fxml`, `MainLayoutController.java`, `MainLayoutControllerTest.java`
  * **Hecho cuando:** Despliega la barra lateral adaptativa que muestra u oculta módulos según el rol (Admin vs Empleado), topbar con datos del usuario activo y botón de logout seguro.

- [x] **T6. Panel de bienvenida e indicadores rápidos (`DashboardView.fxml`, `DashboardController`)**
  * **RFs asociados:** RF-01, RF-05, RF-06
  * **Archivos:** `DashboardView.fxml`, `DashboardController.java`, `DashboardControllerTest.java`
  * **Hecho cuando:** Muestra tarjetas con métricas rápidas del concesionario (vehículos disponibles, transacciones del día, clientes registrados) según permisos.

- [x] **T7. Módulo de Gestión de Empleados (`EmployeeManagementView.fxml`, `EmployeeManagementController`)**
  * **RFs asociados:** RF-05, RF-12
  * **Archivos:** `EmployeeManagementView.fxml`, `EmployeeManagementController.java`, `EmployeeManagementControllerTest.java`
  * **Hecho cuando:** Exclusivo para Administrador: tabla de colaboradores, registro de nuevo empleado, actualización de datos personales y bloqueo permanente con confirmación.

- [x] **T8. Módulo de Inventario de Vehículos (`VehicleManagementView.fxml`, `VehicleManagementController`)**
  * **RFs asociados:** RF-02, RF-07, RF-12
  * **Archivos:** `VehicleManagementView.fxml`, `VehicleManagementController.java`, `VehicleManagementControllerTest.java`
  * **Hecho cuando:** Tabla interactiva con columnas ordenables, búsqueda en tiempo real, filtros por marca/estado y panel lateral sincronizado con ficha técnica y foto del vehículo.

- [x] **T9. Formulario modal reactivo para los 8 tipos de vehículos (`VehicleFormDialog.fxml`, `VehicleFormDialogController`)**
  * **RFs asociados:** RF-02, RF-07, RF-12
  * **Archivos:** `VehicleFormDialog.fxml`, `VehicleFormDialogController.java`, `VehicleFormDialogControllerTest.java`
  * **Hecho cuando:** Formulario modal emergente que muestra u oculta campos específicos según el tipo de vehículo seleccionado (8 tipos) y combustible (eléctrico/híbrido) con selector de fotos 'Examinar'.

- [x] **T10. Módulo de Gestión de Clientes (`CustomerManagementView.fxml`, `CustomerManagementController`)**
  * **RFs asociados:** RF-06, RF-12
  * **Archivos:** `CustomerManagementView.fxml`, `CustomerManagementController.java`, `CustomerManagementControllerTest.java`
  * **Hecho cuando:** Tabla de clientes registrados, formulario de alta con validación de cédula única y formato de correo electrónico.

- [x] **T11. Módulo de Transacciones Comerciales (`TransactionView.fxml`, `TransactionController`)**
  * **RFs asociados:** RF-06, RF-09, RF-10, RF-12
  * **Archivos:** `TransactionView.fxml`, `TransactionController.java`, `TransactionControllerTest.java`
  * **Hecho cuando:** 4 pestañas independientes: Compra (bloqueando si usado sin revisión técnica aprobada), Venta (conmutación a `SOLD`), Alquiler (conmutación a `RENTED`) y Devolución (retorno a `AVAILABLE_FOR_RENT`), más historial de transacciones.

- [x] **T12. Módulo de Reportes Gerenciales con comisión del 2% y exportación (`ReportView.fxml`, `ReportController`)**
  * **RFs asociados:** RF-11, RF-12
  * **Archivos:** `ReportView.fxml`, `ReportController.java`, `ReportControllerTest.java`
  * **Hecho cuando:** Permite al Administrador filtrar por empleado y rango de fechas, muestra tarjetas de balance con el 2% de comisión sobre ventas y exporta el reporte a archivo `.txt`.

- [x] **T13. Integración global en `App.java`, validación de los 6 escenarios de aceptación y verificación final**
  * **RFs asociados:** RF-01 al RF-12, Escenarios 1 al 6
  * **Archivos:** `App.java`, `GuiAcceptanceIntegrationTest.java`
  * **Hecho cuando:** La aplicación se inicia desde `Main.java` y `App.java` enlazada a la persistencia, los 6 escenarios de aceptación oficiales están cubiertos, `mvn clean compile test` pasa al 100% y `mvn javafx:run` levanta sin excepciones.
