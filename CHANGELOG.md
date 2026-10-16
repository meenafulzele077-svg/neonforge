# Changelog

## [0.2.0] - Phase 2 Step 2-C
- Added functional Profile Editor with validation, presets, and persistence.
- Added Settings screen with persistent toggles for haptics, sound, motion, and monitoring interval.
- Added Diagnostics screen showing real device environment and system access status.
- Added JVM unit tests for `DensityPolicy` and `ProfileCodec`.
- Seeded architecture and known-issues documentation.

## [0.1.0] - Phase 2 Steps 2-A & 2-B
- Established cinematic Material 3 design system (colors, typography, shapes).
- Implemented persistent data layer using DataStore and kotlinx-serialization.
- Built functional navigation, Dashboard, and Game Library with real installed-app discovery.
- Configured GitHub Actions for remote CI/CD and APK generation.

## [0.0.1] - Phase 1
- Initial device audit (iQOO Z9s 5G, Android 16, SDK 36).
- Repository bootstrap and environment verification.

## [0.4.0] - Phase 3 T1 verified
- Self-adb privilege channel proven on device: uid=2000(shell) via Wireless Debugging
  pairing from Termux; wm density apply/verify/reset cycle verified (baseline 440).
- Shizuku path left dormant by owner decision.

## [0.4.1] - Bridge channel verified
- localhost bridge end-to-end verified with token auth and command whitelist.
- nf_up.sh recovery helper added for post-reboot channel restoration.
