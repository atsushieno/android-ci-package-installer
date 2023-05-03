package dev.atsushieno.cipackageinstaller

import android.content.pm.PackageManager.NameNotFoundException
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
        Text("fetching repository details from GitHub...")
    } else {
        Text(repoInfo.name, fontSize = 20.sp)

        // It is not reliable at this moment, because we cannot retrieve reliable list of existing apps.
        // Therefore we do not use this variable later when we *could* optionally show "uninstall" button.
        val alreadyExists = AppModel.findExistingPackages(context).contains(repoInfo.packageName)
        if (alreadyExists)
            Text("(It is already installed on your system.)", fontSize = 16.sp)

        if (repo.variants.isEmpty()) {
            Text(text = "There is no available releases.")
            Text("(Note that there might be build \"artifact\" - they are available only to authenticated users via the GitHub API.)")
        }
        repo.variants.forEach { variant ->
            Text(variant.typeName, fontSize = 18.sp, textDecoration = TextDecoration.Underline)
            Text(variant.versionId)
            Text(variant.artifactName)
            Button(onClick = {
                Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                    Toast.makeText(context, "Downloading ${repoInfo.name} ...", Toast.LENGTH_LONG).show()
                }
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
                Text(if (alreadyExists) "Download and Update" else "Download and Install")
            }
            if (!AppModel.isExistingPackageListReliable() || alreadyExists) {
                Button(onClick = {
                    Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                        Toast.makeText(context, "Uninstalling ${repoInfo.name} ...", Toast.LENGTH_LONG).show()
                    }
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
}