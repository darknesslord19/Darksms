package com.darknes.sms.sms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.darknes.sms.DarknesApp
import com.darknes.sms.R
import com.darknes.sms.ui.MainActivity

/**
 * Normal (gizli olmayan) mesajlar icin bildirim gosterir.
 * Gizli kasadaki konusmalar bu yolu hic kullanmaz.
 */
object Notifier {

    fun showIncoming(context: Context, address: String, name: String?, body: String) {
        val nm = context.getSystemService(NotificationManager::class.java)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_thread", address)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, address.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, DarknesApp.CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.sym_action_email)
            .setContentTitle(name ?: address)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        nm.notify(address.hashCode(), notif)
    }
}
