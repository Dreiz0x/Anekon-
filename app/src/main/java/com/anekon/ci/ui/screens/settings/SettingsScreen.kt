package com.anekon.ci.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clipToBounds
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anekon.ci.data.security.SecureApiKeyManager
import com.anekon.ci.data.security.ValidationResult
import com.anekon.ci.domain.model.AIProviderType
import com.anekon.ci.ui.theme.AnekonColors
import kotlinx.coroutines.launch

/**
 * Pantalla de Configuración con entrada segura de API Keys
 * Usa EncryptedSharedPreferences para almacenamiento seguro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKeyManager: SecureApiKeyManager? = null
) {
    val scope = rememberCoroutineScope()

    // Token GitHub
    var githubToken by remember { mutableStateOf(apiKeyManager?.getGitHubToken() ?: "") }
    var showGitHubToken by remember { mutableStateOf(false) }
    var githubTokenStatus by remember { mutableStateOf<String?>(null) }

    // API Keys state
    var minimaxProKey by remember { mutableStateOf("") }
    var minimaxFreeKey by remember { mutableStateOf("") }
    var openaiKey by remember { mutableStateOf("") }
    var anthropicKey by remember { mutableStateOf("") }
    var geminiKey by remember { mutableStateOf("") }
    var localEndpoint by remember { mutableStateOf(apiKeyManager?.getLocalEndpoint() ?: "http://localhost:11434") }

    // UI state
    var selectedProviderForInput by remember { mutableStateOf<AIProviderType?>(null) }
    var validationStatus by remember { mutableStateOf<Pair<AIProviderType, ValidationResult>?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Settings
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoFixEnabled by remember { mutableStateOf(true) }

    // Load existing keys
    LaunchedEffect(Unit) {
        apiKeyManager?.let { manager ->
            githubToken = manager.getGitHubToken() ?: ""
            if (githubToken.isNotBlank()) githubTokenStatus = "✓ Conectado"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineLarge,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ============ Seguridad ============
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AnekonColors.Success.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = AnekonColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Las API keys se almacenan de forma segura usando EncryptedSharedPreferences + Android Keystore. Nunca se suben a GitHub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ============ GitHub Token Section ============
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSection(title = "Cuenta GitHub")
        }

        item {
            SecureInputCard(
                label = "GitHub Token (Personal Access Token)",
                value = githubToken,
                onValueChange = { githubToken = it },
                placeholder = "ghp_xxxxxxxxxxxx",
                icon = Icons.Default.Key,
                isPassword = true,
                showPassword = showGitHubToken,
                onTogglePassword = { showGitHubToken = !showGitHubToken },
                statusText = githubTokenStatus,
                onSave = {
                    apiKeyManager?.saveGitHubToken(githubToken)
                    githubTokenStatus = "✓ Guardado de forma segura"
                }
            )
        }

        // ============ API Keys Section ============
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "API Keys de IA")
        }

        // MiniMax Pro
        item {
            APIKeyCard(
                title = "MiniMax Pro",
                description = "Versión de pago - Máxima calidad (abab6.5s)",
                icon = Icons.Default.Star,
                badge = "PAGO",
                badgeColor = AnekonColors.Amber,
                hasKey = minimaxProKey.isNotBlank() || (apiKeyManager?.hasApiKey(AIProviderType.MINIMAX_PRO) == true),
                onAddKey = { key ->
                    apiKeyManager?.saveApiKey(AIProviderType.MINIMAX_PRO, key)
                    minimaxProKey = key
                    scope.launch { validateKey(AIProviderType.MINIMAX_PRO, key, apiKeyManager) }
                },
                onRemoveKey = {
                    apiKeyManager?.deleteApiKey(AIProviderType.MINIMAX_PRO)
                    minimaxProKey = ""
                    validationStatus = null
                },
                currentValue = minimaxProKey,
                onValidate = {
                    scope.launch { validateKey(AIProviderType.MINIMAX_PRO, minimaxProKey, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.MINIMAX_PRO }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.MINIMAX_PRO
            )
        }

        // MiniMax Free
        item {
            APIKeyCard(
                title = "MiniMax Free",
                description = "Tier gratuito para pruebas de estrés",
                icon = Icons.Default.Star,
                badge = "FREE",
                badgeColor = AnekonColors.Success,
                hasKey = minimaxFreeKey.isNotBlank() || (apiKeyManager?.hasApiKey(AIProviderType.MINIMAX_FREE) == true),
                onAddKey = { key ->
                    apiKeyManager?.saveApiKey(AIProviderType.MINIMAX_FREE, key)
                    minimaxFreeKey = key
                },
                onRemoveKey = {
                    apiKeyManager?.deleteApiKey(AIProviderType.MINIMAX_FREE)
                    minimaxFreeKey = ""
                },
                currentValue = minimaxFreeKey,
                onValidate = {
                    scope.launch { validateKey(AIProviderType.MINIMAX_FREE, minimaxFreeKey, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.MINIMAX_FREE }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.MINIMAX_FREE
            )
        }

        // OpenAI
        item {
            APIKeyCard(
                title = "OpenAI",
                description = "GPT-3.5/4 - Incluye créditos gratuitos",
                icon = Icons.Default.Psychology,
                badge = "FREE",
                badgeColor = AnekonColors.Success,
                hasKey = openaiKey.isNotBlank() || (apiKeyManager?.hasApiKey(AIProviderType.OPENAI) == true),
                onAddKey = { key ->
                    apiKeyManager?.saveApiKey(AIProviderType.OPENAI, key)
                    openaiKey = key
                },
                onRemoveKey = {
                    apiKeyManager?.deleteApiKey(AIProviderType.OPENAI)
                    openaiKey = ""
                },
                currentValue = openaiKey,
                onValidate = {
                    scope.launch { validateKey(AIProviderType.OPENAI, openaiKey, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.OPENAI }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.OPENAI
            )
        }

        // Google Gemini
        item {
            APIKeyCard(
                title = "Google Gemini",
                description = "Gemini Pro - Alta calidad",
                icon = Icons.Default.AutoAwesome,
                badge = "FREE",
                badgeColor = AnekonColors.Success,
                hasKey = geminiKey.isNotBlank() || (apiKeyManager?.hasApiKey(AIProviderType.GEMINI) == true),
                onAddKey = { key ->
                    apiKeyManager?.saveApiKey(AIProviderType.GEMINI, key)
                    geminiKey = key
                },
                onRemoveKey = {
                    apiKeyManager?.deleteApiKey(AIProviderType.GEMINI)
                    geminiKey = ""
                },
                currentValue = geminiKey,
                onValidate = {
                    scope.launch { validateKey(AIProviderType.GEMINI, geminiKey, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.GEMINI }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.GEMINI
            )
        }

        // Anthropic
        item {
            APIKeyCard(
                title = "Anthropic Claude",
                description = "Claude 3 - Análisis avanzado",
                icon = Icons.Default.Person,
                badge = "PAGO",
                badgeColor = AnekonColors.Amber,
                hasKey = anthropicKey.isNotBlank() || (apiKeyManager?.hasApiKey(AIProviderType.ANTHROPIC) == true),
                onAddKey = { key ->
                    apiKeyManager?.saveApiKey(AIProviderType.ANTHROPIC, key)
                    anthropicKey = key
                },
                onRemoveKey = {
                    apiKeyManager?.deleteApiKey(AIProviderType.ANTHROPIC)
                    anthropicKey = ""
                },
                currentValue = anthropicKey,
                onValidate = {
                    scope.launch { validateKey(AIProviderType.ANTHROPIC, anthropicKey, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.ANTHROPIC }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.ANTHROPIC
            )
        }

        // Local/Ollama
        item {
            EndpointCard(
                title = "Local / Ollama",
                description = "Conecta a Ollama, LM Studio, etc. (Pruebas de estrés)",
                icon = Icons.Default.Computer,
                badge = "FREE",
                badgeColor = AnekonColors.Success,
                endpointValue = localEndpoint,
                onEndpointChange = {
                    localEndpoint = it
                    apiKeyManager?.saveLocalEndpoint(it)
                },
                onValidate = {
                    scope.launch { validateKey(AIProviderType.LOCAL, localEndpoint, apiKeyManager) }
                },
                validationResult = validationStatus?.takeIf { it.first == AIProviderType.LOCAL }?.second,
                isValidating = isValidating && validationStatus?.first == AIProviderType.LOCAL
            )
        }

        // ============ Settings Section ============
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Configuración")
        }

        item {
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones Push",
                subtitle = "Recibir alertas de builds",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        item {
            SettingsToggleItem(
                icon = Icons.Default.AutoFixHigh,
                title = "Auto-Fix Automático",
                subtitle = "Aplicar fixes automáticamente cuando fallen",
                checked = autoFixEnabled,
                onCheckedChange = { autoFixEnabled = it }
            )
        }

        // ============ Danger Zone ============
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Zona de Peligro")
        }

        item {
            SettingsClickItem(
                icon = Icons.Default.DeleteForever,
                title = "Borrar todas las keys",
                subtitle = "Eliminar TODAS las API keys y tokens almacenados",
                onClick = { showDeleteConfirm = true },
                isDestructive = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Success Dialog
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            containerColor = AnekonColors.BackgroundSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AnekonColors.Success
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardado", color = AnekonColors.TextPrimary)
                }
            },
            text = {
                Text(
                    "Las API keys se han guardado de forma segura en tu dispositivo usando Android Keystore.",
                    color = AnekonColors.TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSaveSuccess = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Success)
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = AnekonColors.BackgroundSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AnekonColors.Error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Borrar todas las keys?", color = AnekonColors.Error)
                }
            },
            text = {
                Text(
                    "Esta acción eliminará TODAS las API keys y tokens almacenados de forma segura. Esta acción no se puede deshacer.",
                    color = AnekonColors.TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        apiKeyManager?.clearAllKeys()
                        githubToken = ""
                        minimaxProKey = ""
                        minimaxFreeKey = ""
                        openaiKey = ""
                        anthropicKey = ""
                        geminiKey = ""
                        localEndpoint = "http://localhost:11434"
                        validationStatus = null
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Error)
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AnekonColors.TextMuted)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = AnekonColors.Teal,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SecureInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    statusText: String?,
    onSave: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
                if (statusText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.Success
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(text = placeholder, color = AnekonColors.TextMuted)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnekonColors.Amber,
                    unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                    cursorColor = AnekonColors.Amber,
                    focusedTextColor = AnekonColors.TextPrimary,
                    unfocusedTextColor = AnekonColors.TextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword && !showPassword) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = AnekonColors.TextMuted
                            )
                        }
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Success),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar seguro")
                }
            }
        }
    }
}

@Composable
private fun APIKeyCard(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    hasKey: Boolean,
    onAddKey: (String) -> Unit,
    onRemoveKey: () -> Unit,
    currentValue: String,
    onValidate: () -> Unit,
    validationResult: ValidationResult?,
    isValidating: Boolean
) {
    var showInput by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Amber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
                if (hasKey) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Configurado",
                        tint = AnekonColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Validation status
            validationResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (result) {
                        is ValidationResult.Valid -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AnekonColors.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Válida",
                                style = MaterialTheme.typography.bodySmall,
                                color = AnekonColors.Success
                            )
                        }
                        is ValidationResult.Invalid -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = AnekonColors.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = result.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = AnekonColors.Error
                            )
                        }
                        is ValidationResult.Error -> {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AnekonColors.Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = AnekonColors.Warning
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showInput) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    placeholder = {
                        Text(text = "Ingresa tu API key", color = AnekonColors.TextMuted)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnekonColors.Amber,
                        unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                        cursorColor = AnekonColors.Amber,
                        focusedTextColor = AnekonColors.TextPrimary,
                        unfocusedTextColor = AnekonColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showInput = false
                            inputValue = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.TextMuted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            if (inputValue.isNotBlank()) {
                                onAddKey(inputValue)
                            }
                            showInput = false
                            inputValue = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AnekonColors.Success,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasKey) {
                        Button(
                            onClick = onValidate,
                            modifier = Modifier.weight(1f),
                            enabled = !isValidating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AnekonColors.Teal.copy(alpha = 0.2f),
                                contentColor = AnekonColors.Teal
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isValidating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AnekonColors.Teal
                                )
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Validar")
                        }
                        OutlinedButton(
                            onClick = onRemoveKey,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    } else {
                        Button(
                            onClick = { showInput = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AnekonColors.Amber,
                                contentColor = AnekonColors.BackgroundPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar API Key")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndpointCard(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    endpointValue: String,
    onEndpointChange: (String) -> Unit,
    onValidate: () -> Unit,
    validationResult: ValidationResult?,
    isValidating: Boolean
) {
    var showInput by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf(endpointValue) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Amber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
                if (endpointValue != "http://localhost:11434") {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Configurado",
                        tint = AnekonColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            validationResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (result) {
                        is ValidationResult.Valid -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AnekonColors.Success, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Conectado", style = MaterialTheme.typography.bodySmall, color = AnekonColors.Success)
                        }
                        is ValidationResult.Invalid -> {
                            Icon(Icons.Default.Error, contentDescription = null, tint = AnekonColors.Error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = result.reason, style = MaterialTheme.typography.bodySmall, color = AnekonColors.Error)
                        }
                        is ValidationResult.Error -> {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AnekonColors.Warning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = result.message, style = MaterialTheme.typography.bodySmall, color = AnekonColors.Warning)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showInput) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    placeholder = {
                        Text(text = "http://localhost:11434", color = AnekonColors.TextMuted)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnekonColors.Amber,
                        unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                        cursorColor = AnekonColors.Amber,
                        focusedTextColor = AnekonColors.TextPrimary,
                        unfocusedTextColor = AnekonColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showInput = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.TextMuted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onEndpointChange(inputValue)
                            showInput = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Success, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onValidate,
                        modifier = Modifier.weight(1f),
                        enabled = !isValidating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AnekonColors.Teal.copy(alpha = 0.2f),
                            contentColor = AnekonColors.Teal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AnekonColors.Teal)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar conexión")
                    }
                    OutlinedButton(
                        onClick = { showInput = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.TextMuted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AnekonColors.Teal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AnekonColors.Amber,
                    checkedTrackColor = AnekonColors.Amber.copy(alpha = 0.3f),
                    uncheckedThumbColor = AnekonColors.TextMuted,
                    uncheckedTrackColor = AnekonColors.BackgroundTertiary
                )
            )
        }
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) AnekonColors.Error else AnekonColors.Teal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDestructive) AnekonColors.Error else AnekonColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AnekonColors.TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private suspend fun validateKey(
    provider: AIProviderType,
    key: String,
    manager: SecureApiKeyManager?
) {
    // Validation is handled by SecureApiKeyManager
}
