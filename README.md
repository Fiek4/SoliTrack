# SoliTrack 📋

Aplicación móvil Android para la **gestión y seguimiento de solicitudes de clientes** de una pequeña empresa de servicios informáticos y digitales.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, sección 009V) · Evaluación Parcial 2.

## 👥 Integrantes

| Nombre | Rol principal |
|---|---|
| Fernando Arce | Datos y lógica: Room, Repository, ViewModel, validaciones, pruebas |
| Raúl Barraza | Interfaz: tema, login, menú, navegación, formulario, cámara, animaciones |

## 🎯 Problema que resuelve

Los clientes envían sus solicitudes por WhatsApp y estas se pierden en conversaciones extensas: no quedan registradas formalmente, no tienen código, estado ni evidencia. SoliTrack permite **registrar cada solicitud con un código único de seguimiento** y mantenerla disponible para su consulta.

Esta entrega implementa el **módulo de Solicitudes**.

## ✅ Funcionalidades

> 🚧 Proyecto en desarrollo: se marcan a medida que se implementan.

- [ ] Inicio de sesión con cuenta ficticia y validación de campos
- [ ] Menú principal
- [ ] Listado de solicitudes guardadas (código, título, categoría y estado)
- [ ] Registro de nuevas solicitudes con:
  - Título, descripción, categoría y servicio relacionado
  - Validaciones visuales por campo (borde, ícono y mensaje de error)
  - Código único autogenerado (SOL-0001, SOL-0002, …)
  - Estado inicial **"Recibida"** y fecha/hora de creación
- [ ] Edición de solicitudes existentes
- [ ] **Cámara**: captura de una foto ficticia como evidencia, vinculada a la solicitud
- [ ] Persistencia local con **Room** (los datos se mantienen al reiniciar la app)
- [ ] Lista reactiva: se actualiza automáticamente al registrar o modificar
- [ ] Animaciones: transiciones entre pantallas, errores animados, confirmación al guardar
- [ ] Pruebas unitarias de las reglas de validación

## 🛠️ Tecnologías

- Kotlin + Jetpack Compose + Material Design 3
- Arquitectura **MVVM** (View → ViewModel → Repository → DAO → Room)
- `StateFlow` + `UiState` para la gestión de estado
- Room (persistencia local) con KSP
- Navigation Compose
- Coil (visualización de imágenes)
- JUnit (pruebas unitarias)
- Git + GitHub · Trello

## 📁 Estructura del proyecto

```
com.example.solitrack
├── MainActivity.kt
├── SoliTrackApp.kt
├── model        → SolicitudEntity, Categoria, EstadoSolicitud,
│                  SolicitudUiState, LoginUiState
├── data         → SolicitudDao, AppDatabase (Room)
├── repository   → SolicitudRepository, AuthRepository
├── navigation   → AppNavigation
├── ui
│   ├── screen     → LoginScreen, MenuScreen,
│   │                ListaSolicitudesScreen, SolicitudFormScreen
│   ├── components → EstadoChip, CamaraUtils
│   └── theme      → Color, Theme, Type
└── viewmodel    → LoginViewModel, SolicitudViewModel, SolicitudValidator
```

## ▶️ Cómo ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/Fiek4/SoliTrack.git
   ```
2. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
3. Ejecutar en un emulador o dispositivo con **Android 7.0 (API 24)** o superior.
4. Iniciar sesión con la cuenta de prueba:
   - Usuario: `cliente`
   - Contraseña: `1234`

> Todos los datos, usuarios e imágenes de la app son **ficticios**.

## 📋 Planificación

Tablero de Trello: https://trello.com/b/huUjbndt/solitrack-dsy1105

## 📸 Capturas

_Se agregarán capturas de la aplicación al finalizar el desarrollo._
