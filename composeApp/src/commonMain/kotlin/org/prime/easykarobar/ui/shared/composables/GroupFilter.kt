package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
        sheetState = bottomSheetState,
        //contentWindowInsets = { BottomSheetDefaults.windowInsets.union(WindowInsets.ime) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
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

            LazyColumn(modifier = Modifier.weight(1f)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GroupFilterBottomSheetWithGUID(
    show: Boolean,
    items: List<T>,

    // Only store IDs for selection
    selectedIds: List<String>,

    itemIdSelector: (T) -> String,
    itemNameSelector: (T) -> String?,

    onSelectedIdsChange: (List<String>) -> Unit,
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
        sheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Filter",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                    val allIds = items.map { itemIdSelector(it) }
                    onSelectedIdsChange(allIds)
                }) {
                    Text("Select All")
                }

                TextButton(onClick = {
                    onSelectedIdsChange(emptyList())
                }) {
                    Text("Clear")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->

                    val id = itemIdSelector(item)
                    val name = itemNameSelector(item)

                    val isSelected = selectedIds.contains(id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = selectedIds.toMutableList()
                                if (isSelected) {
                                    updated.remove(id)
                                } else {
                                    updated.add(id)
                                }
                                onSelectedIdsChange(updated)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val updated = selectedIds.toMutableList()
                                if (checked) {
                                    updated.add(id)
                                } else {
                                    updated.remove(id)
                                }
                                onSelectedIdsChange(updated)
                            }
                        )

                        Text(
                            text = name ?: "",
                            modifier = Modifier.padding(start = 8.dp)
                        )
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