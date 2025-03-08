package dev.atsushieno.cipackageinstaller.ui.view

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.atsushieno.cipackageinstaller.AppModel

@Composable
fun MainScreen(onItemClicked: (repo: Int) -> Unit) {
    Column {
        GitHubUserCredentialsConfigUI()
        PermissionRequester()
        OperationLogViewer()
        val context = LocalContext.current
        LazyColumn(content = {
            val existingApps = AppModel.findExistingPackages(context)
            AppModel.applicationStore.repositories.forEachIndexed { index, repo ->
                item {
                    Row {
                        //Checkbox(checked = false, onCheckedChange = {})
                        Column(modifier = Modifier.clickable { onItemClicked(index) }) {
                            Row {
                                Text(repo.appLabel, fontSize = 18.sp)
                            }
                            Text(repo.name)
                            Row {
                                if (existingApps.contains(repo.packageName))
                                    Text("[installed] ", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                else
                                    Text("[available] ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                Text(repo.packageName, fontSize = 12.sp)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        })
    }
}

@Composable
fun OperationLogViewer() {
    val hasSomeLogText = "There are some operation logs, tap to view"

    val logs = remember { AppModel.logger.logs }

    if (logs.isEmpty())
        return

    var toggleViewerState by remember { mutableStateOf(false) }
    Column(modifier = Modifier.border(2.dp, color = MaterialTheme.colorScheme.outline)) {
        Row(modifier = Modifier
            .padding(4.dp)
            .clickable { toggleViewerState = !toggleViewerState }) {
            Text(hasSomeLogText)
        }
        if (toggleViewerState) {
            Button(onClick = { AppModel.logger.clearLogs() }) {
                Text("Clear logs")
            }
            LazyColumn {
                items(logs.size) { index ->
                    val entry = logs[index]
                    if (entry.artifact != null)
                        Text(entry.artifact.artifactName, fontSize = 12.sp)
                    Text(entry.text)
                }
            }
        }
    }
}

@Composable
fun GitHubUserCredentialsConfigUI() {
    val context = LocalContext.current

    val alreadyHasUserInfText = "To change GitHub account setup, tap here"
    val credentialsAreSetText = "Username and PAT are set!"
    val needsCredentialsText = "Specify username and PAT"

    val githubCredentials = AppModel.getGitHubCredentials(context)
    var username by remember { mutableStateOf(githubCredentials.username) }
    var pat by remember { mutableStateOf(githubCredentials.pat) }

    val hasGitHubCredentials = username.isNotEmpty() && pat.isNotEmpty()
    var toggleGitHubAccountState by remember { mutableStateOf(!hasGitHubCredentials) }
    val needsUserInfoText = "To get pre-release APKs, set GitHub username and PAT " +
        if (toggleGitHubAccountState) "" else "\uD83D\uDC48"
    val descriptionText = if (hasGitHubCredentials) alreadyHasUserInfText else needsUserInfoText
    var showCredentialRemovalConfirmationState by remember { mutableStateOf(false) }

    Column(modifier = Modifier.border(2.dp, color = MaterialTheme.colorScheme.outline)) {
        Row(modifier = Modifier
            .padding(4.dp)
            .clickable { toggleGitHubAccountState = !toggleGitHubAccountState }) {
            Text(descriptionText)
        }
        if (toggleGitHubAccountState) {
            var isPasswordVisible by remember { mutableStateOf(false) }
            Text("GitHub Username:")
            Row {
                Text(" ", modifier = Modifier.defaultMinSize(30.dp))
                TextField(value = username, singleLine = true, onValueChange = { v: String -> username = v })
            }
            Text("Personal Access Token:")
            Row {
                // see-no-evil monkey vs. monkey
                Text(text = if (isPasswordVisible) "\uD83D\uDC35" else "\uD83D\uDE48", modifier = Modifier
                    .clickable { isPasswordVisible = !isPasswordVisible }
                    .defaultMinSize(30.dp))
                TextField(
                    value = pat, singleLine = true, onValueChange = { v: String -> pat = v },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            }
            Row {
                Button(onClick = {
                    if (username.isNotEmpty() && pat.isNotEmpty()) {
                        AppModel.setGitHubCredentials(context, username, pat)
                        toggleGitHubAccountState = false
                        Toast.makeText(context, credentialsAreSetText, Toast.LENGTH_SHORT)
                            .show()
                    } else
                        Toast.makeText(context, needsCredentialsText, Toast.LENGTH_SHORT).show()
                }) {
                    Text("Set")
                }
                Button(onClick = {
                    if (username.isNotEmpty() && pat.isNotEmpty())
                        showCredentialRemovalConfirmationState = true
                }) {
                    Text("Reset user info")
                }
            }
        }
    }
    if (showCredentialRemovalConfirmationState) {
        AlertDialog(
            text = { Text ("Are you sure to remove your GitHub account information from this app?") },
            dismissButton = {
                Button(onClick = { showCredentialRemovalConfirmationState = false }) { Text("Cancel") }
            },
            confirmButton = {
                Button(onClick = {
                    AppModel.setGitHubCredentials(context, "", "")
                    username = ""
                    pat = ""
                    showCredentialRemovalConfirmationState = false
                }) { Text("OK") }
            },
            onDismissRequest = { showCredentialRemovalConfirmationState = false })
    }
}

