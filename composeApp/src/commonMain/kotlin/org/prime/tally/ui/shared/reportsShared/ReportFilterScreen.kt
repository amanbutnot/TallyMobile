package org.prime.tally.ui.shared.reportsShared

import TallyDatePickerRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import CurrentDate
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Surface
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.globalShared.StartDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterScreen(
    title:String,
    showStartDate: Boolean,
    showEndDate: Boolean,
    showAccountSelect: Boolean,
    buttonText:String="Generate Report",
    onGenerateClick: (GenerateReportData) -> Unit
) {
    val nav = LocalNavigator.currentOrThrow
    TallyScaffold(title, onBack = { nav.pop() }) { paddingValues ->
        var startDate by rememberSaveable { mutableStateOf(StartDate()) }
        var endDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var selectedAccount by rememberSaveable { mutableStateOf("") }
        var showBottomSheet by remember { mutableStateOf(false) }
        val db = DatabaseHolder.instance
        val list = db.ledgerMasterQueries.selectAll().executeAsList()
        val nameList = list.map { it.Name }
        val state = rememberModalBottomSheetState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues).navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            )
            {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fill the filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    if (showAccountSelect) {
                        Column {
                            Text(
                                text = "Select Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showBottomSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAccount.ifEmpty { "Choose an account" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedAccount.isEmpty())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }


                    if (showStartDate) {
                        TallyDatePickerRow(
                            label = "Start Date",
                            selectedDate = startDate,
                            onDateSelected = { startDate = it },
                            defaultDate = CurrentDate()

                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }


                    if (showEndDate) {
                        TallyDatePickerRow(
                            label = "End Date",
                            selectedDate = endDate,
                            defaultDate = CurrentDate(),
                            onDateSelected = { endDate = it }
                        )

                    }



                    BottomSheetItem(
                        showBottomSheet = showBottomSheet,
                        list = nameList,
                        onSelected = {
                            it?.let { selectedAccount = it }
                        },
                        onDismiss = { showBottomSheet = false },
                        bottomSheetState = state
                    )

                }
            }
            Spacer(modifier = Modifier.weight(1f))

            TallyButton(
                label = buttonText,
                onClick = {
                    onGenerateClick(
                        GenerateReportData(
                            accountName = selectedAccount,
                            startDate = startDate,
                            endDate = endDate
                        )
                    )
//                    nav.push(
//                        LedgerReportScreen(selectedAccount, startDate, endDate)
//                    )
                },
                enabled = (!showStartDate || startDate.isNotEmpty()) &&
                        (!showEndDate || endDate.isNotEmpty()) &&
                        (!showAccountSelect || selectedAccount.isNotEmpty()),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

data class GenerateReportData(
    val accountName: String? = null,
    val startDate: String,
    val endDate: String
)