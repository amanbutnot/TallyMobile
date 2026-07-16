package org.prime.easykarobar.ui.screen.reports.outstanding

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.reportsShared.ReportFilterScreen

data class OutstandingDisFilterScreen(val name: String = "Bill Receivable") : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        ReportFilterScreen(
            title = "$name Filter",
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = false,
            buttonText = "Generate",
            onGenerateClick = {
                nav.push(
                    OutstandingReportScreen(
                        name = name,
                        startDate = it.startDate,
                        endDate = it.endDate,
                        cm1 = SharedPrefs.DistributorData.get()?.ledger_name,
                        calculateDays = "Due Date",
                        showOtherToggle = false,
                    )
                )
            },
        )
    }
}
