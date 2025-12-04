package org.prime.tally.data.utils

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import org.prime.tally.data.model.Distributor

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

    object AttendanceLedger {
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

}