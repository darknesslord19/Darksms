package com.darknes.sms.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: MainViewModel, address: String, onBack: () -> Unit) {
    LaunchedEffect(address) { vm.openThread(address) }
    val messages by vm.currentMessages.collectAsState()
    var input by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MessageEntity?>(null) }
    var forwarding by remember { mutableStateOf<MessageEntity?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Multimedya eki secici (resim/video/ses). Secilen ek, mesaj olarak eklenir.
    var attachment by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> attachment = uri }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(address) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Gizli kasaya tasi") }, onClick = {
                            menu = false; vm.moveToVault(address); onBack()
                        })
                        DropdownMenuItem(text = { Text("Konusmayi sil") }, onClick = {
                            menu = false; vm.deleteThread(address); onBack()
                        })
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column {
                    attachment?.let { uri ->
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = uri, contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text("Ek hazir", Modifier.weight(1f))
                            TextButton(onClick = { attachment = null }) { Text("Kaldir") }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { picker.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = "Ek")
                        }
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (editing != null) "Mesaji duzenle" else "Mesaj") },
                            maxLines = 5
                        )
                        IconButton(onClick = {
                            val e = editing
                            val att = attachment
                            when {
                                e != null -> { vm.editOwnMessage(e.id, input); editing = null }
                                att != null -> vm.sendMms(address, input.ifBlank { null }, att)
                                input.isNotBlank() -> vm.send(address, input)
                            }
                            input = ""
                            attachment = null
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gonder")
                        }
                    }
                }
            }
        }
    ) { pad ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(pad).fillMaxSize().padding(horizontal = 12.dp)
        ) {
            items(messages, key = { it.id }) { m ->
                MessageBubble(
                    msg = m,
                    onEdit = { editing = m; input = m.body },      // sadece giden mesajlarda gorunur
                    onForward = { forwarding = m },
                    onDelete = { vm.deleteMessage(m.id) }
                )
            }
        }
    }

    // Iletme hedefi girisi
    forwarding?.let { m ->
        var to by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { forwarding = null },
            title = { Text("Mesaji ilet") },
            text = {
                OutlinedTextField(value = to, onValueChange = { to = it },
                    label = { Text("Alici numarasi") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (to.isNotBlank()) vm.forward(to, m.body)
                    forwarding = null
                }) { Text("Ilet") }
            },
            dismissButton = { TextButton(onClick = { forwarding = null }) { Text("Vazgec") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBubble(
    msg: MessageEntity,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onDelete: () -> Unit
) {
    val outgoing = msg.type == MessageEntity.TYPE_SENT
    var menu by remember { mutableStateOf(false) }
    val bg = if (outgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (outgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier
                .background(Color.Transparent)
                .combinedClickableCompat { menu = true }
                .padding(12.dp)
            ) {
                msg.attachmentUri?.let { path ->
                    val model: Any = if (path.startsWith("content://") || path.startsWith("file://"))
                        path else java.io.File(path)
                    AsyncImage(model = model, contentDescription = null,
                        modifier = Modifier.sizeIn(maxHeight = 220.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(6.dp))
                }
                if (msg.body.isNotEmpty()) Text(msg.body, color = fg)
                if (msg.edited) {
                    Spacer(Modifier.height(2.dp))
                    Text("duzenlendi", style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.7f))
                }
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                // "Duzenle" SADECE kendi gonderdigin mesajlarda; gelen mesaj degistirilemez.
                if (outgoing) {
                    DropdownMenuItem(text = { Text("Duzenle") }, onClick = { menu = false; onEdit() })
                }
                DropdownMenuItem(text = { Text("Ilet") }, onClick = { menu = false; onForward() })
                DropdownMenuItem(text = { Text("Sil") }, onClick = { menu = false; onDelete() })
            }
        }
    }
}
