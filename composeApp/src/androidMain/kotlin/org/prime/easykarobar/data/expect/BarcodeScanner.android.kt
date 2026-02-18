package org.prime.easykarobar.data.expect

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

@Composable
actual fun rememberBarcodeScanner(
    onResult: (BarcodeScanResult) -> Unit
): BarcodeScannerLauncher {

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result: ScanIntentResult ->
        when {
            result.contents == null -> onResult(BarcodeScanResult.Cancelled)
            else -> onResult(BarcodeScanResult.Success(result.contents))
        }
    }

    return remember {
        BarcodeScannerLauncher {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Scan a barcode")
                setBeepEnabled(true)
               // setOrientationLocked(false)
                setBarcodeImageEnabled(false)
                setOrientationLocked(false)
            }
            scanLauncher.launch(options)
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