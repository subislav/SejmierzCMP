# Sejmierz — Compose Multiplatform (Kotlin Multiplatform)

## 1. Purpose of the application

Sejmierz is a client application for the public parliamentary API of the Sejm of the Republic of Poland (`api.sejm.gov.pl`, current term `term10`). The product exposes three primary information domains—Members of Parliament (MPs), votings, and parliamentary clubs—together with an integrated telemetry (benchmarking) surface used for comparative performance evaluation across mobile paradigms.

This repository realises the same product as a **Kotlin Multiplatform / Compose Multiplatform** client: shared business and UI logic in common source sets, with thin native hosts for Android and iOS. The CMP stack is designed for paradigm comparison against the pure native iOS and Android codebases—shared Domain, Data, and Compose UI, with platform-specific engines and sampling.

## 2. Architectural style

The workspace uses **Clean Architecture** across KMP source sets:

| Module | Source sets | Role |
|--------|-------------|------|
| `:domain` | `commonMain` (+ small platform time helpers) | Models, use cases, repositories, paging, benchmark core |
| `:data` | `commonMain` | Ktor client, SQLDelight, repositories, settings, metric sinks |
| `:ui` | `commonMain` (+ `androidMain` / `iosMain` for locale effects) | Compose Multiplatform UI, Decompose MVI, navigation shell |
| `:app` | Android | Android host (`Application`, `MainActivity`, OkHttp engine, JankStats) |
| `:iosApp` | Kotlin/Native + Xcode | Shared framework (`SejmierzShared`), Darwin HTTP, Native SQLite, iOS samplers, Swift UIKit host |

Dependency direction is inward: hosts and UI depend on Data/Domain; Domain has no UI dependencies.

Presentation follows **MVI** with Decompose components and `StateFlow`, identical in spirit to the native Android client. Shared UI is Compose Multiplatform Material 3. The selected `AppLanguage` is published via settings, bridged into Compose through `LocalAppLocale` (expect/actual), and applied so that a language change updates the entire interface—not only the tab bar.

## 3. Module layout

```
SejmierzCMP/
├── domain/          # KMP common domain (+ androidMain / iosMain PlatformTime)
├── data/            # KMP common data (+ SQLDelight)
├── ui/              # CMP common UI + feature packages (+ LocalAppLocale actuals)
├── app/             # Android application host + mipmap / adaptive icons
├── iosApp/
│   ├── src/iosMain/ # Kotlin/Native framework entry, DI, iOS samplers
│   ├── apple/       # Swift UIKit host (MainViewController bridge, Info.plist, assets)
│   ├── Configuration/
│   └── iosApp.xcodeproj/
├── branding/        # Shared brand assets (e.g. sejmierz-logo.png)
└── gradle/          # Version catalogue (libs.versions.toml)
```

UI features under `ui/.../feature/`: `mps`, `mpdetail`, `votings`, `votingdetail`, `clubs`, `clubdetail`, `benchmark`, `settings`. Navigation lives in `ui/.../navigation/`.

The iOS Xcode target embeds and signs the static Kotlin/Native framework `SejmierzShared` via the Gradle task `embedAndSignAppleFrameworkForXcode`, then presents shared Compose UI from `MainViewController`.

## 4. Navigation and interaction model

The shared shell matches the native contract:

1. Four bottom destinations: Posłowie, Głosowania, Kluby, Telemetria  
2. Nested Decompose stacks for the three content domains (independent stacks prevent cross-tab leakage)  
3. Telemetry as a tab root (not a modal shortcut)  
4. Right-side settings drawer for language (PL/EN) and online/offline data-source mode  

Compose resources (`composeResources/values` and `values-pl`) supply localised strings. Language changes flow through settings → `LocalAppLocale` / `applyAppLanguage` so list screens, detail screens, errors, and tab labels recompose together.

## 5. Data and concurrency

- **Networking:** Ktor multiplatform (OkHttp on Android, Darwin on iOS) with network timing instrumentation feeding the benchmark pipeline.
- **Persistence:** SQLDelight with Android SQLite and Native SQLite drivers; local data sources for MPs, votings, and clubs.
- **Repository policy:** online-first with offline mode selectable through settings (explicit data-source mode). Mode changes notify listening screens and trigger refresh.
- **Settings:** in-memory `SettingsRepository` shared across platforms (`DataSourceMode`, `AppLanguage`, `BenchmarkConfig`).
- **DI:** Koin composition roots in Android (`:app`) and iOS (`IosAppModule`).
- **Async:** Kotlin Coroutines / Flow across all layers; Decompose components collect into UI state.

## 6. Telemetry and benchmarking

Benchmarking is a first-class concern. The Domain layer defines metric sinks, runners, statistical summarisation, and CSV export (`MetricCsvExporter`). Platform hosts inject run-context providers and frame/display/CPU/memory samplers.

Shared UI instruments screen display timing and scroll jank; hosts record cold start (`app.coldStart`), main-thread stalls (>16 ms), and CPU avg/peak during scroll and parse under the same metric names as the native apps. Configuration (iteration and warmup counts), CSV export, and metric clearing are available on the Telemetria tab. Clearing updates both the in-memory metric store and the visible sample count/statistics immediately.

**CSV export paths** (aligned with native clients):

- **Android:** app-private `filesDir/benchmark-exports/`
- **iOS:** app sandbox `Documents/benchmark-exports/` (directory created on export; Files app sharing enabled via `UIFileSharingEnabled`)

Global tags include platform id (`CMP-Android` / `CMP-iOS`), device, OS version, build type, a dynamic network type (WIFI / CELLULAR / …), and a per-session run id.

## 7. Principal technologies and dependencies

| Concern | Technology (representative versions) |
|---------|--------------------------------------|
| Language | Kotlin 2.1, KMP |
| UI | Compose Multiplatform 1.7.3, Material 3 |
| Navigation / MVI | Decompose 3.2, StateFlow |
| Networking | Ktor 3.0 (OkHttp / Darwin engines) |
| Persistence | SQLDelight 2.0 |
| DI | Koin 4.0 |
| Android host | AGP 8.7, minSdk 31 |
| iOS host | Kotlin/Native static framework `SejmierzShared` + Xcode UIKit app |
| Localisation | Compose Multiplatform resources (EN/PL) + `LocalAppLocale` |

Version catalogue: `gradle/libs.versions.toml`.

## 8. Design patterns employed

- Clean Architecture with KMP source sets
- MVI + Decompose navigation (typed stacks per tab)
- Repository and use-case patterns
- Expect/actual for platform services (language/locale, samplers, engines, wall-clock helpers)
- Shared UI with thin platform chrome hosts (Android Activity / iOS `UIViewController`)
- Explicit online/offline benchmarking mode (strategy-like data-source switching)
- Composition root DI (Koin modules per host)
