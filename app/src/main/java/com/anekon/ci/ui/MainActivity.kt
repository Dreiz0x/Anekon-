package com.anekon.ci.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anekon.ci.ui.navigation.AnekonNavGraph
import com.anekon.ci.ui.navigation.bottomNavItems
import com.anekon.ci.ui.theme.AnekonColors
import com.anekon.ci.ui.theme.AnekonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnekonTheme {
                AnekonApp()
            }
        }
    }
}

@Composable
fun AnekonApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AnekonColors.BackgroundPrimary,
        bottomBar = {
            NavigationBar(
                containerColor = AnekonColors.BackgroundSecondary,
                contentColor = AnekonColors.TextPrimary
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AnekonColors.Amber,
                            selectedTextColor = AnekonColors.Amber,
                            unselectedIconColor = AnekonColors.TextMuted,
                            unselectedTextColor = AnekonColors.TextMuted,
                            indicatorColor = AnekonColors.BackgroundTertiary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnekonNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
