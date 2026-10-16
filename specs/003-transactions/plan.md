# Plan Técnico: Transacciones Comerciales y Auditoría

* **Feature:** `003-transactions`
* **Estado:** Listo para implementación
* **Tecnologías:** Java 21 LTS, JavaFX 21, Maven 3.10+, JUnit 5

---

## 1. Matriz de Trazabilidad (Requisitos Funcionales vs Componentes)

| Requisito Funcional | Componente / Clase Responsable | Método / Contrato Clave | Cobertura de Prueba |
| :--- | :--- | :--- | :--- |
| **RF-01**, **RF-19** (ID único y autogeneración) | `Transaction`, `Dealership` | `getId()`, `generateTransactionId()` | `testTransactionIdUniquenessAndAutogeneration` |
| **RF-02** (Auditoría obligatoria completa) | `Transaction` (abstract) | Constructor base (`employee`, `customer`, `vehicle`, `timestamp`, `amount`) | `testTransactionAuditFieldsIntegrity` |
| **RF-03** (3 tipos de transacción) | `TransactionType`, Subclases | `getType()` (`PURCHASE`, `SALE`, `RENTAL`) | `testTransactionTypesHierarchy` |
| **RF-04** (Monto positivo) | `Transaction` | Validación `totalAmount > 0` | `testRejectZeroOrNegativeAmount` |
| **RF-05**, **RF-12** (Período y tarifa de alquiler) | `RentalTransaction`, `Dealership` | `rentVehicle(...)`, cálculo `días * tarifaBase` | `testRentalDurationAndPriceCalculation` |
| **RF-06**, **RF-15** (Empleado bloqueado prohibido) | `Dealership`, `Transaction` | Validación `!employee.isBlocked()` | `testRejectTransactionIfEmployeeBlocked` |
| **RF-07**, **RF-16** (Venta solo disponible venta) | `Dealership`, `SaleTransaction` | Validación `vehicle.getStatus() == AVAILABLE_FOR_SALE` | `testRejectSaleIfVehicleNotAvailableForSale` |
| **RF-08**, **RF-17** (Alquiler solo disponible alquiler) | `Dealership`, `RentalTransaction` | Validación `vehicle.getStatus() == AVAILABLE_FOR_RENT` | `testRejectRentalIfVehicleNotAvailableForRent` |
| **RF-09**, **RF-14** (Revisión técnica en compra) | `PurchaseTransaction`, `Dealership` | Validación `vehicle.isTechnicalReviewApproved()` | `testRejectPurchaseOfUsedVehicleWithoutTechnicalReview` |
| **RF-10** (Ingreso al inventario tras compra) | `Dealership` | `buyVehicle(...)`, asignación de estado inicial | `testBuyVehicleEntersInventoryWithSelectedStatus` |
| **RF-11** (Transición a Vendido) | `Dealership` | `sellVehicle(...)` -> `vehicle.setStatus(SOLD)` | `testSellVehicleTransitionsToSold` |
| **RF-13**, **RF-20** (Devolución de alquiler) | `Dealership` | `returnRentedVehicle(plate)` -> `AVAILABLE_FOR_RENT` | `testReturnRentedVehicleSuccessfully`, `testRejectReturnIfVehicleNotRented` |
| **RF-18** (Validación de inconsistencias) | `Transaction` y subclases | Validación de fechas y referencias no nulas | `testRejectInconsistentTransactionData` |

---

## 2. Mapa de Archivos y Responsabilidades

```text
src/main/java/com/omnidrive/
├── exception/
│   ├── VehicleUnavailableException.java     (RF-07, RF-08, RF-16, RF-17, RF-20: Estado de vehículo incompatible)
│   ├── InvalidTransactionException.java     (RF-04, RF-18: Datos inválidos o montos no positivos)
│   └── DuplicateTransactionException.java   (RF-19: ID de transacción en conflicto)
├── model/
│   ├── enums/
│   │   └── TransactionType.java             (RF-03: PURCHASE, SALE, RENTAL)
│   ├── Transaction.java                     (RF-01, RF-02, RF-04: Clase abstracta base Serializable)
│   ├── PurchaseTransaction.java             (RF-09, RF-10: Transacción de compra a cliente)
│   ├── SaleTransaction.java                 (RF-11: Transacción de venta a cliente)
│   ├── RentalTransaction.java               (RF-05, RF-12: Transacción de alquiler con fechas y cálculo)
│   └── Dealership.java                      (Existente: Métodos transaccionales y auditoría en memoria)
```

---

## 3. Lógica de Dominio y Transiciones de Estado

### 3.1. Máquina de Estados del Vehículo en Transacciones
```text
[Cliente Externo] --(Compra / buyVehicle)--> [DISPONIBLE_VENTA o DISPONIBLE_ALQUILER]
[DISPONIBLE_VENTA] --(Venta / sellVehicle)--> [VENDIDO]
[DISPONIBLE_ALQUILER] --(Alquiler / rentVehicle)--> [ALQUILADO]
[ALQUILADO] --(Devolución / returnRentedVehicle)--> [DISPONIBLE_ALQUILER]
```

### 3.2. Contratos Clave en `Dealership.java`
- `buyVehicle(employee, customer, vehicle, amount, initialStatus, timestamp)`:
  - Valida que el empleado no esté bloqueado (`BlockedUserException`).
  - Valida revisión técnica si es usado (`TechnicalReviewRejectedException`).
  - Registra el vehículo en `vehicles` con `initialStatus`.
  - Registra `PurchaseTransaction`.
- `sellVehicle(employee, customer, plate, amount, timestamp)`:
  - Valida empleado activo.
  - Busca vehículo por placa; verifica `status == AVAILABLE_FOR_SALE`. Si no, arroja `VehicleUnavailableException`.
  - Cambia `vehicle.setStatus(SOLD)`.
  - Registra `SaleTransaction`.
- `rentVehicle(employee, customer, plate, startDate, endDate, rentalDays, timestamp)`:
  - Valida empleado activo.
  - Busca vehículo; verifica `status == AVAILABLE_FOR_RENT`. Si no, arroja `VehicleUnavailableException`.
  - Calcula `totalAmount = rentalDays * vehicle.getBasePrice()`.
  - Cambia `vehicle.setStatus(RENTED)`.
  - Registra `RentalTransaction`.
- `returnRentedVehicle(plate)`:
  - Busca vehículo; verifica `status == RENTED`. Si no, arroja `VehicleUnavailableException`.
  - Cambia `vehicle.setStatus(AVAILABLE_FOR_RENT)`.

---

## 4. Decisiones Técnicas y Trade-offs

* **Herencia `Transaction` $\rightarrow$ `PurchaseTransaction`, `SaleTransaction`, `RentalTransaction`:**
  * *Decisión:* Modelar subtipos específicos para encapsular los atributos propios (ej. fechas de alquiler, estado destino en compras).
  * *Alternativa descartada:* Una sola clase monstruo `Transaction` con campos nulos para alquileres o compras.
* **Autogeneración determinista de IDs de transacción (`TXN-%05d`):**
  * *Decisión:* `Dealership` administra una secuencia atómica que genera IDs únicos predecibles para simplificar pruebas y auditoría.
  * *Alternativa descartada:* UUIDs aleatorios no amigables para el usuario final ni para reportes impresos.
* **Inyección explícita del instante temporal (`Instant timestamp`):**
  * *Decisión:* Las transacciones aceptan el `timestamp` como argumento, permitiendo recrear fechas históricas exactas para los reportes de la siguiente especificación (`004-reports`).

---

## 5. Estrategia de Tests con JUnit 5 (`mvn test`)

* `TransactionHierarchyTest`: Pruebas de instanciación de `PurchaseTransaction`, `SaleTransaction` y `RentalTransaction`, validación de montos positivos y fechas.
* `DealershipTransactionsTest`: Pruebas completas de flujos de compra, venta, alquiler y devolución con transiciones de estado en `Dealership`.
* `TransactionAuditTest`: Pruebas defensivas que confirman el rechazo ante empleados bloqueados o estados no permitidos de vehículos.
* `TransactionAcceptanceScenariosTest`: Ejecución de los 7 escenarios de aceptación de `spec.md`.
