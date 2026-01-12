package org.prime.tally.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream


actual class ZipExtractor actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractSingle(zipBytes: ByteArray): ByteArray {
        if (zipBytes.isEmpty()) {
            return ByteArray(0)
        }

        val result = mutableListOf<Byte>()

        memScoped {
            val strm = alloc<z_stream>()

            var ret = inflateInit2_(strm.ptr, 15 + 32)
            if (ret != Z_OK) {
                throw Exception("inflateInit2_ failed with status $ret")
            }

            try {
                zipBytes.usePinned { pinnedIn ->
                    strm.next_in = pinnedIn.addressOf(0).reinterpret()
                    strm.avail_in = zipBytes.size.toUInt()

                    val outBuffer = ByteArray(4096)

                    do {
                        outBuffer.usePinned { pinnedOut ->
                            strm.next_out = pinnedOut.addressOf(0)
                            strm.avail_out = outBuffer.size.toUInt()

                            ret = inflate(strm.ptr, Z_NO_FLUSH)
                            if (ret != Z_OK && ret != Z_STREAM_END) {
                                throw Exception("inflate failed with status $ret")
                            }

                            val have = outBuffer.size - strm.avail_out.toInt()
                            if (have > 0) {
                                result.addAll(outBuffer.copyOfRange(0, have))
                            }
                        }
                    } while (ret != Z_STREAM_END)
                }
            } finally {
                inflateEnd(strm.ptr)
            }
        }

        return result.toByteArray()
    }
}