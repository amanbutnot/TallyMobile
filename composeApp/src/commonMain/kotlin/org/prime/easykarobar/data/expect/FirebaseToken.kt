package org.prime.easykarobar.data.expect

expect fun removeFirebaseToken()
expect suspend fun getFirebaseToken(): String?

expect suspend fun setupFcmSubscription(topic: String): String?

