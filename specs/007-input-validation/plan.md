# Plan Técnico: Motor Centralizado de Validación de Datos

* **Feature:** `007-input-validation`
* **Estado:** borrador
* **Tecnologías:** Java 21 LTS, JavaFX 21 (CSS `.text-field-error`), JUnit 5

---

## 1. Arquitectura y Paquetes

```text
com.omnidrive/
├── validation/
│   ├── ValidationResult.java     (Contenedor inmutable de resultado: isValid, lista de errores, getFirstError)
│   └── InputValidator.java       (Métodos utilitarios puros y desacoplados para placas, teléfonos, emails, números y fechas)
```

### Reglas de Diseño
1. **Desacople total de UI:** `ValidationResult` e `InputValidator` no importan clases de `javafx.*`. Son clases puras de Java, altamente testeables con JUnit 5 en microsegundos.
2. **Defensividad sin excepciones descontroladas:** Convierten fallos de parseo en mensajes de error entendibles por el usuario final.
3. **Consistencia:** Utilizan las mismas expresiones regulares y límites ya aprobados en el modelo de dominio.

---

## 2. Matriz de Trazabilidad (Requisitos vs Componentes)

| Requisito Funcional | Clase / Módulo | Descripción |
| :--- | :--- | :--- |
| **RF-01, RF-02** (Contenedor inmutable y desacoplado) | `ValidationResult.java` | Inmutable, fábrica `success()`, `failure(String...)`, `getErrors()`. |
| **RF-03** (Placas vehiculares) | `InputValidator.validatePlate(...)` | Regex para carros `^[A-Z]{3}-?[0-9]{3}$` y motos `^[A-Z]{3}-?[0-9]{2}[A-Z]$`. |
| **RF-04, RF-06** (Parseo numérico seguro) | `InputValidator.parsePositiveDouble / Int` | Conversión segura de campos de texto sin arrojar `NumberFormatException`. |
| **RF-05** (Estilo CSS y feedback visual) | `styles.css` (`.text-field-error`) | Integración progresiva en formularios para feedback en tiempo real. |

---

## 3. Desglose Secuencial de Tareas (TDD)

1. **T1:** Implementar contenedor `ValidationResult` y su suite de pruebas unitarias (`ValidationResultTest.java`).
2. **T2:** Implementar motor utilitario `InputValidator` con validación de placas, datos personales, números positivos y rangos de fecha (`InputValidatorTest.java`).
3. **T3:** Definir regla visual `.text-field-error` en `styles.css` e integrar `InputValidator` en `CustomerManagementController` y `EmployeeManagementController`.
4. **T4:** Integrar `InputValidator` en `VehicleFormDialogController` eliminando parseos manuales propensos a error.
5. **T5:** Integrar en `TransactionController` y `ReportController`, verificando que la suite completa pase al 100%.
