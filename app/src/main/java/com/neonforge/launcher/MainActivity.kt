package com.neonforge.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neonforge.launcher.data.NfSettings
import com.neonforge.launcher.data.ProfileRepository
import com.neonforge.launcher.data.SettingsRepository
import com.neonforge.launcher.ui.navigation.NfRoutes
import com.neonforge.launcher.ui.screens.DashboardScreen
import com.neonforge.launcher.ui.screens.DiagnosticsScreen
import com.neonforge.launcher.ui.screens.LibraryScreen
import com.neonforge.launcher.ui.screens.ProfileEditorScreen
import com.neonforge.launcher.ui.screens.SettingsScreen
import com.neonforge.launcher.ui.theme.NeonForgeTheme
import com.neonforge.launcher.ui.theme.NfMidnight
import com.neonforge.launcher.ui.theme.NfSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonForgeTheme {
                NfRoot()
            }
        }
    }
}

@Composable
fun NfRoot() {
    val context = LocalContext.current.applicationContext
    val profileRepo = remember { ProfileRepository(context) }
    val settingsRepo = remember { SettingsRepository(context) }
    val profiles by profileRepo.profiles.collectAsState(initial = emptyList())
    val settings by settingsRepo.settings.collectAsState(initial = NfSettings())
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val selected = profiles.firstOrNull { it.packageName == settings.selectedPackage && it.enabled }
        ?: profiles.firstOrNull { it.enabled }

    Scaffold(
        containerColor = NfMidnight,
        bottomBar = {
            NavigationBar(containerColor = NfSurface) {
                NavigationBarItem(
                    selected = route == NfRoutes.DASHBOARD,
                    onClick = {
                        navController.navigate(NfRoutes.DASHBOARD) {
                            popUpTo(NfRoutes.DASHBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = route == NfRoutes.LIBRARY,
                    onClick = { navController.navigate(NfRoutes.LIBRARY) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Library") }
                )
                NavigationBarItem(
                    selected = route == NfRoutes.SETTINGS,
                    onClick = { navController.navigate(NfRoutes.SETTINGS) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NfRoutes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(NfRoutes.DASHBOARD) {
                DashboardScreen(
                    profiles = profiles,
                    selected = selected,
                    onSelectGame = { navController.navigate(NfRoutes.LIBRARY) },
                    onOpenDiagnostics = { navController.navigate(NfRoutes.DIAGNOSTICS) }
                )
            }
            composable(NfRoutes.LIBRARY) {
                LibraryScreen(profiles = profiles) { pkg ->
                    navController.navigate(NfRoutes.profileEditor(pkg))
                }
            }
            composable(
                route = NfRoutes.PROFILE_EDITOR,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val pkg = backStackEntry.arguments?.getString("packageName") ?: ""
                ProfileEditorScreen(
                    packageName = pkg,
                    profileRepo = profileRepo,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NfRoutes.SETTINGS) {
                SettingsScreen(settingsRepo = settingsRepo)
            }
            composable(NfRoutes.DIAGNOSTICS) {
                DiagnosticsScreen()
            }
        }
    }
}
