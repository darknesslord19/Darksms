package com.darknes.sms.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    // Varsayilan SMS uygulamasi olma istegi sonucu -> donunce mesajlari yukle
    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            vm.importMessages()
        }

    // Calisma-ani izin istegi sonucu -> izin gelince mesajlari yukle
    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            vm.importMessages()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        requestDefaultSmsIfNeeded()

        setContent {
            val dark = isSystemInDarkTheme()
            val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val ctx = LocalContext.current
                if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
            } else {
                if (dark) darkColorScheme() else androidx.compose.material3.lightColorScheme()
            }
            MaterialTheme(colorScheme = colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Her one geldiginde mevcut mesajlari tazele (izin varsa).
        if (hasSmsReadPermission()) vm.importMessages()
    }

    private fun hasSmsReadPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestRuntimePermissions() {
        val needed = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val toAsk = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toAsk.isNotEmpty()) permLauncher.launch(toAsk.toTypedArray())
    }

    private fun requestDefaultSmsIfNeeded() {
        val isDefault = Telephony.Sms.getDefaultSmsPackage(this) == packageName
        if (isDefault) { vm.importMessages(); return }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_SMS) && !rm.isRoleHeld(RoleManager.ROLE_SMS)) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
            }
        } else {
            @Suppress("DEPRECATION")
            val intent = android.content.Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivity(intent)
        }
    }
}
