package com.anekon.ci.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Inicio",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "AutoFix",
        route = Screen.AutoFix.route,
        selectedIcon = Icons.Filled.AutoFixHigh,
        unselectedIcon = Icons.Outlined.AutoFixHigh
    ),
    BottomNavItem(
        title = "Constructor",
        route = Screen.Builder.route,
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build
    ),
    BottomNavItem(
        title = "Chat",
        route = Screen.Chat.route,
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    ),
    BottomNavItem(
        title = "Ajustes",
        route = Screen.Settings.route,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
