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
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.tally.GetProductStockList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSelectionBottomSheet(
    productGuid: String,
    show: Boolean,
    onDismiss: () -> Unit,
    onParametersSelected: (List<GetProductStockList>) -> Unit,
    initialSelectedParameters: List<String> = emptyList(),
    title: String = "Select Parameters",
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val db = DatabaseHolder.instance
    var parameters by remember {
        mutableStateOf<List<GetProductStockList>>(emptyList())
    }
    
    val selectedParameters = remember(show) { 
        mutableStateListOf<String>().apply { addAll(initialSelectedParameters) } 
    }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(show) {
        if (show) {
            val perms = SharedPrefs.Permissions.get()
            val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
            val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
            val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

            val excludeGuids = if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
            val godownCodes = if (filterGodown == 1L) {
                perms?.ConfigGodown.parseToStringList().mapNotNull { it.toDoubleOrNull() }
            } else emptyList<Double>()

            // Fetch parameters from barcode report data (ProductParamStock)
            val fetchedParameters = db.productParamStockQueries.getProductStockList(
                productGuid = productGuid,
                filterParam1 = 0L, // Adjust as needed
                configParam1 = emptyList(), // Adjust as needed
                filterGroup = filterGroup,
                groupCodes = filterItemGroupCodes(),
                filterExclude = filterExclude,
                excludeGuids = excludeGuids,
                filterGodown = filterGodown,
                godownCodes = godownCodes
            ).executeAsList()

            // Handle initial selected parameters that might not be in the fetched list
            // (e.g. if we are editing an existing item)
            // However, GetProductStockList has many fields, so creating a dummy might be hard.
            // For now, let's just use what's fetched.
            
            parameters = fetchedParameters
        }
    }

    val filteredList = remember(parameters, searchQuery) {
        smartSearch(
            list = parameters,
            query = searchQuery,
            selectors = listOf { 
                listOfNotNull(it.BCN, it.C1, it.C2, it.C3, it.C4, it.C5).joinToString(" ")
            }
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
                    val paramId = item.BCN ?: "${item.C1 ?: ""}-${item.C2 ?: ""}-${item.C3 ?: ""}"
                    val isSelected = selectedParameters.contains(paramId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedParameters.remove(paramId)
                                } else {
                                    selectedParameters.add(paramId)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked == true) {
                                    selectedParameters.add(paramId)
                                } else {
                                    selectedParameters.remove(paramId)
                                }
                            }
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = item.BCN ?: "No Barcode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val details = listOfNotNull(item.C1, item.C2, item.C3, item.C4, item.C5).joinToString(" | ")
                            if (details.isNotBlank()) {
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val result = parameters.filter { 
                        (it.BCN ?: "${it.C1 ?: ""}-${it.C2 ?: ""}-${it.C3 ?: ""}") in selectedParameters 
                    }
                    onParametersSelected(result)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done (${selectedParameters.size})")
            }
        }
    }
}
