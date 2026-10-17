package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.prime.easykarobar.data.expect.getDeviceIdString
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.DeleteTokenRequest
import org.prime.easykarobar.data.model.FirebaseTokenRequest
import org.prime.easykarobar.data.model.ForgotResponse
import org.prime.easykarobar.data.model.LoginApiWrapper
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.data.model.NotificationPayload
import org.prime.easykarobar.data.model.RegisterRequest
import org.prime.easykarobar.data.model.RegisterResponse
import org.prime.easykarobar.data.model.ResetRequest
import org.prime.easykarobar.data.model.WhatsAppSendResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

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

    suspend fun sendFirebaseToken(request: FirebaseTokenRequest): ApiResponse<Nothing>? {
        return try {
            val token = SharedPrefs.Token.get()
            val response = client.post("${BASE_URL}/Users/savefirebasetoken.php") {
                contentType(ContentType.Application.Json)
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
                setBody(request)
            }
            println("Sending Firebase Token request: $request")
            println("Firebase Token response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error sending firebase token: ${e.message}")
            null
        }
    }

    suspend fun deleteToken(bearerToken: String? = null, deviceId: String? = null): ApiResponse<Nothing>? {
        return try {
            val token = bearerToken?.trim()?.takeIf { it.isNotBlank() } ?: SharedPrefs.Token.get()
            if (token.isNullOrBlank()) return null
            val devId = deviceId?.trim()?.takeIf { it.isNotBlank() } ?: getDeviceIdString()
            val req = DeleteTokenRequest(
                deviceId = devId
            )
            val response = client.post("${BASE_URL}/Users/deleteToken.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(req)
            }
            println("Delete token request body: $req")
            println("Delete token response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error deleting token: ${e.message}")
            null
        }
    }

    suspend fun saveNotification(payload: NotificationPayload): ApiResponse<Nothing>? {
        return try {
            val token = SharedPrefs.Token.get()
            val response = client.post("${BASE_URL}/Users/save_notification.php") {
                contentType(ContentType.Application.Json)
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
                setBody(payload)
            }
            println("Save notification request: $payload")
            println("Save notification response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error saving notification: ${e.message}")
            null
        }
    }

    suspend fun listNotifications(): ApiResponse<List<NotificationPayload>>? {
        return try {
            val token = SharedPrefs.Token.get()
            val response = client.post("${BASE_URL}/Users/list_notifications.php") {
                contentType(ContentType.Application.Json)
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }
            println("List notifications response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error listing notifications: ${e.message}")
            null
        }
    }
}