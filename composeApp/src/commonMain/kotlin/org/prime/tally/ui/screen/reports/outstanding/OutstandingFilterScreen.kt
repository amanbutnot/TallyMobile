package org.prime.tally.ui.screen.reports.outstanding

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.ui.shared.reportsShared.ReportFilterScreen

data class OutstandingFilterScreen(val name: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        ReportFilterScreen(
            title = "$name Filter",
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = false,
            onGenerateClick = {
                nav.push(
                    OutstandingReportScreen(
                        name = name,
                        startDate = it.startDate,
                        endDate = it.endDate
                    )
                )
            }
        )
    }
}
