package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.composables.smartSearch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBillBottomSheet(
    cm1: String,
    showBottomSheet: Boolean,
    ledgerGuid: String,
    isEdit: Boolean,
    vchType: Int,
    uniqueId: String? = null,
    initialSelectedBills: List<BillByBillModel> = emptyList(),
    onBillsSelected: (List<BillByBillModel>) -> Unit,
    totalAmount: Double,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState,
    title: String = "Select Bill References"
) {
    var query by remember { mutableStateOf("") }
    val db = DatabaseHolder.instance

    var billList by remember { mutableStateOf<List<BillByBillModel>>(emptyList()) }
    val selectedBills = remember { mutableStateListOf<BillByBillModel>() }

    // ---------------------------
    // Allocation Logic
    // ---------------------------
    fun allocateAmounts(
        selected: List<BillByBillModel>,
        total: Double
    ): Map<String, Double> {

        var remaining = total

        return selected.associate { item ->
            val key = item.billId.toString()
            val maxAllowed = item.d1?.absoluteValue ?: 0.0

            val allocated = when {
                remaining <= 0 -> 0.0
                remaining >= maxAllowed -> maxAllowed
                else -> remaining
            }

            remaining -= allocated
            key to allocated
        }
    }

    val allocations by remember(selectedBills, totalAmount) {
        derivedStateOf {
            allocateAmounts(selectedBills, totalAmount)
        }
    }

    val currentAllocatedTotal = allocations.values.sum()

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
        if (vchType in listOf(9, 10, 19)) {
            if (ledgerGuid.isNotEmpty()) {
                billList = db.voucherBillAllocationsQueries.billByPayList(
                    CM1 = cm1,
                    filterCm3 = filterBroker,
                    cm3 = configBroker,
                    filterGuid = if (isEdit) 1L else 0L,
                    uniqueID = uniqueId.toString()
                ).executeAsList().mapIndexed { index, list ->
                    BillByBillModel(
                        date = list.date,
                        vchType = list.vchType,
                        billNumber = list.billNumber,
                        cm1 = list.cm1,
                        dueDate = list.dueDate,
                        d1 = list.adjustmentAmount,
                        billId = list.billid.toString(),
                        SrNo = (index + 1).toString(),
                        cm2 = "Agst Ref"
                    )
                }
            }
        } else {

            if (ledgerGuid.isNotEmpty()) {
                billList = db.voucherBillAllocationsQueries.billByBillList(
                    CM1 = cm1,
                    filterCm3 = filterBroker,
                    cm3 = configBroker,
                    filterGuid = if (isEdit) 1L else 0L,
                    uniqueID = uniqueId.toString()
                ).executeAsList().mapIndexed { index, list ->
                    BillByBillModel(
                        date = list.date,
                        vchType = list.vchType,
                        billNumber = list.billNumber,
                        cm1 = list.cm1,
                        dueDate = list.dueDate,
                        d1 = list.adjustmentAmount,
                        billId = list.billid.toString(),
                        SrNo = (index + 1).toString(),
                        cm2 = "Agst Ref"
                    )
                }
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
            sheetState = bottomSheetState, modifier = Modifier.navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
        ) {
            Column(Modifier.fillMaxSize()) {

                // HEADER
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

                // LIST
                LazyColumn(Modifier.weight(1f)) {
                    items(filteredList) { item ->

                        val isSelected = selectedBills.any {
                            it.billId == item.billId
                        }
                        val allocatedAmount = allocations[item.billId] ?: 0.0

                        val pending =
                            (item.d1?.absoluteValue ?: 0.0) - allocatedAmount.absoluteValue

                        val remaining = totalAmount - currentAllocatedTotal
                        val canSelect = isSelected || remaining > 0
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable(enabled = canSelect) {
                                    if (isSelected) {
                                        selectedBills.removeAll { it.billId == item.billId }
                                    } else {
                                        selectedBills.add(item)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {

                            Row(Modifier.padding(16.dp)) {

                                Column(Modifier.weight(1f)) {

                                    Text("Bill: ${item.billNumber}")
                                    //add adjusted amount
                                    Text("Amt: ${item.d1?.absoluteValue?.formatToAmtDec()}")

                                    Text(
                                        "Allocated: ${allocatedAmount.formatToAmtDec()}",
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        "Pending: ${pending.formatToAmtDec()}",
                                        color = if (pending > 0)
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
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

                // FOOTER
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
                            val result = mutableListOf<BillByBillModel>()
                            var remainingTotal = totalAmount

                            // ⚠️ IMPORTANT: enforce order (oldest first or SrNo)
                            val orderedBills = selectedBills.sortedBy { it.SrNo?.toIntOrNull() ?: 0 }

                            orderedBills.forEach { bill ->
                                if (remainingTotal <= 0) return@forEach

                                val billAmount = bill.d1?.absoluteValue ?: 0.0

                                when {
                                    // ✅ Case 1: total <= bill → allocate remaining total
                                    remainingTotal <= billAmount -> {
                                        result.add(
                                            bill.copy(
                                                d1 = remainingTotal,
                                                SrNo = (result.size + 1).toString(),
                                                cm2 = "Agst Ref"
                                            )
                                        )
                                        remainingTotal = 0.0
                                    }

                                    // ✅ Case 2: total > bill → close bill fully
                                    remainingTotal > billAmount -> {
                                        result.add(
                                            bill.copy(
                                                d1 = billAmount,
                                                SrNo = (result.size + 1).toString(),
                                                cm2 = "Agst Ref"
                                            )
                                        )
                                        remainingTotal -= billAmount
                                    }
                                }
                            }
                            if (remainingTotal > 0) {
                                result.add(
                                    BillByBillModel(
                                        SrNo = (result.size + 1).toString(),
                                        date = null,
                                        vchType = null,
                                        billNumber = "",
                                        billId = null,
                                        cm1 = cm1,
                                        cm2 = "New Ref",
                                        dueDate = null,
                                        d1 = remainingTotal
                                    )
                                )
                            }

                            println("Bills selected $result")
                            onBillsSelected(result)
                            onDismiss()
                        },
                        enabled = selectedBills.isNotEmpty() || totalAmount > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save (${selectedBills.size})")
                    }
                }
            }
        }
    }
}

@Serializable
data class BillByBillModel(
    val SrNo: String?,
    val date: String?,
    val vchType: String?,
    val billNumber: String?,
    val uniqueID: String? = null,
    val billId: String?,
    val cm1: String?,
    val cm2: String?,
    val d1: Double?,
    val dueDate: String?,
)