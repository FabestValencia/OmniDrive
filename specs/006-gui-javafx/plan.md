# Plan Técnico: Interfaz Gráfica de Usuario en JavaFX

* **Feature:** `006-gui-javafx`
* **Estado:** borrador
* **Tecnologías:** Java 21 LTS, JavaFX 21.0.2 (`javafx-controls`, `javafx-fxml`), CSS moderno, JUnit 5

---

## 1. Arquitectura y Patrones (MVC Puro)

```text
com.omnidrive/
├── App.java                                 (Arranque JavaFX, carga de persistencia y ventana de Login)
├── context/
│   └── AppContext.java                      (Gestión de sesión activa, instancia central Dealership y navegación)
├── controller/
│   ├── LoginController.java                 (Controlador de acceso y autenticación)
│   ├── PasswordRecoveryController.java      (Modal de recuperación de clave con código temporal)
│   ├── MainLayoutController.java            (Sidebar dinámico por rol, topbar y conmutación de vistas)
│   ├── DashboardController.java             (Tarjetas de bienvenida e indicadores rápidos del concesionario)
│   ├── EmployeeManagementController.java    (Gestión de empleados, edición y bloqueo - Solo Admin)
│   ├── VehicleManagementController.java     (Tabla de vehículos, filtros en tiempo real y panel de detalle)
│   ├── VehicleFormDialogController.java     (Formulario modal dinámico para los 8 vehículos y tecnologías)
│   ├── CustomerManagementController.java    (Listado y registro de clientes)
│   ├── TransactionController.java           (Pestañas de Compra, Venta, Alquiler y Devolución con auditoría)
│   └── ReportController.java                (Reportes gerenciales por empleado, comisión 2% y exportación TXT)
└── util/
    └── AlertUtil.java                       (Diálogos de alerta, confirmación y error estandarizados)
```

### Recursos Visuales (`src/main/resources/com/omnidrive/view/`)
* `styles.css`: Estilos corporativos 'Light Clean', componentes, tablas, botones, formularios, alertas y tarjetas.
* `LoginView.fxml`, `PasswordRecoveryDialog.fxml`, `MainLayoutView.fxml`, `DashboardView.fxml`, `EmployeeManagementView.fxml`, `VehicleManagementView.fxml`, `VehicleFormDialog.fxml`, `CustomerManagementView.fxml`, `TransactionView.fxml`, `ReportView.fxml`.
* `images/`: Silueta de vehículo por defecto (`default_vehicle.png`).

---

## 2. Matriz de Trazabilidad (Requisitos vs Componentes)

| Requisito Funcional | Vistas / Controladores | Modelo / Contrato |
| :--- | :--- | :--- |
| **RF-01, RF-02** (MVC y CSS 'Light Clean') | `App.java`, `styles.css`, `MainLayoutView.fxml` | `Dealership`, `AppContext` |
| **RF-03** (Persistencia automática) | Todos los controladores ante mutación | `DealershipPersistenceManager.save(...)` |
| **RF-04, RF-07, RF-08** (Login y Recuperación) | `LoginView.fxml`, `PasswordRecoveryDialog.fxml` | `dealership.authenticate`, `requestPasswordRecovery` |
| **RF-05, RF-06** (Sidebar adaptativo por rol) | `MainLayoutView.fxml`, `MainLayoutController` | `AppContext.getCurrentUser()`, `Administrator`/`Employee` |
| **RF-09, RF-10** (Transacciones, compras y revisión) | `TransactionView.fxml`, `TransactionController` | `dealership.buyVehicle`, `sellVehicle`, `rentVehicle` |
| **RF-11** (Reportes y comisiones 2%) | `ReportView.fxml`, `ReportController` | `dealership.generateEmployeeReport`, exportación a TXT |
| **RF-12** (Manejo de excepciones de negocio) | `AlertUtil.java`, todos los controladores | Captura de `OmniDriveException` y alertas amigables |

---

## 3. Desglose Granular de Tareas TDD (T1 a T13)

1. **T1:** `AppContext` (sesión y persistencia) y `AlertUtil` (diálogos visuales).
2. **T2:** Sistema de diseño 'Light Clean' en `styles.css` y silueta por defecto.
3. **T3:** Pantalla de autenticación (`LoginView.fxml`, `LoginController`).
4. **T4:** Asistente modal de recuperación de contraseñas (`PasswordRecoveryDialog.fxml`, `PasswordRecoveryController`).
5. **T5:** Shell principal con Sidebar adaptativo por rol (`MainLayoutView.fxml`, `MainLayoutController`).
6. **T6:** Panel de bienvenida y métricas rápidas (`DashboardView.fxml`, `DashboardController`).
7. **T7:** Módulo de Gestión de Empleados (`EmployeeManagementView.fxml`, `EmployeeManagementController`).
8. **T8:** Módulo de Inventario de Vehículos con panel lateral (`VehicleManagementView.fxml`, `VehicleManagementController`).
9. **T9:** Diálogo modal reactivo para 8 vehículos y tecnologías híbridas/eléctricas (`VehicleFormDialog.fxml`).
10. **T10:** Módulo de Gestión de Clientes (`CustomerManagementView.fxml`, `CustomerManagementController`).
11. **T11:** Módulo de Transacciones Comerciales (`TransactionView.fxml`, `TransactionController`) con pestañas compra/venta/alquiler/devolución.
12. **T12:** Módulo de Reportes Gerenciales con comisión 2% y exportación TXT (`ReportView.fxml`, `ReportController`).
13. **T13:** Integración en `App.java`, validación de los 6 escenarios de aceptación (`GuiAcceptanceIntegrationTest`) y verificación final con `mvn clean compile test` y `mvn javafx:run`.
