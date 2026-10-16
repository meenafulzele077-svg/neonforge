package com.neonforge.launcher.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

/**
 * Single gateway for ALL privileged operations.
 * Status/permission methods below are compiler-verified public API in
 * dev.rikka.shizuku:api:13.1.5.
 *
 * TEMPORARY HONEST STUB for command execution:
 * Shizuku.newProcess(String[], String[], String) is PRIVATE in 13.1.5
 * (verified compiler diagnostic: ShizukuBridge.kt:42:45, CI runs #6/#7).
 * runCommand() returns an explicit FAILURE result until the CI javap diagnostic
 * reveals the supported public command-execution API. It never pretends success.
 */
object ShizukuBridge {

    const val PERMISSION_REQUEST_CODE = 1001
    const val EXIT_NOT_INTEGRATED = -4

    fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun serverVersion(): Int? = runCatching { Shizuku.getVersion() }.getOrNull()

    fun isPermissionGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun requestPermission() {
        runCatching { Shizuku.requestPermission(PERMISSION_REQUEST_CODE) }
    }

    fun addPermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        runCatching { Shizuku.addRequestPermissionResultListener(listener) }
    }

    fun removePermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        runCatching { Shizuku.removeRequestPermissionResultListener(listener) }
    }

    fun runCommand(vararg command: String): CommandResult {
        return CommandResult(
            exitCode = EXIT_NOT_INTEGRATED,
            stdout = "",
            stderr = "SHIZUKU_COMMAND_EXECUTION_NOT_INTEGRATED: public command API under verification"
        )
    }
}
