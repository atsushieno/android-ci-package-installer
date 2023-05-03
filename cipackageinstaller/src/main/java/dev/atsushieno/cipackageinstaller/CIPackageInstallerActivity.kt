package dev.atsushieno.cipackageinstaller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.atsushieno.cipackageinstaller.ui.theme.CIPackageInstallerTheme


open class CIPackageInstallerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CIPackageInstallerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TopLevelNavHost()
                }
            }
        }
    }
}

sealed class Routes(val route: String) {
    object Home : Routes("Home")
    object AppDetails : Routes("AppDetails/{index}") {
        fun createRoute(index: Int) = "AppDetails/$index"
    }
}

@Composable
fun TopLevelNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) {
            MainScreen(onItemClicked = {index ->
                navController.navigate(Routes.AppDetails.createRoute(index))
            })
        }
        composable(Routes.AppDetails.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })) {
            RepositoryDetails(navController, index = it.arguments!!.getInt("index", 0))
        }
    }
}

