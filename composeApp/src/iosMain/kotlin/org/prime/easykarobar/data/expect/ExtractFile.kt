package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream
import platform.zlib.zlibVersion

/**
 * Extracts the first file in a simple ZIP archive (deflate compression only, no encryption).
 */
actual class ZipExtractor actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
        println("extractSingle called with zipBytes.size = ${zipBytes.size}")

        // ZIP file signature for local file header: 0x04034b50 (little endian)
        fun readUIntLE(b: ByteArray, offset: Int): Int =
            (b[offset].toInt() and 0xFF) or
                    ((b[offset + 1].toInt() and 0xFF) shl 8) or
                    ((b[offset + 2].toInt() and 0xFF) shl 16) or
                    ((b[offset + 3].toInt() and 0xFF) shl 24)

        // 1. Find local file header
        val sig = readUIntLE(zipBytes, 0)
        if (sig != 0x04034b50) {
            throw IllegalArgumentException("Not a ZIP file (missing local file header signature)!")
        }

        // 2. Parse local file header:
        // Offset   Bytes   Description
        // 0        4       Local file header signature = 0x04034b50
        // 4        2       Version needed to extract
        // 6        2       General purpose bit flag
        // 8        2       Compression method
        // 10       2       Last mod file time
        // 12       2       Last mod file date
        // 14       4       CRC-32
        // 18       4       Compressed size
        // 22       4       Uncompressed size
        // 26       2       File name length (n)
        // 28       2       Extra field length (m)
        // 30       n       File name
        // 30+n     m       Extra field
        // 30+n+m   ?       File data

        val compressionMethod = (zipBytes[8].toInt() and 0xFF) or ((zipBytes[9].toInt() and 0xFF) shl 8)
        val compressedSize = readUIntLE(zipBytes, 18)
        val uncompressedSize = readUIntLE(zipBytes, 22)
        val fileNameLength = (zipBytes[26].toInt() and 0xFF) or ((zipBytes[27].toInt() and 0xFF) shl 8)
        val extraFieldLength = (zipBytes[28].toInt() and 0xFF) or ((zipBytes[29].toInt() and 0xFF) shl 8)
        val fileName = zipBytes.copyOfRange(30, 30 + fileNameLength).toString()
        val fileDataOffset = 30 + fileNameLength + extraFieldLength

        println("ZIP entry fileName: $fileName")
        println("Compression method: $compressionMethod")
        println("Compressed size: $compressedSize")
        println("Uncompressed size: $uncompressedSize")
        println("fileDataOffset: $fileDataOffset")

        val fileBytes = zipBytes.copyOfRange(fileDataOffset, fileDataOffset + compressedSize)

        // 3. Extract file data (if DEFLATE, decompress; if STORE/no compression, return as is)
        return if (compressionMethod == 0) {
            println("Entry is stored (no compression). Returning as is.")
            fileBytes
        } else if (compressionMethod == 8) {
            // DEFLATE - decompress using zlib
            println("Entry is DEFLATE compressed. Decompressing with zlib...")
            decompressDeflate(fileBytes, uncompressedSize)
        } else {
            throw UnsupportedOperationException("ZIP compression method $compressionMethod is not supported.")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun decompressDeflate(compressed: ByteArray, expectedSize: Int): ByteArray {
        val result = mutableListOf<Byte>()
        memScoped {
            val strm = alloc<z_stream>()
            var ret = inflateInit2_(
                strm = strm.ptr,
                windowBits = -15, // raw DEFLATE (the format inside ZIP files)
                version = zlibVersion()?.toKString(),
                stream_size = sizeOf<z_stream>().toInt()
            )

            if (ret != Z_OK) {
                throw Exception("inflateInit2_ failed with status $ret")
            }

            try {
                val outBuffer = ByteArray(4096)
                compressed.usePinned { pinnedIn ->
                    outBuffer.usePinned { pinnedOut ->
                        strm.next_in = pinnedIn.addressOf(0).reinterpret()
                        strm.avail_in = compressed.size.toUInt()

                        do {
                            strm.next_out = pinnedOut.addressOf(0).reinterpret()
                            strm.avail_out = outBuffer.size.toUInt()

                            ret = inflate(strm.ptr, Z_NO_FLUSH)
                            if (ret != Z_OK && ret != Z_STREAM_END) {
                                throw Exception("inflate failed with status $ret")
                            }
                            val have = outBuffer.size - strm.avail_out.toInt()
                            if (have > 0) {
                                result.addAll(outBuffer.copyOfRange(0, have).toList())
                            }
                        } while (ret != Z_STREAM_END)
                    }
                }
            } finally {
                inflateEnd(strm.ptr)
            }
        }
        return result.toByteArray()
    }
}