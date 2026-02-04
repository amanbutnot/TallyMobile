package org.prime.easykarobar.ui.screen.reports.outstanding

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.ui.screen.reports.pendingOrder.PendingOrderPartyList
import org.prime.easykarobar.ui.shared.reportsShared.AllOneFilterScreen

data class OutstandingFilterScreen(val name: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        AllOneFilterScreen(
            title = "$name Filter",
            showStartDate = true,
            showEndDate = true,
            onGenerateClick = {
                if (name == "Bill Receivable" || name == "Bill Payable" || (name == "Pending Sale Order" && it.accountName != "") || (name == "Pending Purchase Order" && it.accountName != "")) {
                    nav.push(
                        OutstandingReportScreen(
                            name = name,
                            startDate = it.startDate,
                            endDate = it.endDate,
                            cm1 = it.accountName
                        )
                    )
                } else {
                    nav.push(
                        PendingOrderPartyList(
                            name = name,
                            startDate = it.startDate,
                            endDate = it.endDate,
                            cm1 = it.accountName
                        )
                    )
                }
            }
        )
    }
}
