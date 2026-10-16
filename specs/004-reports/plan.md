# Plan Técnico: Reportes Gerenciales y Métricas por Empleado

* **Feature:** `004-reports`
* **Estado:** Listo para revisión
* **Tecnologías:** Java 21 LTS, JavaFX 21, Maven 3.10+, JUnit 5

---

## 1. Matriz de Trazabilidad (Requisitos Funcionales vs Componentes)

| Requisito Funcional | Componente / Clase Responsable | Método / Contrato Clave | Cobertura de Prueba |
| :--- | :--- | :--- | :--- |
| **RF-01**, **RF-06** (Estructura de reporte y auditoría) | `EmployeeReport` | Constructor con `employee`, `startInstant`, `endInstant`, `generatedAt`, `transactions` | `testReportStructureAndMetadataIntegrity` |
| **RF-02** (Conteo desglosado de transacciones) | `EmployeeReport`, `Dealership` | `getPurchaseCount()`, `getSaleCount()`, `getRentalCount()`, `getTotalTransactionsCount()` | `testTransactionCountersBreakdown` |
| **RF-03**, **RF-04** (Subtotales y monto global) | `EmployeeReport`, `Dealership` | `getTotalPurchaseAmount()`, `getTotalSaleAmount()`, `getTotalRentalAmount()`, `getGrandTotalAmount()` | `testFinancialTotalsCalculation` |
| **RF-05** (Comisión por ventas del 2%) | `EmployeeReport` | `getSalesCommission()` ($2\% \times \text{totalSaleAmount}$) | `testSalesCommissionCalculation` |
| **RF-07** (Período sin operaciones en cero) | `Dealership`, `EmployeeReport` | `generateEmployeeReport(...)` sin transacciones | `testEmptyPeriodReportGeneratesZeros` |
| **RF-08** (Auditoría sobre empleado bloqueado) | `Dealership`, `EmployeeReport` | `generateEmployeeReport(...)` sobre empleado bloqueado | `testHistoricalReportOnBlockedEmployee` |
| **RF-09**, **RF-10** (Filtro por intervalo cerrado) | `Dealership` | `generateEmployeeReport(employeeId, start, end)` | `testClosedIntervalFiltering` |
| **RF-11** (Empleado inexistente o nulo) | `Dealership` | Validación `findUserById(...)` -> `InvalidUserDataException` | `testRejectNonExistentOrNullEmployee` |
| **RF-12**, **RF-13** (Fechas invertidas o nulas) | `Dealership`, `EmployeeReport` | Validación `start != null && end != null && !start.isAfter(end)` | `testRejectInvalidOrInvertedDateRange` |

---

## 2. Mapa de Archivos y Responsabilidades

```text
src/main/java/com/omnidrive/
├── exception/
│   └── InvalidReportDateRangeException.java (RF-12, RF-13: Rango de fechas nulo o invertido)
├── model/
│   ├── EmployeeReport.java                  (RF-01 al RF-06: Objeto de valor inmutable y Serializable)
│   └── Dealership.java                      (RF-07 al RF-11: Motor de consulta, filtrado temporal y agregación)
src/test/java/com/omnidrive/
└── model/
    ├── EmployeeReportTest.java              (T1: Pruebas unitarias de cálculo, inmutabilidad y serialización)
    ├── DealershipReportTest.java            (T2: Pruebas unitarias del motor de reportes en Dealership)
    └── ReportAcceptanceScenariosTest.java   (T3: Validación integral de los 5 escenarios oficiales)
```

---

## 3. Diseño de Dominio

### 3.1. Objeto de Valor: `EmployeeReport`
* Implementa `java.io.Serializable` con `@Serial private static final long serialVersionUID = 1L;`.
* Inmutabilidad absoluta: todos los atributos son `final` y la lista de transacciones se expone vía `Collections.unmodifiableList`.
* Constante de negocio: `public static final double SALES_COMMISSION_RATE = 0.02;` (2%).
* Métodos de cálculo internos puros que garantizan consistencia en los totales monetarios y conteos.

### 3.2. Contrato en `Dealership.java`
* `public EmployeeReport generateEmployeeReport(String employeeId, Instant startInstant, Instant endInstant)`
  - Valida que `employeeId` no sea nulo ni vacío.
  - Busca al empleado; si no existe o no es un `Employee`, arroja `InvalidUserDataException` ("Empleado no encontrado: ...").
  - Valida que `startInstant != null` y `endInstant != null`.
  - Valida que `!startInstant.isAfter(endInstant)`; de lo contrario arroja `InvalidReportDateRangeException`.
  - Filtra las transacciones asociadas al empleado cuyo `timestamp` satisfaga:  
    `!txn.getTimestamp().isBefore(startInstant) && !txn.getTimestamp().isAfter(endInstant)`
  - Construye y retorna `EmployeeReport`.
* Sobrecarga de conveniencia para JavaFX:
  `public EmployeeReport generateEmployeeReport(String employeeId, LocalDate startDate, LocalDate endDate)`
  - Convierte `startDate` al inicio del día (`00:00:00 UTC`) y `endDate` al final del día (`23:59:59.999999999 UTC`).

---

## 4. Estrategia de Pruebas con JUnit 5 (`mvn test`)

1. `EmployeeReportTest`:
   - Validar que un reporte con transacciones calcula exactamente los conteos, subtotales, monto global y la comisión del 2%.
   - Validar reporte vacío (contadores en 0, montos en 0.0, comisiones en 0.0).
   - Validar inmutabilidad de la lista retornada y serialización de objetos.
2. `DealershipReportTest`:
   - Consulta sobre empleado activo y rango con múltiples transacciones.
   - Consulta sobre rango sin transacciones.
   - Consulta sobre empleado bloqueado.
   - Rechazo de empleado nulo, en blanco o inexistente.
   - Rechazo de rango con `start > end` y fechas nulas.
3. `ReportAcceptanceScenariosTest`:
   - Verificación estricta de los 5 escenarios oficiales de `spec.md`.
