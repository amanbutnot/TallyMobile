package org.prime.tally

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform