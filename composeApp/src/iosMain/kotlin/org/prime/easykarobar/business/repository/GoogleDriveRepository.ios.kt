package org.prime.easykarobar.business.repository

import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.cinterop.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.prime.easykarobar.data.utils.KtorClient
import platform.posix.memset
import platform.zlib.*

@OptIn(ExperimentalForeignApi::class)
actual suspend fun downloadAndExtractGoogleDriveFile(
    fileId: String,
    accessToken: String,
    destinationPath: String,
): Result<String> {
    return try {
        val fileSystem = FileSystem.SYSTEM
        val destination = destinationPath.toPath()

        fileSystem.createDirectories(destination)

        val tempZipPath = destination.resolve("temp_database.zip")

        val response = KtorClient.client.prepareGet(
            "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        ) {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }.execute()

        val channel = response.bodyAsChannel()

        // Download using okio
        fileSystem.sink(tempZipPath).buffer().use { sink ->
            val buffer = ByteArray(8192)
            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    sink.write(buffer, 0, bytesRead)
                }
            }
        }

        // Read ZIP bytes using okio source
        val zipBytes = fileSystem.source(tempZipPath).buffer().use { source ->
            source.readByteArray()
        }

        val entries = parseZip(zipBytes)

        if (entries.isEmpty()) {
            return Result.failure(Exception("No entries found in ZIP"))
        }

        // Write extracted files using okio sink
        entries.forEach { (name, content) ->
            if (!name.endsWith("/")) {
                val destFile = destination.resolve(name)
                destFile.parent?.let { fileSystem.createDirectories(it) }
                fileSystem.sink(destFile).buffer().use { sink ->
                    sink.write(content)
                }
                println(">>> Extracted: $name (${content.size} bytes)")
            }
        }

        // Clean up temp ZIP
        fileSystem.delete(tempZipPath)

        // Find .db file
        val dbFile = fileSystem.listRecursively(destination)
            .firstOrNull { it.name.endsWith(".db") }

        Result.success(dbFile?.toString() ?: destinationPath)

    } catch (e: Exception) {
        Result.failure(e)
    }
}

// --- ZIP Parsing ---

@OptIn(ExperimentalForeignApi::class)
private fun parseZip(bytes: ByteArray): Map<String, ByteArray> {
    val entries = mutableMapOf<String, ByteArray>()

    // Find EOCD signature: 0x06054b50
    var eocdOffset = -1
    for (i in bytes.size - 22 downTo 0) {
        if (bytes[i].toInt() and 0xFF == 0x50 &&
            bytes[i + 1].toInt() and 0xFF == 0x4B &&
            bytes[i + 2].toInt() and 0xFF == 0x05 &&
            bytes[i + 3].toInt() and 0xFF == 0x06
        ) {
            eocdOffset = i
            break
        }
    }

    if (eocdOffset == -1) {
        println("!!! EOCD not found")
        return entries
    }

    val centralDirOffset = readIntLE(bytes, eocdOffset + 16)
    val numEntries = readShortLE(bytes, eocdOffset + 10)

    println(">>> ZIP: centralDirOffset=$centralDirOffset, numEntries=$numEntries")

    var cdOffset = centralDirOffset
    repeat(numEntries) {
        if (cdOffset + 46 > bytes.size) return@repeat

        // Central directory signature: 0x02014b50
        if (bytes[cdOffset].toInt() and 0xFF != 0x50 ||
            bytes[cdOffset + 1].toInt() and 0xFF != 0x4B ||
            bytes[cdOffset + 2].toInt() and 0xFF != 0x01 ||
            bytes[cdOffset + 3].toInt() and 0xFF != 0x02
        ) {
            println("!!! Invalid central directory signature at $cdOffset")
            return@repeat
        }

        val compressionMethod = readShortLE(bytes, cdOffset + 10)
        val compressedSize    = readIntLE(bytes, cdOffset + 20)
        val uncompressedSize  = readIntLE(bytes, cdOffset + 24)
        val filenameLength    = readShortLE(bytes, cdOffset + 28)
        val extraLength       = readShortLE(bytes, cdOffset + 30)
        val commentLength     = readShortLE(bytes, cdOffset + 32)
        val localHeaderOffset = readIntLE(bytes, cdOffset + 42)

        // Fixed: use sliceArray + decodeToString instead of deprecated String constructor
        val filename = bytes.sliceArray(
            (cdOffset + 46) until (cdOffset + 46 + filenameLength)
        ).decodeToString()

        // Read data offset from local file header
        val lfhFilenameLen = readShortLE(bytes, localHeaderOffset + 26)
        val lfhExtraLen    = readShortLE(bytes, localHeaderOffset + 28)
        val dataStart      = localHeaderOffset + 30 + lfhFilenameLen + lfhExtraLen

        if (dataStart + compressedSize > bytes.size) {
            println("!!! Data for '$filename' extends beyond ZIP bounds")
            cdOffset += 46 + filenameLength + extraLength + commentLength
            return@repeat
        }

        val compressedData = bytes.copyOfRange(dataStart, dataStart + compressedSize)

        val fileData = when (compressionMethod) {
            0 -> compressedData // STORED
            8 -> decompressDeflate(compressedData, uncompressedSize) // DEFLATE
            else -> {
                println("!!! Unsupported compression method: $compressionMethod for '$filename'")
                cdOffset += 46 + filenameLength + extraLength + commentLength
                return@repeat
            }
        }

        println(">>> Parsed ZIP entry: '$filename' compressed=$compressedSize uncompressed=${fileData.size}")
        entries[filename] = fileData

        cdOffset += 46 + filenameLength + extraLength + commentLength
    }

    return entries
}

// --- zlib DEFLATE decompression ---

@OptIn(ExperimentalForeignApi::class)
private fun decompressDeflate(compressed: ByteArray, uncompressedSize: Int): ByteArray {
    val output = ByteArray(uncompressedSize)

    compressed.usePinned { inPin ->
        output.usePinned { outPin ->
            memScoped {
                val stream = alloc<z_stream>()
                memset(stream.ptr, 0, sizeOf<z_stream>().toULong())

                stream.next_in   = inPin.addressOf(0).reinterpret()
                stream.avail_in  = compressed.size.toUInt()
                stream.next_out  = outPin.addressOf(0).reinterpret()
                stream.avail_out = uncompressedSize.toUInt()

                val initResult = inflateInit2_(
                    stream.ptr, -15, ZLIB_VERSION, sizeOf<z_stream>().toInt()
                )
                if (initResult != Z_OK) {
                    println("!!! inflateInit2 failed: $initResult")
                    return compressed
                }

                val inflateResult = inflate(stream.ptr, Z_FINISH)
                inflateEnd(stream.ptr)

                if (inflateResult != Z_STREAM_END) {
                    println("!!! inflate failed: $inflateResult")
                    return compressed
                }
            }
        }
    }

    return output
}

// --- Little-endian helpers ---

private fun readIntLE(data: ByteArray, off: Int): Int =
    (data[off].toInt() and 0xFF) or
            ((data[off + 1].toInt() and 0xFF) shl 8) or
            ((data[off + 2].toInt() and 0xFF) shl 16) or
            ((data[off + 3].toInt() and 0xFF) shl 24)

private fun readShortLE(data: ByteArray, off: Int): Int =
    (data[off].toInt() and 0xFF) or
            ((data[off + 1].toInt() and 0xFF) shl 8)