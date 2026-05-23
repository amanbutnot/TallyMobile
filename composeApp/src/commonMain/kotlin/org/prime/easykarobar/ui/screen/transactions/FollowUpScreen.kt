package org.prime.easykarobar.ui.screen.transactions

import CurrentDate
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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.reportsShared.BottomSheetItem
import org.tally.FollowUpOutstanding
import org.tally.vouchersLedgers.LedgerOpeningBalance

class FollowUpScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val db = DatabaseHolder.instance
        val ledgerMasters = remember { getLedgerMasters(db) }
        val nameList = ledgerMasters.map { it.Name }

        var selectedAccount by remember { mutableStateOf("") }
        var ledgerBalance by remember { mutableStateOf<LedgerOpeningBalance?>(null) }
        var outstandingBalance by remember { mutableStateOf<FollowUpOutstanding?>(null) }
        var showBottomSheet by remember { mutableStateOf(false) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        LaunchedEffect(selectedAccount) {
            ledgerBalance =
                db.vouchersLedgersQueries.ledgerOpeningBalance(selectedAccount, CurrentDate())
                    .executeAsOneOrNull()
            outstandingBalance =
                db.companyInformationQueries.followUpOutstanding(
                    date = StartDate(),
                    CM1 = selectedAccount
                )
                    .executeAsOneOrNull()
        }

        TallyScaffold(title = "Follow Up", onBack = { nav.pop() }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Account Selection
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showBottomSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.5.dp,
                            if (selectedAccount.isEmpty()) MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.3f
                            )
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedAccount.ifEmpty { "Select an account" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedAccount.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
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

                if (selectedAccount.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AmountRow(
                                label = "Ledger Balance",
                                amount = ledgerBalance?.OpeningBal?.formatToAmtDec() ?: "-"
                            )
                            AmountRow(
                                label = "Outstanding Balance",
                                amount = outstandingBalance?.PenAmt?.formatToAmtDec() ?: ""
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TallyButton(
                        label = "Follow Up",
                        onClick = {
                            val selectedLedger = ledgerMasters.find { it.Name == selectedAccount }
                            nav.push(
                                FollowUpDetailsScreen(
                                    accountName = selectedAccount,
                                    actCode = selectedLedger?.GUID ?: "",
                                    ledgerBalance = ledgerBalance?.OpeningBal?.formatToAmtDec()
                                        ?: "-",
                                    outstandingBalance = outstandingBalance?.PenAmt?.formatToAmtDec()
                                        ?: ""
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                BottomSheetItem(
                    showBottomSheet = showBottomSheet,
                    list = nameList,
                    onSelected = {
                        it?.let { selectedAccount = it }
                        showBottomSheet = false
                    },
                    onDismiss = { showBottomSheet = false },
                    bottomSheetState = bottomSheetState
                )
            }
        }
    }

    @Composable
    private fun AmountRow(label: String, amount: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
