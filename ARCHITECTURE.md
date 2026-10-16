# NEONFORGE Architecture

## Layers
1. **UI Layer (Jetpack Compose)**: Cinematic neon design system, navigation, and state observation.
2. **Data Layer**: `ProfileRepository` and `SettingsRepository` backed by Jetpack DataStore (Preferences). Serialization via `kotlinx-serialization`.
3. **Domain/Policy Layer**: `DensityPolicy` for strict validation of DPI inputs.
4. **System Integration Layer (Pending Phase 3)**: Shizuku bridge for privileged `wm density` execution, session monitoring, and recovery journal.

## State Management
- Unidirectional data flow. Repositories expose `Flow` of state.
- Compose screens collect state via `collectAsState`.
- User actions trigger `suspend` functions on repositories.

## Build & CI
- Remote builds via GitHub Actions (JDK 17, Gradle 8.11.1, AGP 8.10.0).
- Unit tests run on JVM in CI before APK assembly.
