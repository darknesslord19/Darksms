package com.darknes.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darknes.sms.ui.MainViewModel

/**
 * Gizli kasa icerigi. Buraya yalnizca dogru PIN girilince gelinir.
 * Buradaki konusmalar ana listede ve bildirimlerde gorunmez.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    vm: MainViewModel,
    onOpenThread: (String) -> Unit,
    onBack: () -> Unit
) {
    val threads by vm.vaultThreads.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gizli kasa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { pad ->
        if (threads.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Kasa bos. Bir konusmayi menuden\n\"Gizli kasaya tasi\" ile buraya alabilirsin.",
                    color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(Modifier.padding(pad).fillMaxSize()) {
                items(threads, key = { it.address }) { t ->
                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            ThreadRow(
                                title = t.displayName ?: t.address,
                                snippet = t.snippet,
                                time = t.timestamp,
                                unread = t.unread,
                                onClick = { onOpenThread(t.address) }
                            )
                        }
                    }
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        TextButton(onClick = { vm.removeFromVault(t.address) }) {
                            Text("Kasadan cikar")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
