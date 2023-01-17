package dev.atsushieno.cipackageinstaller

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
import kotlinx.coroutines.Dispatchers

@Composable
fun RepositoryDetails(index: Int) {
    val coroutineScope = rememberCoroutineScope()
    var repoState by remember { mutableStateOf<Repository?>(null) }
    if (repoState == null)
        Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
            repoState = AppModel.applicationStore.repositories[index].createRepository()
        }
    val repo = repoState
    val context = LocalContext.current
    if (repo != null) {
        Column {
            Row {
                Text(repo.info.name, fontSize = 20.sp)
                Button(onClick = {
                    Dispatchers.IO.dispatch(coroutineScope.coroutineContext) {
                        AppModel.performInstallPackage(context, repo)
                    }
                }) {
                    Text("Download")
                }
            }
            Text(repo.versionId)
            Text(repo.appName)
        }
    } else {
        Text("loading...")
    }
}