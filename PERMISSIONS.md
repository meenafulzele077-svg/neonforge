# Permissions Justification

## android.permission.INTERNET
- **Purpose:** HTTP client for localhost bridge communication
- **Scope:** Loopback only (127.0.0.1:38471)
- **Never contacts external servers**
- **Required for:** Privileged density operations via Termux bridge
- **Privacy impact:** None (all traffic stays on device)

## android.permission.QUERY_ALL_PACKAGES
- **Purpose:** Enumerate installed launchable apps for game library
- **Required for:** Building the list of apps users can create density profiles for
- **Privacy impact:** Read-only enumeration, no data collection

## Rationale
NEONFORGE uses a localhost HTTP bridge to communicate with Termux's adb client.
This allows shell-privilege operations (uid 2000) without root or Shizuku.
The INTERNET permission is required for the HTTP client, but all traffic is
loopback (127.0.0.1) and never leaves the device.

## Cleartext traffic policy (network security config)
- Cleartext HTTP permitted ONLY for 127.0.0.1 / localhost (the local Termux bridge).
- All other domains remain under Android's default secure policy.
- Reason: Android 9+ blocks cleartext by default, including loopback; the bridge
  speaks plain HTTP on 127.0.0.1 only. No external cleartext traffic exists.
