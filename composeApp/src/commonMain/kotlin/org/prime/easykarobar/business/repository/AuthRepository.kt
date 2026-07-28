package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.ForgotResponse
import org.prime.easykarobar.data.model.LoginApiWrapper
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.data.model.RegisterRequest
import org.prime.easykarobar.data.model.RegisterResponse
import org.prime.easykarobar.data.model.ResetRequest
import org.prime.easykarobar.data.model.WhatsAppSendResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient

object AuthRepository {
    val client = KtorClient.client

    suspend fun userLogin(loginRequest: LoginRequest): LoginApiWrapper? {
        return try {
            val response = client.post("${BASE_URL}/Users/LoginMulti.php") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            println(loginRequest)
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun userRegister(registerRequest: RegisterRequest): RegisterResponse? {
        return try {
            val response = client.post("${BASE_URL}/Users/CreateUser.php") {
                contentType(ContentType.Application.Json)
                setBody(registerRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }


    suspend fun validateMobile(username: String): ApiResponse<ForgotResponse>? {
        return try {
            val response = client.post("${BASE_URL}/Users/validate_mobile.php") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("Username" to username))
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun resetPassword(mobileNumber: String, newPass: String): ApiResponse<Nothing>? {
        return try {
            val response = client.post("${BASE_URL}/Users/update-password.php") {
                contentType(ContentType.Application.Json)
                setBody(
                    ResetRequest(
                        MobileNo = mobileNumber,
                        NewPassword = newPass
                    )
                )
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun sendOtp(number: String, message: String): WhatsAppSendResponse? {
        return try {
            val responseText = client.post("https://easykarobar.in/api/send-otp.php") {
                // The server is sending text/html, so contentType here is irrelevant
                url {
                    parameters.append("authToken", "182895")
                    parameters.append("receiverId", number)
                    parameters.append("messageText", message)
                }
            }.bodyAsText()

            println(responseText) // log the raw response

            // Parse manually using kotlinx.serialization
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<WhatsAppSendResponse>(responseText)
        } catch (e: Exception) {
            println("Error Occurred: ${e.message}")
            null
        }
    }
}