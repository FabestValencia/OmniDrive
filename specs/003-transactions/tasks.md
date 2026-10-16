# Tareas de Implementación: Transacciones Comerciales y Auditoría

* **Feature:** `003-transactions`
* **Metodología:** TDD estricto (Tests en rojo -> Código en verde -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas

- [x] **T1. Enums y excepciones especializadas del módulo transaccional**
  * **RFs asociados:** RF-03, RF-04, RF-14, RF-16, RF-17, RF-18, RF-19, RF-20
  * **Archivos:** `com.omnidrive.model.enums.TransactionType`, `com.omnidrive.exception.*`
  * **Hecho cuando:** Existen el enum `TransactionType` (`PURCHASE`, `SALE`, `RENTAL`) y las excepciones serializables `VehicleUnavailableException`, `InvalidTransactionException` y `DuplicateTransactionException` heredando de `OmniDriveException`.

- [x] **T2. Clase abstracta base `Transaction` con campos de auditoría obligatorios**
  * **RFs asociados:** RF-01, RF-02, RF-04, RF-18
  * **Archivos:** `Transaction.java`, `TransactionBaseTest.java`
  * **Hecho cuando:** `Transaction` valida ID no vacío, referencias no nulas a empleado, cliente y vehículo, monto positivo mayor a cero y timestamp de auditoría. Pruebas unitarias pasan al 100%.

- [x] **T3. Subclases especializadas: `PurchaseTransaction`, `SaleTransaction` y `RentalTransaction`**
  * **RFs asociados:** RF-03, RF-05, RF-09, RF-12, RF-18
  * **Archivos:** `PurchaseTransaction.java`, `SaleTransaction.java`, `RentalTransaction.java`, `TransactionHierarchyTest.java`
  * **Hecho cuando:** Cada subtipo encapsula sus atributos particulares: compra exige revisión técnica para usados, venta valida monto pactado y alquiler valida fechas consistentes y días positivos. Pruebas unitarias pasan al 100%.

- [x] **T4. Flujos de Compra y Venta en el contenedor central `Dealership`**
  * **RFs asociados:** RF-06, RF-07, RF-09, RF-10, RF-11, RF-14, RF-15, RF-16
  * **Archivos:** `Dealership.java`, `DealershipBuySellTest.java`
  * **Hecho cuando:** `buyVehicle` ingresa el vehículo al inventario con revisión técnica aprobada y estado seleccionado; `sellVehicle` conmuta el estado a `SOLD` y rechaza vehículos no disponibles para venta o empleados bloqueados. Pruebas pasan al 100%.

- [x] **T5. Flujos de Alquiler y Devolución en `Dealership`**
  * **RFs asociados:** RF-05, RF-06, RF-08, RF-12, RF-13, RF-15, RF-17, RF-20
  * **Archivos:** `Dealership.java`, `DealershipRentalTest.java`
  * **Hecho cuando:** `rentVehicle` calcula la tarifa total (días $\times$ tarifa diaria) y cambia a `RENTED`; `returnRentedVehicle` retorna el vehículo a `AVAILABLE_FOR_RENT` y rechaza devoluciones de vehículos no alquilados. Pruebas pasan al 100%.

- [x] **T6. Consultas, autogeneración de IDs y trazabilidad por empleado en `Dealership`**
  * **RFs asociados:** RF-01, RF-02, RF-19
  * **Archivos:** `Dealership.java`, `DealershipTransactionQueryTest.java`
  * **Hecho cuando:** `Dealership` genera identificadores únicos (`TXN-%05d`), permite buscar transacciones por ID y filtrar el historial completo o agrupado por empleado responsable. Pruebas pasan al 100%.

- [x] **T7. Verificación integral de todos los escenarios de aceptación oficiales**
  * **RFs asociados:** RF-01 al RF-20
  * **Archivos:** `TransactionAcceptanceScenariosTest.java`
  * **Hecho cuando:** Los 7 escenarios oficiales de la especificación están cubiertos con pruebas unitarias exhaustivas y `mvn clean compile test` ejecuta con 100% de éxito y cero fallos.
