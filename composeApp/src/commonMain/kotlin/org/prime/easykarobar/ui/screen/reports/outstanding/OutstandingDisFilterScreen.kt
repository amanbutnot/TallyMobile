package org.prime.easykarobar.ui.screen.reports.outstanding

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.reportsShared.ReportFilterScreen

object OutstandingDisFilterScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        ReportFilterScreen(
            title = "Bill Receivable Filter",
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = false,
            buttonText = "Generate",
            onGenerateClick = {
                nav.push(
                    OutstandingReportScreen(
                        name = "Bill Receivable",
                        startDate = it.startDate,
                        endDate = it.endDate,
                        cm1 = SharedPrefs.DistributorData.get()?.ledger_name,       calculateDays = "Due Date",
                    )
                )
            }
        )
    }
}