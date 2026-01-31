package org.prime.easykarobar.business.repository

// iosMain
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import platform.Foundation.*
import kotlinx.cinterop.*
import org.prime.easykarobar.data.utils.KtorClient

@OptIn(ExperimentalForeignApi::class)
actual suspend fun downloadAndExtractGoogleDriveFile(
    fileId: String,
    accessToken: String,
    destinationPath: String,
): Result<String> {
    return try {
        val fileManager = NSFileManager.defaultManager

        // Step 1: Download to temporary location
        val tempZipPath = "$destinationPath/temp_database.zip"

        // Create destination directory
        fileManager.createDirectoryAtPath(
            destinationPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        val response = KtorClient.client.prepareGet("https://www.googleapis.com/drive/v3/files/$fileId?alt=media") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }.execute()

        val channel = response.bodyAsChannel()

        // Download ZIP file
        val outputStream = NSOutputStream.outputStreamToFileAtPath(tempZipPath, append = false)!!
        outputStream.open()

        try {
            val buffer = ByteArray(8192)
            var bytesDownloaded = 0L

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    buffer.usePinned { pinned ->
                     //   outputStream.write(pinned.addressOf(0), bytesRead.toULong())
                    }
                    bytesDownloaded += bytesRead


                }
            }
        } finally {
            outputStream.close()
        }

        // Step 2: Extract ZIP file
        // For iOS, you'll need to add SSZipArchive CocoaPod
        // Add to your Podfile: pod 'SSZipArchive'

        val success = unzipFile(tempZipPath, destinationPath)

        // Step 3: Clean up temp ZIP file
        fileManager.removeItemAtPath(tempZipPath, null)

        if (success) {
            // Find the .db file
            val contents = fileManager.contentsOfDirectoryAtPath(destinationPath, error = null) as? List<*>
            val dbFile = contents?.firstOrNull { (it as? String)?.endsWith(".db") == true } as? String

            Result.success(dbFile?.let { "$destinationPath/$it" } ?: destinationPath)
        } else {
            Result.failure(Exception("Failed to extract ZIP file"))
        }

    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Helper function to unzip on iOS (using SSZipArchive)
private fun unzipFile(zipPath: String, destinationPath: String): Boolean {
    // This requires SSZipArchive CocoaPod
    // You'll need to create a Kotlin/Native interop or use a Swift wrapper

    // For now, return true as placeholder
    // Implement actual unzipping using SSZipArchive
    return true
}