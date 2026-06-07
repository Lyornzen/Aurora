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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.app.data.ApiConfig
import com.aurora.app.data.ApiService
import com.aurora.app.data.UserProfile
import kotlinx.coroutines.launch

// ─── Pre-configured providers ─────────────────────────────────

private data class ProviderPreset(
    val id: String,
    val name: String,
    val baseUrl: String,
    val color: Color,
)

private val PROVIDERS = listOf(
    ProviderPreset("openai", "OpenAI", "https://api.openai.com/v1", Color(0xFF006A2E)),
    ProviderPreset("google", "Gemini", "https://generativelanguage.googleapis.com/v1beta", Color(0xFF445E91)),
    ProviderPreset("anthropic", "Claude", "https://api.anthropic.com", Color(0xFF7B1FA2)),
    ProviderPreset("deepseek", "DeepSeek", "https://api.deepseek.com", Color(0xFF00696D)),
    ProviderPreset("custom", "Custom", "", Color(0xFF79747E)),
)

// ─── ProfileScreen ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
) {
    var showAddApiSheet by remember { mutableStateOf(false) }
    var apiConfigs by remember { mutableStateOf(ApiService.getConfigs()) }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = colorScheme.primary
    val profileName = UserProfile.nickname.ifEmpty { "Aurora User" }
    val profileInitials = if (UserProfile.nickname.isNotBlank())
        UserProfile.nickname.take(2).uppercase() else "AU"

    // Refresh configs periodically
    var refreshTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            val fresh = ApiService.getConfigs()
            if (fresh != apiConfigs) {
                apiConfigs = fresh
                refreshTick++
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── User hero ──
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(accentColor),
                            contentAlignment = Alignment.Center) {
                            Text(profileInitials, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profileName, style = MaterialTheme.typography.titleLarge,
                                color = colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            Text("AI Assistant", style = MaterialTheme.typography.bodyMedium,
                                color = accentColor, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)) {
                                    Text("${apiConfigs.size} API Keys", fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, color = accentColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Active Models ──
        item { SectionLabel("Active Models", Icons.Rounded.AutoAwesome, accentColor, colorScheme) }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val allModels = remember(refreshTick) { ApiService.getAllModels() }
                        if (allModels.isEmpty()) {
                            Text("No models configured", style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Text("Add an API key below to get started", fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        } else {
                            allModels.forEachIndexed { i, (modelId, label) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                                        .background(accentColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.AutoAwesome, null, tint = accentColor,
                                            modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(modelId, style = MaterialTheme.typography.titleMedium,
                                            color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                        Text(label, fontSize = 11.sp, color = colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = accentColor)
                                    }
                                }
                                if (i < allModels.size - 1) {
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = colorScheme.surfaceVariant)
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── API Keys ──
        item { SectionLabel("API Keys", Icons.Rounded.Api, accentColor, colorScheme) }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        apiConfigs.forEachIndexed { i, config ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Api, null,
                                    tint = if (config.enabled) accentColor else colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(config.name, style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    Text(config.apiKey.take(8) + "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
                                        fontSize = 11.sp, color = colorScheme.onSurfaceVariant, maxLines = 1)
                                    if (config.models.isNotEmpty()) {
                                        Text("${config.models.size} models available", fontSize = 10.sp,
                                            color = accentColor)
                                    }
                                }
                                Switch(
                                    checked = config.enabled,
                                    onCheckedChange = {
                                        ApiService.addConfig(config.copy(enabled = it))
                                        apiConfigs = ApiService.getConfigs()
                                        refreshTick++
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = accentColor,
                                        checkedTrackColor = accentColor.copy(alpha = 0.4f),
                                        uncheckedThumbColor = colorScheme.outline,
                                        uncheckedTrackColor = colorScheme.surfaceVariant,
                                    ),
                                )
                                IconButton(onClick = {
                                    ApiService.removeConfig(config.id)
                                    apiConfigs = ApiService.getConfigs()
                                    refreshTick++
                                }) {
                                    Icon(Icons.Outlined.Delete, null,
                                        tint = colorScheme.error,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                            if (i < apiConfigs.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                                    color = colorScheme.surfaceVariant)
                            }
                        }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddApiSheet = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text("+ Add API Key", fontSize = 13.sp, color = accentColor,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Theme ──
        item { SectionLabel("Theme", Icons.Outlined.DarkMode, accentColor, colorScheme) }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                                .background(colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.DarkMode, null, tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dark Mode", style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text("Switch color scheme", fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = darkMode,
                                onCheckedChange = onDarkModeChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accentColor,
                                    checkedTrackColor = accentColor.copy(alpha = 0.4f),
                                    uncheckedThumbColor = colorScheme.outline,
                                    uncheckedTrackColor = colorScheme.surfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // ── About ──
        item { SectionLabel("About", Icons.Outlined.Info, accentColor, colorScheme) }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column {
                        listOf(
                            "App Version" to "Aurora 2.1.0",
                            "Privacy Policy" to null,
                            "Terms of Service" to null,
                            "Send Feedback" to null,
                        ).forEachIndexed { i, (label, value) ->
                            // TODO: Navigate to respective detail screens
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {}.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(label, modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                if (value != null) {
                                    Text(value, style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                } else {
                                    Icon(Icons.Outlined.ChevronRight, null,
                                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (i < 3) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                                    color = colorScheme.surfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // ── Sign Out ──
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                // TODO: Implement sign-out / account management
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colorScheme.error,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── Add API Key Sheet ──
    if (showAddApiSheet) {
        AddApiKeySheet(
            onDismiss = {
                showAddApiSheet = false
                apiConfigs = ApiService.getConfigs()
                refreshTick++
            },
            onAdd = { config ->
                ApiService.addConfig(config)
                apiConfigs = ApiService.getConfigs()
                showAddApiSheet = false
                refreshTick++
            },
            accentColor = accentColor,
            scope = scope,
        )
    }
}

// ─── Add API Key Sheet ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddApiKeySheet(
    onDismiss: () -> Unit,
    onAdd: (ApiConfig) -> Unit,
    accentColor: Color,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    var selectedProvider by remember { mutableStateOf(PROVIDERS[0]) }
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testSuccess by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    val colorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text("Add API Key", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Spacer(Modifier.height(20.dp))

            // Provider selection
            Text("Provider", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PROVIDERS.forEach { provider ->
                    val isSelected = selectedProvider.id == provider.id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else colorScheme.surfaceVariant)
                            .clickable {
                                selectedProvider = provider
                                testResult = null
                                fetchedModels = emptyList()
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(provider.name, fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (selectedProvider.id == "custom") {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Provider Name") },
                    placeholder = { Text("My API") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Base URL") },
                    placeholder = { Text("https://api.example.com/v1") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Text("Endpoint: ${selectedProvider.baseUrl}", fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; testResult = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null, tint = colorScheme.onSurfaceVariant,
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val url = if (selectedProvider.id == "custom") customUrl else selectedProvider.baseUrl
                    val key = apiKey.trim()
                    val cleanUrl = url.trim()
                    if (key.isNotBlank() && cleanUrl.isNotBlank()) {
                        testing = true
                        testResult = null
                        fetchedModels = emptyList()
                        scope.launch {
                            val result = ApiService.fetchModels(cleanUrl, key)
                            testing = false
                            result.fold(
                                onSuccess = { models ->
                                    testSuccess = true
                                    fetchedModels = models
                                    testResult = "Connected! ${models.size} models found"
                                },
                                onFailure = { error ->
                                    testSuccess = false
                                    testResult = "Failed: ${error.message}"
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                ),
                enabled = apiKey.isNotBlank() && !testing &&
                    (selectedProvider.id != "custom" || customUrl.isNotBlank()),
            ) {
                if (testing) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Testing...", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Test Connection", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (testResult != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testSuccess) Color(0xFF386A1F).copy(alpha = 0.08f)
                            else colorScheme.error.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (testSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                null,
                                tint = if (testSuccess) Color(0xFF386A1F) else colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(testResult!!, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (testSuccess) Color(0xFF386A1F) else colorScheme.error)
                        }
                        if (fetchedModels.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = if (testSuccess) Color(0xFF386A1F).copy(alpha = 0.2f)
                                else colorScheme.error.copy(alpha = 0.2f))
                            Spacer(Modifier.height(8.dp))
                            Text("Models:", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                fetchedModels.forEach { model ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.AutoAwesome, null,
                                            tint = accentColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(model, fontSize = 12.sp, color = colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val name = if (selectedProvider.id == "custom") customName else selectedProvider.name
                    val url = if (selectedProvider.id == "custom") customUrl else selectedProvider.baseUrl
                    if (apiKey.isNotBlank() && name.isNotBlank() && url.isNotBlank()) {
                        onAdd(ApiConfig(
                            id = "${selectedProvider.id}_${System.currentTimeMillis()}",
                            name = name.trim(),
                            apiKey = apiKey.trim(),
                            baseUrl = url.trim(),
                            models = fetchedModels,
                            enabled = true,
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                ),
                enabled = apiKey.isNotBlank() && testSuccess,
            ) {
                Text("Add API Key", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ─── Section Label ─────────────────────────────────────────────

@Composable
private fun SectionLabel(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    colorScheme: androidx.compose.material3.ColorScheme,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
    ) {
        Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = accentColor, letterSpacing = 0.5.sp)
    }
}
