package org.prime.tally.data.utils

import com.russhwolf.settings.Settings

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


}