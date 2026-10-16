# Known Issues & Limitations

## Phase 2 Status
- **Shizuku Integration**: BLOCKED. The density engine and launch sequence are disabled until Phase 3.
- **Background Monitoring**: NOT IMPLEMENTED. Session tracking and automatic density restoration will be built in Phase 4.
- **Recovery Journal**: NOT IMPLEMENTED. Durable state for crash/reboot recovery arrives in Phase 3/4.
- **Live Density Read**: The app cannot currently read the device's active density. Diagnostics screen correctly reports "UNREADABLE".

## Device Specifics (iQOO Z9s 5G / Android 16)
- Termux cannot access the `window` service. All density operations MUST route through Shizuku (shell UID).
- Aggressive background process limits expected; foreground service architecture required for Phase 4 monitor.
