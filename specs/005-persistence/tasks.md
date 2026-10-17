# Tareas de Implementación: Persistencia Local del Estado del Concesionario

* **Feature:** `005-persistence`
* **Metodología:** TDD estricto (Tests en rojo -> Código en verde -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas

- [x] **T1. Excepción `PersistenceException` y validaciones defensivas de entrada**
  * **RFs asociados:** RF-09, RF-10
  * **Archivos:** `PersistenceException.java`, `PersistenceExceptionTest.java`
  * **Hecho cuando:** `PersistenceException` hereda de `OmniDriveException`, es `Serializable` con `serialVersionUID`, y se validan defensivamente rutas y referencias nulas. Pruebas unitarias pasan al 100%.

- [x] **T2. Operación de serialización y escritura en disco (`save`)**
  * **RFs asociados:** RF-01, RF-02, RF-05, RF-06
  * **Archivos:** `DealershipPersistenceManager.java`, `DealershipSaveTest.java`
  * **Hecho cuando:** `save` escribe el estado completo de `Dealership` en archivo binario usando `try-with-resources`, crea automáticamente directorios padre inexistentes y maneja rutas protegidas contra escritura. Pruebas unitarias pasan al 100%.

- [x] **T3. Operación de deserialización y restauración (`load`) con protección ante archivos corruptos**
  * **RFs asociados:** RF-02, RF-03, RF-07, RF-08
  * **Archivos:** `DealershipPersistenceManager.java`, `DealershipLoadTest.java`
  * **Hecho cuando:** `load` lee y reconstruye fielmente una instancia de `Dealership` desde disco, y detecta/rechaza archivos corruptos, truncados o con bytes no serializables arrojando `PersistenceException`. Pruebas pasan al 100%.

- [x] **T4. Mecanismo de arranque en frío (`loadOrCreateDefault`)**
  * **RFs asociados:** RF-04
  * **Archivos:** `DealershipPersistenceManager.java`, `DealershipColdStartTest.java`
  * **Hecho cuando:** `loadOrCreateDefault` detecta la ausencia del archivo de datos e inicializa de forma transparente un nuevo `Dealership` garantizando la cuenta del Administrador raíz (`ADMIN-001`, `admin`). Pruebas pasan al 100%.

- [x] **T5. Integración del ciclo completo de persistencia sobre el modelo integral**
  * **RFs asociados:** RF-02, RF-03, RF-06, RF-07
  * **Archivos:** `DealershipFullPersistenceIntegrationTest.java`
  * **Hecho cuando:** Se verifica que tras persistir y restaurar un concesionario con los 8 tipos de vehículos, empleados activos y bloqueados, clientes, transacciones de compra/venta/alquiler y tokens de recuperación, todos los datos, estados y referencias cruzadas se conservan al 100% idénticos. Pruebas pasan al 100%.

- [x] **T6. Verificación integral de todos los escenarios de aceptación oficiales**
  * **RFs asociados:** RF-01 al RF-10 (Escenarios 1 al 5)
  * **Archivos:** `PersistenceAcceptanceScenariosTest.java`
  * **Hecho cuando:** Los 5 escenarios de aceptación oficiales de la especificación están cubiertos con pruebas exhaustivas y `mvn clean compile test` ejecuta con 100% de éxito y cero fallos.
