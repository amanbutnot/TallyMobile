package org.prime.easykarobar.data.expect

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.memcpy
import platform.zlib.*

actual class ZipExtractor actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
        return try {
            val zipData = zipBytes.toNSData()
            val result = extractFromZipData(zipData)
            result.toByteArray()
        } catch (e: Exception) {
            throw IllegalStateException("ZIP extraction failed: ${e.message}", e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun extractFromZipData(zipData: NSData): NSData {
        val parser = ZipParser(zipData)

        // Validate ZIP signature
        parser.validateZipSignature()

        // Parse local file header
        val header = parser.parseLocalFileHeader()

        // Extract and decompress file data
        return when (header.compressionMethod) {
            CompressionMethod.STORE -> parser.extractStoredData(header)
            CompressionMethod.DEFLATE -> parser.extractAndDecompress(header)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ZipParser(private val data: NSData) {
    private val bytes = data.bytes?.reinterpret<ByteVar>()
        ?: throw IllegalStateException("Invalid ZIP data")

    private val length = data.length.toInt()

    fun validateZipSignature() {
        if (length < 30) {
            throw IllegalStateException("File too small to be valid ZIP: $length bytes")
        }

        val signature = readUInt32LE(0)
        if (signature != LOCAL_FILE_HEADER_SIGNATURE) {
            throw IllegalStateException(
                "Invalid ZIP signature: 0x${signature.toString(16).padStart(8, '0')}"
            )
        }
    }

    fun parseLocalFileHeader(): LocalFileHeader {
        return LocalFileHeader(
            versionNeeded = readUInt16LE(4),
            bitFlag = readUInt16LE(6),
            compressionMethod = when (val method = readUInt16LE(8)) {
                0 -> CompressionMethod.STORE
                8 -> CompressionMethod.DEFLATE
                else -> throw IllegalStateException("Unsupported compression: $method")
            },
            crc32 = readUInt32LE(14),
            compressedSize = readUInt32LE(18),
            uncompressedSize = readUInt32LE(22),
            fileNameLength = readUInt16LE(26),
            extraFieldLength = readUInt16LE(28),
            fileName = extractFileName(30, readUInt16LE(26))
        )
    }

    fun extractStoredData(header: LocalFileHeader): NSData {
        val dataOffset = calculateDataOffset(header)
        val dataLength = header.compressedSize.toULong()

        if (dataOffset + dataLength.toInt() > length) {
            throw IllegalStateException("Data extends beyond ZIP bounds")
        }

        return NSData.create(
            bytes = bytes?.plus(dataOffset),
            length = dataLength
        ) ?: throw IllegalStateException("Failed to extract stored data")
    }

    fun extractAndDecompress(header: LocalFileHeader): NSData {
        val dataOffset = calculateDataOffset(header)
        val compressedSize = header.compressedSize

        if (dataOffset + compressedSize > length) {
            throw IllegalStateException("Compressed data extends beyond ZIP bounds")
        }

        val compressedData = NSData.create(
            bytes = bytes?.plus(dataOffset),
            length = compressedSize.toULong()
        ) ?: throw IllegalStateException("Failed to extract compressed data")

        return decompressDeflate(compressedData, header.uncompressedSize)
    }

    private fun calculateDataOffset(header: LocalFileHeader): Int {
        return LOCAL_FILE_HEADER_SIZE + header.fileNameLength + header.extraFieldLength
    }

    private fun extractFileName(offset: Int, length: Int): String {
        if (length == 0) return ""

        val nameData = NSData.create(
            bytes = bytes?.plus(offset),
            length = length.toULong()
        ) ?: return ""

        return NSString.create(
            data = nameData,
            encoding = NSUTF8StringEncoding
        )?.toString() ?: ""
    }

    private fun readUInt8(offset: Int): Int {
        if (offset >= length) throw IndexOutOfBoundsException()
        return bytes!![offset].toInt() and 0xFF
    }

    private fun readUInt16LE(offset: Int): Int {
        return readUInt8(offset) or (readUInt8(offset + 1) shl 8)
    }

    private fun readUInt32LE(offset: Int): Int {
        return readUInt8(offset) or
                (readUInt8(offset + 1) shl 8) or
                (readUInt8(offset + 2) shl 16) or
                (readUInt8(offset + 3) shl 24)
    }

    private companion object {
        const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50
        const val LOCAL_FILE_HEADER_SIZE = 30
    }
}

private data class LocalFileHeader(
    val versionNeeded: Int,
    val bitFlag: Int,
    val compressionMethod: CompressionMethod,
    val crc32: Int,
    val compressedSize: Int,
    val uncompressedSize: Int,
    val fileNameLength: Int,
    val extraFieldLength: Int,
    val fileName: String
)

private enum class CompressionMethod {
    STORE,
    DEFLATE
}

@OptIn(ExperimentalForeignApi::class)
private fun decompressDeflate(compressedData: NSData, expectedSize: Int): NSData {
    // First, try native NSData decompression
    tryNativeDecompression(compressedData)?.let { return it }

    // Fallback to manual zlib inflation
    return manualInflate(compressedData, expectedSize)
}

@OptIn(ExperimentalForeignApi::class)
private fun tryNativeDecompression(compressedData: NSData): NSData? {
    return memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()

        // Try with zlib algorithm (may work if data has zlib wrapper)
        compressedData.decompressedDataUsingAlgorithm(
            NSDataCompressionAlgorithmZlib,
            errorPtr.ptr
        )?.also { return it }

        // Try with lzfse (some systems may support deflate through this)
        compressedData.decompressedDataUsingAlgorithm(
            NSDataCompressionAlgorithmLZFSE,
            errorPtr.ptr
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun manualInflate(compressedData: NSData, expectedSize: Int): NSData {
    return memScoped {
        val stream = alloc<z_stream>()
        platform.posix.memset(stream.ptr, 0, sizeOf<z_stream>().toULong())

        // Initialize for raw DEFLATE (ZIP uses raw DEFLATE without zlib wrapper)
        val windowBits = -MAX_WBITS // Negative value = raw deflate
        val initResult = inflateInit2_(
            stream.ptr,
            windowBits,
            zlibVersion()?.toKString(),
            sizeOf<z_stream>().toInt()
        )

        if (initResult != Z_OK) {
            throw IllegalStateException(
                "Failed to initialize decompression: ${getZlibError(stream, initResult)}"
            )
        }

        try {
            performInflation(stream, compressedData, expectedSize)
        } finally {
            inflateEnd(stream.ptr)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun performInflation(
    stream: z_stream,
    compressedData: NSData,
    expectedSize: Int
): NSData = memScoped {
    // Allocate output buffer with some extra space
    val outputSize = maxOf(expectedSize, expectedSize + 1024)
    val outputBuffer = allocArray<ByteVar>(outputSize)

    // Set up input
    stream.avail_in = compressedData.length.toUInt()
    stream.next_in = compressedData.bytes?.reinterpret()

    // Set up output
    stream.avail_out = outputSize.toUInt()
    stream.next_out = outputBuffer.reinterpret()

    // Perform decompression
    val result = inflate(stream.ptr, Z_FINISH)

    when (result) {
        Z_STREAM_END -> {
            val decompressedSize = outputSize - stream.avail_out.toInt()
            NSData.create(
                bytes = outputBuffer,
                length = decompressedSize.toULong()
            ) ?: throw IllegalStateException("Failed to create decompressed data")
        }
        Z_OK -> throw IllegalStateException("Incomplete decompression")
        Z_BUF_ERROR -> throw IllegalStateException("Buffer error during decompression")
        Z_DATA_ERROR -> throw IllegalStateException(
            "Corrupt data: ${getZlibError(stream, result)}"
        )
        else -> throw IllegalStateException(
            "Decompression failed: ${getZlibError(stream, result)}"
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getZlibError(stream: z_stream, code: Int): String {
    val message = stream.msg?.toKString()
    return if (message != null) {
        "code=$code, message=$message"
    } else {
        "code=$code"
    }
}

// Extension functions for convenient conversion between ByteArray and NSData

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()

    return usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = size.toULong()
        ) ?: throw IllegalStateException("Failed to create NSData")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)

    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, size.toULong())
        }
    }
}