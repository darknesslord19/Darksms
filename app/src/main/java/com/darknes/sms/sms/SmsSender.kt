package com.darknes.sms.sms

import android.content.Context
import android.os.Build
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
