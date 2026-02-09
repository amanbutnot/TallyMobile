package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable

@Composable
expect fun rememberBarcodeScanner(onResult: (BarcodeScanResult) -> Unit): BarcodeScannerLauncher



expect class BarcodeScannerLauncher {
    fun launch()
}

sealed class BarcodeScanResult {
    data class Success(val value: String) : BarcodeScanResult()
    object Cancelled : BarcodeScanResult()
    data class Failure(val error: Throwable?) : BarcodeScanResult()
}
