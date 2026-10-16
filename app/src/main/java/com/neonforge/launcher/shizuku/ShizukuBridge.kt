package com.neonforge.launcher.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

/**
 * Single gateway for ALL privileged operations.
 * Runs commands with shell UID via Shizuku (adb mode on this device).
 * Never guesses: every call returns explicit exit code + streams.
 */
object ShizukuBridge {

    const val PERMISSION_REQUEST_CODE = 1001

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
        if (!isBinderAlive()) {
            return CommandResult(-1, "", "Shizuku binder not alive (is Shizuku running?)")
        }
        val process = runCatching { Shizuku.newProcess(command, null, null) }
            .getOrElse { return CommandResult(-2, "", "Shizuku.newProcess failed: ${it.message}") }
        val stdout = runCatching {
            process.inputStream.bufferedReader().use(BufferedReader::readText)
        }.getOrDefault("")
        val stderr = runCatching {
            process.errorStream.bufferedReader().use(BufferedReader::readText)
        }.getOrDefault("")
        val exit = runCatching { process.waitFor() }.getOrElse { -3 }
        return CommandResult(exit, stdout, stderr)
    }
}
