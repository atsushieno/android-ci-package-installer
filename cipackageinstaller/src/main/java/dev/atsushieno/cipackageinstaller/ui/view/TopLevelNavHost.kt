package dev.atsushieno.cipackageinstaller.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Routes(val route: String) {
    data object Home : Routes("Home")
    data object AppDetails : Routes("AppDetails/{index}") {
        fun createRoute(index: Int) = "AppDetails/$index"
    }
}

@Composable
fun TopLevelNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) {
            MainScreen(onItemClicked = { index ->
                navController.navigate(Routes.AppDetails.createRoute(index))
            })
        }
        composable(
            Routes.AppDetails.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) {
            RepositoryDetails(navController, index = it.arguments!!.getInt("index", 0))
        }
    }
}