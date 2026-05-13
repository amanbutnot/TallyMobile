package org.prime.easykarobar.ui.screen.reports.ledger

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.prime.easykarobar.ui.shared.globalShared.isBusy

data class LedgerReportItemScreen(
    val vchNo: String,
    val date: String,
    val vchType: String,
    val guid: String
) : Screen {

    @Composable
    override fun Content() {
        if (isBusy()) {
            BusyLedgerReportItemScreen(vchNo, date, vchType, guid).Content()
        } else {
            TallyLedgerReportItemScreen(vchNo, date, vchType, guid).Content()
        }
    }
}
