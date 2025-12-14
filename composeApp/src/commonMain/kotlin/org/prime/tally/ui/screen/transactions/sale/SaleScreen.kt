package org.prime.tally.ui.screen.transactions.sale

import CurrentDate
import TallyDatePickerRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
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
import kotlin.math.round
import kotlin.math.abs

// Helper: format a Double to two decimal places without using String.format (KMP safe).
fun formatTwo(value: Double): String {
    // round to nearest cent
    val cents = round(value * 100).toLong()
    val whole = cents / 100
    val frac = abs((cents % 100).toInt())
    return "$whole.${if (frac < 10) "0$frac" else "$frac"}"
}

@Serializable
data class InvoiceItem(
    var name: String,
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

        // When an item is chosen from the selection sheet, we store it here for editing
        var editingItem by remember { mutableStateOf<InvoiceItem?>(null) }

        // pendingSelectedProductName drives creation of editingItem when user selects an item from the sheet
        var pendingSelectedProductName by rememberSaveable { mutableStateOf<String?>(null) }

        val ledgerList = db.ledgerMasterQueries.selectAll().executeAsList()
        val itemsList = db.productsQueries.selectAll().executeAsList()

        val viewmodel: SingleEntryViewModel = viewModel { SingleEntryViewModel() }
        val state by viewmodel.dataState

        val isEdit = false

        // Use a consistent GST percent (matches ExpandedItemEditor usage)
        val gstPercent = 18.0

        // compute items total based on current tax type (so switching tax type updates totals)
        val itemsTotal by derivedStateOf {
            selectedItems.sumOf { item ->
                if (taxType == TaxType.EXTRA) {
                    val taxable = item.price * item.qty
                    taxable + taxable * gstPercent / 100.0
                } else {
                    // INCLUSIVE: stored item.price is gross unit, total is gross*qty
                    item.price * item.qty
                }
            }
        }

        val sundriesTotal = selectedSundries.sumOf { it.amount }
        val grandTotal = itemsTotal + sundriesTotal

        if (state.isLoading) {
            TallyLoadingDialog(if (isEdit) "Editing transaction" else "Creating transaction")
        }

        // When user selects from the item selection sheet, create an editingItem so ExpandedItemEditor shows.
        LaunchedEffect(pendingSelectedProductName) {
            pendingSelectedProductName?.let { name ->
                val prod = itemsList.find { it.Name == name }
                val price = prod?.SalesPrice ?: 0.0
                // create editing item with selected product and default qty 1
                editingItem = InvoiceItem(name = name, price = price, qty = 1)
                // close the sheet
                showItemSheet = false
                // clear pending
                pendingSelectedProductName = null
            }
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
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                        SectionCard(
                            title = "ITEMS",
                            count = selectedItems.size,
                            headerAction = {
                                SmallAddButton(label = "Add Item") { showItemSheet = true }
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // When user selected an item from the sheet we show an expanded editor
                                val pending = editingItem
                                if (pending != null) {
                                    val product = itemsList.find { it.Name == pending.name }
                                    if (product != null) {
                                        ExpandedItemEditor(
                                            name = product.Name ?: pending.name,
                                            defaultListPrice = product.SalesPrice ?: 0.0,
                                            initialQuantity = pending.qty, // now supported
                                            taxType = taxType,
                                            onAdd = { qty, unitPrice ->
                                                // Add the edited item back to the selected list with chosen qty and unit price.
                                                selectedItems = selectedItems + InvoiceItem(
                                                    name = product.Name ?: pending.name,
                                                    price = unitPrice,
                                                    qty = qty
                                                )
                                                editingItem = null
                                            },
                                            onCancel = {
                                                // Re-add the item back if cancelled
                                                selectedItems = selectedItems + pending
                                                editingItem = null
                                            },
                                            gstPercentage = gstPercent
                                        )
                                    } else {
                                        // If product metadata not found, still show editor using pending values
                                        ExpandedItemEditor(
                                            name = pending.name,
                                            defaultListPrice = pending.price,
                                            initialQuantity = pending.qty,
                                            taxType = taxType,
                                            onAdd = { qty, unitPrice ->
                                                selectedItems = selectedItems + InvoiceItem(
                                                    name = pending.name,
                                                    price = unitPrice,
                                                    qty = qty
                                                )
                                                editingItem = null
                                            },
                                            onCancel = {
                                                selectedItems = selectedItems + pending
                                                editingItem = null
                                            },
                                            gstPercentage = gstPercent
                                        )
                                    }
                                }

                                // Compact list of selected items
                                selectedItems.forEach { item ->
                                    CompactItemCard(
                                        item = item,
                                        gstPercentage = gstPercent,
                                        taxType = taxType,
                                        onQuantityChange = { newQty ->
                                            selectedItems = selectedItems.map {
                                                if (it.name == item.name) it.copy(qty = newQty) else it
                                            }
                                        },
                                        onRemove = { selectedItems = selectedItems - item },
                                        onEdit = {
                                            // Remove item from the list and set it as pending for editing
                                            selectedItems = selectedItems - item
                                            // put the exact item into editingItem so ExpandedItemEditor uses its qty
                                            editingItem = item
                                        }
                                    )
                                }

                                if (selectedItems.isNotEmpty()) {
                                    SubtotalRow("Subtotal", itemsTotal)
                                }
                            }
                        }

                        // Sundries Section
                        SectionCard(
                            title = "SUNDRIES",
                            count = selectedSundries.size,
                            headerAction = {
                                SmallAddButton(label = "Add Sundry") { showSundrySheet = true }
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        text = "TOTAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatTwo(grandTotal),
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

                // For items, we DO NOT add immediately. Instead set pendingSelectedProductName so the editor expands.
                SelectionSheet(
                    show = showItemSheet,
                    title = "Select Item",
                    options = itemsList.map { it.Name ?: "" },
                    onSelect = { itemName ->
                        // set pending so the expanded editor will appear
                        pendingSelectedProductName = itemName
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
fun SmallAddButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline
        )
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
fun SectionCard(
    title: String,
    count: Int,
    headerAction: (@Composable () -> Unit)? = null,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.6.sp
                    )
                    if (count > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
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

                if (headerAction != null) {
                    headerAction()
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CompactItemCard(
    item: InvoiceItem,
    gstPercentage: Double,
    taxType: TaxType,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit // Add this callback for editing
) {
    // compute displayed total according to taxType and gstPercentage
    val displayedTotal = remember(item, gstPercentage, taxType) {
        if (taxType == TaxType.EXTRA) {
            val taxable = item.price * item.qty
            taxable + taxable * gstPercentage / 100.0
        } else {
            // INCLUSIVE: item.price is gross unit
            item.price * item.qty
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit), // Make the entire card clickable
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹ ${formatTwo(item.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "x ${item.qty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuantitySelector(
                    qty = item.qty,
                    onDecrease = { onQuantityChange((item.qty - 1).coerceAtLeast(1)) },
                    onIncrease = { onQuantityChange(item.qty + 1) }
                )

                Text(
                    text = "₹ ${formatTwo(displayedTotal)}",
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
fun ExpandedItemEditor(
    name: String,
    defaultListPrice: Double,
    gstPercentage: Double,
    initialQuantity: Int = 1,
    taxType: TaxType,
    onAdd: (qty: Int, unitPrice: Double) -> Unit,
    onCancel: () -> Unit
) {
    var qtyText by rememberSaveable { mutableStateOf(initialQuantity.toString()) }
    var listPriceText by rememberSaveable { mutableStateOf(defaultListPrice.toString()) }
    var discountText by rememberSaveable { mutableStateOf("0") }
    var priceText by rememberSaveable { mutableStateOf("") } // user override for unit price/gross price (blank means computed)

    // Helpers to parse safely
    fun parseDoubleSafe(s: String): Double {
        val filtered = s.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        val valid = if (dotCount > 1) {
            filtered.substringBefore('.') + "." + filtered.substringAfter('.').replace(".", "")
        } else filtered
        return valid.toDoubleOrNull() ?: 0.0
    }

    val qty = parseDoubleSafe(qtyText).toInt().coerceAtLeast(0)
    val listPrice = parseDoubleSafe(listPriceText)
    val discount = parseDoubleSafe(discountText)

    // computed unit price before tax (or gross depending on taxType). We treat listPrice as the base
    val computedUnit = (listPrice - discount).coerceAtLeast(0.0)

    // If user provided an override in priceText, use it; otherwise use computedUnit
    val enteredUnit = if (priceText.isBlank()) computedUnit else parseDoubleSafe(priceText)

    // compute amounts depending on tax type
    val taxableAmount: Double
    val gstAmount: Double
    val netAmount: Double // final gross amount (what user pays per qty * qty)

    if (taxType == TaxType.EXTRA) {
        // enteredUnit is taxable unit price (exclusive of GST)
        taxableAmount = enteredUnit * qty
        gstAmount = taxableAmount * gstPercentage / 100.0
        netAmount = taxableAmount + gstAmount
    } else {
        // INCLUSIVE: enteredUnit is gross unit price (includes GST)
        val grossAmount = enteredUnit * qty
        // taxable base = gross * 100 / (100 + gst)
        taxableAmount = if (gstPercentage == 0.0) grossAmount else (grossAmount * 100.0 / (100.0 + gstPercentage))
        gstAmount = grossAmount - taxableAmount
        netAmount = grossAmount // already inclusive
    }

    // Add / Cancel buttons will provide a unit price to store.
    // For consistency with previous behavior, store unit price as per existing expectations:
    // - For EXTRA: store unit price as taxable unit (enteredUnit)
    // - For INCLUSIVE: store unit price as gross unit (enteredUnit) so totals align with displayed net
    val unitPriceToStore = enteredUnit

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    )
    {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Configure item before adding",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact inputs row: qty | list price | discount | unit price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // QTY
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Qty",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = qtyText,
                        onValueChange = { new ->
                            qtyText = new.filter { it.isDigit() }.ifEmpty { "0" }
                        },
                        keyboardType = KeyboardType.Number
                    )


                }

                // List price
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "List Pr.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = listPriceText,
                        onValueChange = { new ->
                            listPriceText = new.filter { it.isDigit() || it == '.' }
                        },
                        keyboardType = KeyboardType.Decimal
                    )

                }

                // Discount
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = discountText,
                        onValueChange = { new ->
                            discountText = new.filter { it.isDigit() || it == '.' }

                        },
                        keyboardType = KeyboardType.Decimal
                    )

                }

                // Price (unit) - show entered override if present, otherwise show computed unit formatted
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (taxType == TaxType.EXTRA) "Price (ex GST)" else "Price (incl GST)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = if (priceText.isBlank()) formatTwo(computedUnit) else priceText,
                        onValueChange = { new ->
                            priceText = new.filter { it.isDigit() || it == '.' }
                        },
                        keyboardType = KeyboardType.Decimal
                    )

                }
            }

            // Computed read-only row: taxable | gst% | gst amt | net amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Taxable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹ ${formatTwo(taxableAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GST %",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatTwo(gstPercentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GST Amt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹ ${formatTwo(gstAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹ ${formatTwo(netAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Add / Cancel buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    // When adding, pass qty and unitPrice (unit after discount or override)
                    val finalQty = qty.coerceAtLeast(0)
                    if (finalQty > 0) {
                        onAdd(finalQty, unitPriceToStore)
                    } else {
                        // nothing added if zero qty
                    }
                }) {
                    Text("Add")
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                            ),
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
            text = formatTwo(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
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
fun BorderedInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}