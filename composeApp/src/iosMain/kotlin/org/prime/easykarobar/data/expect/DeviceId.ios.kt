package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE_NAME = "com.easykarobar.in"
private const val ACCOUNT_NAME = "device_id"

@Composable
actual fun getDeviceId(): String {
    getKeychainValue()?.let { return it }

    val newId = NSUUID().UUIDString
    saveKeychainValue(newId)

    return newId
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun getKeychainValue(): String? {
    memScoped {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to ACCOUNT_NAME,
            kSecReturnData to kCFBooleanTrue!!,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val result = alloc<CFTypeRefVar>()

        val status = SecItemCopyMatching(
            query as CFDictionaryRef,
            result.ptr
        )

        if (status == errSecSuccess) {
            val data = result.value as? NSData ?: return null

            return NSString.create(
                data = data,
                encoding = NSUTF8StringEncoding
            ) as String
        }
    }

    return null
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun saveKeychainValue(value: String) {
    val data = value.encodeToByteArray().toNSData()

    val query = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrService to SERVICE_NAME,
        kSecAttrAccount to ACCOUNT_NAME,
        kSecValueData to data
    )

    SecItemDelete(query as CFDictionaryRef)

    SecItemAdd(
        query as CFDictionaryRef,
        null
    )
}