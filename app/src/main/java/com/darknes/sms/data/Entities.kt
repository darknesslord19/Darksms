package com.darknes.sms.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bir konusma basligi (bir telefon numarasi / kisi ile tum yazisma).
 * hidden = true ise bu konusma gizli kasada durur; ana listede ve
 * bildirimlerde hic gorunmez.
 */
@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey val address: String,       // normalize edilmis numara, konusmanin kimligi
    val displayName: String? = null,       // rehberdeki ad (varsa)
    val snippet: String = "",              // son mesaj onizlemesi
    val timestamp: Long = 0L,              // son mesaj zamani
    val unread: Int = 0,                   // okunmamis sayisi
    val hidden: Boolean = false,           // gizli kasada mi
    val archived: Boolean = false
)

/**
 * Tek bir mesaj. type alani mesajin yonunu belirtir.
 * edited = kullanicinin KENDI gonderdigi mesaji sonradan degistirmesi
 *          (iMessage/WhatsApp'taki "duzenlendi" ile ayni mantik).
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadAddress: String,
    val body: String,
    val type: Int,                         // 1=gelen, 2=giden
    val timestamp: Long,
    val read: Boolean = false,
    val delivered: Boolean = false,
    val failed: Boolean = false,
    val attachmentUri: String? = null,     // multimedya eki (resim/video/ses)
    val attachmentMime: String? = null,
    val edited: Boolean = false,           // sadece giden mesajlar icin
    val editedAt: Long? = null
) {
    companion object {
        const val TYPE_INBOX = 1
        const val TYPE_SENT = 2
    }
}
