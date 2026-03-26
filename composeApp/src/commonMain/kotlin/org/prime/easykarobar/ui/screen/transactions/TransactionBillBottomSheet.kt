package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.reports.outstanding.DataList
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.Tdate
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

    // Sync internal selection with initialSelectedBills and expand to full page
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            selectedBills.clear()
            selectedBills.addAll(initialSelectedBills)
            bottomSheetState.expand()
        }
    }

    val currentSelectedTotal by remember {
        derivedStateOf {
            selectedBills.sumOf { it.adjustmentAmount?.absoluteValue ?: 0.0 }
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
                    GroupName = it.GroupName, billId = it.billid.toString()
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
            sheetGesturesEnabled = true,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            contentWindowInsets = { WindowInsets(0,0,0,0) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Voucher Total: ${totalAmount.formatToAmtDec()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentSelectedTotal > totalAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { onDismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    Modifier.padding(bottom = 12.dp),
                    DividerDefaults.Thickness,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Search
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TallySearchBar(
                        searchQuery = query,
                        onQueryChange = { query = it },
                    )
                }

                // List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (query.isNotBlank()) {
                        item {
                            Text(
                                text = "${filteredList.size} result${if (filteredList.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = if (query.isBlank()) "No bills found" else "No results found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(filteredList) { item ->
                        val isSelected = selectedBills.any { it.billId == item.billId }
                        val canSelect = isSelected || (currentSelectedTotal < totalAmount)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = canSelect || isSelected) {
                                    if (isSelected) {
                                        selectedBills.removeAll { it.billId == item.billId }
                                    } else {
                                        if (canSelect) {
                                            selectedBills.add(item)
                                        }
                                    }
                                },
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else if (!canSelect)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bill No: ${item.billNumber}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (canSelect || isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = "Date: ${Tdate(item.date ?: "")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(
                                            text = "Amt: ${item.d1?.absoluteValue?.formatToAmtDec()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (canSelect || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = "Pending: ${item.adjustmentAmount?.absoluteValue?.formatToAmtDec()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (canSelect || isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = isSelected,
                                    enabled = canSelect || isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (canSelect) {
                                                selectedBills.add(item)
                                            }
                                        } else {
                                            selectedBills.removeAll { it.VCH_GUID == item.VCH_GUID }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Footer with Action Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = totalAmount.formatToAmtDec(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentSelectedTotal.formatToAmtDec(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val pendingAmount = totalAmount - currentSelectedTotal
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount Pending",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if(pendingAmount<totalAmount) totalAmount.toString() else pendingAmount.formatToAmtDec(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingAmount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = {
                                onBillsSelected(selectedBills.toList())
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedBills.isNotEmpty()
                        ) {
                            Text("Save (${selectedBills.size} Selected)")
                        }
                    }
                }
            }
        }
    }
}
