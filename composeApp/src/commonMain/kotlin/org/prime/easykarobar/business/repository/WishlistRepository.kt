package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.WishlistItem
import org.prime.easykarobar.data.model.WishlistRequest
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

object WishlistRepository {
    private val client = KtorClient.client

    suspend fun addWishlist(request: WishlistRequest): ApiResponse<WishlistItem>? {
        return try {
            val response = client.post("${BASE_URL}/Others/addWishlist.php") {
                val token = SharedPrefs.Token.get()
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            println("Add Wishlist Response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error adding to wishlist: ${e.message}")
            null
        }
    }

    suspend fun getWishlist(mobileNo: String): ApiResponse<List<WishlistItem>>? {
        return try {
            val response = client.post("${BASE_URL}/Others/GetWishlist.php") {
                val token = SharedPrefs.Token.get()
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("mobile_no" to mobileNo))
            }
            println("Get Wishlist Response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error getting wishlist: ${e.message}")
            null
        }
    }

    suspend fun deleteWishlist(mobileNo: String, itemName: String): ApiResponse<JsonElement>? {
        return try {
            val response = client.post("${BASE_URL}/Others/DeleteWishlist.php") {
                val token = SharedPrefs.Token.get()
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("mobile_no" to mobileNo, "item_name" to itemName))
            }
            println("Delete Wishlist Response: ${response.bodyAsText()}")
            response.body()
        } catch (e: Exception) {
            println("Error deleting from wishlist: ${e.message}")
            null
        }
    }
}
