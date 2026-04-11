package com.anekon.ci.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Builder : Screen("builder")
    object Chat : Screen("chat")
    object Projects : Screen("projects")
    object Settings : Screen("settings")
    object AutoFix : Screen("autofix")
    object ProjectDetail : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
    object WorkflowDetail : Screen("workflow/{workflowId}") {
        fun createRoute(workflowId: String) = "workflow/$workflowId"
    }
    object Logs : Screen("logs/{runId}") {
        fun createRoute(runId: String) = "logs/$runId"
    }
    object ProjectCreator : Screen("project-creator")
}
