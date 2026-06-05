package com.aurora.ai.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.aurora.ai.I18nStrings
import com.aurora.ai.LocalI18n
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.components.AuroraCard
import com.aurora.ai.ui.components.AuroraTopBar

// Pre-configured API providers (referencing OpenSeek patterns)
data class ApiProvider(val id: String, val name: String, val baseUrl: String)

val knownApiProviders = listOf(
    ApiProvider("openai", "OpenAI", "https://api.openai.com/v1"),
    ApiProvider("anthropic", "Anthropic", "https://api.anthropic.com"),
    ApiProvider("google", "Google Gemini", "https://generativelanguage.googleapis.com"),
    ApiProvider("deepseek", "DeepSeek", "https://api.deepseek.com"),
    ApiProvider("openrouter", "OpenRouter", "https://openrouter.ai/api/v1"),
    ApiProvider("custom", "Custom (OpenAI Compatible)", ""),
)

val themeLabels = listOf("Light", "Dark", "System")
val themeIcons = listOf(Icons.Filled.LightMode, Icons.Filled.DarkMode, Icons.Filled.Tune)
val languageLabels = listOf("English", "简体中文", "日本語", "한국어")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = AuroraDp.dp80,
    themeIndex: Int = 0,
    onThemeChange: (Int) -> Unit = {},
    languageIndex: Int = 0,
    onLanguageChange: (Int) -> Unit = {},
) {
    val s = LocalI18n.current
    var showAppearance by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    var showApi by remember { mutableStateOf(false) }
    var showModel by remember { mutableStateOf(false) }
    var showAgent by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showAddKey by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(top = AuroraDp.dp8)) {
        AuroraTopBar(title = s.profileTitle, actions = { IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, "More") } })
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = AuroraDp.dp16).padding(bottom = bottomPadding), verticalArrangement = Arrangement.spacedBy(AuroraDp.dp12)) {
            item { UserInfoCard(s) }
            item { UsageStatsCard(s) }
            item { Text(s.profileSettings, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = AuroraDp.dp4)) }
            item { RowItem(s.profileAppearance, themeLabels[themeIndex], Icons.Filled.Brush, Color(0xFF7C4DFF)) { showAppearance = true } }
            item { RowItem(s.profileLanguage, languageLabels[languageIndex], Icons.Filled.Language, Color(0xFF4CAF50)) { showLanguage = true } }
            item { RowItem(s.profileApiSettings, s.apiDescription, Icons.Filled.Api, Color(0xFF2196F3)) { showApi = true } }
            item { RowItem(s.profileModelSettings, "Default: Gemini 2.5 Pro", Icons.Filled.Psychology, Color(0xFFFF9800)) { showModel = true } }
            item { RowItem(s.profileAgentSettings, "Configure autonomous agents", Icons.Filled.SmartToy, Color(0xFFE91E63)) { showAgent = true } }
            item { HorizontalDivider(Modifier.padding(vertical = AuroraDp.dp4), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            item { RowItem(s.profileAbout, "Version 1.0.0", Icons.Filled.Info, Color(0xFF607D8B), false) { showAbout = true } }
            item {
                Surface(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(AuroraDp.dp16), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)) {
                    Row(Modifier.fillMaxWidth().padding(AuroraDp.dp16), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(AuroraDp.dp20))
                        Spacer(Modifier.width(AuroraDp.dp8))
                        Text(s.profileLogout, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    AppearanceSheet(showAppearance, s, themeIndex, { showAppearance = false }) { onThemeChange(it); showAppearance = false }
    LanguageSheet(showLanguage, s, languageIndex, { showLanguage = false }) { onLanguageChange(it); showLanguage = false }
    ApiSettingsSheet(showApi, s, { showApi = false }, { showAddKey = true })
    ModelSettingsSheet(showModel) { showModel = false }
    AgentSettingsSheet(showAgent) { showAgent = false }
    AboutSheet(showAbout, s) { showAbout = false }
    AddApiKeySheet(showAddKey, s) { showAddKey = false }
}

@Composable private fun UserInfoCard(s: I18nStrings) {
    AuroraCard(Modifier.fillMaxWidth(), cornerRadius = AuroraDp.dp24) {
        Row(Modifier.fillMaxWidth().padding(AuroraDp.dp20), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(AuroraDp.dp64).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AuroraDp.dp32))
            }
            Spacer(Modifier.width(AuroraDp.dp16))
            Column(Modifier.weight(1f)) { Text("Aurora User", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(AuroraDp.dp4)); Text("aurora.user@email.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(s.profileEdit, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable private fun UsageStatsCard(s: I18nStrings) {
    AuroraCard(Modifier.fillMaxWidth(), cornerRadius = AuroraDp.dp24) {
        Row(Modifier.fillMaxWidth().padding(AuroraDp.dp20), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat(s.profileRequests, "1,247", "this month"); Stat(s.profileTokens, "2.4M", "total used"); Stat(s.profileModels, "3", "connected")
        }
    }
}

@Composable private fun Stat(l: String, v: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(v, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(AuroraDp.dp2))
        Text(l, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable private fun RowItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, arrow: Boolean = true, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(AuroraDp.dp16), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp16, vertical = AuroraDp.dp14), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(AuroraDp.dp36).clip(RoundedCornerShape(AuroraDp.dp10)).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(AuroraDp.dp20)) }
            Spacer(Modifier.width(AuroraDp.dp14))
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (arrow) Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(AuroraDp.dp16))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AppearanceSheet(visible: Boolean, s: I18nStrings, cur: Int, onDismiss: () -> Unit, onSel: (Int) -> Unit) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text(s.profileAppearance, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AuroraDp.dp20))
            themeLabels.forEachIndexed { i, lb -> Surface(onClick = { onSel(i) }, modifier = Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp4), shape = RoundedCornerShape(AuroraDp.dp16), color = if (i == cur) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.padding(AuroraDp.dp16), verticalAlignment = Alignment.CenterVertically) { Icon(themeIcons[i], null, tint = if (i == cur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(AuroraDp.dp12)); Text(lb, style = MaterialTheme.typography.bodyLarge, fontWeight = if (i == cur) FontWeight.SemiBold else FontWeight.Normal) }
            }}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LanguageSheet(visible: Boolean, s: I18nStrings, cur: Int, onDismiss: () -> Unit, onSel: (Int) -> Unit) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text(s.profileLanguage, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AuroraDp.dp20))
            languageLabels.forEachIndexed { i, l -> Surface(onClick = { onSel(i) }, modifier = Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp4), shape = RoundedCornerShape(AuroraDp.dp16), color = if (i == cur) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.padding(AuroraDp.dp16), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(l, style = MaterialTheme.typography.bodyLarge, fontWeight = if (i == cur) FontWeight.SemiBold else FontWeight.Normal); if (i == cur) Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AuroraDp.dp16)) }
            }}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ApiSettingsSheet(visible: Boolean, s: I18nStrings, onDismiss: () -> Unit, onAdd: () -> Unit) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text(s.apiTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AuroraDp.dp20))
            Text(s.apiDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(AuroraDp.dp20))
            FilledTonalButton(onClick = { onDismiss(); onAdd() }, modifier = Modifier.fillMaxWidth().height(AuroraDp.dp48), shape = RoundedCornerShape(AuroraDp.dp16)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(AuroraDp.dp18)); Spacer(Modifier.width(AuroraDp.dp8)); Text(s.apiAddKey, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AddApiKeySheet(visible: Boolean, s: I18nStrings, onDismiss: () -> Unit) {
    if (!visible) return
    var providerIdx by remember { mutableIntStateOf(0) }
    var apiKey by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var showProviderPicker by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    val curEndpoint = endpoint.ifBlank { knownApiProviders[providerIdx].baseUrl }

    if (showProviderPicker) {
        ProviderPickerSheet(knownApiProviders, providerIdx) { idx ->
            providerIdx = idx
            endpoint = knownApiProviders[idx].baseUrl // Auto-fill endpoint
            fetchedModels = emptyList(); fetchError = null
            showProviderPicker = false
        }
        return
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text(s.apiAddKey, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AuroraDp.dp20))

            // Provider
            OutlinedTextField(value = knownApiProviders[providerIdx].name, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), label = { Text(s.apiProvider) },
                shape = RoundedCornerShape(AuroraDp.dp16), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                singleLine = true, trailingIcon = { IconButton(onClick = { showProviderPicker = true }) { Icon(Icons.Filled.KeyboardArrowDown, "Select") } })
            Spacer(Modifier.height(AuroraDp.dp12))

            // API Key
            OutlinedTextField(value = apiKey, onValueChange = { apiKey = it; fetchError = null }, modifier = Modifier.fillMaxWidth(), label = { Text(s.apiKey) }, placeholder = { Text("sk-...") },
                shape = RoundedCornerShape(AuroraDp.dp16), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                singleLine = true, visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { keyVisible = !keyVisible }) { Icon(if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            Spacer(Modifier.height(AuroraDp.dp12))

            // Endpoint
            OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, modifier = Modifier.fillMaxWidth(), label = { Text(s.apiEndpoint) }, placeholder = { Text(curEndpoint) },
                shape = RoundedCornerShape(AuroraDp.dp16), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
            Spacer(Modifier.height(AuroraDp.dp12))

            // Fetch Models button
            OutlinedButton(
                onClick = {
                    val base = endpoint.ifBlank { knownApiProviders[providerIdx].baseUrl }
                    isFetching = true; fetchError = null; fetchedModels = emptyList()
                    scope.launch {
                        val result = com.aurora.ai.services.ApiService.fetchModels(base, apiKey)
                        isFetching = false
                        if (result.success) fetchedModels = result.models
                        else fetchError = result.error ?: "Unknown error"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(AuroraDp.dp44),
                shape = RoundedCornerShape(AuroraDp.dp16),
                enabled = apiKey.isNotBlank() && !isFetching,
            ) {
                if (isFetching) {
                    CircularProgressIndicator(Modifier.size(AuroraDp.dp18), strokeWidth = 2.dp)
                    Spacer(Modifier.width(AuroraDp.dp8))
                    Text("Fetching...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(AuroraDp.dp18))
                    Spacer(Modifier.width(AuroraDp.dp8))
                    Text("Fetch Available Models", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Error message
            if (fetchError != null) {
                Spacer(Modifier.height(AuroraDp.dp8))
                Text(fetchError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            // Fetched models
            if (fetchedModels.isNotEmpty()) {
                Spacer(Modifier.height(AuroraDp.dp12))
                Text("Available Models (${fetchedModels.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(AuroraDp.dp8))
                Column(verticalArrangement = Arrangement.spacedBy(AuroraDp.dp4)) {
                    fetchedModels.take(10).forEach { model ->
                        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(AuroraDp.dp12), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Text(model, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = AuroraDp.dp12, vertical = AuroraDp.dp8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (fetchedModels.size > 10) Text("+ ${fetchedModels.size - 10} more models", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(AuroraDp.dp20))
            FilledTonalButton(onClick = { onDismiss() }, modifier = Modifier.fillMaxWidth().height(AuroraDp.dp48), shape = RoundedCornerShape(AuroraDp.dp16), enabled = apiKey.isNotBlank()) {
                Text(s.apiSave, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ProviderPickerSheet(providers: List<ApiProvider>, current: Int, onSelect: (Int) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onSelect(current) }, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text("Select Provider", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AuroraDp.dp20))
            providers.forEachIndexed { i, p ->
                Surface(onClick = { onSelect(i) }, modifier = Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp4), shape = RoundedCornerShape(AuroraDp.dp16),
                    color = if (i == current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Column(Modifier.padding(AuroraDp.dp16)) {
                        Text(p.name, style = MaterialTheme.typography.titleSmall, fontWeight = if (i == current) FontWeight.SemiBold else FontWeight.Medium)
                        if (p.baseUrl.isNotEmpty()) Text(p.baseUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun ModelSettingsSheet(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return; ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text("Model Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(AuroraDp.dp20))
            listOf("Gemini 2.5 Pro" to "Fast · Vision · 1M Context", "GPT-5" to "Advanced · Multimodal", "Claude 4" to "Long context · Safe", "DeepSeek V4" to "Open source · Code", "Custom API" to "Your endpoint").forEach { (n, d) ->
                Surface(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp4), shape = RoundedCornerShape(AuroraDp.dp16), color = MaterialTheme.colorScheme.surfaceContainerHigh) { Column(Modifier.padding(AuroraDp.dp16)) { Text(n, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium); Text(d, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun AgentSettingsSheet(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return; ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text("Agent Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(AuroraDp.dp20))
            Text("Configure autonomous agent behavior.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(AuroraDp.dp16))
            listOf("Max Steps: 10", "Auto-approve: Off", "Timeout: 5 min", "Safety Level: High").forEach { s -> Surface(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp4), shape = RoundedCornerShape(AuroraDp.dp16), color = MaterialTheme.colorScheme.surfaceContainerHigh) { Text(s, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(AuroraDp.dp16)) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun AboutSheet(visible: Boolean, s: I18nStrings, onDismiss: () -> Unit) {
    if (!visible) return; ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AuroraDp.dp24).padding(bottom = AuroraDp.dp32)) {
            Text(s.profileAbout, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(AuroraDp.dp20))
            listOf("Version" to "1.0.0", "Build" to "2026.06.06", "SDK" to "Android 16 (API 36)", "License" to "Apache 2.0", "Author" to "Aurora AI Team").forEach { (l, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = AuroraDp.dp6), horizontalArrangement = Arrangement.SpaceBetween) { Text(l, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(v, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) }
            }
        }
    }
}
