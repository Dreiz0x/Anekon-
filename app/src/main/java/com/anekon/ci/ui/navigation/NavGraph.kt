package com.anekon.ci.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anekon.ci.ui.screens.autofix.AutoFixScreen
import com.anekon.ci.ui.screens.builder.BuilderScreen
import com.anekon.ci.ui.screens.chat.ChatScreen
import com.anekon.ci.ui.screens.home.HomeScreen
import com.anekon.ci.ui.screens.projectcreator.ProjectCreatorScreen
import com.anekon.ci.ui.screens.projects.ProjectsScreen
import com.anekon.ci.ui.screens.settings.SettingsScreen

@Composable
fun AnekonNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProjectCreator = { navController.navigate(Screen.ProjectCreator.route) }
            )
        }

        composable(Screen.AutoFix.route) {
            AutoFixScreen(
                failedBuilds = emptyList(),
                isLoading = false,
                onAnalyzeBuild = {},
                onApplyFix = { _, _ -> },
                onNavigateToDetail = {},
                onRefresh = {}
            )
        }

        composable(Screen.Builder.route) {
            BuilderScreen()
        }

        composable(Screen.Chat.route) {
            ChatScreen()
        }

        composable(Screen.Projects.route) {
            ProjectsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.ProjectCreator.route) {
            ProjectCreatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
