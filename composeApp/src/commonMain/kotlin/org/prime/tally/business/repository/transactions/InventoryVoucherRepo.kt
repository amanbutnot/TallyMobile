package org.prime.tally.business.repository.transactions

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.tally.business.repository.DistributorRepository
import org.prime.tally.data.model.ApiResponse
import org.prime.tally.data.model.transactions.InventoryVoucherRequest
import org.prime.tally.data.model.transactions.InventoryVoucherResponse
import org.prime.tally.data.utils.BASE_URL
import org.prime.tally.data.utils.KtorClient
import org.prime.tally.data.utils.SharedPrefs

object InventoryVoucherRepo {

    val client = KtorClient.client

    suspend fun createInventoryVch(inventoryVoucherRequest: InventoryVoucherRequest): ApiResponse<InventoryVoucherResponse>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Transactions/addInventoryVch.php") {
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
}