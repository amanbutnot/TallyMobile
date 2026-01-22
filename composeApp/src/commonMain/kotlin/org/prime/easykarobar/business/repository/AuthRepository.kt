package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.ForgotResponse
import org.prime.easykarobar.data.model.LoginApiWrapper
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.data.model.RegisterRequest
import org.prime.easykarobar.data.model.RegisterResponse
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

    suspend fun resetPassword(userId: Int, newPass: String): ApiResponse<Unit>? {
        return try {
            val response = client.post("${BASE_URL}/Users/reset-password.php") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("UserID" to userId, "NewPassword" to newPass))
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

  suspend fun sendOtp(number: String, message: String): ApiResponse<Unit>? {
        return try {
            val response = client.post("${BASE_URL}/Users/reset-password.php") {
                contentType(ContentType.Application.Json)
                url{
                    parameters.append("authToken","182895")
                    parameters.append("receiverId",number)
                    parameters.append("messageText",message)
                }

            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }


}