package org.prime.easykarobar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform