package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import smartSearch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GroupFilterBottomSheet(
    show: Boolean,
    items: List<T>,
    selectedItems: List<String>,
    itemNameSelector: (T) -> String?,
    onSelectedItemsChange: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState
) {
    if (!show) return

    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        smartSearch(
            list = items,
            query = searchQuery,
            selectors = listOf(itemNameSelector)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Filter",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Search Bar
            TallySearchBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    onSelectedItemsChange(items.mapNotNull(itemNameSelector))
                }) {
                    Text("Select All")
                }
                TextButton(onClick = { onSelectedItemsChange(emptyList()) }) {
                    Text("Clear")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(filteredItems) { item ->
                    val name = itemNameSelector(item)
                    val isSelected = name in selectedItems
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val currentSelection = selectedItems.toMutableList()
                                name?.let {
                                    if (currentSelection.contains(it)) {
                                        currentSelection.remove(it)
                                    } else {
                                        currentSelection.add(it)
                                    }
                                }
                                onSelectedItemsChange(currentSelection)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { isChecked ->
                                val currentSelection = selectedItems.toMutableList()
                                name?.let {
                                    if (isChecked) {
                                        currentSelection.add(it)
                                    } else {
                                        currentSelection.remove(it)
                                    }
                                }
                                onSelectedItemsChange(currentSelection)
                            }
                        )
                        Text(name ?: "", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Apply")
            }
        }
    }
}
