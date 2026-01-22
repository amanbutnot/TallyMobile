package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
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

    suspend fun downloadGoogleDriveFile(fileId: String, accessToken: String): ByteArray? {
        return try {
            val res = client.get("https://www.googleapis.com/drive/v3/files/$fileId?alt=media") {
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
            }
            res.body()
        } catch (e: Exception) {
            print(e.message)
            null
        }
    }

}