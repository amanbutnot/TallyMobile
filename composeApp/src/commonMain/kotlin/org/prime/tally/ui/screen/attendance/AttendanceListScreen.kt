package org.prime.tally.ui.screen.attendance

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.prime.tally.ui.shared.reportsShared.ReportFilterScreen

data class AttendanceListScreen(val isCheckIn: Boolean, val name: String) : Screen {
    @Composable
    override fun Content() {
        ReportFilterScreen(
            title = name,
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = isCheckIn,
            buttonText = "Generate",
            onGenerateClick = {

            }
        )
    }
}