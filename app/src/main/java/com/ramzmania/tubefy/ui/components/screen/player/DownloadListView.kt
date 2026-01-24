package com.ramzmania.tubefy.ui.components.screen.player
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import java.io.File


@Composable
fun FileListScreen(packageName: String) {
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val newNav = LocalNavController.current

    LaunchedEffect(packageName) {
        try {

            val downloadsDir = File(
                context.getExternalFilesDir(null)?.absolutePath+"/downloads"
            )

            if (downloadsDir.exists() && downloadsDir.isDirectory) {
                files = downloadsDir.listFiles()?.toList() ?: emptyList()
            } else {
                errorMessage = "Directory does not exist"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Downloads Directory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (files.isEmpty() && errorMessage == null) {
            Text("No files found")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                items(files) { file ->
//                    FileItem(file)
//                }
                items(files) { file ->
                    FileItem(
                        file = file,
                        onFileClick = {
//                            if (file.isFile && isMediaFile(file)) {
//                                playLocalFile(context, file)
//                            }
                            newNav!!.navigate(
                                NavigationItem.LocalDownloads.createRoute(
                                    "file.absolutePath"
                                )
                            ) {
                                newNav.graph.route?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItem(file: File, onFileClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onFileClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (file.isDirectory) "Directory" else "File - ${file.length() / 1024} KB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}