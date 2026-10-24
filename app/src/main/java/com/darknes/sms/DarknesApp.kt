package com.darknes.sms

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.darknes.sms.data.AppDatabase

class DarknesApp : Application() {

    val database by lazy { AppDatabase.get(this) }

    override fun onCreate() {
        super.onCreate()
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            // Normal mesaj bildirimleri
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Mesajlar",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            // Not: Gizli kasa icin BILINCLI olarak bildirim kanali yok.
            // Kasadaki kisilerin mesajlari hic bildirim uretmez.
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "messages"
    }
}
