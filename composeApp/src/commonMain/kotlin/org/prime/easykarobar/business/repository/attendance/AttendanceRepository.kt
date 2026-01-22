package org.prime.easykarobar.business.repository.attendance

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.easykarobar.data.model.ApiResponse
import org.prime.easykarobar.data.model.attendance.AttendanceListRequest
import org.prime.easykarobar.data.model.attendance.AttendanceListResponse
import org.prime.easykarobar.data.model.attendance.AttendanceRequest
import org.prime.easykarobar.data.model.attendance.AttendanceResponse
import org.prime.easykarobar.data.model.attendance.SalesmanList
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.data.utils.KtorClient
import org.prime.easykarobar.data.utils.SharedPrefs

object AttendanceRepository {

    val client = KtorClient.client
    suspend fun sendAttendance(attendanceRequest: AttendanceRequest): ApiResponse<AttendanceResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("${BASE_URL}/Locations/InsertLocations.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")

                setBody(attendanceRequest)
            }
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun getAttendanceList(attendanceRequest: AttendanceListRequest): ApiResponse<List<AttendanceListResponse>>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("${BASE_URL}/Locations/ViewLocations.php") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")

                setBody(attendanceRequest)
            }
            println(attendanceRequest)
            println(response.bodyAsText())
            response.body()
        } catch (e: Exception) {
            print("Error Occurred: ${e.message}")
            null
        }
    }

    suspend fun getSalesmanList(): ApiResponse<List<SalesmanList>>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("${BASE_URL}/Distributors/listSalesmanMobiles.php") {
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