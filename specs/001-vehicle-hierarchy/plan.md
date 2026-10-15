# Plan Técnico: Catálogo y Jerarquía de Vehículos

* **Feature:** `001-vehicle-hierarchy`
* **Estado:** Listo para implementación
* **Tecnologías:** Java 21 LTS, JavaFX 21, Maven 3.10+, JUnit 5

---

## 1. Matriz de Trazabilidad (Requisitos Funcionales vs Componentes)

| Requisito Funcional | Componente / Clase Responsable | Método / Contrato Clave | Cobertura de Prueba |
| :--- | :--- | :--- | :--- |
| **RF-01** (Placa única) | `Vehicle`, `Dealership` | `getPlate()`, `registerVehicle(Vehicle)` | `testRegisterVehicleWithUniquePlate` |
| **RF-02** (Atributos generales) | `Vehicle` (abstract) | Constructor base y getters | `testVehicleBaseAttributesValidation` |
| **RF-03** (8 tipos de vehículo) | Subclases concretas en `model/` | Herencia POO desacoplada | Pruebas específicas por tipo de vehículo |
| **RF-04** (4 combustibles transversales) | `FuelType`, `Vehicle` | `getFuelType()` | `testVehicleWithAllFuelTypes` |
| **RF-05** (Datos de eléctrico) | `ElectricDetails`, `Vehicle` | `getElectricDetails()` | `testElectricVehicleRequiresDetails` |
| **RF-06** (Datos de híbrido) | `HybridDetails`, `Vehicle` | `getHybridDetails()` | `testHybridVehicleRequiresDetails` |
| **RF-07** (Sedán) | `Sedan` | Constructor y getters específicos | `testSedanCreationAndAttributes` |
| **RF-08** (Deportivo) | `SportsCar` | Constructor y getters específicos | `testSportsCarCreationAndAttributes` |
| **RF-09** (Camioneta/SUV) | `Suv` | Constructor y getters específicos | `testSuvCreationAndAttributes` |
| **RF-10** (Pick Up) | `PickUp` | Constructor y getters específicos | `testPickUpCreationAndAttributes` |
| **RF-11** (Van) | `Van` | Constructor y getters específicos | `testVanCreationAndAttributes` |
| **RF-12** (Bus) | `Bus` | Constructor y getters específicos | `testBusCreationAndAttributes` |
| **RF-13** (Camión) | `Truck`, `TruckType` | Constructor y getters específicos | `testTruckCreationAndAttributes` |
| **RF-14**, **RF-18** (Revisión técnica) | `Vehicle`, `Dealership` | Validación `isTechnicalReviewApproved()` | `testUsedVehicleWithoutTechnicalReviewThrowsException` |
| **RF-15** (Foto por defecto) | `Vehicle` | `getPhotoUrl()` con fallback constante | `testDefaultPhotoWhenNoneProvided` |
| **RF-16** (Estado inicial venta/alquiler) | `VehicleStatus`, `Vehicle` | `getStatus()` | `testInitialVehicleStatusSelection` |
| **RF-17** (Filtros de búsqueda) | `Dealership`, `VehicleFilter` | `filterVehicles(VehicleFilter)` | `testFilterVehiclesByTypeFuelPriceStatus` |
| **RF-19** (Rechazo placa duplicada) | `Dealership` | `registerVehicle(Vehicle)` | `testRegisterDuplicatePlateThrowsException` |
| **RF-20** (Validación de inconsistencias) | `Vehicle` (validaciones base) | `validateBaseAttributes(...)` | `testInconsistentValuesThrowException` |

---

## 2. Mapa de Archivos y Responsabilidades

```text
src/main/java/com/omnidrive/
├── exception/
│   ├── OmniDriveException.java               (Existente: Base serializable)
│   ├── DuplicateVehicleException.java        (RF-19: Placa duplicada)
│   ├── TechnicalReviewRejectedException.java (RF-18: Revisión técnica no aprobada)
│   └── InvalidVehicleDataException.java      (RF-20: Datos numéricos/textos inconsistentes)
└── model/
    ├── enums/
    │   ├── FuelType.java                     (RF-04: GASOLINE, DIESEL, ELECTRIC, HYBRID)
    │   ├── TransmissionType.java             (RF-02: MANUAL, AUTOMATIC)
    │   ├── VehicleCondition.java             (RF-02: NEW, USED)
    │   ├── VehicleStatus.java                (RF-16: AVAILABLE_FOR_SALE, AVAILABLE_FOR_RENT, etc.)
    │   └── TruckType.java                    (RF-13: BOX_TRUCK, TANKER, HOPPER, FLATBED)
    ├── ElectricDetails.java                  (RF-05: Autonomía y tiempo de recarga)
    ├── HybridDetails.java                    (RF-06: Enchufable y mild-hybrid)
    ├── Vehicle.java                          (RF-01, 02, 14, 15, 20: Clase abstracta base Serializable)
    ├── PassengerVehicle.java                 (Herencia intermedia para evitar duplicidad de atributos de pasajeros)
    ├── Motorcycle.java                       (RF-03: Moto)
    ├── Sedan.java                            (RF-07: Sedán)
    ├── SportsCar.java                        (RF-08: Deportivo)
    ├── Suv.java                              (RF-09: Camioneta / SUV)
    ├── PickUp.java                           (RF-10: Pick Up)
    ├── Van.java                              (RF-11: Van)
    ├── Bus.java                              (RF-12: Bus)
    ├── Truck.java                            (RF-13: Camión)
    ├── VehicleFilter.java                    (RF-17: Criterios inmutables de filtrado y búsqueda)
    └── Dealership.java                       (RF-01, 16, 17, 19: Contenedor central del modelo MVC)
```

---

## 3. Lógica de Dominio y Jerarquía POO

### 3.1. Principios de Diseño
1. **Evitar duplicación de código:** `PassengerVehicle` agrupa `passengerCount`, `doorsCount`, `airbagsCount`, `hasAirConditioning`, `hasReverseCamera` y `hasAbs`, heredado por `Sedan`, `Suv`, `Van` y `Bus`.
2. **Propulsión transversal limpia:** En lugar de crear subclases híbridas/eléctricas combinatorias (ej. `ElectricSedan`, `HybridBus`), `Vehicle` contiene opcionalmente `ElectricDetails` o `HybridDetails` según su `FuelType`.
3. **Persistencia Serializable:** Todas las entidades y enums implementan `java.io.Serializable` y definen `serialVersionUID = 1L`.
4. **Validaciones defensivas puras:** El constructor de `Vehicle` normaliza la placa (`trim().toUpperCase()`), valida campos numéricos y rechaza de inmediato vehículos usados sin revisión técnica aprobada.

---

## 4. Decisiones Técnicas y Trade-offs

* **Composición de combustible transversal:**
  * *Decisión adoptada:* Modelar combustible con `FuelType` y clases de valor (`ElectricDetails`, `HybridDetails`).
  * *Alternativa descartada:* Herencia múltiple simulada o 32 subclases combinatorias (`ElectricSedan`, `HybridSedan`, etc.), lo cual generaría explosión de clases y complejidad innecesaria.
* **Herencia intermedia `PassengerVehicle`:**
  * *Decisión adoptada:* Centralizar atributos de pasajeros y seguridad compartidos.
  * *Alternativa descartada:* Repetir 6 atributos y getters en `Sedan`, `Suv`, `Van` y `Bus`.
* **Contenedor `Dealership` en `model/`:**
  * *Decisión adoptada:* Centraliza el inventario en un `Map<String, Vehicle>` indexado por placa para búsquedas $O(1)$ y filtros fluídos con la Streams API.
  * *Alternativa descartada:* Dejar la lista suelta en los controladores violando la arquitectura MVC.

---

## 5. Estrategia de Tests con JUnit 5 (`mvn test`)

* `VehicleValidationTest`: Pruebas de placa obligatoria, valores numéricos no negativos y foto por defecto.
* `TechnicalReviewTest`: Verificación estricta del rechazo de compra/registro de usados si `isTechnicalReviewApproved == false`.
* `VehicleHierarchyTest`: Instanciación y verificación exhaustiva de los 8 tipos de vehículos con sus atributos específicos.
* `FuelTypesTest`: Validación de vehículos eléctricos e híbridos con sus respectivos detalles requeridos.
* `DealershipInventoryTest`: Pruebas de registro, rechazo de duplicados y filtros de búsqueda por tipo, combustible, precio y estado.
