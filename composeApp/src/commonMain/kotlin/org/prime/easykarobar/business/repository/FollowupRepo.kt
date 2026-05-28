package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.easykarobar.business.repository.AuthRepository.client
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.FollowupData
import org.prime.easykarobar.data.model.PostFollowup
import org.prime.easykarobar.data.model.PostFollowupResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.SharedPrefs

object FollowupRepo {
    suspend fun createAccount(postFollowup: PostFollowup): ApiResponse<PostFollowupResponse>? {
        return try {
            val response = client.post("${BASE_URL}/Locations/InsertFollowups.php") {
                val token = SharedPrefs.Token.get()

                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(postFollowup)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun getFollowupList(): ApiResponse<List<FollowupData>>? {
        return try {
            val response = client.post("${BASE_URL}/Locations/ListFollowups.php") {
                val token = SharedPrefs.Token.get()
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }
}
