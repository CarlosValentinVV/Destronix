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
├── data/           # configuración de fuentes, DTOs/API (Retrofit), Room, mappers y repositorios
├── di/             # contenedor de dependencias manual
├── presentation/   # navegación, pantallas, componentes y ViewModels
└── ui/theme/       # paleta, tipografía y tema Material 3
```

La dependencia siempre apunta hacia `domain`: la UI llama a casos de uso, los casos de uso a interfaces de repositorio y la capa `data` implementa esas interfaces. Así se pueden sustituir la API o la base de datos sin tocar la UI.

### Decisiones técnicas

- **Retrofit + Gson** con interceptor de rate limiting (Jikan permite 3 peticiones/segundo) y caché HTTP de 10 MB.
- **Fuentes configurables** en `assets/anime_sources.json` (ignorado por Git): la API y los vídeos se leen del JSON en tiempo de ejecución, sin tocar Kotlin. Si el archivo falta o no es válido, se usa Jikan por defecto.
- **Room** para biblioteca e historial de búsqueda; expone `Flow` para que la UI reaccione a cambios.
- **`AppResult<T>`** para modelar carga, éxito (con origen remoto/local) y error de dominio.
- **Inyección manual** mediante `AppContainer`, sin frameworks de anotación, para mantener un grafo explícito y pruebas rápidas.
- **Reproductor Media3** con vídeos públicos de demostración: Jikan no facilita streams legales de episodios. Al terminar un episodio se actualiza el progreso de la biblioteca.
- **Material 3** con paleta propia (azul noche, violeta y cian), soporte para tema claro/oscuro y dynamic color opcional.
- **DataStore Preferences** para persistir tema, modo de color y borrado del historial; el tema se aplica de forma reactiva desde la raíz de la app.
- **GitHub Actions** compila el APK de depuración y ejecuta las pruebas unitarias en cada pull request.

## Fuentes configurables

La app lee en tiempo de ejecución `app/src/main/assets/anime_sources.json`, que **no se versiona**. Para apuntar a otras direcciones:

1. Copia `anime_sources.example.json` como `anime_sources.json` en la misma carpeta.
2. Edita `apiBaseUrl` de cada fuente y, si procede, `streamBaseUrl` y `websiteUrl`.
3. Elige la fuente activa con `activeSourceId`. Las entradas con `enabled: false` se ignoran.
4. Genera el APK con `./gradlew :app:assembleDebug`; no hace falta modificar código Kotlin.

La API configurada debe responder los mismos endpoints que Jikan v4 (`/top/anime`, `/seasons/now`, `/anime/{id}`, …). `streamBaseUrl` solo afecta a los vídeos de demostración del reproductor. Si el archivo no existe o no es válido, se usa Jikan y los vídeos públicos de prueba, de modo que las compilaciones limpias y la CI siguen funcionando.

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
