package com.neonforge.launcher.ui.navigation

object NfRoutes {
    const val DASHBOARD = "dashboard"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val DIAGNOSTICS = "diagnostics"
    const val BRIDGE_SETUP = "bridge_setup"
    const val PROFILE_EDITOR = "profile/{packageName}"
    fun profileEditor(packageName: String) = "profile/$packageName"
}
