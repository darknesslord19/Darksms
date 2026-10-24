package com.darknes.sms.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Gizli kasanin PIN'ini yonetir. PIN, cihaz anahtari ile sifrelenmis
 * tercihlere SHA-256 ozeti olarak yazilir; duz metin olarak hic tutulmaz.
 *
 * Kasa, WhatsApp'in kilitli sohbetleri gibi dusunulmeli: kullanici kendi
 * cihazindaki kendi mesajlarini bir PIN arkasina koyar. Baska birini
 * kandirmaya yonelik gizli/sahte icerik uretmez.
 */
class VaultManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "darknes_vault",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val isPinSet: Boolean get() = prefs.contains(KEY_PIN)

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, hash(pin)).apply()
    }

    fun verify(pin: String): Boolean =
        prefs.getString(KEY_PIN, null) == hash(pin)

    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    private fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object { private const val KEY_PIN = "vault_pin" }
}
