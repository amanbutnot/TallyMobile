package org.prime.easykarobar.business.repository.transactions

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.transactions.InventoryItemResponse
import org.prime.easykarobar.data.model.transactions.InventoryListRequest
import org.prime.easykarobar.data.model.transactions.InventoryListResponse
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.InventoryVoucherResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

object InventoryVoucherRepo {

    val client = KtorClient.client

    suspend fun createInventoryVch(
        inventoryVoucherRequest: InventoryVoucherRequest,
        endpoint: String
    ): ApiResponse<InventoryVoucherResponse>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Transactions/${endpoint}.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(inventoryVoucherRequest)
            }
            println(inventoryVoucherRequest)
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun listInventoryVch(inventoryListRequest: InventoryListRequest): ApiResponse<List<InventoryListResponse>>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Transactions/listInventory.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(inventoryListRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun getInventoryVoucher(tranId: Int): ApiResponse<InventoryItemResponse>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Transactions/getTransaction.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(mapOf("Transaction_ID" to tranId))
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun deleteInventoryVoucher(
        tranId: Int,
        vchType: Int
    ): ApiResponse<DeleteResponse>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Transactions/deleteInventory.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(mapOf("TransactionID" to tranId, "vch_type" to vchType))
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }
}

@Serializable
data class DeleteResponse(
    val TransactionID: Int
)