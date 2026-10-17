# Especificación Funcional: Interfaz Gráfica de Usuario en JavaFX

* **Identificador:** `006-gui-javafx`
* **Estado:** revisada y lista para aprobación
* **Fecha:** 2026-10-16
* **Autores:** Equipo de Arquitectura OmniDrive & @planner

---

## 1. Contexto y Propósito

El sistema de información OmniDrive cuenta con un modelo de dominio integral, probado al 100% y con persistencia local garantizada. Para brindar una experiencia de usuario de nivel corporativo, ágil, intuitiva y visualmente pulida a los actores del concesionario (Administrador y Empleados), se desarrollará la interfaz gráfica de usuario en **JavaFX 21** bajo el patrón **MVC puro**, aplicando el tema corporativo **'Light Clean'** con navegación por **Sidebar lateral** y contenedor dinámico.

---

## 2. Decisiones Clave de Clarificación (/clarify)

Tras la consulta interactiva de 5 preguntas clave, se definen los siguientes lineamientos:
1. **Navegación general:** Ventana principal moderna con **Sidebar lateral interactivo** que adapta sus opciones según el rol del usuario autenticado (`Administrator` vs `Employee`).
2. **Estilo visual:** Tema corporativo **'Light Clean'** en `styles.css` (fondo gris claro `#f8fafc`, tarjetas y paneles blancos con sombras suaves, tipografía nítida y acentos azul zafiro `#1e40af`).
3. **Catálogo de vehículos:** **Tabla interactiva** con columnas ordenables, búsqueda predictiva en tiempo real y **panel lateral sincronizado** que muestra la ficha técnica completa y la fotografía del vehículo seleccionado.
4. **Formulario de vehículos:** **Diálogo modal emergente reactivo** que despliega u oculta dinámicamente los campos pertinentes según el tipo de vehículo seleccionado (8 tipos) y la tecnología de combustible (eléctrico o híbrido).
5. **Fotografías de vehículos:** Selector de archivos con botón **'Examinar'**, vista previa inmediata en miniatura y fallback a una silueta predeterminada elegante si no se suministra imagen.
6. **Transacciones comerciales:** Módulo organizado mediante **pestañas superiores dedicadas** (`[ Compra | Venta | Alquiler | Devolución ]`), garantizando flujos guiados e independientes.
7. **Reportes gerenciales:** Tarjetas ejecutivas con métricas clave, comisión del 2% sobre ventas, desglose por categoría, tabla detallada de operaciones y **botón para exportar el reporte** a archivo de texto local formateado (`.txt`).

---

## 3. Límites de Alcance

### 3.1. En Alcance (In Scope)
* **Acceso y Autenticación (Login & Recuperación):**
  * Validación segura con hash criptográfico SHA-256.
  * Manejo visual de error para cuentas bloqueadas (`BlockedUserException`).
  * Asistente modal para recuperación de contraseñas mediante código de 6 dígitos (validez de 15 minutos).
* **Workspace Principal (Dashboard & Sidebar):**
  * Barra lateral de navegación adaptativa por rol.
  * Encabezado superior con datos del usuario activo, rol y botón de logout seguro.
  * Persistencia automática en `data/dealership.dat` en arranque en frío y guardado ante cada operación de negocio.
* **Módulo de Empleados (Exclusivo Administrador):**
  * Listado en tabla, registro de colaboradores, edición de datos personales y bloqueo permanente con confirmación.
* **Módulo de Reportes Gerenciales (Exclusivo Administrador):**
  * Selector de colaborador y rango de fechas (intervalo cerrado).
  * Tarjetas de balance (total comprado, vendido, alquilado y 2% de comisión sobre ventas).
  * Tabla detallada y exportación a archivo de texto.
* **Módulo de Inventario de Vehículos (Ambos):**
  * Tabla con filtros en tiempo real y panel lateral con ficha técnica completa y fotografía.
  * Formulario modal reactivo para los 8 tipos de vehículos y variantes híbridas/eléctricas.
* **Módulo de Clientes (Ambos):**
  * Tabla de clientes registrados y formulario de alta con validación de correo RFC y cédula.
* **Módulo de Transacciones Comerciales (Empleado):**
  * Pestaña Compra: Exige validación técnica aprobada obligatoria si el vehículo es usado.
  * Pestaña Venta: Validación de estado `AVAILABLE_FOR_SALE` y conmutación a `SOLD`.
  * Pestaña Alquiler y Devolución: Registro de arriendo con fechas/días y devolución a `AVAILABLE_FOR_RENT`.
  * Historial auditable de transacciones registradas.

### 3.2. Fuera de Alcance (Out of Scope)
* Integración con pasarelas de pago bancarias online externas.
* Generación de facturas electrónicas con firma digital tributaria DIAN.
* Impresión en hardware de impresoras de tickets POS.

---

## 4. Requisitos Funcionales (Formato EARS)

### 4.1. Requisitos Ubicuos
* **RF-01:** El sistema debe ejecutar la GUI en JavaFX 21 desacoplada bajo MVC puro, donde los controladores manejan eventos FXML e interactúan directamente con la instancia de `Dealership`.
* **RF-02:** El sistema debe aplicar un estilo corporativo 'Light Clean' mediante `styles.css` asegurando consistencia visual en toda la aplicación.
* **RF-03:** El sistema debe persistir automáticamente el estado en disco (`data/dealership.dat`) tras cada registro de transacción, empleado, cliente o vehículo.

### 4.2. Requisitos Condicionales
* **RF-04:** Mientras no exista una sesión activa, el sistema debe presentar exclusivamente la vista de Login y el asistente de recuperación.
* **RF-05:** Mientras el rol activo sea `Administrator`, el sistema debe habilitar en la barra lateral el acceso a Empleados, Reportes, Vehículos y Clientes.
* **RF-06:** Mientras el rol activo sea `Employee`, el sistema debe ocultar Empleados y Reportes, y habilitar Vehículos, Clientes, Transacciones y Devoluciones.

### 4.3. Requisitos Basados en Eventos
* **RF-07:** Cuando el usuario ingrese sus credenciales válidas en el Login, el sistema debe transicionar a la pantalla principal cargando los datos del concesionario.
* **RF-08:** Cuando un usuario solicite recuperación de contraseña, el sistema debe generar el código de 6 dígitos mediante `dealership.requestPasswordRecovery`, simular el envío y permitir restablecer la clave en una ventana modal.
* **RF-09:** Cuando un empleado intente registrar la compra de un vehículo usado sin haber aprobado la revisión técnica previa, el sistema debe bloquear la operación y notificar la obligatoriedad de la revisión aprobada.
* **RF-10:** Cuando se complete una venta o alquiler, el sistema debe actualizar de forma reactiva la tabla de inventario reflejando el cambio de estado del vehículo.
* **RF-11:** Cuando el administrador filtre el reporte por empleado y fechas, el sistema debe calcular el 2% de comisión sobre ventas, mostrar los totales consolidados y permitir exportar el reporte a archivo `.txt`.

### 4.4. Requisitos de Respuesta a Errores
* **RF-12:** Si ocurre cualquier excepción de dominio (`BlockedUserException`, `TechnicalReviewRejectedException`, `DuplicateVehicleException`, etc.), el controlador debe interceptarla y mostrar un diálogo de alerta amigable en español.

---

## 5. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Autenticación exitosa y barra lateral por rol
* **Dado** el sistema en el arranque con la cuenta `admin` / `Admin123*`,
* **Cuando** el usuario se autentica en el Login,
* **Entonces** ingresa a la ventana principal con barra lateral completa mostrando accesos a Empleados, Reportes, Inventario y Clientes.

### Escenario 2: Rechazo en login de empleado bloqueado
* **Dado** un empleado con estado `BLOCKED`,
* **Cuando** intenta iniciar sesión en el Login con sus credenciales,
* **Entonces** el sistema bloquea el ingreso y muestra una alerta visual informando que su cuenta se encuentra bloqueada.

### Escenario 3: Recuperación de clave con código temporal
* **Dado** un usuario que olvidó su clave,
* **Cuando** solicita código al correo registrado e ingresa el código de 6 dígitos dentro de los 15 minutos junto con su nueva clave,
* **Entonces** el sistema restablece la contraseña y permite el acceso inmediato con la nueva credencial.

### Escenario 4: Inventario con tabla, filtros y panel lateral de ficha técnica
* **Dado** un inventario con múltiples vehículos,
* **Cuando** el usuario busca por placa/marca o selecciona una fila en la tabla,
* **Entonces** el panel lateral actualiza inmediatamente la fotografía, especificaciones completas y estado del vehículo.

### Escenario 5: Registro polimórfico y bloqueo en compra de usado sin revisión
* **Dado** el formulario de compra de vehículo usado,
* **Cuando** se intenta procesar la compra con la casilla de revisión técnica desmarcada,
* **Entonces** el sistema impide la transacción y muestra una alerta de rechazo por falta de revisión técnica.

### Escenario 6: Generación y exportación de reporte gerencial con comisión del 2%
* **Dado** un empleado con ventas y alquileres registrados,
* **Cuando** el administrador genera el reporte para dicho empleado en un rango de fechas y presiona 'Exportar a TXT',
* **Entonces** la pantalla muestra el 2% de comisión sobre ventas y se crea un archivo de texto en disco con el desglose auditado.
