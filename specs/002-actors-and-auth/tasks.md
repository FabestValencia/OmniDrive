# Tareas de Implementación: Gestión de Actores y Autenticación

* **Feature:** `002-actors-and-auth`
* **Metodología:** TDD estricto (Tests en rojo -> Código en verde -> Refactor)
* **Runner de pruebas:** `mvn test` (JUnit 5)

---

## Lista Secuencial de Tareas

- [x] **T1. Enums de actores y excepciones de dominio especializadas**
  * **RFs asociados:** RF-03, RF-05, RF-08, RF-15, RF-16, RF-17, RF-18, RF-19
  * **Archivos:** `com.omnidrive.model.enums.UserRole`, `com.omnidrive.model.enums.EmployeeStatus`, `com.omnidrive.exception.*`
  * **Hecho cuando:** Existen los enums y las 5 excepciones serializables (`AuthenticationException`, `BlockedUserException`, `DuplicateUserException`, `InvalidUserDataException`, `InvalidRecoveryCodeException`) heredando de `OmniDriveException`.

- [x] **T2. Clases base `Person` y `Customer` con validación de identidad y contacto**
  * **RFs asociados:** RF-01, RF-02, RF-07, RF-18
  * **Archivos:** `Person.java`, `Customer.java`, `PersonTest.java`
  * **Hecho cuando:** `Person` valida cédula no vacía, nombres, teléfono y correo electrónico con formato RFC. `Customer` hereda de `Person` y no expone credenciales. Pruebas unitarias pasan al 100%.

- [x] **T3. Identidad digital: `User`, `Employee` y `Administrator`**
  * **RFs asociados:** RF-04, RF-05, RF-06, RF-11, RF-18
  * **Archivos:** `User.java`, `Employee.java`, `Administrator.java`, `UserHierarchyTest.java`
  * **Hecho cuando:** `User` encapsula username normalizado y verificación de contraseña; `Employee` inicia en `ACTIVE` y provee método irreversible `block()`; `Administrator` representa el rol directivo. Pruebas unitarias pasan al 100%.

- [x] **T4. Tokens de recuperación y servicio de correo electrónico (`EmailService`)**
  * **RFs asociados:** RF-13, RF-14, RF-19
  * **Archivos:** `PasswordRecoveryToken.java`, `EmailService.java`, `MockEmailService.java`, `PasswordRecoveryTokenTest.java`
  * **Hecho cuando:** `PasswordRecoveryToken` evalúa validez temporal de 15 minutos de forma determinista mediante `Instant now` y `MockEmailService` almacena y audita los envíos de correo en memoria.

- [x] **T5. Gestión de Actores en el Contenedor Central `Dealership`**
  * **RFs asociados:** RF-01, RF-09, RF-10, RF-11, RF-12, RF-17, RF-20
  * **Archivos:** `Dealership.java`, `DealershipActorsTest.java`
  * **Hecho cuando:** `Dealership` incluye cuenta de Administrador raíz por defecto al iniciar, registra clientes y empleados validando unicidad de cédula y usuario, permite actualizar datos y bloquear empleados irreversiblemente.

- [x] **T6. Autenticación y flujo de recuperación de clave en `Dealership`**
  * **RFs asociados:** RF-06, RF-08, RF-13, RF-14, RF-15, RF-16, RF-19
  * **Archivos:** `Dealership.java`, `DealershipAuthTest.java`
  * **Hecho cuando:** `authenticate` valida credenciales y rechaza a empleados bloqueados; `requestPasswordRecovery` genera token y envía correo; y `resetPassword` actualiza la clave anulando el token utilizado.

- [x] **T7. Verificación integral de todos los escenarios de aceptación oficiales**
  * **RFs asociados:** RF-01 al RF-20
  * **Archivos:** `ActorAcceptanceScenariosTest.java`
  * **Hecho cuando:** Los 6 escenarios oficiales de la especificación están cubiertos con pruebas unitarias exhaustivas y `mvn clean compile test` ejecuta con 100% de éxito y cero fallos.
