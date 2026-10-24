package com.darknes.sms.sms

import android.content.Context
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import com.darknes.sms.DarknesApp
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.data.ThreadEntity
import java.io.File
import java.io.FileOutputStream

/**
 * Giden MMS (multimedya) gonderimi. Secilen medya + metinden bir M-Send.req
 * PDU uretir, onbellege yazar ve platformun sendMultimediaMessage API'si ile
 * yollar. Ardindan mesaji yerel veritabanina (goruntuleme icin) kaydeder;
 * medyayi kalici olmasi icin uygulama deposuna kopyalar.
 */
object MmsSender {

    suspend fun send(context: Context, address: String, body: String?, mediaUri: Uri) {
        val cr = context.contentResolver
        val mime = cr.getType(mediaUri) ?: "image/jpeg"
        val bytes = cr.openInputStream(mediaUri)?.use { it.readBytes() } ?: return

        // PDU olustur ve onbellege yaz
        val pdu = MmsPdu.buildSendReq(address, body, bytes, mime)
        val fileName = "send.${System.currentTimeMillis()}.dat"
        val pduFile = File(context.cacheDir, fileName)
        FileOutputStream(pduFile).use { it.write(pdu) }

        val contentUri = MmsFileProvider.uriFor(context, fileName)
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") SmsManager.getDefault()
        }
        smsManager.sendMultimediaMessage(context, contentUri, null, null, null)

        // Medyayi kalici depoya kopyala (goruntulemenin surekliligi icin)
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"; "image/gif" -> "gif"; "image/webp" -> "webp"; else -> "jpg"
        }
        val mediaDir = File(context.filesDir, "mms").apply { mkdirs() }
        val saved = File(mediaDir, "m.${System.currentTimeMillis()}.$ext")
        saved.writeBytes(bytes)

        // Yerel kayit + konusma guncelleme
        val app = context.applicationContext as DarknesApp
        val dao = app.database.messageDao()
        val now = System.currentTimeMillis()
        val existing = dao.thread(address)

        dao.insertMessage(
            MessageEntity(
                threadAddress = address,
                body = body ?: "",
                type = MessageEntity.TYPE_SENT,
                timestamp = now,
                read = true,
                delivered = true,
                attachmentUri = saved.absolutePath,
                attachmentMime = mime
            )
        )
        dao.upsertThread(
            ThreadEntity(
                address = address,
                displayName = existing?.displayName,
                snippet = if (body.isNullOrBlank()) "[Medya]" else body,
                timestamp = now,
                unread = 0,
                hidden = existing?.hidden ?: false
            )
        )
    }
}
