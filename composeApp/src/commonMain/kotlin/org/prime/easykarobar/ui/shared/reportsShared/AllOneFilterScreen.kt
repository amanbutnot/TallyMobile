// Modern, polished AllOneFilterScreen with professional design
package org.prime.easykarobar.ui.shared.reportsShared

import CurrentDate
import OutstandingDate
import TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.parseDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllOneFilterScreen(
    title: String,
    showStartDate: Boolean,
    showEndDate: Boolean,
    buttonText: String = "Generate Report",
    onGenerateClick: (GenerateOneAllReportData) -> Unit
) {
    val nav = LocalNavigator.currentOrThrow
    TallyScaffold(title, onBack = { nav.pop() }) { paddingValues ->

        var startDate by rememberSaveable { mutableStateOf(StartDate()) }
        var endDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var selectedAccount by rememberSaveable { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        var selectedGUID by rememberSaveable { mutableStateOf("") }
        var showBottomSheet by remember { mutableStateOf(false) }
        var reportType by rememberSaveable { mutableStateOf("ALL") }

        val db = DatabaseHolder.instance
        val list = getLedgerMasters(db)
        val nameList = list.map { (it.Name ?: "") to (it.GUID ?: "") }
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Compact Header
            Text(
                text = "Report Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Main Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Report Type Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Report Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // All Accounts Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        reportType = "ALL"
                                        selectedAccount = ""
                                        selectedGUID = ""
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (reportType == "ALL")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (reportType == "ALL")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = reportType == "ALL",
                                        onClick = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "All Accounts",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (reportType == "ALL")
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Single Account Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { reportType = "SINGLE" },
                                shape = RoundedCornerShape(8.dp),
                                color = if (reportType == "SINGLE")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (reportType == "SINGLE")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = reportType == "SINGLE",
                                        onClick = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Single Account",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (reportType == "SINGLE")
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Account Selection (only for SINGLE)
                    if (reportType == "SINGLE") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    }

                    // Date Range Section
                    if (showStartDate || showEndDate) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (showStartDate) {
                                TallyDatePickerRow(
                                    label = "Start Date",
                                    selectedDate = startDate,
                                    onDateSelected = { startDate = it },
                                    defaultDate = OutstandingDate()
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
                            // Error Message with Animation
                            AnimatedVisibility(
                                visible = showError,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            )
                            {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Text(
                                            text = "End date cannot be earlier than start date",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Generate Button
            TallyButton(
                label = buttonText,
                onClick = {
                    if(parseDate(startDate)>parseDate(endDate)){
                        showError = true
                    }else{
                        showError = false
                        onGenerateClick(
                            GenerateOneAllReportData(
                                accountGUID = selectedGUID,
                                accountName = selectedAccount,
                                startDate = startDate,
                                endDate = endDate
                            )
                        )
                    }
                },
                enabled = (!showStartDate || startDate.isNotEmpty()) &&
                        (!showEndDate || endDate.isNotEmpty()) &&
                        (reportType == "ALL" || selectedAccount.isNotEmpty()),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Bottom Sheet
        TransactionBottomSheet(
            showBottomSheet = showBottomSheet,
            list = nameList,
            onSelected = {
                selectedAccount = it.first
                selectedGUID = it.second
            },
            onDismiss = { showBottomSheet = false },
            bottomSheetState = state
        )
    }
}

data class GenerateOneAllReportData(
    val accountGUID: String,
    val accountName: String,
    val startDate: String,
    val endDate: String
)