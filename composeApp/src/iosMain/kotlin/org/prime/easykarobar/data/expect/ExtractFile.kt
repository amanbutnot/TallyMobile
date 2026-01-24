package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream
import platform.zlib.zlibVersion

actual class ZipExtractor actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
        val output = mutableListOf<Byte>()
        val chunk = 16384
        val buffer = ByteArray(chunk)

        memScoped {
            val strm = alloc<z_stream>()
            // The inflateInit2_ function is called with the zlib version and stream size.
            if (inflateInit2_(strm.ptr, -15, zlibVersion() as String?, sizeOf<z_stream>().toInt()) != Z_OK) {
                throw IllegalStateException("Failed to initialize zlib.")
            }
            try {
                zipBytes.usePinned { pinnedInput ->
                    buffer.usePinned { pinnedOutput ->
                        var inputPosition = 0
                        var ret: Int
                        do {
                            val remainingInput = (zipBytes.size - inputPosition).toUInt()
                            if (remainingInput == 0u) break
                            strm.avail_in = remainingInput
                            strm.next_in = pinnedInput.addressOf(inputPosition).reinterpret()

                            do {
                                strm.avail_out = chunk.toUInt()
                                strm.next_out = pinnedOutput.addressOf(0).reinterpret()
                                ret = inflate(strm.ptr, platform.zlib.Z_NO_FLUSH)
                                when (ret) {
                                    platform.zlib.Z_STREAM_ERROR, platform.zlib.Z_NEED_DICT, platform.zlib.Z_DATA_ERROR, platform.zlib.Z_MEM_ERROR -> {
                                        throw IllegalStateException("Zlib inflation error: $ret")
                                    }
                                }
                                val have = chunk - strm.avail_out.toInt()
                                if (have > 0) {
                                    output.addAll(buffer.take(have))
                                }
                            } while (strm.avail_out == 0u)
                            inputPosition = (zipBytes.size - strm.avail_in.toInt())
                        } while (ret != Z_STREAM_END)
                    }
                }
            } finally {
                inflateEnd(strm.ptr)
            }
        }
        return output.toByteArray()
    }
}
