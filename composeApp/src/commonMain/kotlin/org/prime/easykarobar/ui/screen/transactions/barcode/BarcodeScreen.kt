package org.prime.easykarobar.ui.screen.transactions.barcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
//import org.ncgroup.kscan.BarcodeFormats
//import org.ncgroup.kscan.BarcodeResult
//import org.ncgroup.kscan.ScannerView
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

object BarcodeScreen : Screen {
    @Composable
    override fun Content() {

        val nav = LocalNavigator.currentOrThrow
        var quantity by remember { mutableStateOf("") }
        var selectedText by remember { mutableStateOf("") }
        var showBarcode by remember { mutableStateOf(false) }

        TallyScaffold("Barcode Screen", onBack = { nav.pop() }) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TallyTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    placeholder = "Enter Quantity",
                    isPassword = false,
                    isNumber = true,
                    label = "Quantity",
                )
                TallyButton(
                    label = "Scan",
                    onClick = { showBarcode = true },
                    enabled = quantity.isNotEmpty()
                )
//                if (showBarcode) {
//                    ScannerView(
//                        codeTypes = listOf(
//                            BarcodeFormats.FORMAT_QR_CODE,
//                            BarcodeFormats.FORMAT_EAN_13,
//                        )
//                    ) { result ->
//                        when (result) {
//                            is BarcodeResult.OnSuccess -> {
//                                println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
//                                selectedText = result.barcode.data
//                            }
//
//                            is BarcodeResult.OnFailed -> {
//                                println("error: ${result.exception.message}")
//                            }
//
//                            BarcodeResult.OnCanceled -> {
//                                println("scan canceled")
//                            }
//                        }
//                    }
//                    Text(selectedText)
//                }
            }
        }
    }
}