# Especificación Funcional: Catálogo y Jerarquía de Vehículos

* **Identificador:** 001-vehicle-hierarchy
* **Fecha:** 2026-10-15
* **Estado:** aprobada

---

## 1. Contexto y Propósito

El concesionario **OmniDrive** requiere renovar la gestión de su inventario de vehículos para soportar operaciones de compra, venta y alquiler. Esta especificación define las reglas de negocio, características obligatorias y ciclo de vida de disponibilidad para los 8 tipos de vehículos comercializados, garantizando la integridad de los datos antes de que los vehículos puedan ser ofrecidos a los clientes o transaccionados por los empleados.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* Identificación única y obligatoria de cada vehículo mediante su placa.
* Registro y clasificación de 8 tipos de vehículos: Motos, Sedanes, Deportivos, Camionetas/SUV, Pick Ups, Vans, Buses y Camiones.
* Atributos generales compartidos: marca, estado (nuevo o usado), modelo (año), número de cambios, velocidad máxima, cilindraje, tipo de combustible, transmisión y precio/tarifa base.
* Propiedades transversales de combustible:
  * Eléctrico: autonomía con carga completa y tiempo promedio de carga.
  * Híbrido: especificación de si es enchufable o no enchufable, y si es híbrido ligero (mild hybrid).
* Atributos específicos y equipamiento de seguridad/capacidad para cada uno de los 8 tipos.
* Gestión de estados de disponibilidad (*Disponible para venta, Disponible para alquiler, Vendido, Alquilado, En mantenimiento*).
* Regla estricta de revisión técnica: rechazo obligatorio de cualquier registro de vehículo usado cuya revisión técnica no esté aprobada.
* Asociación opcional de imagen/foto representativa del vehículo (con imagen por defecto ante su ausencia).

### 2.2. Fuera de Alcance (Out of Scope)
* Registro de clientes y empleados (será abordado en otra especificación).
* Creación y facturación de contratos de venta, compra o alquiler.
* Pantallas gráficas o persistencia técnica en disco.

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El sistema debe identificar a cada vehículo registrado en el inventario mediante una placa única y no vacía.
* **RF-02:** El sistema debe registrar en todo vehículo sus atributos generales: marca, estado (nuevo o usado), modelo (año de fabricación), número de velocidades/cambios, velocidad máxima (km/h), cilindraje (cc), transmisión (manual o automática) y valor base económico (precio de venta o tarifa de alquiler).
* **RF-03:** El sistema debe clasificar todo vehículo en exactamente uno de los siguientes tipos: Moto, Sedán, Deportivo, Camioneta/SUV, Pick Up, Van, Bus o Camión.
* **RF-04:** El sistema debe soportar para cualquiera de los 8 tipos de vehículos uno de los siguientes tipos de combustible: Gasolina, Diésel, Eléctrico o Híbrido.

### 3.2. Requisitos Condicionales
* **RF-05:** Mientras un vehículo sea de tipo combustible Eléctrico, el sistema debe registrar obligatoriamente su autonomía con carga completa (en km) y su tiempo promedio de recarga (en horas).
* **RF-06:** Mientras un vehículo sea de tipo combustible Híbrido, el sistema debe registrar si es enchufable o no enchufable; y si no es enchufable, debe indicar si es híbrido ligero (*mild hybrid*) o no.
* **RF-07:** Mientras el vehículo sea un Sedán, el sistema debe registrar: número de pasajeros, número de puertas, capacidad del maletero (L), presencia de aire acondicionado, cámara de reversa, velocidad de crucero, número de bolsas de aire, frenos ABS, sensores de colisión, sensor de tráfico cruzado y asistente de permanencia en el carril.
* **RF-08:** Mientras el vehículo sea un Deportivo, el sistema debe registrar: número de pasajeros, número de puertas, número de bolsas de aire, caballos de fuerza (HP) y tiempo de aceleración de 0 a 100 km/h (en segundos).
* **RF-09:** Mientras el vehículo sea una Camioneta/SUV, el sistema debe registrar: número de pasajeros, número de puertas, capacidad del maletero (L), presencia de aire acondicionado, cámara de reversa, velocidad de crucero, número de bolsas de aire, frenos ABS, sensores de colisión, sensor de tráfico cruzado, asistente de permanencia en el carril y tracción 4x4.
* **RF-10:** Mientras el vehículo sea una Pick Up, el sistema debe registrar: número de pasajeros, número de puertas, presencia de aire acondicionado, cámara de reversa, número de bolsas de aire, frenos ABS, tracción 4x4 y capacidad de la caja de carga (en kg o volumen).
* **RF-11:** Mientras el vehículo sea una Van, el sistema debe registrar: número de pasajeros, número de puertas, capacidad del maletero (L), presencia de aire acondicionado, cámara de reversa, número de bolsas de aire y frenos ABS.
* **RF-12:** Mientras el vehículo sea un Bus, el sistema debe registrar: número de pasajeros, número de puertas, capacidad del maletero (L), presencia de aire acondicionado, cámara de reversa, número de bolsas de aire, frenos ABS, número de ejes y número de salidas de emergencia.
* **RF-13:** Mientras el vehículo sea un Camión, el sistema debe registrar: capacidad de carga (en toneladas/kg), presencia de aire acondicionado, frenos de aire, frenos ABS, número de ejes y tipo de camión (furgón, cisterna, tolva, plataforma, etc.).
* **RF-14:** Mientras el estado del vehículo sea "usado", el sistema debe verificar obligatoriamente si aprobó la revisión técnica previa.
* **RF-15:** Mientras no se especifique una ruta o imagen personalizada para el vehículo, el sistema debe asignarle automáticamente una imagen de referencia por defecto.

### 3.3. Requisitos Basados en Eventos
* **RF-16:** Cuando se registre un vehículo, el empleado debe seleccionar explícitamente si su estado inicial de disponibilidad es "Disponible para venta" o "Disponible para alquiler".
* **RF-17:** Cuando se consulte el catálogo de vehículos, el sistema debe permitir filtrar los resultados por tipo de vehículo, tipo de combustible, estado (nuevo/usado), rango de precio y disponibilidad.

### 3.4. Requisitos de Respuesta a Errores
* **RF-18:** Si un vehículo usado tiene la revisión técnica rechazada o no aprobada, el sistema debe impedir su registro en el inventario y rechazar la operación informando el motivo del bloqueo.
* **RF-19:** Si se intenta registrar un vehículo con una placa que ya existe en el sistema, la operación debe ser rechazada evitando registros duplicados.
* **RF-20:** Si se ingresan valores numéricos inconsistentes (año de fabricación futuro, cilindraje, velocidad máxima, precio, número de pasajeros, puertas o bolsas de aire menores o iguales a cero cuando correspondan), el sistema debe impedir el registro indicando los campos inválidos.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Registro exitoso de vehículo nuevo (Sedán Gasolina)
* **Dado** que se ingresan los datos válidos de un Sedán con placa única "ABC123", nuevo, combustible Gasolina y destino "Disponible para venta",
* **Cuando** se solicita el registro en el inventario,
* **Entonces** el vehículo queda guardado exitosamente con estado "Disponible para venta".

### Escenario 2: Registro exitoso de vehículo eléctrico (Deportivo)
* **Dado** que se ingresan los datos de un Deportivo con placa única "ELE999", combustible Eléctrico, autonomía de 450 km y tiempo de carga de 2 horas,
* **Cuando** se solicita el registro,
* **Entonces** el sistema valida los parámetros eléctricos y registra el vehículo satisfactoriamente.

### Escenario 3: Rechazo de registro de vehículo usado sin revisión técnica aprobada
* **Dado** que se intenta registrar una Camioneta usada con placa "USD456" y la revisión técnica previa está marcada como no aprobada,
* **Cuando** se procesa la solicitud,
* **Entonces** el sistema no almacena el vehículo y genera una alerta de rechazo por revisión técnica no aprobada.

### Escenario 4: Rechazo por placa duplicada
* **Dado** que ya existe en el inventario un vehículo con placa "DUP001",
* **Cuando** se intenta registrar otro vehículo con la misma placa "DUP001",
* **Entonces** el sistema rechaza la operación e indica que la placa ya se encuentra registrada.

### Escenario 5: Manejo de foto por defecto
* **Dado** que se registra una Moto sin especificar una ruta de foto,
* **Cuando** se completa el registro,
* **Entonces** el vehículo queda asociado a la imagen genérica predeterminada del sistema.
