package org.prime.tally.ui.screen.transactions

import CurrentDate
import TallyDatePickerRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.reportsShared.BottomSheetItem

data class SingleEntryReceipt(val name: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        var selectedAccount by rememberSaveable { mutableStateOf("") }
        var selectedDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var amount by rememberSaveable { mutableStateOf("") }
        var narration by rememberSaveable { mutableStateOf("") }
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }


        TallyScaffold(
            title = "$name Entry",
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TallyDatePickerRow(
                        label = "Entry Date",
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        defaultDate = CurrentDate(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectLedgerRow(
                            selectedAccount = selectedAccount,
                            onShowBottomSheet = { showBottomSheet = true },
                            modifier = Modifier.weight(1f),
                            title = "Ledger"
                        )
                        SelectLedgerRow(
                            selectedAccount = selectedAccount,
                            onShowBottomSheet = { showBottomSheet = true },
                            modifier = Modifier.weight(1f),
                            title = "Settlement"
                        )
                    }
                    TallyAmountField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Amount"
                    )
                    TallyNarrationField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = "Narration"
                    )
                    TallyButton(
                        onClick = { },
                        enabled = true,
                        label = "Create", backgroundColor = MaterialTheme.colorScheme.primary
                    )

                    BottomSheetItem<String>(
                        showBottomSheet = showBottomSheet,
                        list = emptyList(),
                        onSelected = { selectedAccount = it },
                        onDismiss = { showBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(),
                        title = "Ledger Name",
                        itemContent = {}
                    )
                }
            },
        )

    }
}

@Composable
private fun SelectLedgerRow(
    selectedAccount: String,
    onShowBottomSheet: () -> Unit,
    modifier: Modifier = Modifier, title: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Select $title",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedAccount.isNotEmpty())
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            onClick = onShowBottomSheet
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Select $title",
                        tint = if (selectedAccount.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = selectedAccount.ifEmpty { "Select $title" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedAccount.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selectedAccount.isEmpty())
                            FontWeight.Normal
                        else
                            FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}