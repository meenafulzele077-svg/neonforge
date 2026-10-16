package com.neonforge.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neonforge.launcher.data.NfSettings
import com.neonforge.launcher.data.ProfileRepository
import com.neonforge.launcher.data.SettingsRepository
import com.neonforge.launcher.density.DensityController
import com.neonforge.launcher.density.OperationLog
import com.neonforge.launcher.density.RecoveryJournalRepo
import com.neonforge.launcher.privilege.BridgeConfig
import com.neonforge.launcher.privilege.PrivilegeRouter
import com.neonforge.launcher.privilege.TermuxAdbAdapter
import com.neonforge.launcher.shizuku.ShizukuBridge
import com.neonforge.launcher.ui.navigation.NfRoutes
import com.neonforge.launcher.ui.screens.BridgeSetupScreen
import com.neonforge.launcher.ui.screens.DashboardScreen
import com.neonforge.launcher.ui.screens.DiagnosticsScreen
import com.neonforge.launcher.ui.screens.LibraryScreen
import com.neonforge.launcher.ui.screens.ProfileEditorScreen
import com.neonforge.launcher.ui.screens.SettingsScreen
import com.neonforge.launcher.ui.theme.NeonForgeTheme
import com.neonforge.launcher.ui.theme.NfMidnight
import com.neonforge.launcher.ui.theme.NfSurface
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

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
    val journalRepo = remember { RecoveryJournalRepo(context) }
    val opLog = remember { OperationLog(context) }
    val bridgeConfig = remember { BridgeConfig(context) }

    val bridgeToken by bridgeConfig.token.collectAsState(initial = "")
    val bridgePort by bridgeConfig.port.collectAsState(initial = "38471")

    val termuxAdapter = remember(bridgeToken, bridgePort) {
        TermuxAdbAdapter(bridgePort.toIntOrNull() ?: 38471, bridgeToken)
    }
    val router = remember(termuxAdapter) { PrivilegeRouter(termuxAdapter) }
    val controller = remember { DensityController(router, journalRepo, opLog) }

    val profiles by profileRepo.profiles.collectAsState(initial = emptyList())
    val settings by settingsRepo.settings.collectAsState(initial = NfSettings())
    val journal by journalRepo.journal.collectAsState(initial = null)
    val session by controller.session.collectAsState()
    val ops by opLog.records.collectAsState(initial = emptyList())

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val scope = rememberCoroutineScope()

    var channelReady by remember { mutableStateOf(false) }
    var channelDescription by remember { mutableStateOf("Checking...") }
    var launchError by remember { mutableStateOf<String?>(null) }

    val refreshChannel: () -> Unit = {
        scope.launch {
            val status = router.getActiveChannel()
            channelReady = status.ready
            channelDescription = status.description
        }
    }

    LaunchedEffect(Unit) { refreshChannel() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshChannel()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    channelReady = channelReady,
                    channelDescription = channelDescription,
                    session = session,
                    journal = journal,
                    lastOp = ops.firstOrNull(),
                    launchError = launchError,
                    onReadDensity = {
                        scope.launch {
                            controller.readCurrent()
                            refreshChannel()
                        }
                    },
                    onRefreshChannel = { refreshChannel() },
                    onOpenBridgeSetup = { navController.navigate(NfRoutes.BRIDGE_SETUP) },
                    onLaunch = {
                        selected?.let { p ->
                            scope.launch {
                                launchError = null
                                val res = controller.startSession(
                                    p.packageName,
                                    p.displayName,
                                    p.requestedDensity
                                )
                                if (res.isSuccess) {
                                    val intent = context.packageManager.getLaunchIntentForPackage(p.packageName)
                                    val started = intent != null &&
                                        runCatching { context.startActivity(intent) }.isSuccess
                                    if (!started) {
                                        launchError = "Game launch intent failed after density apply. Restore from dashboard."
                                        opLog.record("LAUNCH", -1, "intent failed for ${p.packageName}", false)
                                    } else {
                                        opLog.record("LAUNCH", 0, p.packageName, true)
                                    }
                                }
                                refreshChannel()
                            }
                        }
                    },
                    onRestore = {
                        scope.launch {
                            journal?.let { controller.restoreSession(it) }
                            refreshChannel()
                        }
                    },
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
            composable(NfRoutes.BRIDGE_SETUP) {
                BridgeSetupScreen(
                    bridgeConfig = bridgeConfig,
                    onBack = { navController.popBackStack() },
                    onTestConnection = {
                        scope.launch {
                            refreshChannel()
                        }
                    }
                )
            }
            composable(NfRoutes.SETTINGS) {
                SettingsScreen(settingsRepo = settingsRepo)
            }
            composable(NfRoutes.DIAGNOSTICS) {
                DiagnosticsScreen(
                    channelReady = channelReady,
                    channelDescription = channelDescription,
                    session = session,
                    ops = ops
                )
            }
        }
    }
}
