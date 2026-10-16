package com.neonforge.launcher.shizuku

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

data class BinderDeliveryInfo(
    val providerClassPresent: Boolean,
    val ownProviderRegistered: Boolean,
    val serverProviderVisible: Boolean,
    val requestInfo: String
)

/**
 * Privileged-operation gateway.
 * Command execution: Shizuku.newProcess (public in pinned api 12.2.0, CI-javap verified).
 * Binder delivery: ACTIVE PULL strategy. If pingBinder() is false while the Shizuku
 * server is running, we explicitly invoke the provider's public binder-request entry
 * point (reflectively, for version resilience). All probes are read-only and honest.
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

    /** Actively asks the Shizuku provider to fetch the binder. Returns an honest status string. */
    fun requestBinderExplicitly(context: Context): String {
        val cls = runCatching { Class.forName("rikka.shizuku.ShizukuProvider") }.getOrNull()
            ?: return "ShizukuProvider class MISSING from APK (manifest/merge defect)"
        val method = cls.methods.firstOrNull {
            it.name.contains("requestBinder", ignoreCase = true) &&
                it.parameterTypes.toList() == listOf(Context::class.java)
        } ?: cls.methods.firstOrNull { it.name.contains("requestBinder", ignoreCase = true) }
            ?: return "ShizukuProvider exposes no public requestBinder* method in this version"
        return runCatching {
            method.invoke(null, context)
            "explicit binder pull invoked: ${method.name}"
        }.getOrElse { "explicit binder pull failed: ${it.message}" }
    }

    fun deliveryInfo(context: Context, requestInfo: String): BinderDeliveryInfo {
        val providerClassPresent = runCatching {
            Class.forName("rikka.shizuku.ShizukuProvider")
        }.isSuccess
        val ownProviderRegistered = runCatching {
            context.packageManager.resolveContentProvider(
                "${context.packageName}.shizuku", 0
            ) != null
        }.getOrDefault(false)
        val serverProviderVisible = runCatching {
            context.packageManager.resolveContentProvider(
                "moe.shizuku.privileged.api.shizuku", 0
            ) != null
        }.getOrDefault(false)
        return BinderDeliveryInfo(
            providerClassPresent, ownProviderRegistered, serverProviderVisible, requestInfo
        )
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
