package com.darknes.sms.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * "Mesajla yanitla" (RESPOND_VIA_MESSAGE) icin basliksiz servis.
 * Varsayilan SMS uygulamasi olmak icin gereklidir (gelen aramayi
 * hizli yanitla gibi). Minimal implementasyon yeterlidir.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
