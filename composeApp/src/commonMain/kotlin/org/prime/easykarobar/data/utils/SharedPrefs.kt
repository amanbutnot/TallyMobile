package org.prime.easykarobar.data.utils

import com.russhwolf.settings.Settings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.prime.easykarobar.data.model.CompanyList
import org.prime.easykarobar.data.model.Distributor
import org.prime.easykarobar.data.model.LoginResponse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object SharedPrefs {
    private val settings: Settings = Settings()
    private val settings2: Settings = Settings()
    fun clearAll() {
        settings.clear()
  //      settings2.clear()
    }
    fun logout() {

        // authentication
        Token.clear()
        FileId.clear()
        User.clear()

        // user related data
        DistributorData.clear()
        Permissions.clear()

//        // login info / cached login request
//        LoginData.clear()

        // sync / version data
        LoginVersion.clear()
        LastSync.clear()
    }
    object Token {
        private const val KEY = "token"
        fun save(token: String) {
            settings.putString(KEY, token)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object ShowZeroStock {
        private const val KEY = "ShowZeroStock"
        fun save(value: Boolean) {
            settings.putBoolean(KEY, value)
        }

        fun get(): Boolean? {
            return settings.getBooleanOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object LoginData {
        private const val KEY = "login_request"
        fun save(login: LoginDataModel) {
            val json = Json.encodeToString(login)
            settings2.putString(KEY, json)
        }

        fun get(): LoginDataModel? {
            val stored = settings2.getStringOrNull(KEY) ?: return null
            return runCatching { Json.decodeFromString<LoginDataModel>(stored) }.getOrNull()
        }

        fun clear() {
            settings2.remove(KEY)
        }
    }

    @Serializable
    data class LoginDataModel(
        val username: String,
        val password: String,
        val list: CompanyList
    )

    object FileId {
        private const val KEY = "fileId"
        fun save(fileId: String) {
            settings.putString(KEY, fileId)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object AttendanceDate {
        private const val KEY = "attendance_key"
        fun save(date: String) {
            settings.putString(KEY, date)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }

    }

    object CheckInOutDate {
        private const val KEY = "checkInOutKey"
        fun save(date: String) {
            settings.putString(KEY, date)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }

    }

    object CheckInOutLedger {
        private const val KEY = "attendance_ledger"
        fun save(date: String) {
            settings2.putString(KEY, date)
        }

        fun get(): String? {
            return settings2.getStringOrNull(KEY)
        }

        fun clear() {
            settings2.remove(KEY)
        }

    }

    object DistributorData {
        private const val KEY = "distributor_data"

        fun save(distributor: Distributor) {
            val json = Json.encodeToString(distributor)
            settings.putString(KEY, json)
        }

        fun get(): Distributor? {
            val stored = settings.getStringOrNull(KEY) ?: return null
            return runCatching { Json.decodeFromString<Distributor>(stored) }.getOrNull()
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object Permissions {
        private const val KEY = "permissions"

        fun save(permissions: org.prime.easykarobar.data.model.Permissions) {
            val json = Json.encodeToString(permissions)
            settings.putString(KEY, json)
        }

        fun get(): org.prime.easykarobar.data.model.Permissions? {
            val stored = settings.getStringOrNull(KEY) ?: return null
            return runCatching {
                Json.decodeFromString<org.prime.easykarobar.data.model.Permissions>(
                    stored
                )
            }.getOrNull()
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object User {
        private const val KEY = "user"

        fun save(loginResponse: LoginResponse) {
            val json = Json.encodeToString(loginResponse)
            settings.putString(KEY, json)
        }

        fun get(): LoginResponse? {
            val stored = settings.getStringOrNull(KEY) ?: return null
            return runCatching { Json.decodeFromString<LoginResponse>(stored) }.getOrNull()
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

    object LoginInfo {
        private const val KEY = "loginInfo"
        fun save(login: String) {
            settings2.putString(KEY, login)
        }

        fun get(): String? {
            return settings2.getStringOrNull(KEY)
        }

        fun clear() {
            settings2.remove(KEY)
        }
    }

    object LoginVersion {
        private const val KEY = "loginVersion"
        fun save(login: Int) {
            settings.putInt(KEY, login)
        }

        fun get(): Int? {
            return settings.getIntOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }
    }


    object LastSync {
        private const val KEY = "last_sync_time"

        fun save(timestampMillis: Long) {
            settings.putLong(KEY, timestampMillis)
        }

        @OptIn(ExperimentalTime::class)
        fun get(): String? {
            val timestamp = settings.getLongOrNull(KEY) ?: return null

            val dt = Instant
                .fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())

            return buildString {
                append(dt.dayOfMonth.toString().padStart(2, '0'))
                append("-")
                append(dt.monthNumber.toString().padStart(2, '0'))
                append("-")
                append((dt.year % 100).toString().padStart(2, '0'))
                append(" ")
                append(dt.hour.toString().padStart(2, '0'))
                append(":")
                append(dt.minute.toString().padStart(2, '0'))
                append(":")
                append(dt.second.toString().padStart(2, '0'))
            }
        }

        fun clear() {
            settings.remove(KEY)
        }
    }

}