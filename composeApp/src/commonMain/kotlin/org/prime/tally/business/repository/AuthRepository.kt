package org.prime.tally.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.tally.data.model.ApiResponse
import org.prime.tally.data.model.LoginRequest
import org.prime.tally.data.model.LoginResponse
import org.prime.tally.data.utils.BASE_URL
import org.prime.tally.data.utils.KtorClient

object AuthRepository {
    val client = KtorClient.client

    suspend fun userLogin(loginRequest: LoginRequest): ApiResponse<LoginResponse>? {
        return try {
            val response = client.post("${BASE_URL}/Users/Login.php") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }
}