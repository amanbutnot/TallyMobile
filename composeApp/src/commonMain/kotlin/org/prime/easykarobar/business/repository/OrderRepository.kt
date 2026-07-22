package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.CancelOrderRequest
import org.prime.easykarobar.data.model.CancelOrderResponse
import org.prime.easykarobar.data.model.CreateOrderRequest
import org.prime.easykarobar.data.model.CreateOrderResponse
import org.prime.easykarobar.data.model.Order
import org.prime.easykarobar.data.model.UpdateOrderStatusRequest
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

object OrderRepository {


    suspend fun createNewOrder(createOrderRequest: CreateOrderRequest): ApiResponse<CreateOrderResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val res = KtorClient.client.post("$BASE_URL/Transactions/addDistributors.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(createOrderRequest)
            }
            println(res.bodyAsText())
            res.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun cancelOrder(cancelOrderRequest: CancelOrderRequest): ApiResponse<CancelOrderResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val res = KtorClient.client.post("$BASE_URL/Transactions/cancelOrder.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(cancelOrderRequest)
            }
            println(res.bodyAsText())
            res.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateOrderStatus(updateOrderStatusRequest: UpdateOrderStatusRequest): ApiResponse<String>? {
        val token = SharedPrefs.Token.get()
        return try {
            val res = KtorClient.client.post("$BASE_URL/Transactions/updateOrderStatus.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(updateOrderStatusRequest)
            }
            println(res.bodyAsText())
            res.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun listOrders(): ApiResponse<List<Order>>? {
        val token = SharedPrefs.Token.get()
        return try {
            val res = KtorClient.client.post("$BASE_URL/Transactions/listOfDistributorOrders.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
               setBody(ListRequest(SharedPrefs.DistributorData.get()?.ledger_GUID.toString()) )
            }
            println(res.bodyAsText())
            res.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




@Serializable
data class ListRequest(
    val billing_guid:String
)