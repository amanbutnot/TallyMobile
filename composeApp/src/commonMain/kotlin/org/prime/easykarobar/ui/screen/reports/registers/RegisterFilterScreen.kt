package org.prime.easykarobar.ui.screen.reports.registers

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.ui.shared.reportsShared.ReportFilterScreen

data class RegisterFilterScreen(val name: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        ReportFilterScreen(
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = false,
            onGenerateClick = {
                nav.push(
                    RegisterReportScreen(name, it.startDate, it.endDate)
                )
            },
            title = "$name Filter"
        )
    }
}