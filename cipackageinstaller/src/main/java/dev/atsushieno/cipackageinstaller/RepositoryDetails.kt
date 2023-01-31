package dev.atsushieno.cipackageinstaller

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.coroutineContext

@Composable
fun RepositoryDetails(navController: NavController, index: Int) {
    Column {
        RepositoryDetailsBody(navController, index)
    }
}

@Composable
fun RepositoryDetailsBody(navController: NavController, index: Int) {
    val context = LocalContext.current

    val repoInfo = AppModel.applicationStore.repositories[index]

    val coroutineScope = rememberCoroutineScope()
    var repoState by remember { mutableStateOf<Repository?>(null) }
    val repo = repoState
    if (repo == null) {
        Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
            try {
                repoState = repoInfo.createRepository()
            } catch (ex: CIPackageInstallerException) {
                Log.e(AppModel.LOG_TAG, "Failed to retrieve repository data", ex)
                Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                    navController.navigate(Routes.Home.route) { popUpTo(Routes.Home.route) }
                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        Text("loading...")
    } else {
        Text(repoInfo.name, fontSize = 20.sp)
        repo.variants.forEach { variant ->
            Text(variant.typeName, fontSize = 18.sp, textDecoration = TextDecoration.Underline)
            Text(variant.versionId)
            Text(variant.artifactName)
            Button(onClick = {
                Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                    try {
                        AppModel.performInstallPackage(context, variant)
                    } catch (ex: CIPackageInstallerException) {
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
                    } catch (ex: CIPackageInstallerException) {
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
    }
}