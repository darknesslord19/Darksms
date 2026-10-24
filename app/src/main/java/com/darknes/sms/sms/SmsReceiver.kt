package com.darknes.sms.sms

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.darknes.sms.DarknesApp
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.data.ThreadEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Varsayilan SMS uygulamasiyken gelen SMS'leri yakalar (SMS_DELIVER).
 * Mesaji veritabanina yazar. Eger konusma gizli kasadaysa HICBIR
 * bildirim gostermez; aksi halde normal bildirim cikar.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val address = messages[0].displayOriginatingAddress ?: return
        val body = messages.joinToString("") { it.displayMessageBody ?: "" }
        val now = System.currentTimeMillis()

        val app = context.applicationContext as DarknesApp
        val dao = app.database.messageDao()
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = dao.thread(address)
                val hidden = existing?.hidden ?: false

                // Sistem gelen kutusuna yaz (varsayilan SMS uygulamasinin gorevi).
                try {
                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, address)
                        put(Telephony.Sms.BODY, body)
                        put(Telephony.Sms.DATE, now)
                        put(Telephony.Sms.READ, 0)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                } catch (_: Exception) { /* yerel kayit yeterli */ }

                dao.insertMessage(
                    MessageEntity(
                        threadAddress = address,
                        body = body,
                        type = MessageEntity.TYPE_INBOX,
                        timestamp = now,
                        read = false
                    )
                )
                dao.upsertThread(
                    ThreadEntity(
                        address = address,
                        displayName = existing?.displayName,
                        snippet = body,
                        timestamp = now,
                        unread = (existing?.unread ?: 0) + 1,
                        hidden = hidden
                    )
                )

                // Kasadaki konusmalar sessizdir: bildirim yok.
                if (!hidden) {
                    Notifier.showIncoming(context, address, existing?.displayName, body)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
