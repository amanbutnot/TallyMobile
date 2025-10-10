package org.prime.tally.ui.screen.reports.ledger

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.ui.shared.reportsShared.ReportFilterScreen

object LedgerReportFilterScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        ReportFilterScreen(
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = true,
            onGenerateClick = {
                nav.push(
                    LedgerReportScreen(it.accountName.toString(), it.startDate, it.endDate)
                )
            },
            title = "Ledger Filter"
        )
    }
}