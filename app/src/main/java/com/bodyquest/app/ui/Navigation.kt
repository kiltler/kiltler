package com.bodyquest.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodyquest.app.domain.seed.ProgramSeed
import com.bodyquest.app.domain.seed.QuickWorkout
import com.bodyquest.app.ui.components.ConfettiOverlay
import com.bodyquest.app.ui.components.MeasurementRewardDialog
import com.bodyquest.app.ui.components.WorkoutRewardDialog
import com.bodyquest.app.ui.screens.AchievementsScreen
import com.bodyquest.app.ui.screens.DashboardScreen
import com.bodyquest.app.ui.screens.HistoryScreen
import com.bodyquest.app.ui.screens.LibraryScreen
import com.bodyquest.app.ui.screens.NutritionScreen
import com.bodyquest.app.ui.screens.OnboardingScreen
import com.bodyquest.app.ui.screens.ProfileScreen
import com.bodyquest.app.ui.screens.ProgramScreen
import com.bodyquest.app.ui.screens.ProgressScreen
import com.bodyquest.app.ui.screens.WorkoutScreen
import com.bodyquest.app.ui.theme.BqHairline

private object Routes {
    const val DASHBOARD = "dashboard"
    const val PROGRAM = "program"
    const val PROGRESS = "progress"
    const val NUTRITION = "nutrition"
    const val PROFILE = "profile"
    const val LIBRARY = "library"
    const val ACHIEVEMENTS = "achievements"
    const val WORKOUT = "workout"
    const val HISTORY = "history"
}

private data class Tab(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, "Герой", Icons.Filled.Home, Icons.Outlined.Home),
    Tab(Routes.PROGRAM, "Программа", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    Tab(Routes.PROGRESS, "Прогресс", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart),
    Tab(Routes.NUTRITION, "Питание", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    Tab(Routes.PROFILE, "Профиль", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun BodyQuestRoot(vm: BodyQuestViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            state.loading -> Unit
            !state.onboarded -> OnboardingScreen(onFinish = vm::completeOnboarding)
            else -> MainScaffold(vm, state)
        }
    }
}

@Composable
private fun MainScaffold(vm: BodyQuestViewModel, state: AppUiState) {
    val navController = rememberNavController()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val workoutOutcome by vm.workoutOutcome.collectAsStateWithLifecycle()
    val measurementOutcome by vm.measurementOutcome.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(workoutOutcome) {
        val o = workoutOutcome ?: return@LaunchedEffect
        showConfetti = true
        when {
            o.leveledUp -> Haptics.levelUp(context)
            o.progressed.isNotEmpty() -> Haptics.pr(context)
        }
    }
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearMessage()
        }
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val current = backStackEntry?.destination?.route
            Column {
                HorizontalDivider(thickness = 1.dp, color = BqHairline)
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) tab.iconSelected else tab.iconUnselected,
                                    contentDescription = tab.label,
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Visible,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        state = state,
                        onStartQuest = { navController.navigate("${Routes.WORKOUT}/$it") },
                        onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                        onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                        onClaimChallenge = vm::claimDailyChallenge,
                        onFreezeDay = vm::freezeToday,
                    )
                }
                composable(Routes.PROGRAM) {
                    ProgramScreen(state = state, onStartQuest = { navController.navigate("${Routes.WORKOUT}/$it") })
                }
                composable(Routes.PROGRESS) {
                    ProgressScreen(state, onDeleteSession = vm::deleteSession)
                }
                composable(Routes.NUTRITION) {
                    NutritionScreen(
                        state,
                        onAddWater = vm::addWater,
                        onResetWater = { vm.setWater(0) },
                        onSetSleep = vm::setSleep,
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        state = state,
                        settings = settings,
                        onUpdateProfile = vm::updateProfile,
                        onLogMeasurement = { w, c, s, b, wa, t, h, ins, f ->
                            vm.logMeasurement(w, c, s, b, wa, t, h, ins, f)
                        },
                        onWorkoutReminder = vm::setWorkoutReminder,
                        onWaterReminder = vm::setWaterReminder,
                        onExport = vm::exportBackup,
                        onImport = vm::importBackup,
                        onDeleteMeasurement = vm::deleteMeasurement,
                        onShare = vm::shareProgress,
                        onOpenHistory = { navController.navigate(Routes.HISTORY) },
                        onSetTargets = vm::setTargetMeasurements,
                        onReset = vm::resetProgress,
                    )
                }
                composable(Routes.LIBRARY) { LibraryScreen() }
                composable(Routes.ACHIEVEMENTS) { AchievementsScreen(state) }
                composable(Routes.HISTORY) { HistoryScreen(state) }
                composable("${Routes.WORKOUT}/{dayId}") { entry ->
                    val dayId = entry.arguments?.getString("dayId")
                    val program = state.program
                    val day = when {
                        dayId == QuickWorkout.ID -> QuickWorkout.generate()
                        program == null -> null
                        dayId == ProgramSeed.mobilityDay.id -> ProgramSeed.mobilityDay
                        program.bosses.any { it.id == dayId } -> program.bosses.first { it.id == dayId }
                        else -> program.days.find { it.id == dayId }
                    }
                    if (day == null) {
                        androidx.compose.runtime.LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        WorkoutScreen(
                            day = day,
                            state = state,
                            onFinish = { d, logged, dur ->
                                vm.finishWorkout(d, logged, dur)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() },
                        )
                    }
                }
            }

            workoutOutcome?.let { WorkoutRewardDialog(it, onDismiss = vm::clearWorkoutOutcome) }
            measurementOutcome?.let { MeasurementRewardDialog(it, onDismiss = vm::clearMeasurementOutcome) }
            if (showConfetti) ConfettiOverlay(onDone = { showConfetti = false })

            val retroOffer by vm.retroFreezeOffer.collectAsStateWithLifecycle()
            if (retroOffer.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { vm.dismissRetroFreeze() },
                    confirmButton = {
                        TextButton(onClick = { vm.applyRetroFreeze(retroOffer) }) { Text("Заморозить") }
                    },
                    dismissButton = { TextButton(onClick = { vm.dismissRetroFreeze() }) { Text("Не сейчас") } },
                    title = { Text("Серия под угрозой") },
                    text = {
                        Text(
                            "Пропущено дней: ${retroOffer.size}. Потратить ${retroOffer.size} " +
                                "заморозк(у/и) (есть ${state.freezeTokens}), чтобы сохранить серию?",
                        )
                    },
                )
            }
        }
    }
}
