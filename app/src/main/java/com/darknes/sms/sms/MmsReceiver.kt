package com.darknes.sms.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * MMS (multimedya) teslim bildirimlerini yakalar. Tam MMS indirme/ayristirma
 * operator APN'ine baglidir; bu iskelet, varsayilan SMS uygulamasi
 * gereksinimini karsilamak icin bileseni tanimlar ve WAP push'u sessizce
 * alir. Gelen medya, cihazin MMS saglayicisi uzerinden cozulur.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Operatore ozel MMS indirme burada genisletilebilir.
        // Varsayilan uygulama olmak icin bilesenin var olmasi yeterlidir.
    }
}
