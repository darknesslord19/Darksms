package com.darknes.sms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.darknes.sms.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    vm: MainViewModel,
    onOpenThread: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onSecretEntered: () -> Unit
) {
    val threads by vm.inbox.collectAsState()
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Darknes SMS") },
                actions = {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Ayarlar") }, onClick = {
                            menu = false; onOpenSettings()
                        })
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            // Arama + gizli giris alani.
            // Buraya ayarlarda belirlenen gizli sifre yazilip "ara"ya basilinca
            // kasa acilir; bu metin normal arama gibi de calisir.
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Ara") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (query.isNotBlank() && vm.vault.verify(query)) {
                        query = ""
                        onSecretEntered()
                    }
                })
            )

            val shown = threads.filter {
                query.isBlank() ||
                    (it.displayName?.contains(query, true) == true) ||
                    it.address.contains(query, true) ||
                    it.snippet.contains(query, true)
            }

            if (shown.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henuz mesaj yok", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(shown, key = { it.address }) { t ->
                        ThreadRow(
                            title = t.displayName ?: t.address,
                            snippet = t.snippet,
                            time = t.timestamp,
                            unread = t.unread,
                            onClick = { onOpenThread(t.address) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadRow(title: String, snippet: String, time: Long, unread: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(snippet, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (time > 0) Text(fmt(time), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            if (unread > 0) {
                Spacer(Modifier.height(4.dp))
                Badge { Text(unread.toString()) }
            }
        }
    }
}

private fun fmt(t: Long): String =
    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(t))
