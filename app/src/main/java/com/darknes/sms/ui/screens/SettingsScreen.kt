package com.darknes.sms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.darknes.sms.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    var showPinDialog by remember { mutableStateOf(false) }

    // Gizli tetik: "Surum" satirina ust uste dokunma sayaci.
    // 7 dokunusta kasa PIN ekrani acilir. Ekranda hicbir ipucu yoktur.
    var tapCount by remember { mutableStateOf(0) }
    var lastTap by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            ListItem(
                headlineContent = { Text("Bildirimler") },
                supportingContent = { Text("Yeni mesaj bildirimleri aciktir") }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Uygulama") },
                supportingContent = { Text("Darknes SMS") }
            )
            HorizontalDivider()

            // Sira disi gorunmeyen gizli tetik.
            ListItem(
                headlineContent = { Text("Surum") },
                supportingContent = { Text("1.0") },
                modifier = Modifier.clickable {
                    val now = System.currentTimeMillis()
                    tapCount = if (now - lastTap < 1200) tapCount + 1 else 1
                    lastTap = now
                    if (tapCount >= 7) {
                        tapCount = 0
                        showPinDialog = true
                    }
                }
            )
        }
    }

    if (showPinDialog) {
        var p1 by remember { mutableStateOf("") }
        var p2 by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }
        val alreadySet = vm.vault.isPinSet
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (alreadySet) "PIN degistir" else "PIN belirle") },
            text = {
                Column {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it; err = null },
                        label = { Text("PIN") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = p2, onValueChange = { p2 = it; err = null },
                        label = { Text("Tekrar") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                    err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        p1.length < 4 -> err = "En az 4 haneli olmali"
                        p1 != p2 -> err = "Eslesmiyor"
                        else -> { vm.vault.setPin(p1); showPinDialog = false }
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = {
                Row {
                    if (alreadySet) {
                        TextButton(onClick = { vm.vault.clearPin(); showPinDialog = false }) {
                            Text("Kaldir")
                        }
                    }
                    TextButton(onClick = { showPinDialog = false }) { Text("Vazgec") }
                }
            }
        )
    }
}
