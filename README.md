# Sejmierz — Compose Multiplatform (Kotlin Multiplatform)

## 1. Purpose of the application

Sejmierz consumes the Sejm public API to browse MPs, votings, and clubs, and to run structured performance telemetry. This repository realises the same product as a **Kotlin Multiplatform / Compose Multiplatform** client with shared business and UI logic, and thin native hosts for Android and iOS.

The CMP stack is designed for paradigm comparison against the pure native iOS and Android codebases: shared Domain and Data, shared Compose UI, platform-specific engines and sampling.

## 2. Architectural style

The workspace uses **Clean Architecture** across KMP source sets:

| Module | Source sets | Role |
|--------|-------------|------|
| `:domain` | `commonMain` (+ small platform time helpers) | Models, use cases, repositories, paging, benchmark core |
| `:data` | `commonMain` | Ktor client, SQLDelight, repositories, settings, metric sinks |
| `:ui` | `commonMain` (+ `androidMain` / `iosMain` for locale effects) | Compose Multiplatform UI, Decompose MVI, navigation shell |
| `:app` | Android | Android host (Application, MainActivity, OkHttp, JankStats) |
| `:iosApp` | iOS framework | `MainViewController`, Darwin HTTP, Native SQLite, iOS samplers |

Presentation follows **MVI** with Decompose components and `StateFlow`, identical in spirit to native Android. Shared UI is Compose Multiplatform Material 3.

## 3. Module layout

```
SejmierzCMP/
├── domain/     # KMP common domain
├── data/       # KMP common data (+ SQLDelight)
├── ui/         # CMP common UI + feature packages
├── app/        # Android application host
└── iosApp/     # iOS framework / entry
```

UI features under `ui/.../feature/`: `mps`, `mpdetail`, `votings`, `votingdetail`, `clubs`, `clubdetail`, `benchmark`, `settings`. Navigation lives in `ui/.../navigation/`.

## 4. Navigation and interaction model

The shared shell matches the native contract:

1. Four bottom destinations: Posłowie, Głosowania, Kluby, Telemetria  
2. Nested Decompose stacks for the three content domains  
3. Telemetry as a tab root  
4. Right-side settings drawer for language and online/offline mode  

Compose resources (`composeResources/values` and `values-pl`) supply localised strings. Language changes are applied through expect/actual helpers (`applyAppLanguage`).

## 5. Data and concurrency

- **Networking:** Ktor multiplatform (OkHttp on Android, Darwin on iOS) with network timing instrumentation.
- **Persistence:** SQLDelight with Android SQLite and Native SQLite drivers.
- **Settings:** in-memory `SettingsRepository` shared across platforms.
- **DI:** Koin in common and platform modules.
- **Async:** Kotlin Coroutines / Flow across all layers.

## 6. Telemetry and benchmarking

Benchmark contracts reside in `:domain`. Platform hosts inject run-context providers and frame/display/CPU samplers. Shared UI tracks screen display timing and scroll jank; hosts record cold start, main-thread stalls, and CPU avg/peak under the same metric names as the native apps. The Telemetria tab exposes configuration, export, and statistics identically to the native clients.

## 7. Principal technologies and dependencies

| Concern | Technology (representative versions) |
|---------|--------------------------------------|
| Language | Kotlin 2.1, KMP |
| UI | Compose Multiplatform 1.7.3, Material 3 |
| Navigation | Decompose 3.2 |
| Networking | Ktor 3.0 |
| Persistence | SQLDelight 2.0 |
| DI | Koin 4.0 |
| Android host | AGP 8.7, minSdk 31 |
| iOS host | Kotlin/Native framework consumed by Xcode |

Version catalogue: `gradle/libs.versions.toml`.

## 8. Design patterns employed

- Clean Architecture with KMP source sets
- MVI + Decompose navigation
- Repository and use-case patterns
- Expect/actual for platform services (language, samplers, engines)
- Shared UI with platform chrome hosts
- Explicit online/offline benchmarking mode

## 9. Build and run

**Android host:**

```bash
./gradlew :app:assembleDebug
```

**iOS framework:** build the `:iosApp` Kotlin/Native target from a macOS environment and embed it via the Xcode project associated with `iosApp`.

JDK 17 and Android SDK (`local.properties`) are required for Android builds.
