package org.prime.easykarobar.business.repository

// androidMain
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import org.prime.easykarobar.data.utils.KtorClient
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

actual suspend fun downloadAndExtractGoogleDriveFile(
    fileId: String,
    accessToken: String,
    destinationPath: String,
): Result<String> {
    return try {
        val tempZipFile = File(destinationPath, "temp_database.zip")
        tempZipFile.parentFile?.mkdirs()

        val response = KtorClient.client.prepareGet("https://www.googleapis.com/drive/v3/files/$fileId?alt=media") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }.execute()

        val channel = response.bodyAsChannel()

        FileOutputStream(tempZipFile).use { outputStream ->
            val buffer = ByteArray(8192)
            var bytesDownloaded = 0L

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesDownloaded += bytesRead



                }
            }
        }

        val destDir = File(destinationPath)
        ZipInputStream(tempZipFile.inputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            var extractedDbPath: String? = null

            while (entry != null) {
                val filePath = File(destDir, entry.name)

                if (entry.isDirectory) {
                    filePath.mkdirs()
                } else {
                    filePath.parentFile?.mkdirs()
                    filePath.outputStream().use { output ->
                        zipInputStream.copyTo(output)
                    }

                    if (entry.name.endsWith(".db")) {
                        extractedDbPath = filePath.absolutePath
                    }
                }

                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }

            tempZipFile.delete()

            Result.success(extractedDbPath ?: destDir.absolutePath)
        }

    } catch (e: Exception) {
        Result.failure(e)
    }
}