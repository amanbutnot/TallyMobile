package org.prime.easykarobar.ui.screen.reports.salesman

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.tally.GetSalesmanName
import org.tally.GetSalesmanTargets
import kotlin.text.ifEmpty
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class SalesmanTargetFilterScreen(val isGroup: Boolean) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        var salesmanList by remember { mutableStateOf<List<GetSalesmanName>>(emptyList()) }
        var selectedSalesman by remember { mutableStateOf("") }
        var showSalesmanNameList by remember { mutableStateOf(false) }
        var reportType by rememberSaveable { mutableStateOf("ALL") }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                salesmanList = db.salesManTargetQueries.getSalesmanName().executeAsList()
            }
        }
        TallyScaffold(
            title = if (isGroup) "Salesman Group Wise Target" else "Salesman Wise Target",
            showBottomBar = false,
            bottomBarContent = { },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
                    val currentMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).month
                    var selectedMonth by remember { mutableStateOf(currentMonth) }
                    var selectedYear by remember { mutableStateOf(currentYear) }
                    val nav = LocalNavigator.currentOrThrow

                    // Header Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Select Report Period",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose the month and year for the salesman target report",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    )
                    {
                        // All Accounts Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    reportType = "ALL"
                                    selectedSalesman = ""
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

                    if (!isGroup && reportType == "SINGLE") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Select Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSalesmanNameList = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
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
                                        text = selectedSalesman.ifEmpty { "Choose an account" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedSalesman.isEmpty())
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

                    // Filter Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Report Filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            MonthYearPicker(
                                selectedMonth = selectedMonth,
                                onMonthSelected = { selectedMonth = it },
                                selectedYear = selectedYear,
                                onYearSelected = { selectedYear = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Selected Period Summary
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Selected Period: ${
                                    selectedMonth.name.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                } $selectedYear",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }


                    TransactionOneBottomSheet(
                        showBottomSheet = showSalesmanNameList,
                        list = salesmanList.map { it.SalesmanName.toString() },
                        onSelected = { selectedSalesman = it },
                        onDismiss = { showSalesmanNameList = false },
                        bottomSheetState = rememberModalBottomSheetState(true),
                        title = "Select Saleman",
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Generate Button
                    TallyButton(
                        label = "Generate Report",
                        onClick = {
                            if (isGroup) {
                                nav.push(
                                    SalesmanGroupWiseTargetReport(
                                        selectedMonth.name, selectedYear
                                    )
                                )
                            } else {
                                nav.push(
                                    SalesmanTargetReportScreen(
                                        month = selectedMonth.name,
                                        year = selectedYear,
                                        name = if (selectedSalesman == "") null else selectedSalesman
                                    )
                                )
                            }
                        },
                        enabled = (selectedMonth.name.isNotEmpty() && selectedYear.toString()
                            .isNotEmpty() && (reportType == "ALL" || selectedSalesman.isNotEmpty())),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MonthYearPicker(
    selectedMonth: Month,
    onMonthSelected: (Month) -> Unit,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Picker
        var monthExpanded by remember { mutableStateOf(false) }
        Column {
            Text(
                text = "Month",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedMonth.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Month.values().forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onMonthSelected(month)
                                monthExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // Year Picker
        var yearExpanded by remember { mutableStateOf(false) }
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        val years = (currentYear - 10..currentYear + 10).toList()

        Column {
            Text(
                text = "Year",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedYear.toString(),
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onYearSelected(year)
                                yearExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}