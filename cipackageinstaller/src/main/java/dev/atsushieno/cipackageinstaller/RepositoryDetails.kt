package dev.atsushieno.cipackageinstaller

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.coroutineContext

@Composable
fun RepositoryDetails(navController: NavController, index: Int) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var repoState by remember { mutableStateOf<Repository?>(null) }
    if (repoState == null) {
        Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
            try {
                repoState = AppModel.applicationStore.repositories[index].createRepository()
            } catch(ex: CIPackageInstallerException) {
                Log.e(AppModel.LOG_TAG, "Failed to retrieve repository data", ex)
                Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                    navController.navigate(Routes.Home.route) { popUpTo(Routes.Home.route) }
                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val repo = repoState
    if (repo != null) {
        Column {
            Row {
                Text(repo.info.name, fontSize = 20.sp)
            }
            Text(repo.versionId)
            Text(repo.appName)
            Button(onClick = {
                Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                    try {
                        AppModel.performInstallPackage(context, repo)
                    } catch(ex: CIPackageInstallerException) {
                        Log.e(AppModel.LOG_TAG, "Failed to retrieve repository data", ex)
                        Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                            Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Download and Install")
            }
            Button(onClick = {
                Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                    try {
                        AppModel.performUninstallPackage(context, repo)
                    } catch(ex: CIPackageInstallerException) {
                        Log.e(AppModel.LOG_TAG, "Failed to retrieve repository data", ex)
                        Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                            Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Uninstall")
            }
        }
    } else {
        Text("loading...")
    }
}