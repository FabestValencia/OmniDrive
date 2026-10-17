# Especificación Funcional: Persistencia Local del Estado del Concesionario

* **Identificador:** `005-persistence`
* **Estado:** aprobada
* **Fecha:** 2026-10-15
* **Autores:** Equipo de Arquitectura OmniDrive & @planner

---

## 1. Contexto y Propósito

El sistema OmniDrive gestiona entidades críticas y complejas de negocio: inventario de 8 tipos de vehículos, cuentas y estados de empleados, clientes y registros históricos de compras, ventas y alquileres. 

Para que el concesionario funcione de forma confiable en un entorno de escritorio, es imprescindible garantizar la persistencia del estado en el almacenamiento local. Esta especificación formaliza los requisitos para guardar el estado completo del concesionario en un archivo local y recuperarlo fielmente tras el reinicio de la aplicación, manejando de forma segura el primer arranque y la detección de archivos corruptos o inaccesibles.

---

## 2. Límites de Alcance

### 2.1. En Alcance (In Scope)
* Guardado del estado completo del concesionario en archivo local bajo demanda o al cierre del sistema.
* Carga del estado persistido recuperando vehículos, clientes, empleados, cuentas y transacciones.
* Inicialización automática y transparente con el Administrador raíz por defecto (`ADMIN-001`, `admin`) si el archivo de persistencia no existe previamente.
* Detección estricta y rechazo de archivos corruptos, incompletos o incompatibles, arrojando una excepción de dominio para prevenir corrupción de datos.
* Respeto absoluto del patrón MVC y el desacople de interfaz: el componente de persistencia no depende ni importa clases de presentación gráfica (`javafx.*`).

### 2.2. Fuera de Alcance (Out of Scope)
* Servidores o motores de bases de datos relacionales externos (MySQL, Oracle, PostgreSQL) o NoSQL.
* Sincronización en la nube o almacenamiento distribuido multi-usuario concurrentemente en red.
* Migración automática de esquemas o versiones heterogéneas de objetos serializados.

---

## 3. Requisitos Funcionales (Formato EARS)

### 3.1. Requisitos Ubicuos
* **RF-01:** El sistema debe proporcionar un mecanismo centralizado de persistencia capaz de escribir y leer el estado completo del modelo del concesionario en una ruta de archivo local configurable.
* **RF-02:** El sistema debe conservar en el archivo de persistencia la totalidad de las entidades de negocio: lista de vehículos con sus atributos específicos, clientes registrados, empleados con sus estados laborales y credenciales, cuentas y la secuencia completa de transacciones comerciales.
* **RF-03:** El sistema debe restaurar el estado del concesionario manteniendo inmutables los identificadores únicos, fechas, montos y referencias entre empleados, clientes y vehículos.

### 3.2. Requisitos Condicionales
* **RF-04:** Mientras el archivo de persistencia no exista en la ruta especificada (escenario de primer arranque o instalación limpia), el sistema debe crear un nuevo concesionario predeterminado que cuente de manera garantizada con la cuenta del Administrador raíz (`ADMIN-001`, `admin`).
* **RF-05:** Mientras una operación de guardado se esté ejecutando, el sistema debe asegurar que el archivo se escriba de forma íntegra.

### 3.3. Requisitos Basados en Eventos
* **RF-06:** Cuando el usuario o el sistema solicite explícitamente guardar los datos, el sistema debe volcar el estado actual del concesionario en el archivo local de destino.
* **RF-07:** Cuando el sistema inicie y se invoque la carga de datos, debe leer el archivo existente y retornar la instancia del concesionario con su inventario y transacciones restauradas.

### 3.4. Requisitos de Respuesta a Errores
* **RF-08:** Si el archivo de persistencia existe pero contiene datos corruptos, incompletos o no interpretables, el sistema debe rechazar la carga y arrojar una excepción específica de persistencia detallando la imposibilidad de restaurar el estado.
* **RF-09:** Si la ruta del archivo destino es inaccesible, inválida o protegida contra escritura, el sistema debe abortar la operación de guardado arrojando una excepción descriptiva sin provocar cierres inesperados.
* **RF-10:** Si se suministra una referencia nula al intentar guardar el concesionario, el sistema debe rechazar la operación indicando que no se puede persistir un estado nulo.

---

## 4. Criterios de Aceptación (Escenarios Verificables)

### Escenario 1: Inicialización automática en primer arranque (archivo ausente)
* **Dado** que no existe previamente ningún archivo de datos en la ruta configurada,
* **Cuando** el sistema ejecuta la carga inicial de datos,
* **Entonces** el sistema inicializa una nueva instancia de concesionario, garantiza la presencia del Administrador raíz (`ADMIN-001`, `admin`) y permite iniciar sesión de forma inmediata.

### Escenario 2: Ciclo completo de guardado y restauración exitosa
* **Dado** un concesionario con 3 vehículos registrados, 2 empleados (uno activo y uno bloqueado), 1 cliente y 2 transacciones de compra/venta,
* **Cuando** se ejecuta la operación de guardado en archivo local y posteriormente se carga el concesionario desde dicho archivo en una nueva instancia,
* **Entonces** la instancia restaurada conserva exactamente:
  - Los 3 vehículos con sus estados correspondientes,
  - Los 2 empleados manteniendo su estado (`ACTIVE` y `BLOCKED`),
  - El cliente registrado,
  - Las 2 transacciones con sus montos, fechas y referencias intactas.

### Escenario 3: Rechazo ante archivo corrupto o ilegible
* **Dado** un archivo en la ruta de persistencia con contenido dañado o bytes aleatorios no serializables,
* **Cuando** el sistema intenta cargar el concesionario desde ese archivo,
* **Entonces** el sistema detiene la carga y arroja una excepción de persistencia indicando que el archivo está corrupto.

### Escenario 4: Rechazo ante ruta de archivo inaccesible
* **Dado** una ruta de archivo inválida o un directorio sin permisos de escritura,
* **Cuando** se intenta guardar el estado del concesionario en esa ruta,
* **Entonces** el sistema arroja una excepción de persistencia informando el error de acceso al archivo.

### Escenario 5: Rechazo de guardado de concesionario nulo
* **Dado** una referencia nula de concesionario,
* **Cuando** se invoca el método de guardado suministrando `null`,
* **Entonces** el sistema rechaza la operación informando que la instancia a persistir no puede ser nula.
