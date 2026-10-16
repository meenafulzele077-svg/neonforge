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
import com.neonforge.launcher.shizuku.BinderDeliveryInfo
import com.neonforge.launcher.shizuku.ShizukuBridge
import com.neonforge.launcher.ui.navigation.NfRoutes
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

enum class ShizukuUiState { NOT_INSTALLED, NOT_RUNNING, NOT_AUTHORIZED, READY }

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
    val controller = remember { DensityController(journalRepo, opLog) }

    val profiles by profileRepo.profiles.collectAsState(initial = emptyList())
    val settings by settingsRepo.settings.collectAsState(initial = NfSettings())
    val journal by journalRepo.journal.collectAsState(initial = null)
    val session by controller.session.collectAsState()
    val ops by opLog.records.collectAsState(initial = emptyList())

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val scope = rememberCoroutineScope()

    val shizukuInstalled = remember {
        runCatching {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0) != null
        }.getOrDefault(false)
    }
    var binderAlive by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var requestInfo by remember { mutableStateOf("not attempted yet") }
    var launchError by remember { mutableStateOf<String?>(null) }

    val refreshShizuku: () -> Unit = {
        val alive = ShizukuBridge.isBinderAlive()
        binderAlive = alive
        permissionGranted = ShizukuBridge.isPermissionGranted()
        if (!alive && shizukuInstalled) {
            requestInfo = ShizukuBridge.requestBinderExplicitly(context)
        }
    }

    LaunchedEffect(Unit) { refreshShizuku() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshShizuku()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { code, _ ->
            if (code == ShizukuBridge.PERMISSION_REQUEST_CODE) refreshShizuku()
        }
        ShizukuBridge.addPermissionListener(listener)
        onDispose { ShizukuBridge.removePermissionListener(listener) }
    }

    val delivery = remember(requestInfo, binderAlive) {
        ShizukuBridge.deliveryInfo(context, requestInfo)
    }

    val shizukuState = when {
        !shizukuInstalled -> ShizukuUiState.NOT_INSTALLED
        !binderAlive -> ShizukuUiState.NOT_RUNNING
        !permissionGranted -> ShizukuUiState.NOT_AUTHORIZED
        else -> ShizukuUiState.READY
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
                    shizukuState = shizukuState,
                    session = session,
                    journal = journal,
                    lastOp = ops.firstOrNull(),
                    launchError = launchError,
                    onReadDensity = {
                        scope.launch {
                            controller.readCurrent()
                            refreshShizuku()
                        }
                    },
                    onRequestPermission = { ShizukuBridge.requestPermission() },
                    onRefreshStatus = { refreshShizuku() },
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
                                refreshShizuku()
                            }
                        }
                    },
                    onRestore = {
                        scope.launch {
                            journal?.let { controller.restoreSession(it) }
                            refreshShizuku()
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
            composable(NfRoutes.SETTINGS) {
                SettingsScreen(settingsRepo = settingsRepo)
            }
            composable(NfRoutes.DIAGNOSTICS) {
                DiagnosticsScreen(
                    shizukuState = shizukuState,
                    binderAlive = binderAlive,
                    permissionGranted = permissionGranted,
                    session = session,
                    ops = ops,
                    delivery = delivery
                )
            }
        }
    }
}
