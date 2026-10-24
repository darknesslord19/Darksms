package com.darknes.sms.sms

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * MMS gonderim/alim sirasinda olusturulan ham PDU dosyasini sistem MMS
 * servisine (farkli surec) acmak icin kucuk bir saglayici. Yalnizca kendi
 * onbellek dizinimizdeki pdu dosyalarini salt-okunur acar.
 */
class MmsFileProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: return null
        val name = uri.lastPathSegment ?: return null
        val file = File(ctx.cacheDir, name)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String = "application/vnd.wap.mms-message"

    override fun query(uri: Uri, p: Array<out String>?, s: String?, a: Array<out String>?, o: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, a: Array<out String>?): Int = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<out String>?): Int = 0

    companion object {
        fun authority(context: Context) = "${context.packageName}.mms"
        fun uriFor(context: Context, fileName: String): Uri =
            Uri.parse("content://${authority(context)}/$fileName")
    }
}
