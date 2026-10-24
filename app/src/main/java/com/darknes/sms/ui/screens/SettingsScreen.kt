package com.darknes.sms.ui.screens

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
    var pinSet by remember { mutableStateOf(vm.vault.isPinSet) }

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
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {

            Text("Gizli kasa", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Kasa PIN'i belirlenince, ana ekrandaki arama alanina bu PIN'i " +
                    "yazip arama tusuna basarak kasayi acabilirsin. Kasadaki " +
                    "konusmalar ana listede ve bildirimlerde gorunmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))

            ListItem(
                headlineContent = { Text(if (pinSet) "Kasa PIN'ini degistir" else "Kasa PIN'i belirle") },
                trailingContent = {
                    TextButton(onClick = { showPinDialog = true }) {
                        Text(if (pinSet) "Degistir" else "Belirle")
                    }
                }
            )
            if (pinSet) {
                ListItem(
                    headlineContent = { Text("Kasa PIN'ini kaldir") },
                    trailingContent = {
                        TextButton(onClick = { vm.vault.clearPin(); pinSet = false }) {
                            Text("Kaldir")
                        }
                    }
                )
            }
        }
    }

    if (showPinDialog) {
        var p1 by remember { mutableStateOf("") }
        var p2 by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Kasa PIN'i") },
            text = {
                Column {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it; err = null },
                        label = { Text("Yeni PIN") }, singleLine = true,
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
                        else -> { vm.vault.setPin(p1); pinSet = true; showPinDialog = false }
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("Vazgec") } }
        )
    }
}
