# Known Issues & Limitations

## Phase 2 Status
- **Shizuku Integration**: BLOCKED. The density engine and launch sequence are disabled until Phase 3.
- **Background Monitoring**: NOT IMPLEMENTED. Session tracking and automatic density restoration will be built in Phase 4.
- **Recovery Journal**: NOT IMPLEMENTED. Durable state for crash/reboot recovery arrives in Phase 3/4.
- **Live Density Read**: The app cannot currently read the device's active density. Diagnostics screen correctly reports "UNREADABLE".

## Device Specifics (iQOO Z9s 5G / Android 16)
- Termux cannot access the `window` service. All density operations MUST route through Shizuku (shell UID).
- Aggressive background process limits expected; foreground service architecture required for Phase 4 monitor.

## Phase 3 in-progress (verified via CI compiler diagnostics, runs #6/#7)
- `Shizuku.newProcess(String[], String[], String)` is PRIVATE in dev.rikka.shizuku:api:13.1.5
  (e: ShizukuBridge.kt:42:45 "it is private in 'rikka/shizuku/Shizuku'").
- `ShizukuBridge.runCommand()` is temporarily an honest failure stub (exit code -4).
  It will be replaced with the supported public API once the CI javap diagnostic
  (Maven Central AAR inspection) reports the real public surface.
- All other Shizuku calls in ShizukuBridge are compiler-verified public API.
- No density operation has been performed on the device at any point.
