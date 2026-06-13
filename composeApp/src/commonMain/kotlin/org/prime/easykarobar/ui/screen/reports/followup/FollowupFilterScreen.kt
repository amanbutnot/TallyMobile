package org.prime.easykarobar.ui.screen.reports.followup

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.ui.shared.reportsShared.AllOneFilterScreen

class FollowupFilterScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        AllOneFilterScreen(
            title = "Followup Filter",
            showStartDate = true,
            showEndDate = true,
            showDueDate = false,
            showOtherToggle = false,
            showSalesmanFilter = true,
            onGenerateClick = {
                nav.push(
                    FollowupListScreen(
                        accountName = it.accountName,
                        actCode = it.accountGUID,
                        startDate = it.startDate,
                        endDate = it.endDate,
                        salesman = it.salesmanName
                    )
                )
            }
        )
    }
}
