# Tareas de Implementación: Motor Centralizado de Validación de Datos

* **Feature:** `007-input-validation`
* **Metodología:** TDD estricto (Red -> Green -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas (T1 a T5)

- [x] **T1. Contenedor inmutable de resultados de validación (`ValidationResult`)**
  * **RFs asociados:** RF-01, RF-02
  * **Archivos:** `ValidationResult.java`, `ValidationResultTest.java`
  * **Hecho cuando:** `ValidationResult` ofrece métodos fábrica `success()` y `failure(List<String>)`, encapsula `isValid()`, `getErrors()` inmutable y `getFirstErrorMessage()` con 100% de cobertura en tests.

- [x] **T2. Motor utilitario de validación desacoplado (`InputValidator`)**
  * **RFs asociados:** RF-01, RF-02, RF-03, RF-04, RF-06
  * **Archivos:** `InputValidator.java`, `InputValidatorTest.java`
  * **Hecho cuando:** Valida placas automotrices y motocicletas, teléfonos, cédulas, correos RFC, años de modelo, parseo de números positivos sin lanzar excepciones y rangos cronológicos.

- [x] **T3. Estilo visual de error e integración en gestión de Clientes y Empleados**
  * **RFs asociados:** RF-05
  * **Archivos:** `styles.css`, `CustomerManagementController.java`, `EmployeeManagementController.java`
  * **Hecho cuando:** Se añade `.text-field-error` a `styles.css` y los controladores de clientes y colaboradores validan los campos antes de enviarlos al modelo, marcando visualmente los errores.

- [x] **T4. Refactorización e integración en el formulario de Vehículos (`VehicleFormDialogController`)**
  * **RFs asociados:** RF-04, RF-05, RF-06
  * **Archivos:** `VehicleFormDialogController.java`
  * **Hecho cuando:** Se reemplazan los bloques try-catch `NumberFormatException` por llamadas seguras a `InputValidator`, manteniendo la compatibilidad total con los 8 tipos de vehículos.

- [x] **T5. Integración en Transacciones, Reportes y verificación global**
  * **RFs asociados:** RF-01 al RF-06, Escenarios 1 al 4
  * **Archivos:** `TransactionController.java`, `ReportController.java`
  * **Hecho cuando:** Se integran las validaciones seguras de montos y fechas en operaciones comerciales y reportes, y `mvn clean compile test` pasa al 100% con todos los tests verdes.
