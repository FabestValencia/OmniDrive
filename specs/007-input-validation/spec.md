# Especificación Funcional: Motor Centralizado de Validación de Datos (Input Validation)

* **Identificador:** `007-input-validation`
* **Estado:** borrador inicial
* **Fecha:** 2026-10-17
* **Autores:** Equipo de Arquitectura OmniDrive & @planner

---

## 1. Contexto y Justificación

El sistema OmniDrive cuenta con validaciones defensivas en las entidades del modelo (`model/`) que lanzan excepciones de dominio (`InvalidUserDataException`, `InvalidVehicleDataException`, etc.). Sin embargo, en la capa de presentación (controladores JavaFX), la validación de formularios y el parseo de campos numéricos (`Integer.parseInt`, `Double.parseDouble`) se encuentra dispersa, generando captura repetitiva de `NumberFormatException` y obligando al usuario a esperar hasta presionar "Guardar" para enterarse de errores tipográficos en lugar de recibir retroalimentación visual amigable.

Para elevar la robustez técnica y ergonomía del sistema sin incorporar librerías externas (respetando la Constitución de OmniDrive), se creará un motor ligero de validación desacoplado en el paquete `com.omnidrive.validation`.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* **Contenedor inmutable de resultado (`ValidationResult`):**
  * Estado booleano de validez (`isValid()`), lista inmutable de mensajes de error en español y método utilitario `getFirstErrorMessage()`.
* **Motor utilitario de validación (`InputValidator`):**
  * Placas vehiculares: Validación estricta para automóviles (`AAA-123` o `AAA123`) y motocicletas (`AAA-12A` o `AAA12A`).
  * Identificación / Cédula: Cadena numérica o alfanumérica no vacía de longitud coherente (5 a 15 caracteres).
  * Teléfono: Secuencia numérica de 7 a 12 dígitos, permitiendo guiones y espacios en la entrada de usuario.
  * Correo Electrónico: Validación bajo estándar RFC simplificado.
  * Año de fabricación: Rango coherente entre 1900 y el año actual + 1.
  * Parseo numérico seguro: Métodos para validar y convertir enteros y dobles estrictamente positivos, evitando excepciones no controladas.
  * Rango de fechas: Validación de no nulidad e inversión cronológica para alquileres y reportes gerenciales.
* **Integración en la interfaz gráfica:**
  * Clase CSS `.text-field-error` en `styles.css` para resaltar campos con bordes y acentos de advertencia.
  * Limpieza reactiva del estilo de error cuando el usuario corrige el contenido.

### 2.2. Fuera de Alcance (Out of Scope)
* Inclusión de frameworks externos de anotaciones como Hibernate Validator o Jakarta Validation (incompatibles con la directriz de stack minimalista).
* Servicios de consulta o verificación contra bases de datos externas de Tránsito o RUNT.

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El motor de validación debe residir en `com.omnidrive.validation`, ser completamente agnóstico a JavaFX en sus clases centrales y no poseer dependencias de librerías de terceros.
* **RF-02:** Cada método de validación debe retornar un objeto inmutable `ValidationResult` que describa el éxito o los motivos específicos de fallo en español.

### 3.2. Requisitos Basados en Eventos
* **RF-03:** Cuando se valide una placa vehicular, el sistema debe admitir formatos estándar colombianos tanto para vehículos de 4 o más ruedas como para motocicletas, normalizando a mayúsculas y eliminando espacios superfluos.
* **RF-04:** Cuando se procese un campo numérico (cilindraje, velocidad, capacidad, precio o montos transaccionales), el validador debe verificar formato numérico válido y que el valor sea estrictamente mayor a cero (o no negativo según aplique).
* **RF-05:** Cuando un formulario en la UI contenga entradas no conformes, el controlador debe resaltar los campos erróneos con la clase `.text-field-error` y presentar al usuario una notificación clara.

### 3.3. Requisitos de Respuesta a Errores
* **RF-06:** Si una cadena de texto no puede ser convertida a número, el validador debe reportar un error amigable en español ("Debe ingresar un valor numérico válido") sin lanzar `NumberFormatException` a la capa superior.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Validación estricta y normalización de placas
* **Dado** un texto de placa vehicular ingresado en minúsculas o con espacios,
* **Cuando** se valida mediante `InputValidator.validatePlate`,
* **Entonces** retorna exitoso para formatos válidos (ej. `abc-123`, `XYZ 45D`) y retorna error descriptivo para formatos incorrectos.

### Escenario 2: Validación de datos de personas
* **Dado** un conjunto de datos personales (cédula, correo y teléfono),
* **When** se evalúan mediante las reglas de `InputValidator`,
* **Then** rechaza correos sin `@` o dominio inválido, teléfonos con letras o menos de 7 dígitos y cédulas vacías.

### Escenario 3: Conversión numérica segura sin excepciones
* **Dado** un texto alfanumérico no numérico ingresado en un campo de precio o cilindraje,
* **When** se valida con `InputValidator.parsePositiveDouble` o `parsePositiveInt`,
* **Then** retorna un `ValidationResult` fallido con mensaje descriptivo en español sin arrojar `NumberFormatException`.

### Escenario 4: Validación de rango de fechas
* **Dado** dos fechas seleccionadas en un formulario de alquiler o reporte,
* **When** la fecha de inicio es posterior a la de fin o alguna es nula,
* **Then** el validador reporta la inconsistencia cronológica con un mensaje explicativo.
