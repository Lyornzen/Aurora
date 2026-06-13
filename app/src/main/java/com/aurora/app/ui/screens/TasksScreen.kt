package com.aurora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Webhook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.app.R
import com.aurora.app.ui.theme.Success
import com.aurora.app.ui.theme.SuccessContainer
import com.aurora.app.ui.theme.Warning
import com.aurora.app.ui.theme.WarningContainer

// ─── Data Models ────────────────────────────────────────────────

enum class TaskStatus { Running, Completed, Failed, Paused }

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
    val status: TaskStatus,
    val progress: Int? = null,
    val updatedAt: String,
    val category: String,
)

// TODO: Replace with actual task data source once task management is implemented
private val TASKS = emptyList<Task>()

private data class StatusMeta(val label: String, val color: Color, val bg: Color, val icon: ImageVector?)

// ─── TasksScreen ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    var filter by remember { mutableStateOf("All") }
    var showNewTask by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    val categories = listOf("All", "API", "AI", "Automation", "Data")
    val runningTasks = TASKS.filter { it.status == TaskStatus.Running }
    val filtered = if (filter == "All") TASKS.filter { it.status != TaskStatus.Running }
    else TASKS.filter { it.category == filter && it.status != TaskStatus.Running }

    Box(modifier = modifier.fillMaxSize().background(colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(stringResource(R.string.tasks_title), style = MaterialTheme.typography.headlineLarge, color = colorScheme.onSurface)
                        Text(
                            "${if (runningTasks.isNotEmpty()) stringResource(R.string.tasks_running_count, runningTasks.size) else ""}${stringResource(R.string.tasks_total_count, TASKS.size)}",
                            style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // Running task hero
            runningTasks.forEach { task ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(36.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                    .background(colorScheme.primary), contentAlignment = Alignment.Center) {
                                    Icon(task.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleMedium, color = colorScheme.onPrimaryContainer)
                                    Text(task.description, fontSize = 12.sp, color = colorScheme.primary)
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("${task.progress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (task.progress ?: 0) / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)),
                                color = colorScheme.primary,
                                trackColor = colorScheme.primaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.tasks_updated, task.updatedAt), fontSize = 11.sp, color = colorScheme.primary)
                        }
                    }
                }
            }
            // Filter chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    categories.forEach { c ->
                        val selected = filter == c
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) colorScheme.primary else colorScheme.surfaceVariant)
                                .clickable { filter = c }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(c, fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.W600 else FontWeight.W400,
                                color = if (selected) Color.White else colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            // Task list
            items(filtered, key = { it.id }) { task -> TaskCard(task = task) }
            // Bottom spacer for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }

        // FAB
        FloatingActionButton(
            onClick = { showNewTask = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp),
            shape = CircleShape,
            containerColor = colorScheme.primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(Icons.Rounded.Add, null)
        }
    }

    // New Task Bottom Sheet
    if (showNewTask) {
        ModalBottomSheet(
            onDismissRequest = { showNewTask = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                // Drag handle
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.width(32.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(colorScheme.outlineVariant))
                }
                Text(stringResource(R.string.tasks_new), style = MaterialTheme.typography.titleLarge, color = colorScheme.onSurface)
                Text(stringResource(R.string.tasks_new_hint),
                    style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp))
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text(stringResource(R.string.tasks_name_label)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                )
                // TODO: Bind to a description state variable when task creation is fully implemented
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.tasks_desc_label)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showNewTask = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary),
                    ) { Text(stringResource(R.string.btn_cancel)) }
                    Button(
                        onClick = { showNewTask = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) { Text(stringResource(R.string.tasks_create)) }
                }
            }
        }
    }
}

// ─── TaskCard ───────────────────────────────────────────────────

@Composable
private fun TaskCard(task: Task) {
    val colorScheme = MaterialTheme.colorScheme
    val statusMeta = mapOf(
        TaskStatus.Running to StatusMeta(stringResource(R.string.tasks_status_running), colorScheme.primary, colorScheme.primaryContainer, null),
        TaskStatus.Completed to StatusMeta(stringResource(R.string.tasks_status_done), Success, SuccessContainer, Icons.Outlined.CheckCircle),
        TaskStatus.Failed to StatusMeta(stringResource(R.string.tasks_status_failed), colorScheme.error, colorScheme.errorContainer, Icons.Outlined.Error),
        TaskStatus.Paused to StatusMeta(stringResource(R.string.tasks_status_paused), colorScheme.secondary, colorScheme.secondaryContainer, Icons.Outlined.PauseCircle),
    )
    val meta = statusMeta[task.status]!!
    Card(
        modifier = Modifier.fillMaxWidth()
            .then(if (task.status == TaskStatus.Failed) Modifier.background(colorScheme.errorContainer.copy(alpha = 0.3f)) else Modifier),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.clickable {}.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(task.iconBg),
                    contentAlignment = Alignment.Center) {
                    Icon(task.icon, null, tint = task.iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium, color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                        // TODO: Implement task context menu
                        IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.MoreVert, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(task.description, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(meta.bg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            meta.icon?.let {
                                Icon(it, null, tint = meta.color, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                            }
                            Text(meta.label, fontSize = 10.sp, fontWeight = FontWeight.W600, color = meta.color)
                        }
                        Text(task.updatedAt, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (task.progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)),
                    color = if (task.status == TaskStatus.Paused) colorScheme.secondary else colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant,
                )
            }
        }
    }
}
