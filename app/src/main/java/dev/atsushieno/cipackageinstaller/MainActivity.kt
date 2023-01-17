package dev.atsushieno.cipackageinstaller

import android.R.id
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.widget.Toast
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


class MainActivity : CIPackageInstallerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppModel.applicationStore.initialize(this)
        AppModel.findExistingPackages = { context -> queryInstalledAudioPluginPackages(context) }
    }

    fun queryInstalledAudioPluginPackages(context: Context, packageNameFilter: String? = null): List<String> {
        val AAP_ACTION_NAME = "org.androidaudioplugin.AudioPluginService.V2"

        val intent = Intent(AAP_ACTION_NAME)
        if (packageNameFilter != null)
            intent.setPackage(packageNameFilter)
        return context.packageManager.queryIntentServices(intent, 0).map { it.serviceInfo.packageName }
            .distinct()
    }
}

open class CIPackageInstallerActivity : ComponentActivity() {
    companion object {
        const val PACKAGE_INSTALLED_ACTION = "dev.atsushieno.cipackageinstaller.SESSION_API_PACKAGE_INSTALLED"
        const val REQUEST_INSTALL = 1
        const val REQUEST_UNINSTALL = 2
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQUEST_INSTALL) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Install canceled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Install Failed!", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_UNINSTALL) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Uninstall succeeded!", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Uninstall canceled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Uninstall Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.action == PACKAGE_INSTALLED_ACTION) {
                val extras = intent.extras!!
                val status = extras.getInt(PackageInstaller.EXTRA_STATUS)
                val message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
                when (status) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        // This test app isn't privileged, so the user has to confirm the install.
                        startActivity(extras.get(Intent.EXTRA_INTENT) as Intent)
                    }
                    PackageInstaller.STATUS_SUCCESS -> Toast.makeText(
                        this,
                        "Install succeeded!",
                        Toast.LENGTH_SHORT
                    ).show()

                    PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> Toast.makeText(
                        this, "Install failed! $status" + ", " + id.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> Toast.makeText(
                        this, "Unrecognized status received from installer: $status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        super.onNewIntent(intent)
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
            RepositoryDetails(index = it.arguments!!.getInt("index", 0))
        }
    }
}

