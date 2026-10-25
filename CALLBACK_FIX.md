# Experimental CameraX buffer-lost callback repair

This fork preserves Camera2 Magic 2.3.0 at commit `cf14e9e` and adds a narrowly scoped experimental repair. Original project history is preserved from `wudixxqq/Camera2Magic`, whose README identifies `servant1228/Camera2Magic` as the prior project location and Atomos-X/Camera2Magic as its basis. This is an independent fork, not an official release.

## Problem and change

The replacement preview output can be reported by Android in `onCaptureBufferLost`. CameraX keeps its stream map under the caller's original Surface object; an unknown replacement can cause `IllegalStateException: Unable to find the streamId for Surface`.

The patch records weak reverse aliases when BlackHole outputs are created. It observes Android's `CameraCaptureSessionImpl.createCaptureCallbackProxy` factory and normalizes only known aliases in the returned proxy's buffer-lost callback before Android forwards it to the caller. Real failures are still delivered, unknown outputs pass through, and application exceptions are not suppressed. Aliases remain weakly reachable after output cleanup so queued late callbacks can still be normalized.

No target-app code, location module, camera permissions, server responses or capture success callbacks are changed. The prebuilt native `libcamera3.so` remains unchanged; its source is not provided by the upstream project.

## Validation status

- Four pure-JVM JUnit regression tests cover passthrough, exact object identity, late callbacks across replacement sessions, and concurrent registrations.
- Local verification on 2026-10-25 passed: all 14 project unit tests (including the four regression tests), Android Kotlin/Java compilation, and `assembleDebug` APK packaging with JDK 21. `git diff --check` also passed.
- Phone validation is pending. Registry tests and a successful Android build do not prove hook installation, media replacement or successful photo confirmation.
- The adapter depends on an internal Android framework factory. If it is unavailable, the module logs a diagnostic and leaves callbacks unchanged. No specific Android build or target application is claimed compatible until tested.
- Device checks should use a camera test application with no attendance or other business submission: preview, capture, cancel, confirm, reopen and lens switching. Do not validate by blindly triggering business submission buttons.

## Build

Use JDK 21 and the repository Gradle wrapper on Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugKotlin assembleDebug --no-configuration-cache
```

Debug builds use a debug certificate and cannot replace an installed official build signed with another key. Preserve the original APK and configuration; do not uninstall the official build merely to make a debug APK install. A separate install identity and branding are required before distributing a parallel test build.
