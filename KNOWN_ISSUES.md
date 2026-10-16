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

## Shizuku binder delivery behavior (verified on device, 9:38-9:41 screenshots)
- NEONFORGE launched while Shizuku was stopped keeps a dead binder (pingBinder false)
  until process restart or resume; UI correctly showed NOT RUNNING and therefore hid
  GRANT ACCESS (grant only makes sense with a live binder).
- Owner authorized NEONFORGE manually via Shizuku > Application management (toggle ON): valid.
- Fix shipped: privilege status refresh on every ON_RESUME + manual REFRESH STATUS button
  + per-state guidance text on the dashboard.

## API version decision (evidence: CI javap summary, run of commit 4fdccb5)
- api 13.1.5: Shizuku.newProcess NOT in public surface; ShizukuRemoteProcess ctors
  package-private; IShizukuService has no process/shell methods. Public exec path in
  13.1.5 = user service mechanism only.
- Action: pinned dev.rikka.shizuku:{api,provider} to 12.2.0 (public newProcess generation).
  Verification gates: CI compile + on-device READ DENSITY returning real numbers.
- Fallback if 12.2.0 also hides newProcess: implement user-service mechanism or adopt
  an externally documented method (owner-approved research path).

## Binder delivery: active-pull strategy (replaces passive wait)
- Verified: server running + NEONFORGE authorized, yet client pingBinder()=false
  (op log exit=-1 series, 10-16 22:23-22:24). Delivery chain defect isolated to client side.
- New behavior: on every resume with dead binder, NEONFORGE reflectively invokes the
  Shizuku provider's public requestBinder* entry point (compile-safe across versions)
  and Diagnostics > "Binder Delivery Chain" reports: provider class present,
  own provider registered, Shizuku provider visible, pull result string.
- CI now prints provider 12.2.0 public surface and merged-manifest shizuku lines
  into the run Summary for permanent evidence.
