package org.prime.easykarobar.ui.shared.reportsShared

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
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

    val selectedQuantities = remember(show, parameters) {
        mutableStateMapOf<String, Int>().apply {
            initialSelectedParameters.groupBy { it }.forEach { (k, v) -> put(k, v.size) }
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            isLoading = true
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
println(fetchedParameters)
            // Handle initial selected parameters that might not be in the fetched list
            // (e.g. if we are editing an existing item)
            // However, GetProductStockList has many fields, so creating a dummy might be hard.
            // For now, let's just use what's fetched.
            
            parameters = fetchedParameters
            isLoading = false
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

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (parameters.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No parameters found for this item.\nQuantity will be locked to 1.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    items(filteredList) { item ->
                        val paramId = item.BCN ?: "${item.C1 ?: ""}-${item.C2 ?: ""}-${item.C3 ?: ""}"
                        val isSelected = selectedQuantities.containsKey(paramId)
                        val qty = selectedQuantities[paramId] ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isSelected) {
                                        selectedQuantities.remove(paramId)
                                    } else {
                                        selectedQuantities[paramId] = 1
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedQuantities[paramId] = 1
                                    } else {
                                        selectedQuantities.remove(paramId)
                                    }
                                }
                            )

                            Column(modifier = Modifier.weight(1f)) {
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

                            if (isSelected) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable(enabled = false) {}
                                    ) {
                                        BasicTextField(
                                            value = if (qty == 0) "" else qty.toString(),
                                            onValueChange = { newValue ->
                                                if (newValue.isEmpty()) {
                                                    selectedQuantities[paramId] = 0
                                                } else {
                                                    val newQty = newValue.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                    selectedQuantities[paramId] = newQty
                                                }
                                            },
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val result = mutableListOf<GetProductStockList>()
                    parameters.forEach { item ->
                        val paramId = item.BCN ?: "${item.C1 ?: ""}-${item.C2 ?: ""}-${item.C3 ?: ""}"
                        val qty = selectedQuantities[paramId] ?: 0
                        repeat(qty) {
                            result.add(item)
                        }
                    }
                    onParametersSelected(result)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val totalItems = selectedQuantities.values.sum()
                Text("Done (${if (parameters.isEmpty()) 1 else totalItems})")
            }
        }
    }
}
