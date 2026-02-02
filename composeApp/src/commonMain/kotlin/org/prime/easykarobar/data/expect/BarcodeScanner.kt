package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable

@Composable
expect fun rememberBarcodeScanner(onResult: (String?) -> Unit): BarcodeScannerLauncher


expect class BarcodeScannerLauncher {
    fun launch()
}