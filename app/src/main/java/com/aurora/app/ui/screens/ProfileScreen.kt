package com.aurora.app.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.app.BuildConfig
import com.aurora.app.R
import com.aurora.app.data.ApiConfig
import com.aurora.app.data.ApiService
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
    val configVersion = ApiService.configVersion
    var apiConfigs by remember { mutableStateOf(ApiService.getConfigs()) }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = colorScheme.primary

    // Refresh configs when ApiService notifies a change
    LaunchedEffect(configVersion) {
        apiConfigs = ApiService.getConfigs()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Model Management ──
        item { SectionLabel(stringResource(R.string.profile_active_models).uppercase(), Icons.Rounded.AutoAwesome, accentColor, colorScheme) }
        item {
            androidx.compose.runtime.key(configVersion) {
                val configsForModels = ApiService.getConfigs().filter { it.enabled }
                Column {
                    configsForModels.forEach { config ->
                        val allConfigModels = config.models.filter { it !in config.disabledModels } +
                            config.models.filter { it in config.disabledModels }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
                                Text(config.name, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = accentColor, modifier = Modifier.padding(bottom = 8.dp))
                                if (allConfigModels.isEmpty()) {
                                    Text(stringResource(R.string.profile_no_models_text), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                } else {
                                    allConfigModels.forEachIndexed { i, modelId ->
                                        val isDisabled = modelId in config.disabledModels
                                        val alpha = if (isDisabled) 0.4f else 1f
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                                .animateContentSize()) {
                                                Box(
                                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                                        .background(if (isDisabled) colorScheme.surfaceVariant else accentColor.copy(alpha = 0.12f))
                                                        .clickable {
                                                            ApiService.toggleModelDisabled(config.id, modelId, !isDisabled)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        if (isDisabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                        contentDescription = if (isDisabled) stringResource(R.string.profile_show) else stringResource(R.string.profile_hide),
                                                        tint = if (isDisabled) colorScheme.onSurfaceVariant else accentColor,
                                                        modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(Modifier.width(4.dp))
                                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
                                                    .background(accentColor.copy(alpha = 0.12f * alpha)),
                                                    contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Rounded.AutoAwesome, null, tint = accentColor.copy(alpha = alpha),
                                                        modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(modelId, fontSize = 13.sp,
                                                        color = colorScheme.onSurface.copy(alpha = alpha),
                                                        fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                    Text(if (isDisabled) stringResource(R.string.profile_inactive) else stringResource(R.string.label_active), fontSize = 10.sp,
                                                        color = if (isDisabled) colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else accentColor,
                                                        fontWeight = FontWeight.Medium)
                                                }
                                                if (i > 0) IconButton(onClick = {
                                                    val reordered = allConfigModels.toMutableList()
                                                    reordered.removeAt(i); reordered.add(i - 1, modelId)
                                                    ApiService.reorderModels(config.id, reordered)
                                                }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Rounded.KeyboardArrowUp, null,
                                                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                                }
                                                if (i < allConfigModels.size - 1) IconButton(onClick = {
                                                    val reordered = allConfigModels.toMutableList()
                                                    reordered.removeAt(i); reordered.add(i + 1, modelId)
                                                    ApiService.reorderModels(config.id, reordered)
                                                }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Rounded.KeyboardArrowDown, null,
                                                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        if (i < allConfigModels.size - 1)
                                            HorizontalDivider(color = colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── API Keys ──
        item { SectionLabel(stringResource(R.string.profile_api_keys).uppercase(), Icons.Rounded.Api, accentColor, colorScheme) }
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
                                        Text(stringResource(R.string.profile_models_available, config.models.size), fontSize = 10.sp,
                                            color = accentColor)
                                    }
                                }
                                Switch(
                                    checked = config.enabled,
                                    onCheckedChange = {
                                        ApiService.addConfig(config.copy(enabled = it))
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
                            Text(stringResource(R.string.profile_add_api_key), fontSize = 13.sp, color = accentColor,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Theme ──
        item { SectionLabel(stringResource(R.string.profile_theme).uppercase(), Icons.Outlined.DarkMode, accentColor, colorScheme) }
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
                                Text(stringResource(R.string.profile_dark_mode), style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.profile_dark_mode_hint), fontSize = 11.sp,
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
        item { SectionLabel(stringResource(R.string.profile_about).uppercase(), Icons.Outlined.Info, accentColor, colorScheme) }
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column {
                        var showDisclaimer by remember { mutableStateOf(false) }
                        var showLicenses by remember { mutableStateOf(false) }
                        listOf(
                            stringResource(R.string.profile_app_version) to "Aurora ${BuildConfig.VERSION_NAME}",
                            stringResource(R.string.profile_privacy_terms) to "disclaimer",
                            stringResource(R.string.profile_licenses) to "licenses",
                            stringResource(R.string.profile_feedback) to null,
                        ).forEachIndexed { i, (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        when (label) {
                                            "Privacy & Terms" -> showDisclaimer = true
                                            "Open Source Licenses" -> showLicenses = true
                                        }
                                    }.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(label, modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                if (value != null && value != "disclaimer") {
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
                        // Disclaimer dialog
                        if (showDisclaimer) {
                            AlertDialog(
                                onDismissRequest = { showDisclaimer = false },
                                title = { Text("Privacy Policy & Terms of Service", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column {
                                        Text("By using Aurora, you agree to the following terms:", fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(8.dp))
                                        Text("1. This application is provided \"as is\" without warranties of any kind.", fontSize = 14.sp)
                                        Text("2. The developers are not responsible for any content generated by AI models.", fontSize = 14.sp)
                                        Text("3. Users are solely responsible for the API keys they configure and the data they transmit.", fontSize = 14.sp)
                                        Text("4. This app does not collect personal data. All data is stored locally on your device.", fontSize = 14.sp)
                                        Text("5. AI-generated content may be inaccurate or offensive. Use at your own discretion.", fontSize = 14.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text("If you do not agree with these terms, please discontinue use of the application.", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showDisclaimer = false }) {
                                        Text("OK", fontWeight = FontWeight.Bold)
                                    }
                                },
                            )
                        }
                        // Open Source Licenses dialog
                        if (showLicenses) {
                            AlertDialog(
                                onDismissRequest = { showLicenses = false },
                                title = { Text("Open Source Licenses", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        OssItem("OkHttp", "4.12.0", "Apache 2.0", "https://square.github.io/okhttp/")
                                        OssItem("JSON-java", "20231013", "JSON License", "https://github.com/stleary/JSON-java")
                                        OssItem("Jetpack Compose", "BOM 2024.12.01", "Apache 2.0", "https://developer.android.com/jetpack/compose")
                                        OssItem("Navigation Compose", "2.8.5", "Apache 2.0", "https://developer.android.com/jetpack/compose/navigation")
                                        OssItem("Material Icons Extended", "—", "Apache 2.0", "https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons")
                                        OssItem("marked.js", "12.0.2", "MIT", "https://github.com/markedjs/marked")
                                        OssItem("KaTeX", "0.16.11", "MIT", "https://katex.org")
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showLicenses = false }) {
                                        Text("OK", fontWeight = FontWeight.Bold)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Add API Key Sheet ──
    if (showAddApiSheet) {
        AddApiKeySheet(
            onDismiss = {
                showAddApiSheet = false
            },
            onAdd = { config ->
                ApiService.addConfig(config)
                showAddApiSheet = false
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
    val context = LocalContext.current
    var selectedProvider by remember { mutableStateOf(PROVIDERS[0]) }
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customProtocol by remember { mutableStateOf("openai") }
    var systemPrompt by remember { mutableStateOf("") }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(stringResource(R.string.add_api_title), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Spacer(Modifier.height(20.dp))

            // Provider selection
            Text(stringResource(R.string.add_api_provider), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
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
                    label = { Text(stringResource(R.string.profile_provider_name)) },
                    placeholder = { Text("My API") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.profile_base_url)) },
                    placeholder = { Text("https://api.example.com/v1") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))
                // Protocol selector (only for custom providers)
                Text(stringResource(R.string.profile_protocol), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("openai" to "OpenAI Compatible", "anthropic" to "Anthropic").forEach { (proto, label) ->
                        val isSelected = customProtocol == proto
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else colorScheme.surfaceVariant)
                                .clickable { customProtocol = proto }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accentColor else colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            } else {
                Text(stringResource(R.string.profile_endpoint, selectedProvider.baseUrl), fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; testResult = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_api_key_label)) },
                placeholder = { Text(stringResource(R.string.add_api_key_placeholder)) },
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

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_api_system_prompt)) },
                placeholder = { Text(stringResource(R.string.add_api_system_prompt_hint)) },
                minLines = 2,
                maxLines = 4,
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
                                    testResult = context.getString(R.string.profile_connected_found, models.size)
                                },
                                onFailure = { error ->
                                    testSuccess = false
                                    testResult = context.getString(R.string.profile_connection_failed, error.message ?: "Unknown")
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
                    Text(stringResource(R.string.add_api_testing), color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(stringResource(R.string.add_api_test), color = Color.White, fontWeight = FontWeight.Bold)
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
                            Text(stringResource(R.string.profile_models_label), fontSize = 11.sp, fontWeight = FontWeight.Bold,
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
                        val proto = when (selectedProvider.id) {
                            "anthropic" -> "anthropic"
                            "custom" -> customProtocol
                            else -> "openai"
                        }
                        onAdd(ApiConfig(
                            id = "${selectedProvider.id}_${System.currentTimeMillis()}",
                            name = name.trim(),
                            apiKey = apiKey.trim(),
                            baseUrl = url.trim(),
                            models = fetchedModels,
                            enabled = true,
                            protocol = proto,
                            systemPrompt = systemPrompt.trim(),
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
                Text(stringResource(R.string.add_api_add), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ─── Open Source Item ──────────────────────────────────────────

@Composable
private fun OssItem(name: String, version: String, license: String, url: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colorScheme.onSurface)
        Text("v$version  ·  $license", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        Text(url, fontSize = 10.sp, color = colorScheme.primary.copy(alpha = 0.7f))
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
