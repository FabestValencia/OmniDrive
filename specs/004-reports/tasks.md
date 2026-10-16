# Tareas de Implementación: Reportes Gerenciales y Métricas por Empleado

* **Feature:** `004-reports`
* **Metodología:** TDD estricto (Tests en rojo -> Código en verde -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas

- [x] **T1. Excepción `InvalidReportDateRangeException` y modelo inmutable `EmployeeReport`**
  * **RFs asociados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-12, RF-13
  * **Archivos:** `InvalidReportDateRangeException.java`, `EmployeeReport.java`, `EmployeeReportTest.java`
  * **Hecho cuando:** `EmployeeReport` es `Serializable`, inmutable, calcula conteos, subtotales por tipo, total global y comisión del 2% sobre ventas. Pruebas unitarias pasan al 100%.

- [x] **T2. Motor de generación de reportes y filtrado temporal en `Dealership`**
  * **RFs asociados:** RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13
  * **Archivos:** `Dealership.java`, `DealershipReportTest.java`
  * **Hecho cuando:** `Dealership` ofrece `generateEmployeeReport` con validación de empleado (existente y no nulo), intervalo temporal cerrado `[start, end]`, soporte de empleados bloqueados y sobrecarga para `LocalDate`. Pruebas pasan al 100%.

- [x] **T3. Verificación integral de todos los escenarios de aceptación oficiales**
  * **RFs asociados:** RF-01 al RF-13 (Escenarios 1 al 5)
  * **Archivos:** `ReportAcceptanceScenariosTest.java`
  * **Hecho cuando:** Los 5 escenarios oficiales de la especificación están cubiertos con pruebas unitarias exhaustivas y `mvn clean compile test` ejecuta con 100% de éxito y cero fallos.
