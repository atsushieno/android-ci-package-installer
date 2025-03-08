package dev.atsushieno.cipackageinstaller.ui.view

import android.icu.text.DecimalFormat
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import dev.atsushieno.cipackageinstaller.AppModel
import dev.atsushieno.cipackageinstaller.CIPackageInstallerException
import dev.atsushieno.cipackageinstaller.Repository
import kotlinx.coroutines.Dispatchers

@Composable
fun RepositoryDetails(navController: NavController, index: Int) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        RepositoryDetailsContent(navController = navController, index)
    }
}

@RequiresApi(Build.VERSION_CODES.N)
private val decimalFormat = DecimalFormat.getInstance()
private fun formatLong(value: Long) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        decimalFormat.format(value.toBigDecimal())
    else
        value.toString()

@Composable
fun RepositoryDetailsContent(navController: NavController, index: Int) {
    val context = LocalContext.current

    val repoInfo = AppModel.applicationStore.repositories[index]

    val coroutineScope = rememberCoroutineScope()
    var repoState by remember { mutableStateOf<Repository?>(null) }
    val repo = repoState
    if (repo == null) {
        Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
            try {
                repoState = repoInfo.createRepository()
            } catch (ex: /*CIPackageInstallerException*/Exception) {
                AppModel.logger.logError("Failed to retrieve repository data: ${ex.message}", ex)
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
        val alreadyExists =
            AppModel.findExistingPackages(context).contains(repoInfo.packageName)
        if (alreadyExists)
            Text("(It is already installed on your system.)", fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)

        if (repo.variants.isEmpty()) {
            Text(text = "There is no available releases.")
            Text("(Note that there might be build \"artifact\" - they are available only to authenticated users via the GitHub API.)")
        }
        repo.variants.forEach { variant ->
            Text(variant.typeName, fontSize = 18.sp, textDecoration = TextDecoration.Underline)
            Text(variant.versionId)
            Text("${formatLong(variant.artifactSizeInBytes)} bytes")
            Text(variant.artifactName)
            Button(onClick = {
                Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                    Toast.makeText(context, "Downloading ${repoInfo.name} ...", Toast.LENGTH_LONG).show()
                }
                Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                    AppModel.performDownloadAndInstallation(context, variant)
                }
            }) {
                Text(if (alreadyExists) "Download and Update" else "Download and Install")
            }
            if (!AppModel.isExistingPackageListReliable() || alreadyExists) {
                Button(onClick = {
                    Dispatchers.Main.dispatch(coroutineScope.coroutineContext) {
                        Toast.makeText(
                            context,
                            "Uninstalling ${repoInfo.name} ...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                        try {
                            AppModel.performUninstallPackage(context, repo)
                        } catch (ex: CIPackageInstallerException) {
                            AppModel.logger.logError("Failed to retrieve repository data: ${ex.message}", ex)
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