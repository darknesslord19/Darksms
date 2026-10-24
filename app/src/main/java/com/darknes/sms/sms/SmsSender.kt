package com.darknes.sms.sms

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import com.darknes.sms.DarknesApp
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.data.ThreadEntity

/**
 * Giden SMS gonderimi. Metni operator uzerinden yollar, veritabanina
 * "giden" olarak yazar ve konusma basligini gunceller.
 */
object SmsSender {

    suspend fun send(context: Context, address: String, body: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") SmsManager.getDefault()
        }
        val parts = smsManager.divideMessage(body)
        smsManager.sendMultipartTextMessage(address, null, parts, null, null)

        // Sistem SMS kutusuna "giden" olarak yaz (kalicilik + diger
        // uygulamalarla tutarlilik). Varsayilan SMS uygulamasiyken izinlidir.
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (_: Exception) { /* saglayiciya yazilamazsa yerel kayit yeterli */ }

        val app = context.applicationContext as DarknesApp
        val dao = app.database.messageDao()
        val now = System.currentTimeMillis()
        val existing = dao.thread(address)

        dao.insertMessage(
            MessageEntity(
                threadAddress = address,
                body = body,
                type = MessageEntity.TYPE_SENT,
                timestamp = now,
                read = true,
                delivered = true
            )
        )
        dao.upsertThread(
            ThreadEntity(
                address = address,
                displayName = existing?.displayName,
                snippet = body,
                timestamp = now,
                unread = 0,
                hidden = existing?.hidden ?: false
            )
        )
    }
}
