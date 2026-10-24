package com.darknes.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.darknes.sms.ui.MainViewModel

/**
 * Kasa kilit ekrani. setupMode=true ise ilk kez PIN belirlenir,
 * aksi halde girilen PIN dogrulanir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultLockScreen(
    vm: MainViewModel,
    setupMode: Boolean,
    onUnlocked: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (setupMode) "Kasa PIN belirle" else "Gizli kasa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Icon(Icons.Default.Lock, contentDescription = null,
                modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it; error = null },
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            if (setupMode) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = null },
                    label = { Text("PIN (tekrar)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (setupMode) {
                        when {
                            pin.length < 4 -> error = "PIN en az 4 haneli olmali"
                            pin != confirm -> error = "PIN'ler eslesmiyor"
                            else -> { vm.vault.setPin(pin); onUnlocked() }
                        }
                    } else {
                        if (vm.vault.verify(pin)) onUnlocked()
                        else error = "Yanlis PIN"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (setupMode) "Kaydet" else "Kiliti ac") }
        }
    }
}
