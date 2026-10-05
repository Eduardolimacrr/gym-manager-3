package com.example.gymmobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymmobile.feature.active.ActiveWorkoutScreen
import com.example.gymmobile.feature.create.CreateWorkoutScreen
import com.example.gymmobile.feature.home.HomeScreen
import com.example.gymmobile.feature.profile.ProfileScreen
import com.example.gymmobile.feature.progress.ProgressDetailScreen
import com.example.gymmobile.feature.progress.ProgressScreen
import com.example.gymmobile.feature.workouts.WorkoutDetailScreen
import com.example.gymmobile.feature.workouts.WorkoutsScreen
import com.example.gymmobile.ui.theme.GymColors

@Composable
fun GymApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = GymColors.Bg,
        bottomBar = {
            // `.bottom-nav.hidden`: some fora das 4 abas.
            if (currentRoute in Rotas.TAB_ROUTES) {
                GymBottomBar(currentRoute) { navController.navigateToTab(it) }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Rotas.HOME) {
                HomeScreen(
                    onStartWorkout = { id -> navController.navigate(Rotas.active(id)) },
                )
            }
            composable(Rotas.WORKOUTS) {
                WorkoutsScreen(
                    onOpenTemplate = { id -> navController.navigate(Rotas.workoutDetail(id)) },
                    onStartTemplate = { id -> navController.navigate(Rotas.active(id)) },
                    onCreate = { navController.navigate(Rotas.CREATE) },
                )
            }
            composable(Rotas.PROGRESS) {
                ProgressScreen(
                    onOpenExercise = { id -> navController.navigate(Rotas.progressDetail(id)) },
                )
            }
            composable(Rotas.PROFILE) {
                ProfileScreen()
            }
            composable(Rotas.CREATE) {
                CreateWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(
                route = Rotas.WORKOUT_DETAIL,
                arguments = listOf(navArgument(Rotas.ARG_TEMPLATE_ID) { type = NavType.StringType }),
            ) { entry ->
                val templateId = entry.arguments?.getString(Rotas.ARG_TEMPLATE_ID).orEmpty()
                WorkoutDetailScreen(
                    templateId = templateId,
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { id -> navController.navigate(Rotas.progressDetail(id)) },
                    onStart = { navController.navigate(Rotas.active(templateId)) },
                )
            }
            composable(
                route = Rotas.ACTIVE,
                arguments = listOf(navArgument(Rotas.ARG_TEMPLATE_ID) { type = NavType.StringType }),
            ) { entry ->
                ActiveWorkoutScreen(
                    templateId = entry.arguments?.getString(Rotas.ARG_TEMPLATE_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                    onFinish = {
                        navController.navigate(Rotas.HOME) {
                            popUpTo(Rotas.HOME) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Rotas.PROGRESS_DETAIL,
                arguments = listOf(navArgument(Rotas.ARG_EXERCISE_ID) { type = NavType.StringType }),
            ) { entry ->
                ProgressDetailScreen(
                    exerciseId = entry.arguments?.getString(Rotas.ARG_EXERCISE_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Troca de aba sem empilhar destinos, preservando o estado de cada aba. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
