package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
actual fun rememberBarcodeScanner(
    onResult: (BarcodeScanResult) -> Unit
): BarcodeScannerLauncher {
    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    return remember {
        BarcodeScannerLauncher {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    onResult(
                        BarcodeScanResult.Success(barcode.rawValue.orEmpty())
                    )
                }
                .addOnFailureListener { e ->
                    onResult(BarcodeScanResult.Failure(e))
                }
                .addOnCanceledListener {
                    onResult(BarcodeScanResult.Cancelled)
                }
        }
    }
}

actual class BarcodeScannerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}