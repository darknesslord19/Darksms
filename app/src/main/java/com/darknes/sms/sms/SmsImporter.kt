package com.darknes.sms.sms

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.darknes.sms.data.MessageDao
import com.darknes.sms.data.MessageEntity
import com.darknes.sms.data.ThreadEntity

/**
 * Telefonun sistem SMS saglayicisindaki (content://sms) tum mesajlari okuyup
 * yerel Room veritabanina aktarir. Boylece uygulama, Google Messages gibi
 * cihazdaki mevcut tum yazismalari gosterir.
 *
 * Her calismada mesaj tablosu yeniden kurulur (saglayici kaynak kabul edilir),
 * ancak konusmalarin "gizli kasa" durumu (hidden) korunur.
 */
object SmsImporter {

    suspend fun importAll(context: Context, dao: MessageDao) {
        val cr = context.contentResolver

        // Once mevcut gizli-kasa durumlarini sakla (address -> hidden)
        val hiddenMap = HashMap<String, Boolean>()
        val displayMap = HashMap<String, String?>()

        // Saglayicidan tum SMS'leri cek
        val cursor: Cursor = cr.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ
            ),
            null, null,
            Telephony.Sms.DATE + " ASC"
        ) ?: return

        // address -> son mesaj bilgisi
        data class Agg(var snippet: String, var time: Long, var unread: Int)
        val agg = LinkedHashMap<String, Agg>()

        val messages = ArrayList<MessageEntity>()

        cursor.use { c ->
            val iAddr = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val iBody = c.getColumnIndex(Telephony.Sms.BODY)
            val iDate = c.getColumnIndex(Telephony.Sms.DATE)
            val iType = c.getColumnIndex(Telephony.Sms.TYPE)
            val iRead = c.getColumnIndex(Telephony.Sms.READ)

            while (c.moveToNext()) {
                val rawAddr = c.getString(iAddr) ?: continue
                val addr = rawAddr.trim()
                if (addr.isEmpty()) continue
                val body = c.getString(iBody) ?: ""
                val date = c.getLong(iDate)
                val sysType = c.getInt(iType)
                val read = c.getInt(iRead) == 1

                // Sistem turu: 1=inbox, 2=sent. Digerlerini (taslak vs) atla.
                val type = when (sysType) {
                    Telephony.Sms.MESSAGE_TYPE_INBOX -> MessageEntity.TYPE_INBOX
                    Telephony.Sms.MESSAGE_TYPE_SENT -> MessageEntity.TYPE_SENT
                    else -> continue
                }

                messages.add(
                    MessageEntity(
                        threadAddress = addr,
                        body = body,
                        type = type,
                        timestamp = date,
                        read = read || type == MessageEntity.TYPE_SENT
                    )
                )

                val a = agg.getOrPut(addr) { Agg(body, date, 0) }
                a.snippet = body
                a.time = date
                if (type == MessageEntity.TYPE_INBOX && !read) a.unread += 1
            }
        }

        // Rehberden isim coz
        for (addr in agg.keys) {
            displayMap[addr] = lookupContactName(context, addr)
        }

        // Mevcut gizli durumlari koru
        for (addr in agg.keys) {
            hiddenMap[addr] = dao.thread(addr)?.hidden ?: false
        }

        // Yeniden kur: mesajlari temizle, saglayicidan gelenleri yaz
        dao.clearAllMessages()
        for (m in messages) dao.insertMessage(m)

        // Konusmalari guncelle (gizli durum korunarak)
        for ((addr, a) in agg) {
            dao.upsertThread(
                ThreadEntity(
                    address = addr,
                    displayName = displayMap[addr],
                    snippet = a.snippet,
                    timestamp = a.time,
                    unread = a.unread,
                    hidden = hiddenMap[addr] ?: false
                )
            )
        }
    }

    /** Numaraya karsilik gelen rehber adini dondurur (yoksa null). */
    private fun lookupContactName(context: Context, number: String): String? {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (e: SecurityException) {
            null // rehber izni yoksa sorun degil
        }
    }
}
