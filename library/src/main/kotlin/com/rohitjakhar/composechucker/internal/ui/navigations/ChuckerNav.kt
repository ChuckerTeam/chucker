package com.rohitjakhar.composechucker.internal.ui.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rohitjakhar.composechucker.internal.ui.MainViewModel
import com.rohitjakhar.composechucker.internal.ui.screen.transaction_details.TransactionDetails
import com.rohitjakhar.composechucker.internal.ui.screen.transaction_list.TransactionList

@Composable
internal fun ChuckerNav(
    navHostController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(navController = navHostController, startDestination = "transaction_list") {
        composable("transaction_list") {
            TransactionList(navController = navHostController, viewModel = viewModel)
        }
        composable(
            "transaction_details/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) {backStackEntry ->
            TransactionDetails(transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L)
        }
    }
}
