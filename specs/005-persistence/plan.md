# Plan Técnico: Persistencia Local del Estado del Concesionario

* **Feature:** `005-persistence`
* **Estado:** Listo para implementación
* **Tecnologías:** Java 21 LTS, Java I/O (`ObjectOutputStream`, `ObjectInputStream`), JUnit 5 (`@TempDir`)

---

## 1. Matriz de Trazabilidad (Requisitos Funcionales vs Componentes)

| Requisito Funcional | Componente / Clase Responsable | Método / Contrato Clave | Cobertura de Prueba |
| :--- | :--- | :--- | :--- |
| **RF-01**, **RF-05**, **RF-06** (Serialización y escritura) | `DealershipPersistenceManager` | `save(Dealership dealership, Path filePath)` | `testSaveDealershipSuccessfully`, `DealershipSaveTest` |
| **RF-02**, **RF-03**, **RF-07** (Deserialización y restauración) | `DealershipPersistenceManager` | `load(Path filePath)` | `testLoadDealershipRestoresCompleteState`, `DealershipLoadTest` |
| **RF-04** (Primer arranque en frío) | `DealershipPersistenceManager` | `loadOrCreateDefault(Path filePath)` | `testLoadOrCreateDefaultWhenFileDoesNotExist`, `DealershipColdStartTest` |
| **RF-08** (Protección ante archivo corrupto) | `DealershipPersistenceManager` | `load(Path filePath)` -> `PersistenceException` | `testRejectCorruptedFile`, `DealershipLoadTest` |
| **RF-09** (Ruta inaccesible o protegida) | `DealershipPersistenceManager` | `save(...)` -> `PersistenceException` | `testRejectInaccessiblePath`, `DealershipSaveTest` |
| **RF-10** (Rechazo de instancia nula) | `DealershipPersistenceManager` | `save(null, ...)` -> `PersistenceException` | `testRejectNullDealership`, `PersistenceExceptionTest` |

---

## 2. Mapa de Archivos y Responsabilidades

```text
src/main/java/com/omnidrive/
├── exception/
│   └── PersistenceException.java                     (RF-08, RF-09, RF-10: Excepción para fallos de lectura/escritura)
└── util/
    └── DealershipPersistenceManager.java             (RF-01 al RF-07: Motor de serialización, guardado y restauración)
src/test/java/com/omnidrive/
└── util/
    ├── PersistenceExceptionTest.java                 (T1: Pruebas unitarias de la excepción y validación de entradas)
    ├── DealershipSaveTest.java                       (T2: Pruebas de guardado, flujos y creación de directorios)
    ├── DealershipLoadTest.java                       (T3: Pruebas de carga y rechazo de archivos corruptos)
    ├── DealershipColdStartTest.java                  (T4: Pruebas de arranque en frío y Administrador raíz)
    ├── DealershipFullPersistenceIntegrationTest.java (T5: Integración del ciclo completo sobre el modelo completo)
    └── PersistenceAcceptanceScenariosTest.java       (T6: Validación integral de los 5 escenarios oficiales)
```

---

## 3. Desglose Granular de Tareas TDD

1. **T1:** `PersistenceException` y validación defensiva de argumentos nulos o rutas en blanco.
2. **T2:** Método `save` con serialización atómica en disco y creación automática de carpetas contenedor.
3. **T3:** Método `load` con deserialización y control de archivos dañados/ilegibles.
4. **T4:** Método `loadOrCreateDefault` con inicialización garantizada de cuenta raíz `admin`.
5. **T5:** Test de integración integral persistiendo y restaurando el estado completo del concesionario (8 vehículos, todos los actores, transacciones y tokens).
6. **T6:** Ejecución de los 5 escenarios oficiales de aceptación.
