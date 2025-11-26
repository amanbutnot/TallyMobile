package org.prime.tally.business.repository.attendance

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.prime.tally.data.model.ApiResponse
import org.prime.tally.data.model.LoginRequest
import org.prime.tally.data.model.LoginResponse
import org.prime.tally.data.model.attendance.AttendanceListRequest
import org.prime.tally.data.model.attendance.AttendanceListResponse
import org.prime.tally.data.model.attendance.AttendanceRequest
import org.prime.tally.data.model.attendance.AttendanceResponse
import org.prime.tally.data.utils.BASE_URL
import org.prime.tally.data.utils.KtorClient
import org.prime.tally.data.utils.SharedPrefs

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
    suspend fun getAttendanceList(attendanceRequest: AttendanceListRequest): ApiResponse<AttendanceListResponse>? {
        val token = SharedPrefs.Token.get()
        return try {
            val response = client.post("${BASE_URL}/Locations/ViewLocations.php") {
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
}