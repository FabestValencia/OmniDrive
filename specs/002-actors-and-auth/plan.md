# Plan Técnico: Gestión de Actores y Autenticación

* **Feature:** `002-actors-and-auth`
* **Estado:** Listo para implementación
* **Tecnologías:** Java 21 LTS, JavaFX 21, Maven 3.10+, JUnit 5

---

## 1. Matriz de Trazabilidad (Requisitos Funcionales vs Componentes)

| Requisito Funcional | Componente / Clase Responsable | Método / Contrato Clave | Cobertura de Prueba |
| :--- | :--- | :--- | :--- |
| **RF-01** (Cédula única obligatoria) | `Person`, `Dealership` | `getId()`, validación en constructor y registro | `testPersonIdCannotBeEmpty`, `testDuplicateIdRejected` |
| **RF-02** (Datos personales y email) | `Person` | `getFirstName()`, `getLastName()`, `getEmail()` con regex RFC | `testValidEmailFormat`, `testInvalidEmailThrowsException` |
| **RF-03** (3 roles exclusivos) | `Customer`, `Employee`, `Administrator` | Jerarquía POO heredando de `Person` / `User` | Pruebas de instanciación por cada rol |
| **RF-04** (Credenciales únicas) | `User` (abstract) | `getUsername()`, `verifyPassword(String)` | `testUserCredentialsValidation` |
| **RF-05** (Estado inicial activo) | `Employee`, `EmployeeStatus` | `getStatus() == EmployeeStatus.ACTIVE` | `testEmployeeInitialStatusIsActive` |
| **RF-06**, **RF-11**, **RF-16** (Bloqueo permanente) | `Employee`, `Dealership` | `block()`, `isBlocked()`, `authenticate(...)` | `testBlockedEmployeeCannotAuthenticateOrOperate` |
| **RF-07** (Cliente sin credenciales) | `Customer` | Hereda directamente de `Person` | `testCustomerHasNoCredentials` |
| **RF-08**, **RF-15** (Autenticación segura) | `Dealership` | `authenticate(username, password)` | `testSuccessfulAuthentication`, `testInvalidCredentialsRejected` |
| **RF-09**, **RF-17** (Registro de empleado) | `Dealership` | `registerEmployee(Employee)` | `testRegisterEmployeeDuplicateIdOrUsername` |
| **RF-10** (Actualización de empleado) | `Dealership` | `updateEmployee(id, firstName, lastName, phone, email)` | `testUpdateEmployeeData` |
| **RF-12** (Registro de cliente) | `Dealership` | `registerCustomer(Customer)` | `testRegisterCustomerDuplicateId` |
| **RF-13**, **RF-14**, **RF-19** (Recuperación de clave) | `PasswordRecoveryToken`, `Dealership`, `EmailService` | `requestPasswordRecovery(...)`, `resetPassword(...)` | `testPasswordRecoveryFlowWithin15Minutes`, `testExpiredCodeRejected` |
| **RF-18** (Validación de formatos) | `Person`, `User` | Validaciones defensivas en constructores y setters | `testBlankFieldsThrowInvalidUserDataException` |
| **RF-20** (Administrador por defecto) | `Dealership` | Constructor de `Dealership` crea admin raíz si no existe | `testDefaultRootAdministratorExistsOnStartup` |

---

## 2. Mapa de Archivos y Responsabilidades

```text
src/main/java/com/omnidrive/
├── exception/
│   ├── AuthenticationException.java          (RF-08, RF-15: Credenciales inválidas)
│   ├── BlockedUserException.java             (RF-06, RF-16: Empleado bloqueado)
│   ├── DuplicateUserException.java           (RF-17: Cédula o nombre de usuario repetido)
│   ├── InvalidUserDataException.java         (RF-18: Email inválido o campos vacíos)
│   └── InvalidRecoveryCodeException.java     (RF-19: Código expirado o erróneo)
├── model/
│   ├── enums/
│   │   ├── UserRole.java                     (RF-03: ADMINISTRATOR, EMPLOYEE, CUSTOMER)
│   │   └── EmployeeStatus.java               (RF-05, RF-06: ACTIVE, BLOCKED)
│   ├── Person.java                           (RF-01, RF-02, RF-18: Clase base abstracta serializable)
│   ├── User.java                             (RF-04: Clase abstracta con username y contraseña)
│   ├── Customer.java                         (RF-03, RF-07: Cliente para transacciones)
│   ├── Employee.java                         (RF-03, RF-05, RF-06, RF-11: Empleado con estado y bloqueo)
│   ├── Administrator.java                    (RF-03, RF-20: Administrador del sistema)
│   ├── PasswordRecoveryToken.java            (RF-13, RF-14, RF-19: Token con expiración de 15 min)
│   └── Dealership.java                       (Existente: Se extiende con gestión de actores y autenticación)
└── util/
    ├── EmailService.java                     (Interfaz desacoplada de envío de correos)
    └── MockEmailService.java                 (Implementación verificable en memoria para pruebas y auditoría)
```

---

## 3. Lógica de Dominio y Jerarquía POO

### 3.1. Estructura de Clases
1. **`Person` (Abstracta, `Serializable`):**
   - Atributos: `id` (cédula), `firstName`, `lastName`, `phone`, `email`.
   - Validación: regex de correo electrónico `^[A-Za-z0-9+_.-]+@(.+)$`, identificador no vacío.
2. **`User` (Abstracta, extiende `Person`):**
   - Atributos: `username` (normalizado en minúsculas), `password` (con hashing básico SHA-256 para no guardar texto plano, según directriz de AGENTS.md).
3. **`Customer` (Extiende `Person`):**
   - Representa al comprador/arrendatario sin acceso al sistema.
4. **`Employee` (Extiende `User`):**
   - Atributo: `status` (`EmployeeStatus.ACTIVE` o `BLOCKED`).
   - Método irreversible: `block()` fija el estado en `BLOCKED`.
5. **`Administrator` (Extiende `User`):**
   - Rol directivo con permisos para gestionar empleados y generar reportes.
6. **`PasswordRecoveryToken` (`Serializable`):**
   - Atributos: `code`, `username`, `expiresAt` (`Instant`), `isUsed`.
   - Método puro con inyección temporal: `boolean isValid(String code, Instant now)`.

### 3.2. Algoritmo de Autenticación en `Dealership`
```text
Función authenticate(username, password):
    Buscar usuario por username en Administrador o Empleados.
    Si usuario no existe:
        Lanzar AuthenticationException("Credenciales incorrectas.")
    Si usuario es Employee y employee.isBlocked():
        Lanzar BlockedUserException("La cuenta de empleado se encuentra bloqueada.")
    Si no usuario.verifyPassword(password):
        Lanzar AuthenticationException("Credenciales incorrectas.")
    Retornar usuario autenticado.
```

---

## 4. Decisiones Técnicas y Trade-offs

* **Herencia `Person` -> `User` -> `Employee` / `Administrator`:**
  * *Decisión:* Separar a `Person` (datos civiles compartidos con `Customer`) de `User` (identidad digital con credenciales).
  * *Alternativa descartada:* Poner credenciales en `Person` permitiendo contraseñas nulas para `Customer` (rompe cohesión y principio de sustitución de Liskov).
* **Bloqueo irreversible sin método `unblock()`:**
  * *Decisión:* Acatar la respuesta del usuario en la entrevista; un empleado bloqueado no puede reactivarse.
  * *Alternativa descartada:* Agregar flag booleano reversible `setActive(true)`.
* **Inyección de `Instant now` en validación de tokens:**
  * *Decisión:* Métodos de expiración aceptan el tiempo actual `Instant now` como parámetro, permitiendo tests 100% deterministas sin usar `Thread.sleep`.
  * *Alternativa descartada:* Usar `Instant.now()` rígido dentro de la clase impidiendo simular expiración de 15 minutos en tests.
* **Servicio de correo mockeable (`EmailService`):**
  * *Decisión:* Mantener interfaz `EmailService` con implementación `MockEmailService` para pruebas sin depender de servidores SMTP reales ni librerías externas pesadas.
  * *Alternativa descartada:* Incorporar dependencias JavaMail / Jakarta Mail complejas en `pom.xml`.

---

## 5. Estrategia de Tests con JUnit 5 (`mvn test`)

* `PersonValidationTest`: Validación de formatos de email, cédula y nombres obligatorios.
* `AuthenticationTest`: Pruebas de credenciales válidas, credenciales incorrectas y rechazo a empleados bloqueados.
* `EmployeeManagementTest`: Creación, actualización, bloqueo irreversible y rechazo de duplicados de empleados.
* `PasswordRecoveryTest`: Generación de token, envío simulado por correo, cambio exitoso de clave y rechazo de código expirado (+15 min).
* `ActorAcceptanceScenariosTest`: Ejecución de los 6 escenarios oficiales de aceptación de `spec.md`.
