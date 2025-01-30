package com.bot.calling_app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Dialer : Screen("dialer")
    object CallLogs : Screen("call_logs")
    object Contacts : Screen("contacts")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Dialer.route) {
        composable(Screen.Dialer.route) { DialerScreen() }
        composable(Screen.CallLogs.route) { CallLogsScreen() }
        composable(Screen.Contacts.route) { ContactsScreen() }
    }
}
