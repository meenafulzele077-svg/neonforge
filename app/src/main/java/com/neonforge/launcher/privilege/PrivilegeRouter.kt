package com.neonforge.launcher.privilege

import com.neonforge.launcher.shizuku.CommandResult
import com.neonforge.launcher.shizuku.ShizukuBridge

enum class ChannelType { TERMUX_ADB, SHIZUKU, NONE }

data class ChannelStatus(
    val type: ChannelType,
    val ready: Boolean,
    val description: String
)

/**
 * Routes privileged commands to the active channel.
 * Priority: Termux-ADB (primary, verified) > Shizuku (dormant fallback) > NONE.
 * Honest reporting: no fake "ready" when channel is actually down.
 */
class PrivilegeRouter(private val termuxAdapter: TermuxAdbAdapter) {

    suspend fun getActiveChannel(): ChannelStatus {
        // Primary: Termux-ADB bridge
        if (termuxAdapter.isBridgeAlive()) {
            return ChannelStatus(ChannelType.TERMUX_ADB, true, "Termux bridge ready (uid 2000 shell)")
        }
        // Fallback: Shizuku (dormant, may be authorized but binder delivery broken on this device)
        if (ShizukuBridge.isBinderAlive() && ShizukuBridge.isPermissionGranted()) {
            return ChannelStatus(ChannelType.SHIZUKU, true, "Shizuku ready (fallback)")
        }
        // None available
        return ChannelStatus(ChannelType.NONE, false, "No privilege channel available")
    }

    suspend fun runCommand(vararg command: String): CommandResult {
        val channel = getActiveChannel()
        return when (channel.type) {
            ChannelType.TERMUX_ADB -> termuxAdapter.runCommand(*command)
            ChannelType.SHIZUKU -> ShizukuBridge.runCommand(*command)
            ChannelType.NONE -> CommandResult(-100, "", "No privilege channel available")
        }
    }
}
