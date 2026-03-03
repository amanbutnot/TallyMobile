package org.prime.easykarobar.business.repository.masters

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.prime.easykarobar.business.repository.AuthRepository.client
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.masters.AccountModel

object AccountRepository {
    suspend fun createAccount(accountModel: AccountModel): ApiResponse<AccountModel>? {
        return try {
            val response = client.post("${BASE_URL}/Accounts/create_master_ledger.php") {
                val token = SharedPrefs.Token.get()

                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(accountModel)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun listAccount(): ApiResponse<List<AccountModel>>? {
        return try {
            val response = client.post("${BASE_URL}/Accounts/list_master_ledger.php") {
                val token = SharedPrefs.Token.get()

                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }
            println("l;ksadjf;lkasdjl;kfjqs;kldfj;laksdfj;lkasdjfl;kasdjf;lkasjdf;lkasjdlfk;jl;kf")
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }
}

@Serializable
data class AccountResponse(
    val name: String
)