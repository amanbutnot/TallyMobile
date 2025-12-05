package org.prime.tally.ui.screen.transactions.sale

import CurrentDate
import TallyDatePickerRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.serialization.Serializable
import org.prime.tally.business.viewmodel.transactions.SingleEntryViewModel
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.screen.transactions.SelectLedgerRow
import org.prime.tally.ui.screen.transactions.TallyNarrationField
import org.prime.tally.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyScaffold

@Serializable
data class InvoiceItem(
    val name: String,
    val price: Double,
    val qty: Int = 1,
    val priceType: Int = 0
) {
    val total: Double get() = price * qty
}

@Serializable
data class SundryItem(
    val name: String,
    val amount: Double = 0.0
)

enum class TaxType {
    INCLUSIVE,
    EXTRA
}

data class SaleScreen(val name: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow

        var selectedLedger by rememberSaveable { mutableStateOf("") }
        var selectedLedgerGUID by rememberSaveable { mutableStateOf("") }
        var narration by rememberSaveable { mutableStateOf("") }
        var selectedDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var taxType by rememberSaveable { mutableStateOf(TaxType.INCLUSIVE) }

        var selectedItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var selectedSundries by remember { mutableStateOf<List<SundryItem>>(emptyList()) }

        var showLedgerSheet by rememberSaveable { mutableStateOf(false) }
        var showItemSheet by rememberSaveable { mutableStateOf(false) }
        var showSundrySheet by rememberSaveable { mutableStateOf(false) }

        val ledgerList = db.ledgerMasterQueries.selectAll().executeAsList()
        val itemsList = db.productsQueries.selectAll().executeAsList()

        val viewmodel: SingleEntryViewModel = viewModel { SingleEntryViewModel() }
        val state by viewmodel.dataState

        val isEdit = false

        val itemsTotal = selectedItems.sumOf { it.total }
        val sundriesTotal = selectedSundries.sumOf { it.amount }
        val grandTotal = itemsTotal + sundriesTotal

        if (state.isLoading) {
            TallyLoadingDialog(if (isEdit) "Editing transaction" else "Creating transaction")
        }

        TallyScaffold(
            title = if (isEdit) "Edit $name" else "$name Invoice",
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Header Card with Date and Party
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TallyDatePickerRow(
                                    label = "Entry Date",
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    defaultDate = CurrentDate()
                                )

                                SelectLedgerRow(
                                    selectedAccount = selectedLedger,
                                    onShowBottomSheet = { showLedgerSheet = true },
                                    title = "Party Ledger"
                                )
                            }
                        }

                        // Tax Type Section
                        TaxTypeSelector(
                            selectedTaxType = taxType,
                            onTaxTypeSelected = { taxType = it }
                        )

                        // Items Section
                        SectionCard(title = "ITEMS", count = selectedItems.size) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AddButton(label = "Add Item") { showItemSheet = true }

                                selectedItems.forEach { item ->
                                    ItemCard(
                                        item = item,
                                        onQuantityChange = { newQty ->
                                            selectedItems = selectedItems.map {
                                                if (it.name == item.name) it.copy(qty = newQty) else it
                                            }
                                        },
                                        onRemove = { selectedItems = selectedItems - item }
                                    )
                                }

                                if (selectedItems.isNotEmpty()) {
                                    SubtotalRow("Subtotal", itemsTotal)
                                }
                            }
                        }

                        // Sundries Section
                        SectionCard(title = "SUNDRIES", count = selectedSundries.size) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AddButton(label = "Add Sundry") { showSundrySheet = true }

                                selectedSundries.forEach { sundry ->
                                    SundryCard(
                                        sundry = sundry,
                                        onAmountChange = { newAmount ->
                                            selectedSundries = selectedSundries.map {
                                                if (it.name == sundry.name) it.copy(amount = newAmount) else it
                                            }
                                        },
                                        onRemove = { selectedSundries = selectedSundries - sundry }
                                    )
                                }

                                if (selectedSundries.isNotEmpty()) {
                                    SubtotalRow("Subtotal", sundriesTotal)
                                }
                            }
                        }

                        // Narration Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                TallyNarrationField(
                                    value = narration,
                                    onValueChange = { narration = it },
                                    label = "Narration"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(60.dp))
                    }

                    // Bottom Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 12.dp,
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL AMOUNT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${grandTotal}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                TallyButton(
                                    onClick = { /* Transaction logic */ },
                                    enabled = selectedLedger.isNotEmpty() && selectedItems.isNotEmpty(),
                                    label = if (isEdit) "Update" else "Create Invoice",
                                    backgroundColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Bottom Sheets
                SelectionSheet(
                    show = showLedgerSheet,
                    title = "Select Party Ledger",
                    options = ledgerList.map { it.Name ?: "" },
                    onSelect = { selected ->
                        selectedLedger = selected
                        selectedLedgerGUID = ledgerList.find { l -> l.Name == selected }?.GUID ?: ""
                    },
                    onDismiss = { showLedgerSheet = false }
                )

                SelectionSheet(
                    show = showItemSheet,
                    title = "Select Item",
                    options = itemsList.map { it.Name ?: "" },
                    onSelect = { itemName ->
                        itemsList.find { it.Name == itemName }?.let {
                            val newItem = InvoiceItem(
                                name = it.Name ?: "",
                                price = it.SalesPrice ?: 0.0,
                                qty = 1,
                                priceType = 0
                            )
                            if (!selectedItems.any { item -> item.name == newItem.name }) {
                                selectedItems = selectedItems + newItem
                            }
                        }
                    },
                    onDismiss = { showItemSheet = false }
                )

                SelectionSheet(
                    show = showSundrySheet,
                    title = "Select Sundry Ledger",
                    options = ledgerList.map { it.Name ?: "" },
                    onSelect = { sundryName ->
                        if (!selectedSundries.any { it.name == sundryName }) {
                            selectedSundries = selectedSundries + SundryItem(sundryName, 0.0)
                        }
                    },
                    onDismiss = { showSundrySheet = false }
                )
            }
        )
    }
}

@Composable
fun TaxTypeSelector(
    selectedTaxType: TaxType,
    onTaxTypeSelected: (TaxType) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TAX TYPE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaxTypeOption(
                    label = "Tax Inclusive",
                    selected = selectedTaxType == TaxType.INCLUSIVE,
                    onClick = { onTaxTypeSelected(TaxType.INCLUSIVE) },
                    modifier = Modifier.weight(1f)
                )
                TaxTypeOption(
                    label = "Tax Extra",
                    selected = selectedTaxType == TaxType.EXTRA,
                    onClick = { onTaxTypeSelected(TaxType.EXTRA) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TaxTypeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    count: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.6.sp
                )
                if (count > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$count",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AddButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        shape = MaterialTheme.shapes.small
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(
    show: Boolean,
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionOneBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(),
        title = title
    )
}

@Composable
fun QuantitySelector(
    qty: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$qty",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ItemCard(
    item: InvoiceItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    LaunchedEffect(item.qty) {
        if (item.qty == 0) {
            onRemove()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₹ ${item.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuantitySelector(
                    qty = item.qty,
                    onDecrease = { onQuantityChange(item.qty - 1) },
                    onIncrease = { onQuantityChange(item.qty + 1) }
                )

                Text(
                    text = "₹ ${item.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.widthIn(min = 70.dp),
                    textAlign = TextAlign.End
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SundryCard(
    sundry: SundryItem,
    onAmountChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sundry.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.width(110.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        BasicTextField(
                            value = if (sundry.amount == 0.0) "" else sundry.amount.toString(),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    onAmountChange(0.0)
                                } else {
                                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                                    val dotCount = filtered.count { it == '.' }
                                    val validInput = if (dotCount > 1) {
                                        filtered.substringBefore('.') + "." +
                                                filtered.substringAfter('.').replace(".", "")
                                    } else {
                                        filtered
                                    }
                                    onAmountChange(validInput.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            decorationBox = { innerTextField ->
                                if (sundry.amount == 0.0) {
                                    Text(
                                        text = "0.00",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            textAlign = TextAlign.End
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SubtotalRow(label: String, amount: Double) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${amount}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}