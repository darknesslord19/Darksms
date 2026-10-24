package com.darknes.sms.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darknes.sms.ui.screens.ChatScreen
import com.darknes.sms.ui.screens.ConversationsScreen
import com.darknes.sms.ui.screens.SettingsScreen
import com.darknes.sms.ui.screens.VaultLockScreen
import com.darknes.sms.ui.screens.VaultScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "conversations") {

        composable("conversations") {
            ConversationsScreen(
                vm = vm,
                onOpenThread = { nav.navigate("chat/${enc(it)}") },
                onOpenSettings = { nav.navigate("settings") },
                // Arama alanina dogru gizli sifre yazilinca kasa acilir.
                onSecretEntered = {
                    if (vm.vault.isPinSet) nav.navigate("vault")
                    else nav.navigate("vaultlock?setup=true")
                }
            )
        }

        composable(
            "chat/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) {
            val address = dec(it.arguments?.getString("address") ?: "")
            ChatScreen(vm = vm, address = address, onBack = { nav.popBackStack() })
        }

        composable("vaultlock?setup={setup}",
            arguments = listOf(navArgument("setup") { defaultValue = "false" })
        ) {
            val setup = it.arguments?.getString("setup") == "true"
            VaultLockScreen(
                vm = vm,
                setupMode = setup,
                onUnlocked = { nav.navigate("vault") { popUpTo("conversations") } },
                onBack = { nav.popBackStack() }
            )
        }

        composable("vault") {
            VaultScreen(
                vm = vm,
                onOpenThread = { nav.navigate("chat/${enc(it)}") },
                onBack = { nav.popBackStack("conversations", false) }
            )
        }

        composable("settings") {
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}

private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun dec(s: String) = URLDecoder.decode(s, "UTF-8")
