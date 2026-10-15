# Especificación Funcional: Gestión de Actores y Autenticación

* **Identificador:** 002-actors-and-auth
* **Fecha:** 2026-10-15
* **Estado:** aprobada

---

## 1. Contexto y Propósito

El concesionario **OmniDrive** requiere administrar de forma confiable a las personas que interactúan con la plataforma: los **Clientes** que adquieren, venden o alquilan vehículos; los **Empleados** encargados de la atención operativa; y el **Administrador** que supervisa la fuerza laboral y las políticas de acceso. 

Esta especificación establece las reglas de identidad, autenticación, control de estados de acceso (activo/bloqueado irreversible), recuperación de contraseñas por correo electrónico y gestión de datos personales, garantizando que solo el personal autorizado y habilitado pueda operar en el sistema.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* Identificación única y obligatoria de toda persona mediante su número de cédula o documento de identidad.
* Unicidad obligatoria del nombre de usuario (`username`) para todos los usuarios con credenciales de acceso.
* Registro de datos personales obligatorios para todos los actores: cédula, nombres, apellidos, teléfono y correo electrónico.
* Credenciales de acceso (nombre de usuario único y contraseña protegida) exclusivas para Administradores y Empleados.
* Estados de acceso del empleado: **Activo** y **Bloqueado**.
* Restricción total de inicio de sesión y operaciones para empleados que se encuentren en estado bloqueado.
* Capacidad del Administrador para:
  * Registrar nuevos empleados con sus datos completos y credenciales iniciales.
  * Actualizar la información personal de los empleados existentes.
  * Bloquear empleados de manera permanente e irreversible ante faltas o desvinculación laboral.
* Capacidad para registrar y consultar Clientes para futuras operaciones comerciales.
* Mecanismo de recuperación de contraseña mediante envío de código de verificación al correo electrónico registrado del usuario, con expiración de 15 minutos y uso único.
* Inicialización garantizada de una cuenta de Administrador raíz para el primer arranque del sistema.
* Validación estricta de formatos: correo electrónico válido, números de identificación no vacíos y contraseñas no en blanco.

### 2.2. Fuera de Alcance (Out of Scope)
* Registro o procesamiento de transacciones financieras de compra, venta o alquiler de vehículos (será abordado en la especificación `003-transactions`).
* Cálculo y visualización de reportes gerenciales de ventas por empleado en rangos de fechas (será abordado en la especificación `004-reports`).
* Interfaces gráficas en JavaFX o ventanas de login.
* Integración con servidores SMTP reales externos (se modelará el servicio de mensajería desacoplado y verificable).

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El sistema debe identificar a toda persona registrada (Administrador, Empleado, Cliente) mediante un número de cédula o documento de identidad único y no vacío.
* **RF-02:** El sistema debe registrar en toda persona sus datos de contacto obligatorios: nombres, apellidos, número de teléfono y correo electrónico con formato válido.
* **RF-03:** El sistema debe clasificar a cada persona en exactamente uno de los siguientes roles de actor: Administrador, Empleado o Cliente.
* **RF-04:** El sistema debe asociar un nombre de usuario único y una contraseña protegida a cada Administrador y Empleado para su autenticación en la plataforma.
* **RF-05:** El sistema debe iniciar toda cuenta de empleado en estado "Activo" tras su creación por parte del Administrador.

### 3.2. Requisitos Condicionales
* **RF-06:** Mientras un empleado permanezca en estado "Bloqueado", el sistema debe rechazar cualquier intento de inicio de sesión y prohibir la ejecución de cualquier operación operativa en el concesionario.
* **RF-07:** Mientras una persona sea un Cliente, el sistema no debe exigirle nombre de usuario ni contraseña.

### 3.3. Requisitos Basados en Eventos
* **RF-08:** Cuando un Administrador o Empleado ingrese su nombre de usuario y contraseña correctos, el sistema debe autenticar satisfactoriamente al usuario y retornar su perfil con sus permisos correspondientes.
* **RF-09:** Cuando el Administrador registre un nuevo empleado, el sistema debe validar la unicidad de su cédula y de su nombre de usuario, almacenando sus datos y credenciales en el registro de empleados.
* **RF-10:** Cuando el Administrador actualice los datos personales de un empleado existente, el sistema debe aplicar los cambios conservando la cédula y el historial del empleado.
* **RF-11:** Cuando el Administrador solicite bloquear la cuenta de un empleado, el sistema debe cambiar su estado a "Bloqueado" de forma inmediata y permanente, sin permitir reversión ni desbloqueo.
* **RF-12:** Cuando un empleado o usuario autorizado registre a un nuevo Cliente, el sistema debe validar que la cédula no exista previamente y guardar su información en el registro de clientes.
* **RF-13:** Cuando un usuario solicite la recuperación de su contraseña indicando su nombre de usuario o correo electrónico, el sistema debe generar un código de verificación con validez temporal de 15 minutos y enviarlo a su dirección de correo electrónico registrada.
* **RF-14:** Cuando el usuario ingrese el código de recuperación válido y no expirado junto con una nueva contraseña, el sistema debe actualizar la contraseña del usuario y anular inmediatamente el código utilizado.
* **RF-20:** Cuando el sistema se inicializa por primera vez, debe garantizar la existencia de una cuenta de Administrador raíz activa con credenciales iniciales para permitir la administración del concesionario.

### 3.4. Requisitos de Respuesta a Errores
* **RF-15:** Si se ingresan credenciales de acceso inválidas (usuario no existente o contraseña errónea), el sistema debe denegar el acceso informando que las credenciales son incorrectas sin revelar detalles de existencia del usuario.
* **RF-16:** Si un empleado en estado bloqueado intenta autenticarse, el sistema debe rechazar el acceso indicando explícitamente que la cuenta se encuentra bloqueada.
* **RF-17:** Si se intenta registrar un empleado o cliente con una cédula o nombre de usuario que ya existe en el sistema, la operación debe ser rechazada evitando registros duplicados.
* **RF-18:** Si se ingresan datos con formato incorrecto (correo electrónico sin estructura válida, campos de texto vacíos o nulos, o contraseñas en blanco), el sistema debe rechazar la operación indicando los campos con error.
* **RF-19:** Si se ingresa un código de recuperación de contraseña expirado (más de 15 minutos), inexistente o erróneo, el sistema debe rechazar la actualización de la contraseña.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Autenticación exitosa de Empleado activo
* **Dado** que existe un empleado activo con usuario "carlos.vendedor" y contraseña válida,
* **Cuando** el empleado suministra su usuario y contraseña correctos,
* **Entonces** el sistema valida las credenciales y permite el acceso otorgando permisos de empleado.

### Escenario 2: Rechazo de autenticación para Empleado bloqueado
* **Dado** que el Administrador ha marcado al empleado "laura.ventas" en estado "Bloqueado",
* **Cuando** dicho empleado intenta iniciar sesión con su contraseña correcta,
* **Entonces** el sistema bloquea el ingreso e informa que la cuenta está deshabilitada.

### Escenario 3: Rechazo de registro por cédula o usuario duplicado
* **Dado** que ya existe un empleado registrado con cédula "1094888999" y usuario "emp.juan",
* **Cuando** se intenta registrar otro usuario con la misma cédula "1094888999" o el mismo usuario "emp.juan",
* **Entonces** el sistema rechaza la operación e indica el conflicto por duplicidad.

### Escenario 4: Bloqueo permanente de empleado por parte del Administrador
* **Dado** un empleado activo "juan.perez" registrado en el concesionario,
* **Cuando** el Administrador ejecuta la acción de bloqueo sobre su cuenta,
* **Entonces** el estado del empleado cambia a "Bloqueado", no existe mecanismo para reactivarlo y futuras solicitudes de autenticación son rechazadas.

### Escenario 5: Recuperación exitosa de contraseña vía correo electrónico dentro de los 15 minutos
* **Dado** un usuario que olvidó su contraseña con correo registrado "admin@omnidrive.com",
* **Cuando** solicita la recuperación de clave y suministra el código de seguridad recibido en su correo dentro de la ventana de 15 minutos junto con su nueva contraseña,
* **Entonces** el sistema actualiza la contraseña exitosamente permitiendo el inicio de sesión inmediato con la nueva clave.

### Escenario 6: Rechazo de código de recuperación expirado
* **Dado** que se generó un código de recuperación hace más de 15 minutos,
* **Cuando** el usuario intenta restablecer la contraseña con dicho código expirado,
* **Entonces** el sistema rechaza la solicitud e indica que el código ha caducado.
