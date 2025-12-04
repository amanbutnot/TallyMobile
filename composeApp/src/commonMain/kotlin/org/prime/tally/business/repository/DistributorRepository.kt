package org.prime.tally.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.tally.data.model.ApiResponse
import org.prime.tally.data.model.DistributorRequest
import org.prime.tally.data.model.DistributorResponse
import org.prime.tally.data.utils.BASE_URL
import org.prime.tally.data.utils.KtorClient
import org.prime.tally.data.utils.SharedPrefs

object DistributorRepository {
    val client = KtorClient.client

    suspend fun createDistributor(distributorRequest: DistributorRequest): ApiResponse<DistributorResponse>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Distributors/createDistributor.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(distributorRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun listDistributor(): ApiResponse<List<DistributorRequest>>? {
        val token = SharedPrefs.Token.get()
        return try {

            val response = client.post("${BASE_URL}/Distributors/list-distributors.php") {
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