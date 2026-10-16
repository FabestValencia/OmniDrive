# Especificación Funcional: Reportes Gerenciales y Métricas de Negocio

* **Identificador:** `004-reports`
* **Estado:** aprobada
* **Fecha:** 2026-10-15
* **Autores:** Equipo de Arquitectura OmniDrive & @planner

---

## 1. Contexto y Propósito

El concesionario OmniDrive requiere dotar a la cuenta de Administrador de visibilidad integral sobre el rendimiento operativo y comercial del equipo de ventas. Actualmente el sistema registra con rigor transacciones de compra, venta y alquiler asociadas a cada empleado activo. 

Esta especificación formaliza la capacidad de consolidar y analizar la actividad comercial de cualquier empleado dentro de un rango temporal específico, permitiendo evaluar el volumen de negocios cerrados, el flujo monetario total, la comisión devengada por ventas y el registro histórico detallado de transacciones.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* Generación de reportes gerenciales filtrados por empleado y rango cerrado de fechas (`[fechaInicio, fechaFin]`).
* Consolidación de contadores de operaciones clasificadas por tipo (número de compras, ventas y alquileres).
* Cálculo de montos monetarios acumulados por categoría (total comprado, total vendido, total facturado por alquileres).
* Cálculo de balance neto o volumen global transaccionado en el período.
* Cálculo automático de comisión por ventas del 2% sobre el valor total de vehículos vendidos por el empleado en el período.
* Inclusión del listado cronológico de las transacciones asociadas que sustentan los totales.
* Auditoría histórica sobre empleados que se encuentren actualmente en estado "Bloqueado".
* Manejo de empleados sin operaciones registradas en el período (reporte con métricas en cero).
* Validación de consistencia temporal del rango consultado.

### 2.2. Fuera de Alcance (Out of Scope)
* Generación o exportación física a formatos PDF, Excel o CSV (la presentación y estructura de datos se encapsula a nivel de modelo para la vista).
* Pago, dispersión bancaria o liquidación contable de nómina.
* Configuración de porcentajes de comisión diferenciados por vehículo o por empleado.
* Edición o alteración retroactiva de los reportes generados.

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El sistema debe estructurar a cada reporte gerencial asociándole el empleado consultado, su estado laboral actual, la fecha/hora de emisión del reporte y el rango temporal evaluado (`fechaInicio` y `fechaFin`).
* **RF-02:** El sistema debe registrar en el reporte el número total de transacciones realizadas en el período, desglosado en: cantidad de compras, cantidad de ventas y cantidad de alquileres.
* **RF-03:** El sistema debe calcular y presentar en el reporte los subtotales monetarios acumulados para: monto total pagado en compras a clientes, monto total facturado en ventas y monto total facturado en alquileres.
* **RF-04:** El sistema debe calcular el monto global acumulado sumando la totalidad de transacciones ejecutadas en el período.
* **RF-05:** El sistema debe calcular la comisión por ventas como el 2% del monto total facturado exclusivamente en ventas de vehículos en dicho período.
* **RF-06:** El sistema debe incluir en el reporte la colección cronológica de todas las transacciones individuales que sustentan las métricas calculadas.

### 3.2. Requisitos Condicionales
* **RF-07:** Mientras el empleado no tenga transacciones registradas dentro del rango temporal consultado, el sistema debe emitir un reporte válido con todos los contadores en cero (0), subtotales y comisiones en $0.00 y una lista vacía de transacciones.
* **RF-08:** Mientras la cuenta del empleado se encuentre en estado "Bloqueado", el sistema debe permitir al Administrador consultar sus reportes históricos de auditoría, indicando explícitamente en el reporte el estado bloqueado del empleado.
* **RF-09:** Mientras la fecha y hora de una transacción caiga dentro del intervalo cerrado `[fechaInicio, fechaFin]`, el sistema debe contabilizarla en el reporte correspondiente.

### 3.3. Requisitos Basados en Eventos
* **RF-10:** Cuando el Administrador solicite la generación de un reporte suministrando un empleado válido y un rango temporal coherente, el sistema debe consolidar las métricas de inmediato y retornar el reporte estructurado e inmutable.

### 3.4. Requisitos de Respuesta a Errores
* **RF-11:** Si se solicita un reporte suministrando una referencia nula a empleado o un identificador de empleado inexistente en el sistema, la operación debe ser rechazada informando que el empleado no fue encontrado.
* **RF-12:** Si se solicita un reporte suministrando un rango temporal donde la fecha de inicio sea posterior a la fecha de fin, el sistema debe rechazar la operación indicando que el rango de fechas es inválido.
* **RF-13:** Si se solicita un reporte suministrando fechas nulas (inicio o fin), el sistema debe rechazar la operación indicando la obligatoriedad de los límites temporales.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Generación exitosa de reporte con actividad mixta y cálculo de comisiones
* **Dado** un empleado activo que ha realizado en el mes:
  - 1 compra por $30,000,
  - 2 ventas por un total de $70,000 ($40,000 y $30,000),
  - 1 alquiler por $1,000,
* **Cuando** el Administrador genera el reporte del empleado para dicho mes,
* **Entonces** el reporte consolida:
  - 4 transacciones totales (1 compra, 2 ventas, 1 alquiler),
  - Total compras: $30,000,
  - Total ventas: $70,000,
  - Total alquileres: $1,000,
  - Monto global movilizado: $101,000,
  - Comisión por ventas (2% de $70,000): $1,400,
  - Lista cronológica con las 4 transacciones detalladas.

### Escenario 2: Generación de reporte para período sin actividad comercial
* **Dado** un empleado activo que no ha efectuado transacciones entre el 1 y el 15 de marzo,
* **Cuando** el Administrador genera el reporte para ese rango temporal,
* **Entonces** el sistema genera un reporte con:
  - Conteo total de transacciones: 0,
  - Subtotales monetarios en $0.00,
  - Comisión en $0.00,
  - Lista de transacciones vacía.

### Escenario 3: Auditoría histórica sobre empleado bloqueado
* **Dado** un empleado que realizó ventas el mes pasado y fue bloqueado posteriormente por el Administrador,
* **Cuando** el Administrador solicita el reporte de dicho empleado para el mes pasado,
* **Entonces** el sistema genera exitosamente el reporte con las transacciones históricas, refleja las ventas y comisiones devengadas, e indica que el empleado se encuentra en estado `BLOCKED`.

### Escenario 4: Rechazo ante rango temporal invertido
* **Dado** un empleado registrado en el concesionario,
* **Cuando** el Administrador solicita un reporte con fecha de inicio posterior a la fecha de fin (ejemplo: inicio 2026-06-30 y fin 2026-06-01),
* **Entonces** el sistema rechaza la solicitud e informa que el rango de fechas es inconsistente.

### Escenario 5: Rechazo ante empleado inexistente o nulo
* **Dado** un identificador de empleado que no existe en el concesionario o es nulo,
* **Cuando** se intenta generar un reporte con dicho identificador,
* **Entonces** el sistema rechaza la operación informando que el empleado no existe.
