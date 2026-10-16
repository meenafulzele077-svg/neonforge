# DEVICE_AUDIT.md — NEONFORGE

Audit date: 2026-10-16
Source: Termux diagnostic script (owner-executed), screenshot transcription + probe log.

## Verified device facts
- Model (ro.product.model): I2403
- Android version: 16 (SDK 36)
- Display ID: PD2406GF_EX_A.16.2.15.0.W30
- Fingerprint: iQOO/I2403i/I2403:16/BP2A.250605.031.A3/compiler260911174032:user/release-keys
- Termux: aarch64, OpenJDK 21.0.12, git 2.56.0
- Repository at start: EMPTY (bootstrapped from zero)

## Environment notes
- `wm` is not on Termux PATH; system-binary probe result appended below.
- Privileged density operations are planned exclusively via Shizuku (shell UID), never Termux.

## Limitations of evidence
- Density baseline and Shizuku state: see appended probe log.
- Shizuku *running/authorized* state cannot be determined from Termux; verified later in-app.
=== PROBE A: wm binary + read-only density baseline ===
wm not in Termux PATH (expected)
-rwxr-xr-x. 1 root shell 33 Jan  1  2009 /system/bin/wm
cmd: Can't find service: window
--- cmd window probe ---
cmd: Can't find service: window

=== PROBE B: Shizuku presence ===
Shizuku package NOT installed

=== PROBE C: debugging-related global settings (read-only) ===
adb_wifi_enabled=cmd: Failure calling service settings: Failed transaction (2147483646)
development_settings_enabled=cmd: Failure calling service settings: Failed transaction (2147483646)

## Probe interpretation (Phase 1 close, 2026-10-16)
- /system/bin/wm exists (33-byte wrapper) but from Termux both `wm` and `cmd window`
  fail with "cmd: Can't find service: window" (service-manager/SELinux restriction on
  untrusted_app). VERIFIED: Termux can neither read nor write display density.
  All density operations must run through Shizuku (shell UID).
- Shizuku: NOT installed at audit time. Owner installation + Wireless Debugging start required.
- Global settings reads from Termux fail (Failed transaction 2147483646); wireless-debug
  state must be confirmed in Settings UI or the Shizuku app.
- Repository: https://github.com/meenafulzele077-svg/neonforge.git, branch main,
  initial commit bc309b8 pushed successfully.

## T1 self-adb privilege proof (VERIFIED, device time ~12:47)
- adb pair 10.85.123.125:36175 -> Successfully paired [guid=adb-10BF2H1MMS006E9-OLMQn5]
- adb connect 10.85.123.125:36205 -> connected
- adb shell id -> uid=2000(shell) gid=2000(shell) context=u:r:shell:s0
- BASELINE TRUTH: Physical density 440, no initial override.
- Cycle verified: apply 512 -> "Override density: 512"; reset -> override removed;
  physical stayed 440 throughout.
- Channel: Termux android-tools adb client -> device's own adbd (Wireless Debugging).
- Caveat: adb connection dies on reboot / wireless-debugging toggle; manual reconnect
  required (bridge will report honest ADB-not-connected errors until then).

## Termux localhost bridge verified (device time ~12:52)
- Bridge: python3 http.server on 127.0.0.1:38471, token-auth, whitelist-only
  (wm density read / set<int 160-1200> / reset). All six probe results passed:
  health ok; read exit0 Physical 440; apply 512 exit0; read-after Override 512;
  reset exit0; bad-token 403; non-whitelist 403.
- Channel chain: NEONFORGE -> 127.0.0.1 bridge -> Termux adb client -> device adbd
  (Wireless Debugging) -> uid 2000 shell -> wm density.
- No internet involvement in operations; all traffic loopback/local.
- Recovery tool: ~/neonforge_bridge/nf_up.sh (reconnect + bridge health, one command).
