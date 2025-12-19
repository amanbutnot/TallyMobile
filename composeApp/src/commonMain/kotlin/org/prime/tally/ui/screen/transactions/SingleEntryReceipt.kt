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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.business.viewmodel.transactions.SingleEntryViewModel
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.model.transactions.TranListResponse
import org.prime.tally.data.model.transactions.TranRequest
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyResultDialog
import org.prime.tally.ui.shared.composables.TallyScaffold

data class SingleEntryReceipt(
    val name: String,
    val vchType: Int,
    val existingTransaction: TranListResponse? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow

        var selectedAccount by rememberSaveable { mutableStateOf(existingTransaction?.C1 ?: "") }
        var selectedAccountGUID by rememberSaveable {
            mutableStateOf(
                existingTransaction?.CM1 ?: ""
            )
        }
        var selectedSettlement by rememberSaveable { mutableStateOf(existingTransaction?.C2 ?: "") }
        var selectedSettlementGUID by rememberSaveable {
            mutableStateOf(
                existingTransaction?.CM2 ?: ""
            )
        }
        var amount by rememberSaveable { mutableStateOf(existingTransaction?.D2?.toString() ?: "") }
        var narration by rememberSaveable { mutableStateOf(existingTransaction?.Narration ?: "") }
        var selectedDate by rememberSaveable {
            mutableStateOf(
                existingTransaction?.TranDate ?: CurrentDate()
            )
        }


        var showPopup by remember { mutableStateOf(false) }
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }
        var showSettlementBottomSheet by rememberSaveable { mutableStateOf(false) }
        val list = db.ledgerMasterQueries.selectAll().executeAsList()


        val viewmodel: SingleEntryViewModel = viewModel { SingleEntryViewModel() }
        val state by viewmodel.dataState

        val isEdit = existingTransaction != null


        if (state.isLoading) {
            if (isEdit) {

                TallyLoadingDialog("Editing your transaction")
            } else {

                TallyLoadingDialog("Creating your transaction")
            }
        }


        TallyScaffold(
            title = if (isEdit) "Edit $name Entry" else "Create $name Entry",
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (isEdit) {
                        InfoRow("Voucher No", existingTransaction?.VchNo ?: "")
                    }


                    TallyDatePickerRow(
                        label = "Entry Date",
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        defaultDate = CurrentDate(),

                        )

                    SelectLedgerRow(
                        selectedAccount = selectedAccount,
                        onShowBottomSheet = { showBottomSheet = true },

                        title = "Ledger"
                    )


                    SelectLedgerRow(
                        selectedAccount = selectedSettlement,
                        onShowBottomSheet = { showSettlementBottomSheet = true },

                        title = "Settlement"
                    )

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
                        onClick = {

                            if (isEdit) {
                                println(
                                    TranRequest(
                                        VchType = vchType,
                                        TransactionID = existingTransaction.TransactionID,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        Narration = narration
                                    )
                                )
                                viewmodel.updateSingleTran(

                                    TranRequest(
                                        VchType = vchType,
                                        TransactionID = existingTransaction.TransactionID,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        Narration = narration
                                    ),
                                    onSuccess = {
                                        showPopup = true
                                    })
                            } else {
                                viewmodel.addSingleTran(
                                    TranRequest(
                                        VchType = vchType,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        Narration = narration
                                    ),
                                    onSuccess = {
                                        showPopup = true
                                    }
                                )
                            }


                        },
                        enabled = listOf(
                            selectedAccount,
                            selectedAccountGUID,
                            selectedSettlement,
                            selectedSettlementGUID,
                            amount,
                            selectedDate
                        ).all { it.isNotEmpty() },
                        label = if (isEdit) "Modify" else "Create",
                        backgroundColor = MaterialTheme.colorScheme.primary
                    )

                    val filteredLedgerList = if (name == "Contra") {
                        list.filter { it.L2 == 1.0 || it.L3 == 1.0 }
                    } else {
                        list.filter { it.L1 == 1.0 }
                    }

                    val filteredSettlementList = list.filter { it.L2 == 1.0 || it.L3 == 1.0 }

                    TransactionBottomSheet(
                        showBottomSheet = showBottomSheet,
                        list = filteredLedgerList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelected = {
                            it.let {
                                selectedAccount = it.first
                                selectedAccountGUID = it.second
                            }
                        },
                        onDismiss = { showBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(),
                        title = "Ledger Name",
                    )
                    TransactionBottomSheet(
                        showBottomSheet = showSettlementBottomSheet,
                        list = filteredSettlementList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelected = {
                            it.let {
                                selectedSettlement = it.first
                                selectedSettlementGUID = it.second
                            }
                        },
                        onDismiss = { showSettlementBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(),
                        title = "Settlement",
                    )
                }
                if (showPopup) {
                    TallyResultDialog(
                        message = (state.message + " ${state.data?.VoucherNumber ?: ""}"),
                        onDone = {
                            if (state.success) {
                                if (isEdit) {
                                    showPopup = false
                                    nav.pop()
                                } else {
                                    selectedAccount = ""
                                    selectedAccountGUID = ""
                                    selectedSettlement = ""
                                    selectedSettlementGUID = ""
                                    amount = ""
                                    narration = ""
                                    selectedDate = CurrentDate()
                                    showPopup = false
                                }

                            } else {
                                showPopup = false
                            }

                        },
                        isSuccess = state.success,
                        confirmText = if (state.success) "Done" else "Try Again"
                    )
                }
            },
        )

    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f, fill = false)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun SelectLedgerRow(
    selectedAccount: String,
    onShowBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = true
) {
    val borderColor =
        when {
            !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            selectedAccount.isNotEmpty() ->
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        }

    val bgColor =
        if (enabled) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)

    val textColor =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            selectedAccount.isEmpty() ->
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

            else -> MaterialTheme.colorScheme.onSurface
        }

    val iconColor =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            selectedAccount.isEmpty() ->
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

            else -> MaterialTheme.colorScheme.primary
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Select $title",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor),
            enabled = enabled,
            onClick = {
                if (enabled) onShowBottomSheet()
            }
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
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = selectedAccount.ifEmpty { "Select $title" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = if (selectedAccount.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (enabled)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
