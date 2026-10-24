package com.darknes.sms.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.darknes.sms.DarknesApp
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.data.ThreadEntity
import com.darknes.sms.data.VaultManager
import com.darknes.sms.sms.SmsImporter
import com.darknes.sms.sms.SmsSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as DarknesApp).database.messageDao()
    val vault = VaultManager(app)

    // Ana ekran: gizli olmayan konusmalar
    val inbox: StateFlow<List<ThreadEntity>> =
        dao.threads(hidden = false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Kasa ekrani: gizli konusmalar (sadece kilit acilinca gosterilir)
    val vaultThreads: StateFlow<List<ThreadEntity>> =
        dao.threads(hidden = true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentAddress = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> =
        currentAddress.flatMapLatest { addr ->
            if (addr == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else dao.messages(addr)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Telefondaki mevcut SMS'leri sistem saglayicisindan yukler. */
    fun importMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try { SmsImporter.importAll(getApplication(), dao) } catch (_: Exception) {}
        }
    }

    fun openThread(address: String) {
        currentAddress.value = address
        viewModelScope.launch { dao.clearUnread(address) }
    }

    fun send(address: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch { SmsSender.send(getApplication(), address, body) }
    }

    fun forward(toAddress: String, body: String) {
        viewModelScope.launch { SmsSender.send(getApplication(), toAddress, body) }
    }

    /** Sadece KENDI giden mesajini duzenler; "duzenlendi" etiketi eklenir. */
    fun editOwnMessage(id: Long, newBody: String) {
        viewModelScope.launch {
            val m = dao.message(id) ?: return@launch
            if (m.type != MessageEntity.TYPE_SENT) return@launch // gelen mesaj degistirilemez
            dao.updateMessage(m.copy(body = newBody, edited = true, editedAt = System.currentTimeMillis()))
        }
    }

    fun deleteMessage(id: Long) = viewModelScope.launch { dao.deleteMessage(id) }

    fun moveToVault(address: String) = viewModelScope.launch { dao.setHidden(address, true) }
    fun removeFromVault(address: String) = viewModelScope.launch { dao.setHidden(address, false) }

    fun deleteThread(address: String) = viewModelScope.launch {
        dao.deleteThreadMessages(address)
        dao.deleteThread(address)
    }

    fun search(q: String): Flow<List<MessageEntity>> = dao.search(q)
}
