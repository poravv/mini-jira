# Estructura del código y función de sus partes.
## mini-jira

Mini Jira es el proyecto en el que estamos trabajando con el objetivo de desarrollar conocimientos de desarrollo de software.

### `.agents`

Contiene guías para los agentes de IA que trabajan en el proyecto.

- `skills/backend-feature/`: guía para agregar módulos o endpoints al backend.
- `skills/frontend-feature/`: guía para agregar funcionalidades al frontend.
- `skills/pr-ready/`: checklist para preparar un Pull Request.

### `.claude`

Contiene guías equivalentes para Claude Code.

### `.github`

Contiene configuraciones y plantillas utilizadas por GitHub.

- `pull_request_template.md`: plantilla que aparece al crear un Pull Request y solicita describir el problema, la solución, los endpoints modificados, los cambios en la base de datos y las pruebas realizadas.

### `apps`

Contiene el frontend y el backend: el código, el diseño y la estructura de la aplicación.

#### Backend

Es la parte que funciona detrás de la interfaz visual. Procesa solicitudes, aplica reglas y consulta la base de datos.
- `.maven-home/`: carpeta local relacionada con Maven. No contiene funcionalidades de Mini Jira.
- `.mvn/`: configuración del Maven Wrapper.
- `src/`: significa *source* (código fuente) y contiene el código de la aplicación.
  - main: Contiene el backend que se ejecuta 
    - java:
      - auth: ingreso y autenticación de usuarios 
        - config: configuración de seguridad 
        - controller: endpoint de inicio de sesión 
        - dto: datos de autenticación
        - exception: errores de autenticación 
        - service: JWT y lógica de autenticación 
      - common: 
        - GlobalExceptionHandler.java: maneja los errores generales de la aplicación 
      - issue: 
        - controller: endpoints de incidencias
        - dto: datos de entrada y salida
        - entity: incidencia, estado y prioridad
        - exception: errores de incidencias
        - mapper: convierte entidades en DTO.
        - repository: acceso a base de datos 
        - service: logica de incidencias 
      - proyecto:
        - controller: endpoints de proyectos
        - dto: datos de entrada y salida de proyectos y miembros
        - entity: entidad Proyecto
        - exception: errores de proyectos y miembros
        - mapper: convierte entidades en DTO
        - repository: acceso a los datos de proyectos
        - service: lógica de proyectos y miembros

      - user:
        - controller: endpoints de usuarios
        - dto: datos de entrada y salida de usuarios
        - entity: entidad User y roles
        - exception: errores de usuarios
        - mapper: convierte entidades en DTO
        - repository: acceso a los datos de usuarios
        - service: lógica de usuarios

      - weather:
        - controller: endpoint del clima
        - dto: datos meteorológicos
        - exception: errores relacionados con el clima
        - repository: consulta al servicio externo del clima
        - service: lógica de la consulta meteorológica

      - MiniJiraApplication.java: inicia la aplicación de Spring Boot

    - resources: recursos
      - application.yml: configuración del backend
      - db: data base
        - changelog
          - 001-create-issues-table.yaml
          - 002-create-usuario-table.yaml
          - 003-insert-admin-user.yaml
          - 004-create-proyecto-tables.yaml
          - 005-add-issue-project-assignee.yaml
          - 006-add-issue-project-assignee-indexes.yaml
          - db.changelog-master.yaml: organiza las migraciones de la base de datos

  - test
    - java
      - com/minijira : pruebas automáticas de los módulos

Un **DTO** (*Data Transfer Object*) es un objeto utilizado para transportar datos entre las distintas capas o entre el frontend y el backend.

- `target/`: carpeta generada automáticamente por Maven al compilar el backend.
  - `classes/`: código Java compilado.
  - test-classes: pruebas compiladas 
  - surefire-reports: resultados de las pruebas
  - *.jar: backend empaquetado para ejecutarse

- `.dockerignore`: indica qué elementos no deben copiarse al construir la imagen de Docker.

- `Dockerfile`: contiene instrucciones para construir una imagen de Docker.

- `mvnw`: Maven Wrapper para Linux y macOS; permite usar la misma versión de Maven.
- `mvnw.cmd`: script equivalente para Windows.
- `pom.xml`: archivo principal de configuración de Maven. POM significa *Project Object Model* y define dependencias, versión de Java y tareas de compilación.

- `README.md`: indica cómo se compone el backend, qué herramientas utiliza y cómo usarlo.

#### Frontend

Es la parte visual de Mini Jira. Está construida con Angular, TypeScript, HTML y CSS.
- `.angular/`: caché que Angular crea automáticamente.
- `.vscode/`: configuraciones de Visual Studio Code para el proyecto.
- `dist/`: resultado compilado y listo para publicar del frontend.
- `node_modules/`: librerías y paquetes que necesita Angular para funcionar.
- `public/`: archivos públicos y estáticos.
- `src/`: código fuente del frontend.
  - `app/`
    - `features/`: funcionalidades principales de la aplicación.
      - auth
        - login-page: contiene el HTML, CSS y TypeScript de la pantalla de login.
        - auth.model.ts: define la forma de los datos de autenticación.
        - auth.routes.ts: define las rutas de autenticación.
        - auth.service.ts: envía los datos al backend mediante POST /api/auth/login.
        - auth.service.spec.ts: contiene pruebas automáticas del servicio.
        - user-session.service.ts: guarda, recupera y cierra la sesión del usuario.

      - issues
        - issue-form: permite crear o editar incidencias.
        - issue-list: muestra las incidencias.
        - issue.model.ts: define la estructura de los datos de una incidencia.
        - issue.service.spec.ts: pruebas automatizadas del servicio.
        - issue.service.ts: se comunica con la API de incidencias.
        - issues.routes.ts: define las rutas de incidencias.

      - projects
        - project-detail: muestra la información de un proyecto, sus miembros y sus incidencias.
        - project-form: permite crear o editar un proyecto.
        - project-list: muestra la lista de proyectos.
        - project.model.ts: define la estructura de los datos del proyecto.
        - project.service.spec.ts: contiene las pruebas automáticas del servicio.
        - project.service.ts: se comunica con la API de proyectos y administra miembros.
        - projects.routes.ts: define las rutas de proyectos.

      - users
        - user-form: permite crear o editar usuarios.
        - user-list: muestra la lista de usuarios.
        - user.model.ts: define la estructura de los datos de usuario.
        - user.service.spec.ts: contiene las pruebas automáticas del servicio.
        - user.service.ts: se comunica con la API de usuarios.
        - users.routes.ts: define las rutas de usuarios.
       
      - weather
        - weather-card: componente visual que muestra el clima.
        - weather.model.ts: define la estructura de los datos meteorológicos.
        - weather.service.spec.ts: contiene pruebas automáticas del servicio.
        - weather.service.ts: solicita el clima al backend mediante GET /api/weather.

    - admin.guard.ts: comprueba que el usuario tenga el rol ADMIN.

    - app.component.css: contiene los estilos del componente principal.

    - app.component.html: es la estructura principal de la aplicación 

    - app.component.ts: controla el componente principal AppComponent.

    - app.config.ts: contiene la configuración global de Angular, como router, HttpClient e interceptores.

    - app.routes.ts: define las rutas principales de la aplicación 

    - auth.guard.ts: comprueba que exista una sesión iniciada antes de permitir el acceso.

    - auth.interceptor.spec.ts: contiene las pruebas automaticas del interceptor

    - auth.interceptor.ts: interviene en las solicitudes HTTP y agrega el token JWT

  - index.html: es el HTML base que carga primero el navegador.
  - main.ts: es el punto de entrada de Angular; inicia la aplicación y carga AppComponent.
  - styles.css: contiene los estilos globales del frontend.
- .dockerignore: archivos que Docker no debe copiar.
- .editorconfig: reglas de formato del código.
- .gitignore: archivos que Git debe ignorar.
- angular.json: configuración general de Angular.
- Dockerfile: instrucciones para construir el contenedor del frontend.
- nginx.conf: configuración de nginx para servir Angular.
- package-lock.json: versiones exactas de las dependencias instaladas.
- package.json: dependencias y comandos del frontend.
- proxy.conf.json: redirige las solicitudes /api hacia el backend durante el desarrollo.
- README.md: documentación del frontend.
- tsconfig.app.json: configuración de TypeScript para la aplicación.
- tsconfig.json: configuración general de TypeScript.
- tsconfig.spec.json: configuración de TypeScript para las pruebas.
### docs
- `ARCHITECTURE.md`: explica la arquitectura técnica del sistema.
- `CHECKLIST.md`: muestra qué tareas están terminadas y cuáles faltan.
- `definicion-proyecto-colaborativo-dev-jr.md`: contiene la definición general del proyecto y la división de tareas.
- `ESTRUCTURA.md`: describe la estructura de carpetas y archivos.
- `GIT-FLOW.md`: explica cómo trabajar con ramas, commits y Pull Requests.
- `RESTCLIENT-PROXY.md`: explica cómo el backend consulta servicios externos, como el clima.
- `RULE-STATUS-PRIORITY.MD`: documenta las reglas para cambiar el estado y la prioridad de las incidencias.

### `.env`

Contiene valores reales de configuración, como contraseñas, puertos y `JWT_SECRET`. No debe subirse a Git.

### `.env.example`

Muestra qué variables necesita el proyecto, sin incluir valores sensibles. Sirve como plantilla.

### `.gitignore`

Indica a Git qué archivos y carpetas debe ignorar.

### `AGENTS.md`

Contiene instrucciones para los agentes de IA que trabajan en el proyecto.

### `CLAUDE.md`

Contiene instrucciones específicas para Claude Code.

### `CONTRIBUTING.md`

Define las reglas para colaborar: ramas, commits, pruebas y Pull Requests.

### `docker-compose.yml`

Coordina los contenedores del proyecto: backend, frontend, PostgreSQL y MongoDB.

### `README.md`

Es la documentación inicial del proyecto: descripción, instalación, ejecución y uso.
