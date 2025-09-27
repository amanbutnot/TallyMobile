package org.prime.tally.data.expect

import org.tally.TallyDatabase

actual fun initializeDatabase(byteArray: ByteArray): TallyDatabase {
    return createDatabase(DriverFactory(), byteArray)
}