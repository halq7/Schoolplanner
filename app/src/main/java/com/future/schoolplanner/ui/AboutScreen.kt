package com.future.schoolplanner.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.R
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import androidx.core.content.ContextCompat
import com.future.schoolplanner.BuildConfig

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val published_at: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var latestRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var versionClickCount by remember { mutableIntStateOf(0) }
    var showDeveloperToast by remember { mutableStateOf(false) }

    val currentVersion = BuildConfig.VERSION_NAME

    // BroadcastReceiver for download completion
    val downloadReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                isDownloading = false
                downloadProgress = 0f

                val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId != null && downloadId != -1L) {
                    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    val uri = downloadManager?.getUriForDownloadedFile(downloadId)

                    if (uri != null) {
                        // Install the APK
                        installApk(context!!, uri)
                    }
                }
            }
        }
    }

    // Register receiver when screen loads
    DisposableEffect(Unit) {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(downloadReceiver)
        }
    }

    // Show developer options toast
    LaunchedEffect(showDeveloperToast) {
        if (showDeveloperToast) {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.developer_options_unlocked),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            onNavigateToDeveloperOptions()
            showDeveloperToast = false
        }
    }

    // Check for updates when screen loads
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val client = HttpClient {
                    install(ContentNegotiation) {
                        json(Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        })
                    }
                }

                // Get releases as string first, then parse manually
                val responseText: String = client.get("https://api.github.com/repos/sergey842248/schoolplanner/releases").body()
                val json = Json { ignoreUnknownKeys = true }

                // Parse the JSON array manually
                val releasesJson = json.parseToJsonElement(responseText)
                if (releasesJson is kotlinx.serialization.json.JsonArray && releasesJson.isNotEmpty()) {
                    // Get the first release object
                    val firstReleaseJson = releasesJson[0]
                    if (firstReleaseJson is kotlinx.serialization.json.JsonObject) {
                        val tagName = firstReleaseJson["tag_name"]?.toString()?.removeSurrounding("\"") ?: ""
                        val name = firstReleaseJson["name"]?.toString()?.removeSurrounding("\"") ?: ""
                        val publishedAt = firstReleaseJson["published_at"]?.toString()?.removeSurrounding("\"") ?: ""
                        val body = firstReleaseJson["body"]?.toString()?.removeSurrounding("\"") ?: ""

                        latestRelease = GitHubRelease(
                            tag_name = tagName,
                            name = name,
                            published_at = publishedAt,
                            body = body
                        )
                    }
                }

                isLoading = false
                client.close()
            } catch (e: Exception) {
                error = context.getString(R.string.update_load_error, e.message)
                isLoading = false
            }
        }
    }

    fun downloadAndInstallUpdate() {
        latestRelease?.let { release ->
            val latestVersion = release.tag_name.removePrefix("v")
            if (compareVersions(currentVersion, latestVersion) >= 0) return

            isDownloading = true

            val downloadUrl = "https://github.com/sergey842248/schoolplanner/releases/download/${release.tag_name}/Schoolplanner.apk"

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("School Planner Update")
                .setDescription("Downloading update to version $latestVersion")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Schoolplanner_${latestVersion}.apk")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

            downloadManager.enqueue(request)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text(stringResource(R.string.about_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                }
            },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name_full),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "${stringResource(R.string.author)}: Ha Le (7janv)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.version, currentVersion),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.clickable {
                                versionClickCount++
                                if (versionClickCount == 10) {
                                    showDeveloperToast = true
                                    versionClickCount = 0 // Reset counter
                                }
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.updates),
                            style = MaterialTheme.typography.titleLarge
                        )

                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Text(stringResource(R.string.checking_updates), style = MaterialTheme.typography.bodyMedium)
                            }
                        } else if (error != null) {
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (latestRelease != null) {
                            val latestVersion = latestRelease!!.tag_name.removePrefix("v")
                            val isUpdateAvailable = compareVersions(currentVersion, latestVersion) < 0

                            Text(
                                text = stringResource(R.string.latest_version, latestVersion),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (isDownloading) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = stringResource(R.string.download_running),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else if (isUpdateAvailable) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = stringResource(R.string.update_available),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Button(
                                        onClick = { downloadAndInstallUpdate() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.download_update))
                                    }
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.version_current),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.no_releases),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun compareVersions(version1: String, version2: String): Int {
    val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }

    val maxLength = maxOf(parts1.size, parts2.size)

    for (i in 0 until maxLength) {
        val part1 = parts1.getOrElse(i) { 0 }
        val part2 = parts2.getOrElse(i) { 0 }

        if (part1 < part2) return -1
        if (part1 > part2) return 1
    }

    return 0
}

private fun installApk(context: Context, apkUri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: try to find the APK file manually in downloads
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val apkFile = downloadsDir.listFiles()?.find { it.name.startsWith("Schoolplanner_") && it.name.endsWith(".apk") }

            if (apkFile != null) {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } else {
                throw Exception("APK file not found")
            }
        } catch (e2: Exception) {
            // If all else fails, show a toast
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.developer_options_unlocked),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.apk_install_error),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
