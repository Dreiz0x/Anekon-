package com.anekon.ci.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekon.ci.ui.theme.AnekonColors

@Composable
fun HomeScreen(
    onNavigateToProjectCreator: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Hoy", "Esta Semana", "Este Mes")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnekonColors.TextMuted
                    )
                    Text(
                        text = "Anekon",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = AnekonColors.TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        item {
            // Tabs de tiempo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AnekonColors.BackgroundSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    TabButton(
                        text = tab,
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }

        item {
            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Builds",
                    value = "24",
                    change = "+12%",
                    isPositive = true
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Errores",
                    value = "3",
                    change = "-25%",
                    isPositive = true
                )
            }
        }

        item {
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Tiempo Promedio",
                value = "4m 32s",
                change = "-8s",
                isPositive = true
            )
        }

        item {
            Text(
                text = "Herramientas",
                style = MaterialTheme.typography.titleLarge,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            // Acceso rápido al Creador de Proyectos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AnekonColors.Amber.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                onClick = onNavigateToProjectCreator
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AnekonColors.Amber.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AnekonColors.Amber,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Crear Nuevo Proyecto",
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Genera estructura Android con IA",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.TextMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AnekonColors.TextMuted
                    )
                }
            }
        }

        item {
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleLarge,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(recentActivity) { activity ->
            ActivityCard(activity = activity)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) AnekonColors.Amber
                else AnekonColors.BackgroundSecondary
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) AnekonColors.BackgroundPrimary
                    else AnekonColors.TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    change: String,
    isPositive: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = AnekonColors.TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) AnekonColors.Success else AnekonColors.Error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPositive) AnekonColors.Success else AnekonColors.Error
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (activity.status) {
                            ActivityStatus.SUCCESS -> AnekonColors.Success.copy(alpha = 0.2f)
                            ActivityStatus.FAILED -> AnekonColors.Error.copy(alpha = 0.2f)
                            ActivityStatus.RUNNING -> AnekonColors.Amber.copy(alpha = 0.2f)
                            ActivityStatus.PENDING -> AnekonColors.TextMuted.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.status) {
                        ActivityStatus.SUCCESS -> Icons.Default.CheckCircle
                        ActivityStatus.FAILED -> Icons.Default.Cancel
                        ActivityStatus.RUNNING -> Icons.Default.PlayCircle
                        ActivityStatus.PENDING -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = when (activity.status) {
                        ActivityStatus.SUCCESS -> AnekonColors.Success
                        ActivityStatus.FAILED -> AnekonColors.Error
                        ActivityStatus.RUNNING -> AnekonColors.Amber
                        ActivityStatus.PENDING -> AnekonColors.TextMuted
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }

            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = AnekonColors.TextMuted
            )
        }
    }
}

enum class ActivityStatus {
    SUCCESS, FAILED, RUNNING, PENDING
}

data class Activity(
    val title: String,
    val subtitle: String,
    val time: String,
    val status: ActivityStatus
)

private val recentActivity = listOf(
    Activity(
        title = "Build MiApp-debug.apk",
        subtitle = "main • #142",
        time = "Hace 5m",
        status = ActivityStatus.SUCCESS
    ),
    Activity(
        title = "Lint Check",
        subtitle = "develop • #89",
        time = "Hace 23m",
        status = ActivityStatus.FAILED
    ),
    Activity(
        title = "Unit Tests",
        subtitle = "feature/new-ui • #34",
        time = "Hace 1h",
        status = ActivityStatus.SUCCESS
    ),
    Activity(
        title = "Deploy to Play Store",
        subtitle = "release/v1.2.0",
        time = "Hace 2h",
        status = ActivityStatus.RUNNING
    )
)
