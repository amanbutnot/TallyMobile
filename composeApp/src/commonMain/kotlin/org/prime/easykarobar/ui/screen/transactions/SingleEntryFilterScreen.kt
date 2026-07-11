package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.ui.screen.transactions.sale.SaleScreen
import org.prime.easykarobar.ui.shared.reportsShared.ReportFilterScreen

data class SingleEntryFilterScreen(val name: String, val vchType: Int) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        ReportFilterScreen(
            title = "$name Filter",
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = false,
            buttonText = "Show List",
            showAddButton = true,
            onAddButtonClick = {
                when (vchType) {
                    14, 19, 16, 15, 18, 17 -> {
                        nav.push(
                            SingleEntryReceipt(
                                vchType = vchType,
                                name = name,
                                showPdc = vchType == 19 || vchType == 14
                            )
                        )
                    }

                    12, 3, 9, 13, 10, 2, 7,22,23 -> {
                        nav.push(
                            SaleScreen(
                                vchType = vchType,
                                name = name
                            )
                        )
                    }

                }
            },
            onGenerateClick = {
                when (vchType) {
                    14, 19, 16, 15, 17, 18 -> {
                        nav.push(
                            SingleEntryListScreen(
                                startDate = it.startDate,
                                endDate = it.endDate,
                                vchType = vchType,
                                name = name
                            )
                        )
                    }

                    12, 3, 9, 13, 10, 2, 7 -> {
                        nav.push(
                            InventoryListScreen(
                                startDate = it.startDate,
                                endDate = it.endDate,
                                vchType = vchType,
                                name = name
                            )
                        )
                    }

                }

            },
        )
    }
}
