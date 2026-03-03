package org.prime.easykarobar.ui.screen.masters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.enums.MasterEnums
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.agrpGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterAGRPGroups
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getItemMasters
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.tally.GodownMaster
import org.tally.LedgerGroupMaster
import org.tally.LedgerMaster
import org.tally.ProductGroupMaster
import org.tally.ProductUnitMaster
import org.tally.Products
import smartSearch

data class MasterListScreen(val masterEnum: MasterEnums) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val allItems = remember { mutableStateOf<List<Any>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        val showBottomSheet = remember { mutableStateOf(false) }
        val selectedItem = remember { mutableStateOf<Any?>(null) }
        val nav = LocalNavigator.currentOrThrow


println("FilterGroups in account groups are: ${filterItemGroups()}")
println("groupCodes in account groups are: ${itemGroupCodes().map { it.toInt().toString() }}")
        LaunchedEffect(Unit) {
            allItems.value = when (masterEnum) {

                MasterEnums.ACCOUNTS -> getLedgerMasters(db)
                MasterEnums.ACCOUNT_GROUP -> db.ledgerGroupMasterQueries.selectAll(
                    filterGroup = filterAGRPGroups(),
                    groupCodes = agrpGroupCodes()
                ).executeAsList()

                MasterEnums.ITEMS -> getItemMasters(db)
                MasterEnums.ITEM_GROUP -> db.productGroupMasterQueries.selectAll(
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes()
                ).executeAsList()

                MasterEnums.ITEM_UNIT -> db.productUnitMasterQueries.selectAll().executeAsList()
                MasterEnums.MATERIAL_CENTER -> db.godownMasterQueries.selectAll().executeAsList()
            }
        }
        println("list in account groups are: ${allItems.value}")

        TallyScaffold(
            title = masterEnum.name.replace("_", " ").lowercase().split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } },
           showAddBar = masterEnum == MasterEnums.ACCOUNTS || masterEnum == MasterEnums.ITEMS,
        //    showAddBar = false,
            onAddClick = {
                if (masterEnum == MasterEnums.ACCOUNTS) {
                    nav.push(AccountAddScreen)
                } else {
                    nav.push(ItemAddScreen)
                }
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true
                    )

                    val filteredItems = remember(allItems.value, searchQuery) {
                        smartSearch(
                            list = allItems.value,
                            query = searchQuery,
                            selectors = listOf { item ->
                                getMasterItemData(item).first
                            }
                        )
                    }



                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        itemsIndexed(filteredItems) { index, item ->
                            val (name, parent) = getMasterItemData(item)
                            MasterListItem(index = index + 1, name = name, parentName = parent) {
                                if (item is LedgerMaster || item is Products) {
                                    selectedItem.value = item
                                    showBottomSheet.value = true
                                }
                            }
                        }
                    }

                }
                if (showBottomSheet.value) {
                    val bottomState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    LaunchedEffect(Unit) {
                        bottomState.expand()
                    }

                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet.value = false },
                        sheetState = bottomState,
                        sheetGesturesEnabled = false,
                        dragHandle = {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    showBottomSheet.value = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Icon")
                                }
                            }
                        }
                    ) {
                        selectedItem.value?.let {
                            BottomSheetContent(
                                it
                            )

                        }
                    }
                }
            }
        )
    }

    @Composable
    private fun MasterListItem(
        index: Int,
        name: String?,
        parentName: String? = null,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle with index
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    name?.let {
                        Text(
                            text = it,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    parentName?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }


}

private fun getMasterItemData(item: Any): Pair<String?, String?> {
    return when (item) {
        is LedgerMaster -> item.Name to item.GroupName
        is LedgerGroupMaster -> item.Name to item.GroupName
        is Products -> item.Name to item.GroupName
        is ProductGroupMaster -> item.Name to item.GroupName
        is ProductUnitMaster -> item.Name to null
        is GodownMaster -> item.Name to null
        else -> null to null
    }
}


@Composable
private fun BottomSheetContent(item: Any) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth().wrapContentHeight()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            when (item) {
                is LedgerMaster -> {
                    Text(
                        text = "Ledger Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }

                is Products -> {
                    Text(
                        text = "Product Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }

        when (item) {
            is LedgerMaster -> {
                item {
                    SectionHeader("Basic Information")
                }

                item.Name?.let {
                    item { SheetRow(label = "Ledger Name", value = it, isHighlighted = true) }
                }
                item.Alias?.let {
                    item { SheetRow(label = "Alias", value = it) }
                }
                item.GroupName?.let {
                    item { SheetRow(label = "Group Name", value = it) }
                }

                if (item.Address1 != null || item.MobileNo != null || item.Email != null) {
                    item {
                        SectionHeader("Contact Information")
                    }
                    item.Address1?.let {
                        item { SheetRow(label = "Address", value = it) }
                    }
                    item.MobileNo?.let {
                        item { SheetRow(label = "Mobile", value = it) }
                    }
                    item.Email?.let {
                        item { SheetRow(label = "Email", value = it) }
                    }
                }

                if (item.PanNo != null || item.GSTIN != null) {
                    item {
                        SectionHeader("Tax Information")
                    }
                    item.PanNo?.let {
                        item { SheetRow(label = "PAN", value = it) }
                    }
                    item.GSTIN?.let {
                        item { SheetRow(label = "GSTIN", value = it) }
                    }
                }
            }

            is Products -> {
                item {
                    SectionHeader("Basic Information")
                }

                item.Name?.let {
                    item { SheetRow(label = "Item Name", value = it, isHighlighted = true) }
                }
                item.Alias?.let {
                    item { SheetRow(label = "Alias", value = it) }
                }
                item.GroupName?.let {
                    item { SheetRow(label = "Category", value = it) }
                }
                item.UnitName?.let {
                    item { SheetRow(label = "Unit", value = it) }
                }

                item {
                    SectionHeader("Pricing Information")
                }
                item.PurcPrice?.let {
                    item { SheetRow(label = "Purchase Price", value = it.formatToAmtDec()) }
                }
                item.SalesPrice?.let {
                    item { SheetRow(label = "Sales Price", value = it.formatToAmtDec()) }
                }
                item.SelfValPrice?.let {
                    item { SheetRow(label = "Self Value Price", value = it.formatToAmtDec()) }
                }
                item.MinSalesPrice?.let {
                    item { SheetRow(label = "Min Sales Price", value = it.formatToAmtDec()) }
                }

            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun SheetRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = value.trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(2f)
            )
        }
    }
}