package com.bodyquest.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodyquest.app.domain.seed.ProgramSeed
import com.bodyquest.app.ui.components.MeasurementRewardDialog
import com.bodyquest.app.ui.components.WorkoutRewardDialog
import com.bodyquest.app.ui.screens.AchievementsScreen
import com.bodyquest.app.ui.screens.DashboardScreen
import com.bodyquest.app.ui.screens.LibraryScreen
import com.bodyquest.app.ui.screens.NutritionScreen
import com.bodyquest.app.ui.screens.OnboardingScreen
import com.bodyquest.app.ui.screens.ProfileScreen
import com.bodyquest.app.ui.screens.ProgramScreen
import com.bodyquest.app.ui.screens.ProgressScreen
import com.bodyquest.app.ui.screens.WorkoutScreen

private object Routes {
    const val DASHBOARD = "dashboard"
    const val PROGRAM = "program"
    const val PROGRESS = "progress"
    const val NUTRITION = "nutrition"
    const val PROFILE = "profile"
    const val LIBRARY = "library"
    const val ACHIEVEMENTS = "achievements"
    const val WORKOUT = "workout"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, "Герой", Icons.Filled.Home),
    Tab(Routes.PROGRAM, "Программа", Icons.Filled.FitnessCenter),
    Tab(Routes.PROGRESS, "Прогресс", Icons.Filled.ShowChart),
    Tab(Routes.NUTRITION, "Питание", Icons.Filled.Restaurant),
    Tab(Routes.PROFILE, "Профиль", Icons.Filled.Person),
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

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val current = backStackEntry?.destination?.route
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
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
                    )
                }
                composable(Routes.PROGRAM) {
                    ProgramScreen(state = state, onStartQuest = { navController.navigate("${Routes.WORKOUT}/$it") })
                }
                composable(Routes.PROGRESS) { ProgressScreen(state) }
                composable(Routes.NUTRITION) {
                    NutritionScreen(state, onAddWater = vm::addWater, onResetWater = { vm.setWater(0) })
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
                        onReset = vm::resetProgress,
                    )
                }
                composable(Routes.LIBRARY) { LibraryScreen() }
                composable(Routes.ACHIEVEMENTS) { AchievementsScreen(state) }
                composable("${Routes.WORKOUT}/{dayId}") { entry ->
                    val dayId = entry.arguments?.getString("dayId")
                    val program = state.program
                    val day = when {
                        program == null -> null
                        dayId == program.boss.id -> program.boss
                        dayId == ProgramSeed.mobilityDay.id -> ProgramSeed.mobilityDay
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
        }
    }
}
