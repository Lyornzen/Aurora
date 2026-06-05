package com.aurora.ai.ui.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.components.AuroraCard
import com.aurora.ai.ui.components.AuroraProgressBar
import com.aurora.ai.ui.components.AuroraStatusBadge
import com.aurora.ai.ui.components.AuroraTopBar

// ============================================================
// Data Models
// ============================================================

enum class TaskStatus(val label: String, val color: Color) {
    Running("Running", Color(0xFF7C4DFF)),
    Completed("Completed", Color(0xFF4CAF50)),
    Paused("Paused", Color(0xFFF8BBD0)),
    Failed("Failed", Color(0xFFEF5350)),
}

data class AuroraTask(
    val id: String,
    val name: String,
    val status: TaskStatus,
    val progress: Int, // 0-100
    val description: String = "",
    val timestamp: String = "",
)

data class TimelineStep(
    val id: String,
    val label: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
)

val sampleTasks = listOf(
    AuroraTask(
        id = "t1",
        name = "Weekly Report",
        status = TaskStatus.Running,
        progress = 73,
        description = "Collect data and generate weekly performance report.",
        timestamp = "Started 10 min ago",
    ),
    AuroraTask(
        id = "t2",
        name = "Data Analysis",
        status = TaskStatus.Running,
        progress = 45,
        description = "Analyze user behavior patterns from last month.",
        timestamp = "Started 25 min ago",
    ),
    AuroraTask(
        id = "t3",
        name = "Code Review",
        status = TaskStatus.Completed,
        progress = 100,
        description = "Review PR #342 for authentication module.",
        timestamp = "Completed 1 hour ago",
    ),
    AuroraTask(
        id = "t4",
        name = "API Migration",
        status = TaskStatus.Paused,
        progress = 60,
        description = "Migrate REST endpoints to GraphQL.",
        timestamp = "Paused 2 hours ago",
    ),
    AuroraTask(
        id = "t5",
        name = "Database Backup",
        status = TaskStatus.Failed,
        progress = 88,
        description = "Automated backup to cloud storage.",
        timestamp = "Failed 30 min ago",
    ),
)

// ============================================================
// Tasks Screen
// ============================================================

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (AuroraTask) -> Unit = {},
    bottomPadding: Dp = AuroraDp.dp80,
) {
    var tasks by remember { mutableStateOf(emptyList<AuroraTask>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = AuroraDp.dp8),
    ) {
        // Header
        AuroraTopBar(
            title = "Tasks",
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
            },
        )

        // New Task Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraDp.dp16, vertical = AuroraDp.dp8)
                .clip(RoundedCornerShape(AuroraDp.dp20)),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            onClick = { },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraDp.dp20, vertical = AuroraDp.dp16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AuroraDp.dp20),
                )
                Spacer(modifier = Modifier.width(AuroraDp.dp8))
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(AuroraDp.dp8))

        // Task List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AuroraDp.dp16)
                .padding(bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(AuroraDp.dp12),
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                )
            }
        }
    }
}

// ============================================================
// Task Card
// ============================================================

@Composable
fun TaskCard(
    task: AuroraTask,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val statusColor = when (task.status) {
        TaskStatus.Running -> MaterialTheme.colorScheme.primary
        TaskStatus.Completed -> Color(0xFF4CAF50)
        TaskStatus.Paused -> MaterialTheme.colorScheme.tertiary
        TaskStatus.Failed -> MaterialTheme.colorScheme.error
    }

    AuroraCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = AuroraDp.dp24,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = AuroraDp.dp16,
                vertical = AuroraDp.dp14,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(AuroraDp.dp8))
                AuroraStatusBadge(
                    text = task.status.label,
                    color = statusColor,
                )
            }

            Spacer(modifier = Modifier.height(AuroraDp.dp10))

            // Progress bar with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp10),
            ) {
                AuroraProgressBar(
                    progress = task.progress / 100f,
                    progressColor = statusColor,
                    modifier = Modifier.weight(1f),
                    height = AuroraDp.dp6,
                )
                Text(
                    text = "${task.progress}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(AuroraDp.dp6))

            Text(
                text = task.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ============================================================
// Task Detail Screen
// ============================================================

@Composable
fun TaskDetailScreen(
    task: AuroraTask,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeline = remember {
        listOf(
            TimelineStep("s1", "Start", true, false),
            TimelineStep("s2", "Search Data", true, false),
            TimelineStep("s3", "Analyze Data", true, false),
            TimelineStep("s4", "Generate Report", false, true),
            TimelineStep("s5", "Send Email", false, false),
        )
    }

    val statusColor = when (task.status) {
        TaskStatus.Running -> MaterialTheme.colorScheme.primary
        TaskStatus.Completed -> Color(0xFF4CAF50)
        TaskStatus.Paused -> MaterialTheme.colorScheme.tertiary
        TaskStatus.Failed -> MaterialTheme.colorScheme.error
    }

    Scaffold(
        topBar = {
            AuroraTopBar(
                title = task.name,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraDp.dp16, vertical = AuroraDp.dp12)
                    .padding(bottom = AuroraDp.dp8),
                horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp12),
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(AuroraDp.dp48),
                    shape = RoundedCornerShape(AuroraDp.dp16),
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(AuroraDp.dp18))
                    Spacer(Modifier.width(AuroraDp.dp8))
                    Text("Pause")
                }
                FilledTonalButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(AuroraDp.dp48),
                    shape = RoundedCornerShape(AuroraDp.dp16),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(AuroraDp.dp18),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(AuroraDp.dp8))
                    Text("Cancel")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AuroraDp.dp16),
        ) {
            // Task Summary Card
            AuroraCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = AuroraDp.dp24,
            ) {
                Column(
                    modifier = Modifier.padding(AuroraDp.dp16),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = task.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        AuroraStatusBadge(
                            text = task.status.label,
                            color = statusColor,
                        )
                    }

                    Spacer(modifier = Modifier.height(AuroraDp.dp12))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp10),
                    ) {
                        AuroraProgressBar(
                            progress = task.progress / 100f,
                            progressColor = statusColor,
                            modifier = Modifier.weight(1f),
                            height = AuroraDp.dp8,
                            cornerRadius = AuroraDp.dp4,
                        )
                        Text(
                            text = "${task.progress}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(AuroraDp.dp12))

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AuroraDp.dp20))

            // Execution Timeline
            Text(
                text = "Execution Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(AuroraDp.dp12))

            // Timeline
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AuroraDp.dp0),
            ) {
                timeline.forEachIndexed { index, step ->
                    TimelineStepItem(
                        step = step,
                        isLast = index == timeline.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineStepItem(
    step: TimelineStep,
    isLast: Boolean,
) {
    val stepColor = when {
        step.isCompleted -> Color(0xFF4CAF50)
        step.isCurrent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AuroraDp.dp2),
    ) {
        // Timeline indicator column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(AuroraDp.dp40),
        ) {
            // Dot
            Box(
                modifier = Modifier
                    .size(
                        if (step.isCurrent) AuroraDp.dp16 else AuroraDp.dp12,
                    )
                    .clip(CircleShape)
                    .then(
                        if (step.isCompleted || step.isCurrent) {
                            Modifier.background(stepColor)
                        } else {
                            Modifier.background(
                                stepColor.copy(alpha = 0.3f),
                                CircleShape,
                            )
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (step.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(AuroraDp.dp10),
                    )
                }
            }

            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(AuroraDp.dp2)
                        .height(AuroraDp.dp32)
                        .clip(RoundedCornerShape(AuroraDp.dp1))
                        .background(
                            if (step.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.width(AuroraDp.dp12))

        // Label
        Text(
            text = step.label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (step.isCurrent)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (step.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = AuroraDp.dp2),
        )
    }
}
