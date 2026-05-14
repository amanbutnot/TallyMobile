package org.prime.easykarobar.ui.shared.reportsShared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.tally.SerialNoEnterReportSale
import org.prime.easykarobar.ui.shared.composables.smartSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerialNumberBottomSheet(
    productGuid: String,
    show: Boolean,
    onDismiss: () -> Unit,
    onSerialNumbersSelected: (List<SerialNoEnterReportSale>) -> Unit,
    initialSelectedSerialNumbers: List<String> = emptyList(),
    title: String = "Select Serial Numbers",
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val db = DatabaseHolder.instance
    var serialNumbers by remember {
        mutableStateOf<List<SerialNoEnterReportSale>>(
            initialSelectedSerialNumbers.map { serialNo ->
                SerialNoEnterReportSale(
                    SerialNo = serialNo,
                    MasterCode1 = productGuid.toDoubleOrNull(),
                    ProductName = "",
                    UnitName = null,
                    GroupName = null,
                    Value1 = 1.0,
                    Value2 = 0.0,
                    Value3 = 0.0, MasterCode2 = ""
                )
            }
        )
    }
    println("List of serial number is : $serialNumbers")
    val selectedSerialNumbers = remember(show) { mutableStateListOf<String>().apply { addAll(initialSelectedSerialNumbers) } }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(show) {
        if (show) {
            println("All selected serial numbers are " + initialSelectedSerialNumbers)
            // We don't clear selectedSerialNumbers here if we already initialized it, 
            // but for safety with re-opens:
            selectedSerialNumbers.clear()
            selectedSerialNumbers.addAll(initialSelectedSerialNumbers)

            val perms = SharedPrefs.Permissions.get()
            val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
            val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
            val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

            val excludeGuids = if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
            val godownCodes = if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()

            val fetchedSerials = db.productSerialNoQueries.serialNoEnterReportSale(
                filterGroup = filterGroup,
                groupCodes = filterItemGroupCodes(),
                filterExclude = filterExclude,
                excludeGuids = excludeGuids,
                filterGodown = filterGodown,
                godownCodes = godownCodes,
                filterSingle = 1L,
                includeSingle = productGuid.toDoubleOrNull() ?: 0.0,
                filterSingleG = 0L,
                includeSingleG = ""
            ).executeAsList()

            println("Fetched serials size: ${fetchedSerials.size} for productGuid: $productGuid")

            // Ensure initial selected serials are in the list even if query doesn't return them (e.g. if already sold)
            val missingSerials = initialSelectedSerialNumbers.filter { initial ->
                fetchedSerials.none { it.SerialNo == initial }
            }.map { serialNo ->
                SerialNoEnterReportSale(
                    SerialNo = serialNo,
                    MasterCode1 = productGuid.toDoubleOrNull(),
                    ProductName = "",
                    UnitName = null,
                    GroupName = null,
                    Value1 = 1.0,
                    Value2 = 0.0,
                    Value3 = 0.0, MasterCode2 = ""
                )
            }

            serialNumbers = fetchedSerials + missingSerials
        }
    }

    val filteredList = remember(serialNumbers, searchQuery) {
        smartSearch(
            list = serialNumbers,
            query = searchQuery,
            selectors = listOf { it.SerialNo ?: "" }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TallySearchBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                items(filteredList) { item ->
                    val serialNo = item.SerialNo ?: ""
                    val isSelected = selectedSerialNumbers.contains(serialNo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedSerialNumbers.remove(serialNo)
                                } else {
                                    selectedSerialNumbers.add(serialNo)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked == true) {
                                    selectedSerialNumbers.add(serialNo)
                                } else {
                                    selectedSerialNumbers.remove(serialNo)
                                }
                            }
                        )
                        Text(
                            text = serialNo,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val result = serialNumbers.filter { it.SerialNo in selectedSerialNumbers }
                    onSerialNumbersSelected(result)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            ) {
                Text("Done (${selectedSerialNumbers.size})")
            }
        }
    }
}
