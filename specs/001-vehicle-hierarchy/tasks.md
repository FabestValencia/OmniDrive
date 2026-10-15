# Tareas de Implementación: Catálogo y Jerarquía de Vehículos

* **Feature:** `001-vehicle-hierarchy`
* **Metodología:** TDD estricto (Tests en rojo -> Código en verde -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas

- [x] **T1. Enums de dominio y excepciones especializadas**
  * **RFs asociados:** RF-02, RF-04, RF-13, RF-16, RF-18, RF-19, RF-20
  * **Archivos:** `com.omnidrive.model.enums.*`, `com.omnidrive.exception.*`
  * **Hecho cuando:** Existen los 5 enums (`FuelType`, `TransmissionType`, `VehicleCondition`, `VehicleStatus`, `TruckType`) y las 3 excepciones que heredan de `OmniDriveException` (`DuplicateVehicleException`, `TechnicalReviewRejectedException`, `InvalidVehicleDataException`), todas serializables con `serialVersionUID = 1L`.

- [x] **T2. Detalles de combustible y clase abstracta base `Vehicle`**
  * **RFs asociados:** RF-01, RF-02, RF-04, RF-05, RF-06, RF-14, RF-15, RF-18, RF-20
  * **Archivos:** `ElectricDetails.java`, `HybridDetails.java`, `Vehicle.java`, `VehicleTest.java`
  * **Hecho cuando:** `Vehicle` valida placa obligatoria no vacía, valores numéricos positivos, foto con fallback por defecto, exigencia de `ElectricDetails`/`HybridDetails` según el combustible, y lanza `TechnicalReviewRejectedException` si es usado y no aprobó revisión técnica. Los tests unitarios pasan al 100%.

- [x] **T3. Jerarquía intermedia `PassengerVehicle` y tipos ligeros (`Motorcycle`, `SportsCar`)**
  * **RFs asociados:** RF-03, RF-07, RF-08
  * **Archivos:** `PassengerVehicle.java`, `Motorcycle.java`, `SportsCar.java`, `LightVehiclesTest.java`
  * **Hecho cuando:** `PassengerVehicle` agrupa los atributos comunes de pasajeros y seguridad; `Motorcycle` y `SportsCar` implementan sus atributos específicos con tests unitarios validando su instanciación y comportamiento.

- [x] **T4. Tipos utilitarios y familiares (`Sedan`, `Suv`, `PickUp`)**
  * **RFs asociados:** RF-07, RF-09, RF-10
  * **Archivos:** `Sedan.java`, `Suv.java`, `PickUp.java`, `FamilyVehiclesTest.java`
  * **Hecho cuando:** Se implementan `Sedan` (con maletero, crucero, asistentes y sensores), `Suv` (con 4x4) y `PickUp` (con caja de carga y 4x4); validados exhaustivamente con tests unitarios.

- [x] **T5. Tipos de pasajeros y carga pesada (`Van`, `Bus`, `Truck`)**
  * **RFs asociados:** RF-11, RF-12, RF-13
  * **Archivos:** `Van.java`, `Bus.java`, `Truck.java`, `HeavyVehiclesTest.java`
  * **Hecho cuando:** Se implementan `Van`, `Bus` (con ejes y salidas de emergencia) y `Truck` (con capacidad de carga, frenos de aire y tipo de camión); validados con tests unitarios.

- [x] **T6. Criterios de filtrado y contenedor central del modelo (`Dealership`)**
  * **RFs asociados:** RF-01, RF-16, RF-17, RF-19
  * **Archivos:** `VehicleFilter.java`, `Dealership.java`, `DealershipTest.java`
  * **Hecho cuando:** `Dealership` registra vehículos validando placa única (lanzando `DuplicateVehicleException`), y permite filtrar vehículos por tipo, combustible, estado, rango de precios y disponibilidad.

- [x] **T7. Verificación integral de todos los escenarios de aceptación**
  * **RFs asociados:** RF-01 al RF-20
  * **Archivos:** `src/test/java/com/omnidrive/...`
  * **Hecho cuando:** Todos los escenarios de aceptación de la spec (1 al 5) están cubiertos con pruebas unitarias y `mvn clean compile test` ejecuta con 100% de éxito y cero fallos.
