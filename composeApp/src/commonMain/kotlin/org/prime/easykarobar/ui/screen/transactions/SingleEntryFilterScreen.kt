package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
            onGenerateClick = {
                when (vchType) {
                    14, 19, 16, 15 -> {
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

            }
        )
    }
}
