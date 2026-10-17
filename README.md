# OmniDrive — Sistema de Información para Concesionario

Sistema de información de escritorio desarrollado en **Java 21 LTS** y **JavaFX 21** bajo el patrón arquitectónico **MVC puro** (Modelo-Vista-Controlador), diseñado para la gestión integral del inventario vehicular, administración de colaboradores y clientes, procesamiento seguro de transacciones comerciales y generación de reportes gerenciales auditables.

---

## 🚀 Características Principales

1. **Gestión de Inventario Multitipo (8 Familias de Vehículos):**
   * **Sedán, Deportivo, Camioneta / SUV, Pick Up, Van, Bus, Camión y Moto**.
   * Soporte transversal de tecnologías de propulsión: **Gasolina, Diésel, Eléctrico** (autonomía y tiempo de carga) e **Híbrido** (enchufable y ligero/mild-hybrid).
   * Ciclo de vida y disponibilidad: `DISPONIBLE_VENTA`, `DISPONIBLE_ALQUILER`, `ALQUILADO`, `VENDIDO` y `EN_MANTENIMIENTO`.

2. **Seguridad y Control de Acceso por Roles (RBAC):**
   * **Administrador:** Gestión y bloqueo permanente de colaboradores, acceso a reportes y configuración de cuentas. Cuenta raíz de arranque predeterminada (`ADMIN-001` / `admin`).
   * **Empleado:** Registro de clientes, publicación de vehículos y ejecución de transacciones comerciales.
   * **Seguridad Criptográfica:** Hashing SHA-256 (`MessageDigest.isEqual`) y recuperación de credenciales mediante tokens de un solo uso con ventana de 15 minutos.

3. **Operaciones Comerciales Auditadas:**
   * **Compra:** Validación técnica previa obligatoria para vehículos usados (`revisionTecnicaAprobada`).
   * **Venta:** Facturación comercial con transferencia de titularidad y transición a estado vendido.
   * **Alquiler y Devolución:** Registro de rangos cronológicos, días de arriendo y retorno seguro a disponible.
   * **Historial Completo:** Registro de fecha, empleado responsable, cliente, vehículo y montos involucrados.

4. **Reportes Gerenciales y Comisiones:**
   * Filtros dinámicos por colaborador y rango de fechas.
   * Cálculo automático del **2% de comisión sobre ventas realizadas**.
   * Exportación de reportes detallados y firmados en archivos de texto plano (`.txt`).

5. **Validación Defensiva y Experiencia de Usuario:**
   * Motor utilitario de validación desacoplado (`InputValidator`) para placas colombianas (carro y moto), cédulas, teléfonos y correos RFC.
   * Parseo seguro de importes numéricos y fechas sin excepciones no controladas.
   * Retroalimentación visual en tiempo real en la UI (`.text-field-error`).

6. **Persistencia Local Segura:**
   * Almacenamiento serializado de objetos (`java.io.Serializable`) con sincronización continua y respaldo en parada de la aplicación (`App.stop()`).

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21 LTS.
* **Interfaz Gráfica:** JavaFX 21.0.2 (`javafx-controls`, `javafx-fxml`).
* **Gestor de Construcción:** Apache Maven 3.10+.
* **Testing:** JUnit 5 (Jupiter 5.10.2) — **256 pruebas automatizadas (100% pasando)**.
* **Persistencia:** Serialización nativa Java (`java.io.Serializable`).

---

## 📂 Estructura del Proyecto

```text
OmniDrive/
├── pom.xml
├── README.md
├── AGENTS.md
├── MEMORY.md
├── data/                                 # Almacén de persistencia local (.dat)
├── specs/                                # Especificaciones y planes de desarrollo
│   ├── 001-vehicle-hierarchy/
│   ├── 002-actors-and-auth/
│   ├── 003-transactions/
│   ├── 004-reports/
│   ├── 005-persistence/
│   ├── 006-gui-javafx/
│   └── 007-input-validation/
├── src/
│   ├── main/
│   │   ├── java/com/omnidrive/
│   │   │   ├── Main.java                 # Punto de entrada Java no modular
│   │   │   ├── App.java                  # Ciclo de vida JavaFX Application
│   │   │   ├── context/AppContext.java   # Estado global y sesión activa
│   │   │   ├── controller/               # Controladores JavaFX (MVC)
│   │   │   ├── exception/                # Jerarquía OmniDriveException
│   │   │   ├── model/                    # Entidades de dominio desacopladas
│   │   │   ├── util/                     # Serialización y persistencia
│   │   │   └── validation/               # Motor de validación de entradas
│   │   └── resources/com/omnidrive/
│   │       └── view/                     # Vistas .fxml y estilos .css
│   └── test/java/com/omnidrive/          # Suite exhaustiva de pruebas unitarias
```

---

## ⚙️ Requisitos Previos

* **JDK:** Java 21 LTS o superior instalado (`java -version`).
* **Maven:** Apache Maven 3.8+ instalado (`mvn -version`).

---

## 🏁 Instalación y Ejecución

1. **Clonar el repositorio:**
   ```powershell
   git clone https://github.com/FabestValencia/OmniDrive.git
   cd OmniDrive
   ```

2. **Compilar el proyecto:**
   ```powershell
   mvn clean compile
   ```

3. **Ejecutar la suite completa de pruebas:**
   ```powershell
   mvn test
   ```

4. **Iniciar la aplicación (JavaFX):**
   ```powershell
   mvn javafx:run
   ```

---

## 🔑 Credenciales Iniciales

El sistema realiza arranque en frío garantizando la existencia de una cuenta de Administrador por defecto:

| Rol | Usuario | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin` | `Admin123*` |

---

## 📄 Licencia

Este proyecto ha sido desarrollado con fines académicos y de demostración técnica de buenas prácticas de programación orientada a objetos (POO), patrones de arquitectura limpia y desarrollo guiado por pruebas (TDD).
