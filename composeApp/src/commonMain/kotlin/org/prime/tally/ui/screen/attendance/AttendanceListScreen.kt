package org.prime.tally.ui.screen.attendance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.business.viewmodel.attendance.AttendanceViewModel
import org.prime.tally.data.model.attendance.AttendanceListRequest
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.reportsShared.ReportFilterScreen

data class AttendanceListScreen(val isCheckIn: Boolean, val name: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        ReportFilterScreen(
            title = name,
            showStartDate = true,
            showEndDate = true,
            showAccountSelect = isCheckIn,
            buttonText = "Generate",
            onGenerateClick = {
                nav.push(
                    AttendanceScreenUi(
                        startDate = it.startDate,
                        endDate = it.endDate,
                        accountName = it.accountName,
                        vchType = if (isCheckIn) 2 else 1
                    )
                )
            }
        )
    }
}

data class AttendanceScreenUi(
    val startDate: String,
    val endDate: String,
    val vchType: Int,
    val accountName: String? = null
) : Screen {
    @Composable
    override fun Content() {
        TallyScaffold(
            title = "Demo",
            showBottomBar = false,
            bottomBarContent = { },
            content = { paddingValues ->
                val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
                val state by viewModel.listState
                LaunchedEffect(Unit) {
                    viewModel.getAttendance(
                        attendanceRequest = AttendanceListRequest(
                            VchType = vchType,
                            StartDate = startDate,
                            EndDate = endDate
                        )
                    )
                }
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            TallyCircularLoader()
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(state.data.toString())
                        }

                    }
                }
            }
        )
    }
}