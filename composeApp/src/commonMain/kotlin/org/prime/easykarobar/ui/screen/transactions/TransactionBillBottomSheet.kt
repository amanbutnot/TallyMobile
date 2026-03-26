package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.reports.outstanding.DataList
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import smartSearch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBillBottomSheet(
    cm1: String,
    showBottomSheet: Boolean,
    ledgerGuid: String,
    initialSelectedBills: List<DataList> = emptyList(),
    onBillsSelected: (List<DataList>) -> Unit,
    totalAmount: Double,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState,
    title: String = "Select Bill References"
) {
    var query by remember { mutableStateOf("") }
    val db = DatabaseHolder.instance
    var billList by remember { mutableStateOf<List<DataList>>(emptyList()) }
    val selectedBills = remember { mutableStateListOf<DataList>() }

    // ---------------------------
    // 🔥 Allocation Logic
    // ---------------------------
    fun allocateAmounts(
        selected: List<DataList>,
        total: Double
    ): Map<String, Double> {

        var remaining = total

        val sorted = selected.sortedBy { it.date } // enforce order

        return sorted.associate { item ->
            val key = item.billId ?: item.hashCode().toString()
            val maxAllowed = item.adjustmentAmount?.absoluteValue ?: 0.0

            val allocated = when {
                remaining <= 0 -> 0.0
                remaining >= maxAllowed -> maxAllowed
                else -> remaining
            }

            remaining -= allocated
            key to allocated
        }
    }
    // recompute allocation whenever selection changes
    val allocations by remember(selectedBills, totalAmount) {
        derivedStateOf {
            allocateAmounts(selectedBills, totalAmount)
        }
    }

    val currentAllocatedTotal by remember {
        derivedStateOf { allocations.values.sum() }
    }

    // ---------------------------

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            selectedBills.clear()
            selectedBills.addAll(initialSelectedBills)
            bottomSheetState.expand()
        }
    }

    val perms = SharedPrefs.Permissions.get()
    val filterBroker = if (perms?.FilterBroker == "Y") 1L else 0L
    val configBroker =
        if (filterBroker == 1L) perms?.ConfigBroker.parseToStringList() else emptyList()

    LaunchedEffect(ledgerGuid) {
        if (ledgerGuid.isNotEmpty()) {
            billList = db.voucherBillAllocationsQueries.billByBillList(
                CM1 = cm1,
                filterCm3 = filterBroker,
                cm3 = configBroker
            ).executeAsList().map {
                DataList(
                    VCH_GUID = it.VCH_GUID,
                    date = it.date,
                    vchType = it.vchType,
                    billNumber = it.billNumber,
                    cm1 = it.cm1,
                    dueDate = it.dueDate,
                    d1 = it.d1,
                    adjustmentAmount = it.adjustmentAmount,
                    GroupName = it.GroupName,
                    billId = it.billid.toString()
                )
            }
        }
    }

    val filteredList = remember(billList, query) {
        smartSearch(
            list = billList,
            query = query,
            selectors = listOf { it.billNumber ?: "" }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0,0,0,0) }
        ) {
            Column(Modifier.fillMaxSize()) {

                // ---------------------------
                // HEADER
                // ---------------------------
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleLarge)

                        Text(
                            "Voucher Total: ${totalAmount.formatToAmtDec()}",
                            color = if (currentAllocatedTotal > totalAmount)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ---------------------------
                // LIST
                // ---------------------------
                LazyColumn(Modifier.weight(1f)) {

                    items(filteredList) { item ->

                        val isSelected = selectedBills.any { it.billId == item.billId }

                        val allocatedAmount = allocations[item.billId] ?: 0.0

                        val canSelect =
                            isSelected || currentAllocatedTotal < totalAmount

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable(enabled = canSelect || isSelected) {
                                    if (isSelected) {
                                        selectedBills.removeAll { it.billId == item.billId }
                                    } else {
                                        selectedBills.add(item)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {

                            Row(Modifier.padding(16.dp)) {

                                Column(Modifier.weight(1f)) {

                                    Text("Bill: ${item.billNumber}")

                                    Text("Amt: ${item.d1?.formatToAmtDec()}")

                                    Text(
                                        "Allocated: ${allocatedAmount.formatToAmtDec()}",
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        "Pending: ${item.adjustmentAmount?.formatToAmtDec()}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (it) selectedBills.add(item)
                                        else selectedBills.removeAll { it.billId == item.billId }
                                    }
                                )
                            }
                        }
                    }
                }

                // ---------------------------
                // FOOTER
                // ---------------------------
                Column(Modifier.padding(16.dp)) {

                    val pendingAmount = totalAmount - currentAllocatedTotal

                    Text("Selected: ${currentAllocatedTotal.formatToAmtDec()}")

                    Text(
                        "Pending: ${pendingAmount.formatToAmtDec()}",
                        color = if (pendingAmount < 0)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = {
                            // Map each selected bill with its allocated d1 value
                            val billsWithAllocations = selectedBills.map { bill ->
                                val key = bill.billId ?: bill.hashCode().toString()
                                bill.copy(d1 = allocations[key])
                            }
                            onBillsSelected(billsWithAllocations)
                            onDismiss()
                        },
                        enabled = selectedBills.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save (${selectedBills.size})")
                    }
                }
            }
        }
    }
}
