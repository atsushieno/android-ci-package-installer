package dev.atsushieno.cipackageinstaller

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onItemClicked: (repo: Int) -> Unit) {
    val needsUserInfoText = "To get APKs, set GitHub username and PAT"
    val alreadyHasUserInfText = "To change GitHub account setup, tap here"
    val credentialsAreSetText = "Username and PAT are set!"
    val needsCredentialsText = "Specify username and PAT"

    val context = LocalContext.current
    val githubCredentials = AppModel.getGitHubCredentials(context)
    var username by remember { mutableStateOf(githubCredentials.username) }
    var pat by remember { mutableStateOf(githubCredentials.pat) }
    val hasGitHubCredentials = username.isNotEmpty() && pat.isNotEmpty()
    var toggleGitHubAccountState by remember { mutableStateOf(!hasGitHubCredentials) }
    val descriptionText = if (hasGitHubCredentials) alreadyHasUserInfText else needsUserInfoText

    Column {
        Column(modifier = Modifier.border(2.dp, color = Color.Gray)) {
            Row(modifier = Modifier.padding(4.dp).clickable { toggleGitHubAccountState = !toggleGitHubAccountState }) {
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
                    Text(text = if (isPasswordVisible) "\uD83D\uDC35" else "\uD83D\uDE48", modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible}.defaultMinSize(30.dp))
                    TextField(
                        value = pat, singleLine = true, onValueChange = { v: String -> pat = v },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                }
                Button(onClick = {
                    if (username.isNotEmpty() && pat.isNotEmpty()) {
                        AppModel.setGitHubCredentials(context, username, pat)
                        toggleGitHubAccountState = false
                        Toast.makeText(context, credentialsAreSetText, Toast.LENGTH_SHORT).show()
                    }
                    else
                        Toast.makeText(context, needsCredentialsText, Toast.LENGTH_SHORT).show()
                }) {
                    Text("Set")
                }
            }
        }
        LazyColumn(content = {
            AppModel.applicationStore.repositories.forEachIndexed { index, repo ->
                item {
                    Row {
                        //Checkbox(checked = false, onCheckedChange = {})
                        Column(modifier = Modifier.clickable { onItemClicked(index) }) {
                            Row {
                                Text(repo.appLabel, fontSize = 18.sp)
                            }
                            Text(repo.name)
                            Text(repo.packageName, fontSize = 12.sp)
                        }
                    }
                }
            }
        })
    }
}