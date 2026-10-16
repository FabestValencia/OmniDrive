# Especificación Funcional: Transacciones Comerciales y Auditoría

* **Identificador:** 003-transactions
* **Fecha:** 2026-10-15
* **Estado:** aprobada

---

## 1. Contexto y Propósito

El concesionario **OmniDrive** requiere registrar y auditar de forma confiable todas sus transacciones comerciales: **compras de vehículos** a clientes, **ventas de vehículos** a clientes y **alquileres de vehículos**, así como la **devolución de vehículos alquilados**.

Esta especificación establece las reglas del ciclo de vida de disponibilidad de los vehículos, las condiciones obligatorias de auditoría (empleado activo responsable, cliente, fecha/hora y montos) y la exigencia estricta de revisión técnica previa para la compra de vehículos usados.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* Identificación única de cada transacción mediante un código alfanumérico no vacío (con soporte para autogeneración secuencial tipo `TXN-00001`).
* Clasificación en 3 tipos de transacciones comerciales: **Compra**, **Venta** y **Alquiler**.
* Registro de auditoría obligatorio en toda transacción:
  * Empleado responsable (debe encontrarse en estado activo).
  * Cliente involucrado (comprador, vendedor o arrendatario).
  * Vehículo objeto de la negociación.
  * Fecha y hora exacta de registro de la transacción.
  * Monto económico total pactado.
* Reglas de **Compra de vehículos**:
  * Adquisición de vehículos nuevos o usados a clientes.
  * Verificación de placa no repetida en el inventario.
  * Verificación obligatoria de revisión técnica aprobada para vehículos usados (`isTechnicalReviewApproved == true`).
  * Asignación explícita del estado inicial en el inventario por parte del empleado: *Disponible para venta* o *Disponible para alquiler*.
* Reglas de **Venta de vehículos**:
  * Solo aplicable a vehículos cuyo estado actual sea exactamente *Disponible para venta*.
  * Transición inmediata del vehículo al estado *Vendido*.
* Reglas de **Alquiler de vehículos**:
  * Solo aplicable a vehículos cuyo estado actual sea exactamente *Disponible para alquiler*.
  * Registro del período de alquiler (fechas de inicio y fin, o cantidad de días).
  * Cálculo del monto total del alquiler (días de alquiler $\times$ tarifa diaria base del vehículo).
  * Transición inmediata del vehículo al estado *Alquilado*.
* Reglas de **Devolución de alquiler**:
  * Registro de la entrega y retorno del vehículo alquilado al estado *Disponible para alquiler* (solo aplicable a vehículos actualmente en estado *Alquilado*).
* Restricciones de seguridad y disponibilidad:
  * Prohibición absoluta de registrar transacciones si el empleado responsable está en estado bloqueado.
  * Rechazo de ventas o alquileres sobre vehículos no disponibles.

### 2.2. Fuera de Alcance (Out of Scope)
* Generación de reportes gerenciales con filtros de fechas agrupados por empleado (será abordado en la especificación `004-reports`).
* Integración con pasarelas de pago bancarias externas o emisión de facturación electrónica fiscal.
* Formularios y vistas gráficas en JavaFX.

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El sistema debe identificar a cada transacción registrada mediante un identificador o código único no vacío, generado automáticamente o asignado manualmente.
* **RF-02:** El sistema debe registrar en toda transacción su fecha y hora de realización, el empleado responsable, el cliente beneficiario o vendedor, el vehículo involucrado y el monto monetario de la operación.
* **RF-03:** El sistema debe clasificar a cada transacción en exactamente uno de los siguientes tipos: Compra, Venta o Alquiler.
* **RF-04:** El sistema debe registrar el monto total de cada transacción como un valor numérico positivo mayor a cero.

### 3.2. Requisitos Condicionales
* **RF-05:** Mientras una transacción sea de tipo Alquiler, el sistema debe registrar obligatoriamente la fecha de inicio, la fecha estimada de finalización y el número de días contratados (mínimo 1 día).
* **RF-06:** Mientras un empleado se encuentre en estado "Bloqueado", el sistema debe prohibirle de forma absoluta registrar cualquier tipo de transacción (compra, venta o alquiler).
* **RF-07:** Mientras un vehículo no se encuentre en estado "Disponible para venta", el sistema debe prohibir su venta a cualquier cliente.
* **RF-08:** Mientras un vehículo no se encuentre en estado "Disponible para alquiler", el sistema debe prohibir su alquiler a cualquier cliente.

### 3.3. Requisitos Basados en Eventos
* **RF-09:** Cuando un empleado registre la compra de un vehículo usado a un cliente, el sistema debe verificar que la revisión técnica esté marcada como aprobada antes de concretar la transacción.
* **RF-10:** Cuando se complete la compra de un vehículo, el sistema debe validar que la placa no exista en el inventario e ingresarlo asignándole el estado de disponibilidad seleccionado por el empleado ("Disponible para venta" o "Disponible para alquiler").
* **RF-11:** Cuando se complete la venta de un vehículo, el sistema debe cambiar su estado de disponibilidad a "Vendido" y registrar la transacción con el valor acordado.
* **RF-12:** Cuando se complete el alquiler de un vehículo, el sistema debe cambiar su estado de disponibilidad a "Alquilado" y calcular el monto total multiplicando el número de días por la tarifa diaria base del vehículo.
* **RF-13:** Cuando se registre la devolución de un vehículo alquilado, el sistema debe verificar que el vehículo esté en estado "Alquilado" y cambiar su estado nuevamente a "Disponible para alquiler".

### 3.4. Requisitos de Respuesta a Errores
* **RF-14:** Si se intenta registrar la compra de un vehículo usado que no haya aprobado la revisión técnica previa, el sistema debe rechazar la transacción e impedir el ingreso del vehículo al inventario informando la causa del rechazo.
* **RF-15:** Si un empleado en estado bloqueado intenta registrar una compra, venta o alquiler, el sistema debe abortar la transacción indicando que el empleado está deshabilitado.
* **RF-16:** Si se intenta vender un vehículo cuyo estado no sea "Disponible para venta" (por ejemplo, ya vendido, alquilado o en mantenimiento), el sistema debe rechazar la operación indicando que el vehículo no está disponible para venta.
* **RF-17:** Si se intenta alquilar un vehículo cuyo estado no sea "Disponible para alquiler", el sistema debe rechazar la operación indicando que el vehículo no está disponible para alquiler.
* **RF-18:** Si se intentan ingresar datos inconsistentes en la transacción (fechas de alquiler invertidas donde el fin sea anterior al inicio, montos negativos o cero, o referencias nulas a empleado, cliente o vehículo), el sistema debe rechazar la transacción detallando los campos inválidos.
* **RF-19:** Si se intenta registrar una transacción con un identificador que ya existe en el sistema, la operación debe ser rechazada evitando duplicados.
* **RF-20:** Si se intenta registrar la devolución de un vehículo que no se encuentra en estado "Alquilado", el sistema debe rechazar la operación indicando que el vehículo no está bajo contrato de alquiler vigente.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Venta exitosa de vehículo disponible
* **Dado** un vehículo Sedán en estado "Disponible para venta" con precio base de $25,000 y un empleado activo,
* **Cuando** el empleado registra la venta del vehículo a un cliente registrado por $25,000,
* **Entonces** el sistema registra la transacción de venta y el vehículo pasa a estado "Vendido".

### Escenario 2: Alquiler exitoso y cálculo de tarifa
* **Dado** una camioneta SUV en estado "Disponible para alquiler" con tarifa diaria de $100 y un empleado activo,
* **Cuando** el empleado registra el alquiler a un cliente por 5 días,
* **Entonces** el sistema registra la transacción con un monto calculado de $500 y el vehículo pasa a estado "Alquilado".

### Escenario 3: Devolución de vehículo alquilado
* **Dado** un vehículo en estado "Alquilado",
* **Cuando** se registra la devolución del vehículo al concesionario,
* **Entonces** el vehículo pasa a estado "Disponible para alquiler" quedando habilitado para futuras operaciones.

### Escenario 4: Rechazo de compra de vehículo usado sin revisión técnica aprobada
* **Dado** un vehículo usado cuya revisión técnica está no aprobada o rechazada,
* **Cuando** un empleado intenta registrar la compra del vehículo a un cliente,
* **Entonces** el sistema rechaza la transacción, arroja alerta por revisión técnica rechazada y no almacena el vehículo.

### Escenario 5: Rechazo de transacción operada por empleado bloqueado
* **Dado** un empleado que ha sido bloqueado por el Administrador,
* **Cuando** se intenta procesar una venta o alquiler asociando a dicho empleado como responsable,
* **Entonces** el sistema rechaza la transacción e informa que el empleado está bloqueado.

### Escenario 6: Rechazo de venta sobre vehículo no disponible para venta
* **Dado** un vehículo que se encuentra en estado "Disponible para alquiler" o "Alquilado",
* **Cuando** se intenta registrar una venta sobre dicho vehículo,
* **Entonces** el sistema rechaza la operación informando que el vehículo no se encuentra disponible para venta.

### Escenario 7: Rechazo de devolución de vehículo no alquilado
* **Dado** un vehículo que se encuentra en estado "Disponible para venta" o "Vendido",
* **Cuando** se intenta registrar una devolución de alquiler sobre dicho vehículo,
* **Entonces** el sistema rechaza la operación informando que el vehículo no está en alquiler.
