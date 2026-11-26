# Destronix

Aplicación Android para descubrir, organizar y ver anime. Construida con **Jetpack Compose**, **Material 3** y arquitectura limpia por capas. Los datos del catálogo proceden de la API pública [Jikan v4](https://jikan.moe/) (MyAnimeList), sin credenciales.

## Estado del proyecto

| Módulo | Estado |
| --- | --- |
| Arquitectura, datos y tema | ✅ implementado |
| Pantalla de inicio | ✅ implementado |
| Búsqueda y filtros | ✅ implementado |
| Detalle de anime | ✅ implementado |
| Biblioteca personal | ✅ implementado |
| Reproductor | ✅ implementado |
| Ajustes y pruebas finales | ✅ implementado |

Cada fase se entrega en su propia rama `feature/*` con una Pull Request documentada.

## Arquitectura

```
app/src/main/java/com/project/destronix/
├── core/           # Result, errores de dominio, dispatchers
├── domain/         # modelos, contratos de repositorio y casos de uso
├── data/           # DTOs/API (Retrofit), Room, mappers y repositorios
├── di/             # contenedor de dependencias manual
├── presentation/   # navegación, pantallas, componentes y ViewModels
└── ui/theme/       # paleta, tipografía y tema Material 3
```

La dependencia siempre apunta hacia `domain`: la UI llama a casos de uso, los casos de uso a interfaces de repositorio y la capa `data` implementa esas interfaces. Así se pueden sustituir la API o la base de datos sin tocar la UI.

### Decisiones técnicas

- **Retrofit + Gson** con interceptor de rate limiting (Jikan permite 3 peticiones/segundo) y caché HTTP de 10 MB.
- **Room** para biblioteca e historial de búsqueda; expone `Flow` para que la UI reaccione a cambios.
- **`AppResult<T>`** para modelar carga, éxito (con origen remoto/local) y error de dominio.
- **Inyección manual** mediante `AppContainer`, sin frameworks de anotación, para mantener un grafo explícito y pruebas rápidas.
- **Reproductor Media3** con vídeos públicos de demostración: Jikan no facilita streams legales de episodios. Al terminar un episodio se actualiza el progreso de la biblioteca.
- **Material 3** con paleta propia (azul noche, violeta y cian), soporte para tema claro/oscuro y dynamic color opcional.
- **DataStore Preferences** para persistir tema, modo de color y borrado del historial; el tema se aplica de forma reactiva desde la raíz de la app.
- **GitHub Actions** compila el APK de depuración y ejecuta las pruebas unitarias en cada pull request.

## Requisitos y ejecución

- Android Studio con SDK 37 instalado (mínimo `minSdk = 24`).
- JDK 21.
- `./gradlew :app:assembleDebug` para compilar.
- `./gradlew test` para ejecutar pruebas unitarias.

La app necesita conexión a internet para cargar el catálogo; la biblioteca personal funciona sin conexión.

## Pruebas

Las pruebas unitarias cubren mapeo de DTOs, conversiones de error, repositorios con servicios falsos y los estados de todos los ViewModels, sin depender de la red. Se ejecutan con `./gradlew :app:testDebugUnitTest` y también en CI.

## Licencia de datos

Los datos y las imágenes pertenecen a MyAnimeList y se sirven a través de Jikan. Este proyecto es una demostración educativa y no afirma ser un proveedor oficial de contenido.
