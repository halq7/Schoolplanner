package com.future.schoolplanner.data.sync

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URLEncoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class NextcloudCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
)

data class NextcloudRemoteFile(
    val exists: Boolean,
    val lastModified: Long = 0L
)

data class NextcloudSyncResult(
    val appDataJson: String,
    val remoteLastModified: Long,
    val uploaded: Boolean,
    val downloaded: Boolean
)

class NextcloudSyncManager {
    private val client = HttpClient(Android) {
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        expectSuccess = false
    }

    private val webDavClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun sync(
        credentials: NextcloudCredentials,
        localJson: String,
        localUpdatedAt: Long,
        lastRemoteModified: Long,
        localDirty: Boolean
    ): NextcloudSyncResult = withContext(Dispatchers.IO) {
        ensureSchoolplannerFolder(credentials)

        val remoteFile = getRemoteFileInfo(credentials)
        if (!remoteFile.exists) {
            val uploadedRemoteModified = uploadAppData(credentials, localJson)
            return@withContext NextcloudSyncResult(
                appDataJson = localJson,
                remoteLastModified = uploadedRemoteModified,
                uploaded = true,
                downloaded = false
            )
        }

        val remoteChanged = remoteFile.lastModified > 0L &&
            (lastRemoteModified == 0L || remoteFile.lastModified > lastRemoteModified + 1_000L)

        if (remoteChanged && localDirty) {
            return@withContext if (remoteFile.lastModified > localUpdatedAt) {
                val remoteJson = downloadAppData(credentials)
                NextcloudSyncResult(
                    appDataJson = remoteJson,
                    remoteLastModified = remoteFile.lastModified,
                    uploaded = false,
                    downloaded = true
                )
            } else {
                val uploadedRemoteModified = uploadAppData(credentials, localJson)
                NextcloudSyncResult(
                    appDataJson = localJson,
                    remoteLastModified = uploadedRemoteModified,
                    uploaded = true,
                    downloaded = false
                )
            }
        }

        if (remoteChanged) {
            val remoteJson = downloadAppData(credentials)
            return@withContext NextcloudSyncResult(
                appDataJson = remoteJson,
                remoteLastModified = remoteFile.lastModified,
                uploaded = false,
                downloaded = true
            )
        }

        if (localDirty) {
            val uploadedRemoteModified = uploadAppData(credentials, localJson)
            return@withContext NextcloudSyncResult(
                appDataJson = localJson,
                remoteLastModified = uploadedRemoteModified,
                uploaded = true,
                downloaded = false
            )
        }

        NextcloudSyncResult(
            appDataJson = localJson,
            remoteLastModified = remoteFile.lastModified,
            uploaded = false,
            downloaded = false
        )
    }

    private suspend fun ensureSchoolplannerFolder(credentials: NextcloudCredentials) {
        withContext(Dispatchers.IO) {
            val authHeader = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("${credentials.username}:${credentials.password}".toByteArray())

            val request = Request.Builder()
                .url(folderUrl(credentials))
                .addHeader("Authorization", authHeader)
                .method("MKCOL", ByteArray(0).toRequestBody(null, 0, 0))
                .build()

            val response = webDavClient.newCall(request).execute()
            val statusCode = response.code
            response.close()

            if (statusCode != 201 && statusCode != 405) {
                throw IOException("Schoolplanner folder could not be created (HTTP $statusCode)")
            }
        }
    }

    private suspend fun getRemoteFileInfo(credentials: NextcloudCredentials): NextcloudRemoteFile {
        val response = client.head(fileUrl(credentials)) {
            basicAuth(credentials.username, credentials.password)
        }

        return when (response.status) {
            HttpStatusCode.OK -> NextcloudRemoteFile(
                exists = true,
                lastModified = parseHttpDate(response.headers[HttpHeaders.LastModified])
            )
            HttpStatusCode.NotFound -> NextcloudRemoteFile(false)
            else -> throw IOException("Remote sync file could not be checked (HTTP ${response.status.value})")
        }
    }

    private suspend fun downloadAppData(credentials: NextcloudCredentials): String {
        val response = client.get(fileUrl(credentials)) {
            basicAuth(credentials.username, credentials.password)
        }

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Remote sync file could not be downloaded (HTTP ${response.status.value})")
        }
        return response.bodyAsText()
    }

    private suspend fun uploadAppData(credentials: NextcloudCredentials, json: String): Long {
        val response = client.put(fileUrl(credentials)) {
            basicAuth(credentials.username, credentials.password)
            contentType(ContentType.Application.Json)
            setBody(json)
        }

        if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.NoContent && response.status != HttpStatusCode.OK) {
            throw IOException("Remote sync file could not be uploaded (HTTP ${response.status.value})")
        }
        return parseHttpDate(response.headers[HttpHeaders.LastModified]).takeIf { it > 0L }
            ?: System.currentTimeMillis()
    }

    private fun folderUrl(credentials: NextcloudCredentials): String {
        return "${davBaseUrl(credentials)}/Schoolplanner"
    }

    private fun fileUrl(credentials: NextcloudCredentials): String {
        return "${davBaseUrl(credentials)}/Schoolplanner/school_planner_data.json"
    }

    private fun davBaseUrl(credentials: NextcloudCredentials): String {
        val server = credentials.serverUrl.trim().trimEnd('/')
        val username = encodePathSegment(credentials.username.trim())
        return "$server/remote.php/dav/files/$username"
    }

    private fun encodePathSegment(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
    }

    private fun parseHttpDate(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        return runCatching {
            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.getOrDefault(0L)
    }
}
