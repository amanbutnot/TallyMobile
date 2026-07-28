package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.DriveTokenResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

object GoogleDriveRepository {
    val client = KtorClient.client
    suspend fun getDriveToken(): ApiResponse<DriveTokenResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val res = client.get("${BASE_URL}/Users/getDriveToken.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }
            res.body()
        } catch (e: Exception) {
            print(e.message)
            null
        }
    }
}

// commonMain
expect suspend fun downloadAndExtractGoogleDriveFile(
    fileId: String,
    accessToken: String,
    destinationPath: String,
): Result<String>

expect suspend fun downloadAndExtractZip(
    url: String,
    destinationPath: String,
    headers: Map<String, String> = emptyMap()
): Result<String>
