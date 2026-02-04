package org.prime.easykarobar.ui.shared.reportsShared

import CurrentDate
import TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.transactions.sale.SmallAddButton
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.parseDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ReportFilterScreen(
    title: String,
    showStartDate: Boolean,
    showEndDate: Boolean,
    showAccountSelect: Boolean,
    showDateRangeSelector: Boolean = false,
    buttonText: String = "Generate Report",
    showAddButton: Boolean = false,
    onAddButtonClick: () -> Unit = {},
    onGenerateClick: (GenerateReportData) -> Unit
) {
    val nav = LocalNavigator.currentOrThrow
    TallyScaffold(title, onBack = { nav.pop() }) { paddingValues ->
        var startDate by rememberSaveable { mutableStateOf(StartDate()) }
        var endDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var selectedAccount by rememberSaveable { mutableStateOf("") }
        var showBottomSheet by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }
        var selectedRange by rememberSaveable { mutableStateOf("Custom") }

        val db = DatabaseHolder.instance
        val list = getLedgerMasters(db)
        val nameList = list.map { it.Name }
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var showDateRangeMenu by remember { mutableStateOf(false) }
        val dateRanges = listOf(
            "Today",
            "Yesterday",
            "Tomorrow",
            "This Week",
            "Last Week",
            "Last 7 Days",
            "Last 30 Days",
            "This Month",
            "This Year"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            if (showDateRangeSelector) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Date Range",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedRange,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box {
                            Surface(
                                onClick = { showDateRangeMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Select",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showDateRangeMenu,
                                onDismissRequest = { showDateRangeMenu = false },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 6.dp)
                            ) {
                                dateRanges.forEach { range ->

                                    val isSelected = selectedRange == range

                                    DropdownMenuItem(
                                        onClick = {
                                            val (start, end) = getDateRange(range)
                                            startDate = start
                                            endDate = end
                                            selectedRange = range
                                            showDateRangeMenu = false
                                        },
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected)
                                                            MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.1f
                                                            )
                                                        else
                                                            MaterialTheme.colorScheme.surface,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    text = range,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (isSelected)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }

                        }
                    }
                }
            }

            // Filters Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Account Selection
                    if (showAccountSelect) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Account",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showBottomSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selectedAccount.isEmpty())
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAccount.ifEmpty { "Select an account" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedAccount.isEmpty())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        if (showStartDate || showEndDate) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )
                        }
                    }

                    if (showAddButton) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmallAddButton(
                                label = "Add New",
                                onClick = { onAddButtonClick() },
                            )

                        }
                    }

                    // Start Date
                    if (showStartDate) {
                        TallyDatePickerRow(
                            label = "Start Date",
                            selectedDate = startDate,
                            onDateSelected = {
                                startDate = it
                                if (showDateRangeSelector) selectedRange = "Custom"
                            },
                            defaultDate = CurrentDate()
                        )

                        if (showEndDate) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // End Date
                    if (showEndDate) {
                        TallyDatePickerRow(
                            label = "End Date",
                            selectedDate = endDate,
                            defaultDate = CurrentDate(),
                            onDateSelected = {
                                endDate = it
                                if (showDateRangeSelector) selectedRange = "Custom"
                            }
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

            // Error Message
            AnimatedVisibility(visible = showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "End date cannot be earlier than start date",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Generate Button
            TallyButton(
                label = buttonText,
                onClick = {
                    if (parseDate(startDate) > parseDate(endDate)) {
                        showError = true
                    } else {
                        showError = false
                        onGenerateClick(
                            GenerateReportData(
                                accountName = selectedAccount,
                                startDate = startDate,
                                endDate = endDate
                            )
                        )
                    }
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

@OptIn(ExperimentalTime::class)
fun getDateRange(range: String): Pair<String, String> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    return when (range) {
        "Today" -> today to today
        "Yesterday" -> {
            val y = today.minus(1, DateTimeUnit.DAY)
            y to y
        }

        "Tomorrow" -> {
            val t = today.plus(1, DateTimeUnit.DAY)
            t to t
        }

        "This Week" -> {
            val start = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
            start to today
        }

        "Last Week" -> {
            val start = today.minus(today.dayOfWeek.isoDayNumber + 6, DateTimeUnit.DAY)
            val end = start.plus(6, DateTimeUnit.DAY)
            start to end
        }

        "Last 7 Days" -> today.minus(6, DateTimeUnit.DAY) to today
        "Last 30 Days" -> today.minus(29, DateTimeUnit.DAY) to today
        "This Month" -> today.minus(today.dayOfMonth - 1, DateTimeUnit.DAY) to today
        "This Year" -> today.minus(today.dayOfYear - 1, DateTimeUnit.DAY) to today
        else -> today to today
    }.let { it.first.toString() to it.second.toString() }
}


val DayOfWeek.isoDayNumber: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
    }

data class GenerateReportData(
    val accountName: String? = null,
    val startDate: String,
    val endDate: String
)