package org.prime.tally.business.repository.transactions

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.prime.tally.data.model.ApiResponse
import org.prime.tally.data.model.transactions.TranListRequest
import org.prime.tally.data.model.transactions.TranListResponse
import org.prime.tally.data.model.transactions.TranRequest
import org.prime.tally.data.model.transactions.TranResponse
import org.prime.tally.data.utils.BASE_URL
import org.prime.tally.data.utils.KtorClient
import org.prime.tally.data.utils.SharedPrefs

object SingleEntryRepository {
    val client = KtorClient.client
    suspend fun saveTransaction(tranRequest: TranRequest): ApiResponse<TranResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("$BASE_URL/Transactions/saveTransaction.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(tranRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }

    suspend fun modifyTransaction(tranRequest: TranRequest): ApiResponse<TranResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("$BASE_URL/Transactions/updateTransaction.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(tranRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }

    suspend fun listTransactions(tranListRequest: TranListRequest): ApiResponse<List<TranListResponse>>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("$BASE_URL/Transactions/listTransactions.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(tranListRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }
}