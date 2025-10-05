package org.prime.tally.ui.screen.reports.ledger

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.TallyButton
import org.prime.tally.ui.shared.TallyScaffold
import org.prime.tally.ui.shared.reportsShared.BottomSheetItem

object LedgerReportFilterScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        TallyScaffold("Ledger Filter", onBack = { nav.pop() }) { paddingValues ->
            var startDate by remember { mutableStateOf("") }
            var endDate by remember { mutableStateOf("") }
            var selectedAccount by remember { mutableStateOf("") }
            var showBottomSheet by remember { mutableStateOf(false) }
            val db = DatabaseHolder.instance
            val list = db.ledgerMasterQueries.selectAll().executeAsList()
            val nameList = list.map { it.Name }
            val state = rememberModalBottomSheetState()
            val nav = LocalNavigator.currentOrThrow

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            text = "Select period for your report and account",
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
                        TallyDatePickerRow(
                            label = "Start Date",
                            selectedDate = startDate,
                            onDateSelected = { startDate = it }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        TallyDatePickerRow(
                            label = "End Date",
                            selectedDate = endDate,
                            onDateSelected = { endDate = it }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column {
                            Text(
                                text = "Select Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TallyButton(
                                label = selectedAccount.ifEmpty { "Select Account" },
                                onClick = { showBottomSheet = true },
                                backgroundColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                enabled = true
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
                Button(onClick = { nav.push(
                    LedgerReportScreen("Cash", "2020-01-01", "2025-10-04")
                )}){
                    Text("Demo")
                }


                Spacer(modifier = Modifier.weight(1f))

                TallyButton(
                    label = "Generate Report",
                    onClick = {
                        nav.push(
                            LedgerReportScreen(selectedAccount, startDate, endDate)
                        )
                    },
                    enabled = startDate.isNotEmpty() && endDate.isNotEmpty() && selectedAccount.isNotEmpty(),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}