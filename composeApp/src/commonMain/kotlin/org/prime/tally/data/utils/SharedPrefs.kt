package org.prime.tally.data.utils

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import org.prime.tally.data.model.Distributor
import org.prime.tally.data.model.LoginResponse

object SharedPrefs {
    private val settings: Settings = Settings()

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
            settings.putString(KEY, date)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
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

        fun save(permissions: org.prime.tally.data.model.Permissions) {
            val json = Json.encodeToString(permissions)
            settings.putString(KEY, json)
        }

        fun get(): org.prime.tally.data.model.Permissions? {
            val stored = settings.getStringOrNull(KEY) ?: return null
            return runCatching { Json.decodeFromString<org.prime.tally.data.model.Permissions>(stored) }.getOrNull()
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
            settings.putString(KEY, login)
        }

        fun get(): String? {
            return settings.getStringOrNull(KEY)
        }

        fun clear() {
            settings.remove(KEY)
        }
    }
}