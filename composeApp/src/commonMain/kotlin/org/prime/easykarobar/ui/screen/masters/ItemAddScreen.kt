package org.prime.easykarobar.ui.screen.masters


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

// ─────────────────────────────────────────────────────────────────────────────
//  Top-level helpers  (Pair = display Name + GUID / Code)
// ─────────────────────────────────────────────────────────────────────────────
private val EmptyPair = Pair("", "")
private val Pair<String, String>.isSelected get() = first.isNotBlank() && second.isNotBlank()

// Conversion type options
private val CON_TYPE_OPTIONS = listOf("Main / Alt", "Alt / Main")

// Default qty options for Alt unit
private val DEFAULT_QTY_OPTIONS = listOf("1", "0", "NI")

// ─────────────────────────────────────────────────────────────────────────────
//  Section Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Form Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FormField — wraps TallyTextField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    isNumber: Boolean = false,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    errorMessage: String = "Required",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        TallyTextField(
            value = value,
            onValueChange = onChange,
            placeholder = placeholder.ifEmpty { label },
            isPassword = isPassword,
            isNumber = isNumber,
            label = if (isError) "$label *" else label,
            imeAction = imeAction,
            modifier = Modifier.fillMaxWidth()
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  BottomSheetTriggerField — read-only field that opens a bottom sheet
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetTriggerField(
    label: String,
    value: String,
    isError: Boolean = false,
    trailingIcon: ImageVector = Icons.Default.AccountTree,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            isError = isError,
            label = {
                Text(
                    if (isError) "$label *" else label,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            supportingText = if (isError) ({
                Text(
                    "Required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }) else null,
            trailingIcon = {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TallyDropdown — simple fixed list
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TallyDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (opt == selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { onSelect(opt); expanded = false },
                    leadingIcon = if (opt == selected) ({
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }) else null
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TallyToggle row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TallyToggle(
    label: String,
    subLabel: String = "",
    value: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subLabel.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor   = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DefaultQtyChips — chips for 1 / 0 / NI default qty selection
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DefaultQtyChips(selected: String, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Default Qty",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DEFAULT_QTY_OPTIONS.forEach { opt ->
                FilterChip(
                    selected = selected == opt,
                    onClick = { onSelect(opt) },
                    label = {
                        Text(opt, style = MaterialTheme.typography.labelMedium)
                    },
                    leadingIcon = if (selected == opt) ({
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }) else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Validation dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RequiredFieldsDialog(
    missingFields: List<String>,
    extraMessage: String? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Required Fields Missing", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (extraMessage != null) {
                    Text(
                        extraMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(6.dp))
                }
                if (missingFields.isNotEmpty()) {
                    Text(
                        "Please fill in the following required fields:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    missingFields.forEach { field ->
                        Text(
                            "• $field",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  ItemAddScreen
// ─────────────────────────────────────────────────────────────────────────────
object ItemAddScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav    = LocalNavigator.currentOrThrow
        val scroll = rememberScrollState()
        val scope  = rememberCoroutineScope()

        // ── 1. Identity ────────────────────────────────────────────────────
        var name      by remember { mutableStateOf("") }
        var alias     by remember { mutableStateOf("") }   // auto = name, editable
        var printName by remember { mutableStateOf("") }   // auto = name, editable

        // ── 2. Group ───────────────────────────────────────────────────────
        // Pair(Name, GUID) from item group master
        var group         by remember { mutableStateOf(EmptyPair) }
        var groupCode     by remember { mutableStateOf("") }
        var guid          by remember { mutableStateOf("") }
        var showGroupSheet by remember { mutableStateOf(false) }
        val groupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // ── 3. Units ───────────────────────────────────────────────────────
        // Pair(Name, Code) from unit master
        var mainUnit          by remember { mutableStateOf(EmptyPair) }
        var showMainUnitSheet  by remember { mutableStateOf(false) }
        val mainUnitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // altUnit: toggle "same as main" vs separate
        var altSameAsMain     by remember { mutableStateOf(true) }
        var altUnit           by remember { mutableStateOf(EmptyPair) }
        var showAltUnitSheet   by remember { mutableStateOf(false) }
        val altUnitSheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Conversion
        var conType    by remember { mutableStateOf(CON_TYPE_OPTIONS[0]) } // Main/Alt or Alt/Main
        var conFactor  by remember { mutableStateOf("") }
        var defaultQty by remember { mutableStateOf("1") }                 // 1 | 0 | NI

        // ── 4. Tax ─────────────────────────────────────────────────────────
        // Pair(Name, Code) from tax category master
        var taxCategory         by remember { mutableStateOf(EmptyPair) }
        var showTaxCategorySheet by remember { mutableStateOf(false) }
        val taxCategorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var taxCategoryCode     by remember { mutableStateOf("") }

        // ── 5. Opening Stock ───────────────────────────────────────────────
        var opQty    by remember { mutableStateOf("") }    // in Main unit
        var opQtyAlt by remember { mutableStateOf("") }    // in Alt unit
        var opAmount by remember { mutableStateOf("") }

        // ── 6. Pricing ─────────────────────────────────────────────────────
        var salePrice    by remember { mutableStateOf("") }
        var purchPrice   by remember { mutableStateOf("") }
        var mrp          by remember { mutableStateOf("") }
        var minSalePrice by remember { mutableStateOf("") }
        var selfValPrice by remember { mutableStateOf("") }

        // ── 7. Discounts ───────────────────────────────────────────────────
        var saleDiscount by remember { mutableStateOf("") }
        var purchDiscount by remember { mutableStateOf("") }

        // ── 8. Description ─────────────────────────────────────────────────
        var desc1 by remember { mutableStateOf("") }
        var desc2 by remember { mutableStateOf("") }
        var desc3 by remember { mutableStateOf("") }
        var desc4 by remember { mutableStateOf("") }

        // ── Validation ─────────────────────────────────────────────────────
        var showDialog     by remember { mutableStateOf(false) }
        var dialogMissing  by remember { mutableStateOf(listOf<String>()) }
        var dialogExtraMsg by remember { mutableStateOf<String?>(null) }
        var attempted      by remember { mutableStateOf(false) }

        val nameError         = attempted && name.isBlank()
        val aliasEqName       = alias.isNotBlank() && alias.trim().equals(name.trim(), ignoreCase = true)
        val aliasError        = attempted && aliasEqName
        val groupError        = attempted && !group.isSelected
        val mainUnitError     = attempted && !mainUnit.isSelected
        val taxCategoryError  = attempted && !taxCategory.isSelected

        // ── Sheet hide helpers ──────────────────────────────────────────────
        fun hideGroupSheet()       { scope.launch { groupSheetState.hide() }.invokeOnCompletion { showGroupSheet = false } }
        fun hideMainUnitSheet()    { scope.launch { mainUnitSheetState.hide() }.invokeOnCompletion { showMainUnitSheet = false } }
        fun hideAltUnitSheet()     { scope.launch { altUnitSheetState.hide() }.invokeOnCompletion { showAltUnitSheet = false } }
        fun hideTaxCategorySheet() { scope.launch { taxCategorySheetState.hide() }.invokeOnCompletion { showTaxCategorySheet = false } }

        fun resetAll() {
            name = ""; alias = ""; printName = ""
            group = EmptyPair; groupCode = ""; guid = ""
            mainUnit = EmptyPair; altSameAsMain = true; altUnit = EmptyPair
            conType = CON_TYPE_OPTIONS[0]; conFactor = ""; defaultQty = "1"
            taxCategory = EmptyPair; taxCategoryCode = ""
            opQty = ""; opQtyAlt = ""; opAmount = ""
            salePrice = ""; purchPrice = ""; mrp = ""; minSalePrice = ""; selfValPrice = ""
            saleDiscount = ""; purchDiscount = ""
            desc1 = ""; desc2 = ""; desc3 = ""; desc4 = ""
            attempted = false
        }

        fun validate(): Boolean {
            attempted = true
            val missing = mutableListOf<String>()
            if (name.isBlank())          missing += "Name"
            if (!group.isSelected)       missing += "Group"
            if (!mainUnit.isSelected)    missing += "Main Unit"
            if (!taxCategory.isSelected) missing += "Tax Category"

            if (aliasEqName) {
                dialogExtraMsg = "Name and Alias cannot be the same."
                dialogMissing  = missing
                showDialog     = true
                return false
            }
            if (missing.isNotEmpty()) {
                dialogExtraMsg = null
                dialogMissing  = missing
                showDialog     = true
                return false
            }
            return true
        }

        // ── Dialogs & sheets at top level ──────────────────────────────────
        if (showDialog) {
            RequiredFieldsDialog(
                missingFields = dialogMissing,
                extraMessage  = dialogExtraMsg,
                onDismiss     = { showDialog = false }
            )
        }

        // Group bottom sheet
        TransactionBottomSheet(
            showBottomSheet  = showGroupSheet,
            list             = emptyList(), // TODO: List<Pair(GroupName, GUID)> from ViewModel
            onSelected       = { pair -> group = pair; groupCode = pair.second; hideGroupSheet() },
            onDismiss        = { hideGroupSheet() },
            bottomSheetState = groupSheetState,
            title            = "Select Group"
        )

        // Main Unit bottom sheet
        TransactionBottomSheet(
            showBottomSheet  = showMainUnitSheet,
            list             = emptyList(), // TODO: List<Pair(UnitName, UnitCode)> from ViewModel
            onSelected       = { pair ->
                mainUnit = pair
                if (altSameAsMain) altUnit = pair
                hideMainUnitSheet()
            },
            onDismiss        = { hideMainUnitSheet() },
            bottomSheetState = mainUnitSheetState,
            title            = "Select Main Unit"
        )

        // Alt Unit bottom sheet (only when not same as main)
        TransactionBottomSheet(
            showBottomSheet  = showAltUnitSheet,
            list             = emptyList(), // TODO: List<Pair(UnitName, UnitCode)> from ViewModel
            onSelected       = { pair -> altUnit = pair; hideAltUnitSheet() },
            onDismiss        = { hideAltUnitSheet() },
            bottomSheetState = altUnitSheetState,
            title            = "Select Alt Unit"
        )

        // Tax Category bottom sheet
        TransactionBottomSheet(
            showBottomSheet  = showTaxCategorySheet,
            list             = emptyList(), // TODO: List<Pair(TaxCategoryName, Code)> from ViewModel
            onSelected       = { pair -> taxCategory = pair; taxCategoryCode = pair.second; hideTaxCategorySheet() },
            onDismiss        = { hideTaxCategorySheet() },
            bottomSheetState = taxCategorySheetState,
            title            = "Select Tax Category"
        )

        // ── Scaffold ───────────────────────────────────────────────────────
        TallyScaffold(
            title        = "Add Item",
            showEditIcon = false,
            onEditClick  = {},
            onBack       = { nav.pop() }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .verticalScroll(scroll)
                    .padding(bottom = 32.dp)
            ) {

                // ── 1. Basic Information ──────────────────────────────────
                SectionHeader("Basic Information", Icons.Default.Inventory2)
                FormCard {
                    // Name → auto mirrors to Alias and PrintName
                    FormField(
                        label        = "Name *",
                        value        = name,
                        onChange     = { v ->
                            name = v
                            if (alias.isEmpty()     || alias == name.dropLast(1))     alias     = v
                            if (printName.isEmpty() || printName == name.dropLast(1)) printName = v
                        },
                        placeholder  = "Enter item name",
                        isError      = nameError,
                        errorMessage = "Name is required"
                    )
                    FormField(
                        label        = "Alias",
                        value        = alias,
                        onChange     = { alias = it },
                        placeholder  = "Short alias (auto-filled from Name)",
                        isError      = aliasError,
                        errorMessage = "Alias cannot be the same as Name"
                    )
                    FormField(
                        label       = "Print Name",
                        value       = printName,
                        onChange    = { printName = it },
                        placeholder = "Name for printing (auto-filled from Name)"
                    )
                }

                // ── 2. Group & Classification ─────────────────────────────
                SectionHeader("Group & Classification", Icons.Default.AccountTree)
                FormCard {
                    BottomSheetTriggerField(
                        label        = "Group *",
                        value        = group.first,
                        isError      = groupError,
                        trailingIcon = Icons.Default.AccountTree,
                        onClick      = { showGroupSheet = true }
                    )
                    // GroupCode is auto-filled from group selection but editable
                    FormField(
                        label       = "Group Code",
                        value       = groupCode,
                        onChange    = { groupCode = it },
                        placeholder = "Auto-filled from Group"
                    )
                    FormField(
                        label       = "GUID",
                        value       = guid,
                        onChange    = { guid = it },
                        placeholder = "Globally unique identifier"
                    )
                }

                // ── 3. Units & Conversion ─────────────────────────────────
                SectionHeader("Units & Conversion", Icons.Default.Balance)
                FormCard {
                    // Main Unit
                    BottomSheetTriggerField(
                        label        = "Main Unit *",
                        value        = mainUnit.first,
                        isError      = mainUnitError,
                        trailingIcon = Icons.Default.Balance,
                        onClick      = { showMainUnitSheet = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Alt Unit same-as-main toggle
                    TallyToggle(
                        label     = "Alt Unit same as Main",
                        subLabel  = if (altSameAsMain) "Using ${mainUnit.first.ifEmpty { "Main Unit" }}" else "Custom alt unit selected",
                        value     = altSameAsMain,
                        onToggle  = { checked ->
                            altSameAsMain = checked
                            if (checked) altUnit = mainUnit
                        }
                    )

                    // Alt Unit selector (only when not same as main)
                    if (!altSameAsMain) {
                        BottomSheetTriggerField(
                            label        = "Alt Unit",
                            value        = altUnit.first,
                            trailingIcon = Icons.Default.Balance,
                            onClick      = { showAltUnitSheet = true }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Conversion type + factor side by side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            TallyDropdown(
                                label    = "Con Type",
                                options  = CON_TYPE_OPTIONS,
                                selected = conType,
                                onSelect = { conType = it }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            FormField(
                                label       = "Con Factor",
                                value       = conFactor,
                                onChange    = { conFactor = it },
                                placeholder = "e.g. 12",
                                isNumber    = true
                            )
                        }
                    }

                    // Default qty chips: 1 / 0 / NI
                    DefaultQtyChips(selected = defaultQty, onSelect = { defaultQty = it })
                }

                // ── 4. Tax Category ───────────────────────────────────────
                SectionHeader("Tax Category", Icons.Default.Receipt)
                FormCard {
                    BottomSheetTriggerField(
                        label        = "Tax Category *",
                        value        = taxCategory.first,
                        isError      = taxCategoryError,
                        trailingIcon = Icons.Default.Receipt,
                        onClick      = { showTaxCategorySheet = true }
                    )
                    // Tax category code — auto-filled from selection
                    FormField(
                        label       = "Tax Category Code",
                        value       = taxCategoryCode,
                        onChange    = { taxCategoryCode = it },
                        placeholder = "Auto-filled from Tax Category"
                    )
                }

                // ── 5. Opening Stock ──────────────────────────────────────
                SectionHeader("Opening Stock", Icons.Default.Inventory2)
                FormCard {
                    // Op Qty in Main unit, Alt/Main label shows conversion direction
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            FormField(
                                label       = "Op Qty (${mainUnit.first.ifEmpty { "Main" }})",
                                value       = opQty,
                                onChange    = { opQty = it },
                                placeholder = "0",
                                isNumber    = true
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            FormField(
                                label       = "Op Qty Alt (${altUnit.first.ifEmpty { "Alt" }})",
                                value       = opQtyAlt,
                                onChange    = { opQtyAlt = it },
                                placeholder = "0",
                                isNumber    = true
                            )
                        }
                    }
                    FormField(
                        label       = "Op Amount",
                        value       = opAmount,
                        onChange    = { opAmount = it },
                        placeholder = "0.00",
                        isNumber    = true
                    )
                }

                // ── 6. Pricing ────────────────────────────────────────────
                SectionHeader("Pricing", Icons.Default.LocalOffer)
                FormCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField("Sale Price",   salePrice,  { salePrice  = it }, "0.00", isNumber = true)
                        }
                        Box(Modifier.weight(1f)) {
                            FormField("Purc Price",   purchPrice, { purchPrice = it }, "0.00", isNumber = true)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField("MRP",          mrp,          { mrp          = it }, "0.00", isNumber = true)
                        }
                        Box(Modifier.weight(1f)) {
                            FormField("Min Sale Price", minSalePrice, { minSalePrice = it }, "0.00", isNumber = true)
                        }
                    }
                    FormField(
                        label       = "Self Val Price",
                        value       = selfValPrice,
                        onChange    = { selfValPrice = it },
                        placeholder = "0.00",
                        isNumber    = true
                    )
                }

                // ── 7. Discounts ──────────────────────────────────────────
                SectionHeader("Discounts", Icons.Default.Percent)
                FormCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField("Sale Disc %",   saleDiscount,  { saleDiscount  = it }, "0.00", isNumber = true)
                        }
                        Box(Modifier.weight(1f)) {
                            FormField("Purc Disc %",   purchDiscount, { purchDiscount = it }, "0.00", isNumber = true)
                        }
                    }
                }

                // ── 8. Description ────────────────────────────────────────
                SectionHeader("Description", Icons.Default.Person)
                FormCard {
                    FormField("Description 1", desc1, { desc1 = it }, "Line 1")
                    FormField("Description 2", desc2, { desc2 = it }, "Line 2")
                    FormField("Description 3", desc3, { desc3 = it }, "Line 3")
                    FormField(
                        label       = "Description 4",
                        value       = desc4,
                        onChange    = { desc4 = it },
                        placeholder = "Line 4",
                        imeAction   = ImeAction.Done
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Save ──────────────────────────────────────────────────
                Button(
                    onClick = {
                        if (validate()) {
                            // TODO: Build ItemModel and call viewModel.save(item); nav.pop()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape  = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Item", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(8.dp))

                // ── Cancel + Reset ────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick   = { nav.pop() },
                        modifier  = Modifier.weight(1f).height(46.dp),
                        shape     = MaterialTheme.shapes.medium,
                        colors    = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick   = { resetAll() },
                        modifier  = Modifier.weight(1f).height(46.dp),
                        shape     = MaterialTheme.shapes.medium,
                        colors    = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reset", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}