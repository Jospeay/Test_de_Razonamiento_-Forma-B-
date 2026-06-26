#  Test de Razonamiento B — UAM
### Plataforma de Evaluación Psicométrica (Backend OpenXava & REST API)
#### Universidad Americana (UAM) — Ingeniería en Sistemas de Información / Ingeniería de Software

Este repositorio contiene la implementación del componente de administración y backend transaccional (REST API) para la plataforma del **Test de Razonamiento - Forma B**. 

El sistema está desarrollado sobre **OpenXava** y **Java Persistence API (JPA)**, proporcionando una interfaz web administrativa robusta para el diseño de pruebas, parametrización de baremos y visualización de resultados, además de exponer una API servlet personalizada para la comunicación con el frontend dinámico en HTML5/CSS3/JavaScript.

---

##  Integrantes del Equipo y Colaboradores

| Nombre Completo | Usuario GitHub | Rol en el Proyecto | Contribuciones Principales |
| :--- | :--- | :--- | :--- |
| **Lesther Galeano** | [@Jospeay](https://github.com/Jospeay) | **Líder de Proyecto / Integración** | Coordinación, diseño de la API Servlet (`ExamenServlet`), integración de CORS y conexión frontend-backend. |
| **Gabriel Orozco** | [@gabrielorozcob](https://github.com/gabrielorozcob) | **DBA & Administrador de Datos** | Diseño del modelo físico/entidades JPA, persistencia de datos con Hibernate y scripts de inicialización SQL (`DbInitializerListener`). |
| **Donald Mercado** | [@JayWFx](https://github.com/JayWFx) | **Desarrollador Backend API** | Implementación del controlador de flujo de examen, lógica de cálculo de puntajes percentiles, y endpoints REST. |
| **Zahid Reyes** | [@rzahidb](https://github.com/rzahidb) | **Desarrollador OpenXava / UI** | Definición y maquetación de las vistas en OpenXava, configuración de relaciones complejas JPA (`BaremoNormativo`, `ParteRazonamiento`). |

---

##  Arquitectura y Tecnologías
- **Core Framework**: [OpenXava v7.x](https://www.openxava.org/) (Model-Driven Development).
- **Persistencia**: Java Persistence API (JPA) 2.x & Hibernate ORM.
- **Base de Datos**: HSQLDB (HyperSQL Database) embebida / PostgreSQL (según perfil de Maven).
- **Servidor de Aplicaciones**: Apache Tomcat 9.x / Embedded Tomcat.
- **REST API**: Servlets nativos de Java EE (`HttpServlet`) con soporte completo de CORS y formato JSON (`javax.json` / `gson`).
- **Construcción y Dependencias**: Apache Maven 3.x.

---

##  Modelo de Datos (Dominio JPA)

El backend define el modelo de negocio necesario para gestionar las evaluaciones psicométricas de razonamiento abstracto y lógico:

1. **`Aspirante`**: Registra la información sociodemográfica de los aplicantes (cédula, nombres, correo, sexo, departamento, tipo de colegio, zona).
2. **`PruebaRazonamiento`**: Define una prueba configurable (código, nombre, duración en segundos, estado activo/inactivo).
3. **`ParteRazonamiento`**: Divide la prueba en secciones o partes temáticas (número, descripción, cantidad de ítems).
4. **`ItemRazonamiento`**: Representa las preguntas individuales (número de ítem, enunciado, tipo de pregunta [Abstracta, Verbal, Lógica, etc.], nivel de dificultad [Fácil, Medio, Difícil], estado activo e imagen opcional).
5. **`OpcionRespuesta`**: Las alternativas de respuesta (Letra [A, B, C, D], texto, orden y bandera `esCorrecta`).
6. **`SesionTest`**: Instancia de ejecución de un aspirante realizando una prueba específica. Registra fecha/hora de inicio, fecha/hora de finalización, respuestas recibidas, estado de la sesión (`INICIADA`, `COMPLETADA`, `EXPIRADA`) y el token de acceso.
7. **`RespuestaUsuario`**: Relaciona cada ítem respondido por el usuario en una sesión con su respectiva opción seleccionada.
8. **`BaremoNormativo`**: Tabla de equivalencias de percentiles de acuerdo con el puntaje directo obtenido y el sexo del aspirante.
9. **`ResultadoEvaluacion`**: Almacena el reporte consolidado final de la prueba, incluyendo el puntaje directo, el percentil calculado y el diagnóstico sugerido.

---

##  API REST (ExamenServlet Endpoints)

La interacción entre el frontend y el backend OpenXava se realiza a través del Servlet `ExamenServlet` expuesto en `/api/exam/*`. Todos los endpoints aplican cabeceras CORS (`Access-Control-Allow-Origin: *`) para permitir clientes externos.

### 1. Registro de Aspirante
* **Endpoint**: `POST /api/exam/register`
* **Content-Type**: `application/x-www-form-urlencoded` o `application/json`
* **Parámetros**: `cedula`, `primerNombre`, `segundoNombre`, `primerApellido`, `segundoApellido`, `correo`, `sexo`, `departamento`, `tipoColegio`, `zona`.
* **Respuesta (JSON)**:
  ```json
  {
    "status": "success",
    "aspiranteId": "UUID-DEL-ASPIRANTE",
    "nombreCompleto": "Nombres Apellidos"
  }
  ```

### 2. Iniciar Sesión de Examen
* **Endpoint**: `POST /api/exam/start`
* **Parámetros**: `aspiranteId`
* **Respuesta (JSON)**:
  ```json
  {
    "status": "success",
    "token": "TOKEN-SESION-UUID",
    "duracionSegundos": 720,
    "preguntas": [
      {
        "id": "UUID-PREGUNTA",
        "numero": 1,
        "enunciado": "¿Qué figura sigue en la secuencia?",
        "tipo": "Abstracta",
        "nivel": "Medio",
        "opciones": [
          { "letra": "A", "texto": "Opción A" },
          { "letra": "B", "texto": "Opción B" }
        ]
      }
    ]
  }
  ```

### 3. Enviar Respuesta de Pregunta
* **Endpoint**: `POST /api/exam/answer`
* **Parámetros**: `token`, `preguntaId`, `letraOpcion`
* **Respuesta (JSON)**:
  ```json
  {
    "status": "success",
    "mensaje": "Respuesta guardada"
  }
  ```

### 4. Finalizar Examen y Obtener Resultados
* **Endpoint**: `POST /api/exam/submit`
* **Parámetros**: `token`
* **Respuesta (JSON)**:
  ```json
  {
    "status": "success",
    "nombreAspirante": "Nombre Completo",
    "aciertos": 18,
    "totalPreguntas": 20,
    "percentil": 85,
    "rango": "Superior al Promedio",
    "fechaFinalizacion": "2026-06-26T01:00:00Z"
  }
  ```

---

## 🛠️ Configuración e Instalación del Servidor

### Requisitos Previos
- **Java JDK 11** o superior instalado.
- **Apache Maven 3.6+**.
- **IDE** (IntelliJ IDEA recomendado / Eclipse con plugins OpenXava).

### Instrucciones de Ejecución (Local)

1. **Clonar el Repositorio**:
   ```bash
   git clone https://github.com/Jospeay/Test_de_Razonamiento_-Forma-B-.git
   cd Test_de_Razonamiento_-Forma-B-
   ```

2. **Construir el Proyecto**:
   ```bash
   mvn clean package
   ```

3. **Ejecutar el Servidor Embebido**:
   En IntelliJ IDEA:
   - Haz clic derecho sobre el proyecto principal.
   - Selecciona **Run 'Test_Razonamiento_B'**.
   - O ejecuta a través de terminal:
     ```bash
     mvn spring-boot:run
     ```
   El backend estará disponible en `http://localhost:8080/Test_Razonamiento_B`.

4. **Acceder a la Consola de Administración**:
   Abre tu navegador e ingresa a `http://localhost:8080/Test_Razonamiento_B`. Utiliza las credenciales por defecto (si están configuradas) o accede directamente al modo desarrollo.
   * *Asegúrate de ir a la sección **Prueba razonamiento** y dar de alta una prueba con código `RAZONAMIENTO_B` en estado activa para habilitar el flujo de la API.*

---

##  Políticas de Contribución y Git Flow

Para mantener un historial de commits limpio y simular un flujo de trabajo real de 4 personas:
- **`main`**: Rama protegida que contiene únicamente las versiones estables entregables del software.
- **`develop`**: Rama de integración donde se consolidan las funcionalidades desarrolladas.
- **Feature Branches**: Cada integrante trabaja en ramas con el prefijo `feature/[nombre-tarea]`. Por ejemplo:
  - `feature/jpa-entities` (Gabriel)
  - `feature/examen-api-servlet` (Lesther & Donald)
  - `feature/openxava-views` (Zahid)

---
*Este proyecto es para fines estrictamente académicos como parte de la evaluación final del curso de Programación Web Avanzada en la UAM.*
