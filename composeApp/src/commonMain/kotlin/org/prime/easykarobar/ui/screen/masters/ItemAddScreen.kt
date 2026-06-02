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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.prime.easykarobar.business.viewmodel.masters.AccountViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes

// ─────────────────────────────────────────────────────────────────────────────
//  Constants
// ─────────────────────────────────────────────────────────────────────────────
private val CON_TYPE_OPTIONS = listOf("Main / Alt", "Alt / Main")

// ─────────────────────────────────────────────────────────────────────────────
//  ItemFormData — built only after successful validation
// ─────────────────────────────────────────────────────────────────────────────
@Serializable
data class ItemFormData(
    // Identity
    val name: String,
    val alias: String,
    val printName: String,
    // Group
    val parentGroup: String,
    val parentGroupGuid: String,
    // Units
    val mainUnit: String,
    val mainUnitGuid: String,
    val altUnit: String,
    val altUnitGuid: String,
    val altSameAsMain: Boolean,
    val conType: String,
    val conFactor: Double,
    // Tax
    val taxCategoryName: String,
    val taxCategoryGuid: String,
    // Opening Stock
    val opQty: String,
    val opQtyAlt: String,
    val opAmount: String,
    // Pricing
    val salePrice: String,
    val purchPrice: String,
    val mrp: String,
    val minSalePrice: String,
    val selfValPrice: String,
    // Discounts
    val saleDiscount: String,
    val purchDiscount: String,
    // Description
    val desc1: String,
    val desc2: String,
    val desc3: String,
    val desc4: String,
)

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
//  FormField
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
//  DropdownSelector
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                1.dp,
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { "Select…" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (isError) {
            Text(
                text = "Required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TallyDropdown — fixed option list (Con Type)
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
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (opt == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { onSelect(opt); expanded = false },
                    leadingIcon = if (opt == selected) ({
                        Icon(
                            Icons.Default.Check, null,
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
//  TallyToggle
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
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subLabel.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ItemAddScreen
// ─────────────────────────────────────────────────────────────────────────────
object ItemAddScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val scroll = rememberScrollState()
        val scope = rememberCoroutineScope()

        // ── 1. Identity ────────────────────────────────────────────────────
        var name by remember { mutableStateOf("") }
        var alias by remember { mutableStateOf("") }
        var printName by remember { mutableStateOf("") }

        // ── 2. Group ───────────────────────────────────────────────────────
        var parentGroup by remember { mutableStateOf("") }
        var parentGroupGUID by remember { mutableStateOf("") }
        var showGroupSheet by remember { mutableStateOf(false) }
        val groupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // ── 3. Units ───────────────────────────────────────────────────────
        var mainUnit by remember { mutableStateOf("") }
        var mainUnitGuid by remember { mutableStateOf("") }
        var showMainUnitSheet by remember { mutableStateOf(false) }
        val mainUnitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var altSameAsMain by remember { mutableStateOf(true) }
        var altUnit by remember { mutableStateOf("") }
        var altUnitGuid by remember { mutableStateOf("") }
        var showAltUnitSheet by remember { mutableStateOf(false) }
        val altUnitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var conType by remember { mutableStateOf(CON_TYPE_OPTIONS[1]) }
        var conFactor by remember { mutableStateOf("") }

        // ── 4. Tax ─────────────────────────────────────────────────────────
        var taxCategoryName by remember { mutableStateOf("") }
        var taxCategoryGuid by remember { mutableStateOf("") }
        var taxCategoryCode by remember { mutableStateOf("") }
        var showTaxCategorySheet by remember { mutableStateOf(false) }
        val taxCategorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // ── 5. Opening Stock ───────────────────────────────────────────────
        var opQty by remember { mutableStateOf("") }
        var opQtyAlt by remember { mutableStateOf("") }
        var opAmount by remember { mutableStateOf("") }

        // ── 6. Pricing ─────────────────────────────────────────────────────
        var salePrice by remember { mutableStateOf("") }
        var purchPrice by remember { mutableStateOf("") }
        var mrp by remember { mutableStateOf("") }
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
        var showDialog by remember { mutableStateOf(false) }
        var dialogMessage by remember { mutableStateOf("") }
        var attempted by remember { mutableStateOf(false) }

        val aliasEqName = alias.isNotBlank() && alias.trim().equals(name.trim(), ignoreCase = true)
        val nameError = attempted && name.isBlank()
        val aliasError = attempted && aliasEqName
        val groupError = attempted && parentGroup.isBlank()
        val mainUnitError = attempted && mainUnit.isBlank()
        val altUnitError = attempted && !altSameAsMain && altUnit.isBlank()
        val taxCategoryError = attempted && taxCategoryName.isBlank()
        val conFactorError = attempted && (conFactor.isBlank() || conFactor.toDoubleOrNull() == 0.0)

        // ── Data ───────────────────────────────────────────────────────────
        val db = DatabaseHolder.instance
        val groupList = db.productGroupMasterQueries
            .selectAll(filterGroup = filterItemGroups(), groupCodes = itemGroupCodes())
            .executeAsList()
        val unitList = db.productUnitMasterQueries.selectAll().executeAsList()
        val taxCategoryList: List<Pair<String, String>> =
            db.taxCategoryMastQueries.selectAll().executeAsList().map { Pair(it.Name, it.GUID) }

        // ── Sheet dismissal helper ─────────────────────────────────────────
        fun hideSheet(state: SheetState, hide: () -> Unit) {
            scope.launch { state.hide() }.invokeOnCompletion { hide() }
        }

        var showDuplicateDialog by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }
        val viewModel: AccountViewModel = viewModel { AccountViewModel() }
        val state by viewModel.itemDataState

        if (showResultDialog) {
            TallyResultDialog(
                message = state.message ?: "Error Occurred",
                onDone = { nav.pop() },
                isSuccess = state.success,
                confirmText = "OK"
            )
        }
        if (state.isLoading) {
            TallyLoadingDialog("Creating your item")
        }
        if (showDuplicateDialog) {
            TallyResultDialog(
                message = "Account with this name or alias already exists",
                onDone = { showDuplicateDialog = false },
                isSuccess = state.success,
                confirmText = "OK"
            )
        }


        // ── Validate + build ItemFormData ──────────────────────────────────
        fun validateAndBuild(): ItemFormData? {
            attempted = true

            if (aliasEqName) {
                dialogMessage = "Name and Alias cannot be the same value."
                showDialog = true
                return null
            }

            val cf = conFactor.toDoubleOrNull()
            if ((cf == null || cf == 0.0) || !altSameAsMain ) {
                dialogMessage = "Conversion Factor cannot be zero or empty."
                showDialog = true
                return null
            }

            val missing = mutableListOf<String>()
            if (name.isBlank()) missing += "• Name"
            if (parentGroup.isBlank()) missing += "• Item Group"
            if (mainUnit.isBlank()) missing += "• Main Unit"
            if (!altSameAsMain && altUnit.isBlank()) missing += "• Alt Unit"
            if (taxCategoryName.isBlank()) missing += "• Tax Category"
            if (missing.isNotEmpty()) {
                dialogMessage =
                    "Please fill in the following required fields:\n\n${missing.joinToString("\n")}"
                showDialog = true
                return null
            }

            return ItemFormData(
                name = name.trim(),
                alias = alias.trim(),
                printName = printName.trim(),
                parentGroup = parentGroup,
                parentGroupGuid = parentGroupGUID,
                mainUnit = mainUnit,
                mainUnitGuid = mainUnitGuid,
                altUnit = altUnit,
                altUnitGuid = altUnitGuid,
                altSameAsMain = altSameAsMain,
                conType = conType,
                conFactor = cf,
                taxCategoryName = taxCategoryName,
                taxCategoryGuid = taxCategoryGuid,
                opQty = opQty,
                opQtyAlt = opQtyAlt,
                opAmount = opAmount,
                salePrice = salePrice,
                purchPrice = purchPrice,
                mrp = mrp,
                minSalePrice = minSalePrice,
                selfValPrice = selfValPrice,
                saleDiscount = saleDiscount,
                purchDiscount = purchDiscount,
                desc1 = desc1,
                desc2 = desc2,
                desc3 = desc3,
                desc4 = desc4,
            )
        }

        // ── Dialog ─────────────────────────────────────────────────────────
        if (showDialog) {
            TallyResultDialog(
                message = dialogMessage,
                isSuccess = false,
                confirmText = "OK",
                onDone = { showDialog = false }
            )
        }

        // ── Bottom sheets ──────────────────────────────────────────────────
        TransactionBottomSheet(
            showBottomSheet = showGroupSheet,
            list = groupList.map { Pair(it.Name.toString(), it.GUID.toString()) },
            onSelected = { parentGroup = it.first; parentGroupGUID = it.second },
            onDismiss = { hideSheet(groupSheetState) { showGroupSheet = false } },
            bottomSheetState = groupSheetState,
            title = "Select Item Group"
        )
        TransactionBottomSheet(
            showBottomSheet = showMainUnitSheet,
            list = unitList.map { Pair(it.Name.toString(), it.GUID.toString()) },
            onSelected = {
                mainUnit = it.first
                mainUnitGuid = it.second
                if (altSameAsMain) {
                    altUnit = it.first; altUnitGuid = it.second
                }
            },
            onDismiss = { hideSheet(mainUnitSheetState) { showMainUnitSheet = false } },
            bottomSheetState = mainUnitSheetState,
            title = "Select Main Unit"
        )
        TransactionBottomSheet(
            showBottomSheet = showAltUnitSheet,
            list = unitList.map { Pair(it.Name.toString(), it.GUID.toString()) },
            onSelected = { altUnit = it.first; altUnitGuid = it.second },
            onDismiss = { hideSheet(altUnitSheetState) { showAltUnitSheet = false } },
            bottomSheetState = altUnitSheetState,
            title = "Select Alt Unit"
        )
        TransactionBottomSheet(
            showBottomSheet = showTaxCategorySheet,
            list = taxCategoryList,
            onSelected = { taxCategoryName = it.first; taxCategoryGuid = it.second },
            onDismiss = { hideSheet(taxCategorySheetState) { showTaxCategorySheet = false } },
            bottomSheetState = taxCategorySheetState,
            title = "Select Tax Category"
        )

        // ── Scaffold ───────────────────────────────────────────────────────
        TallyScaffold(
            title = "Add Item",
            showEditIcon = false,
            onEditClick = {},
            onBack = { nav.pop() }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues).navigationBarsPadding()
                    .verticalScroll(scroll)
                    .padding(bottom = 32.dp)
            ) {

                // ── 1. Basic Information ──────────────────────────────────
                SectionHeader("Basic Information", Icons.Default.Inventory2)
                FormCard {
                    FormField(
                        label = "Name *",
                        value = name,
                        onChange = { v ->
                            name = v
                            printName = v
                        },
                        placeholder = "Enter item name",
                        isError = nameError,
                        errorMessage = "Name is required"
                    )
                    FormField(
                        label = "Alias",
                        value = alias,
                        onChange = { alias = it },
                        placeholder = "Short alias (auto-filled from Name)",
                        isError = aliasError,
                        errorMessage = "Alias cannot be the same as Name"
                    )
                    FormField(
                        label = "Print Name",
                        value = printName,
                        onChange = { printName = it },
                        placeholder = "Name for printing (auto-filled from Name)"
                    )
                }

                // ── 2. Group ──────────────────────────────────────────────
                SectionHeader("Group", Icons.Default.AccountTree)
                FormCard {
                    DropdownSelector(
                        label = "Item Group",
                        value = parentGroup,
                        isError = groupError,
                        onClick = { showGroupSheet = true }
                    )
                }

                // ── 3. Units & Conversion ─────────────────────────────────
                SectionHeader("Units & Conversion", Icons.Default.Balance)
                FormCard {
                    DropdownSelector(
                        label = "Main Unit",
                        value = mainUnit,
                        isError = mainUnitError,
                        onClick = { showMainUnitSheet = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    TallyToggle(
                        label = "Alt Unit same as Main",
                        subLabel = if (altSameAsMain) "Using ${mainUnit.ifEmpty { "Main Unit" }}"
                        else "Custom alt unit selected",
                        value = altSameAsMain,
                        onToggle = { checked ->
                            altSameAsMain = checked
                            if (checked) {
                                altUnit = mainUnit
                                altUnitGuid = mainUnitGuid
                            } else {
                                altUnit = ""
                                altUnitGuid = ""
                            }
                        }
                    )

                    if (!altSameAsMain) {
                        DropdownSelector(
                            label = "Alt Unit",
                            value = altUnit,
                            isError = altUnitError,
                            onClick = { showAltUnitSheet = true }
                        )
                    }


                    if (!altSameAsMain) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) {
                                TallyDropdown(
                                    label = "Con Type",
                                    options = CON_TYPE_OPTIONS,
                                    selected = conType,
                                    onSelect = { conType = it }
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormField(
                                    label = "Con Factor",
                                    value = conFactor,
                                    onChange = { conFactor = it },
                                    placeholder = "e.g. 12",
                                    isNumber = true,
                                    isError = conFactorError,
                                    errorMessage = "Cannot be zero or empty"
                                )
                            }
                        }
                    }
                }

                // ── 4. Tax Category ───────────────────────────────────────
                SectionHeader("Tax Category", Icons.Default.Receipt)
                FormCard {
                    DropdownSelector(
                        label = "Tax Category *",
                        value = taxCategoryName,
                        isError = taxCategoryError,
                        onClick = { showTaxCategorySheet = true }
                    )
                }

                // ── 5. Opening Stock ──────────────────────────────────────
                SectionHeader("Opening Stock", Icons.Default.Inventory2)
                FormCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField(
                                label = "Op Qty (Main)",
                                value = opQty,
                                onChange = {
                                    if (conType == CON_TYPE_OPTIONS[0]) {
                                        opQty = it; opQtyAlt = ((opQty.toDoubleOrNull()
                                            ?: 0.0) / (conFactor.toDoubleOrNull() ?: 0.0)).toString()
                                    }else{
                                        opQty = it; opQtyAlt = ((opQty.toDoubleOrNull()
                                            ?: 0.0) * (conFactor.toDoubleOrNull() ?: 0.0)).toString()
                                    }
                                },
                                placeholder = "0",
                                isNumber = true
                            )
                        }
                        if(!altSameAsMain){
                            Box(Modifier.weight(1f)) {
                                FormField(
                                    label = "Op Qty (Alt)",
                                    value = opQtyAlt,
                                    onChange = {
                                        if (conType == CON_TYPE_OPTIONS[0]) {
                                            opQtyAlt = it; opQty = ((opQtyAlt.toDoubleOrNull()
                                                ?: 0.0) * (conFactor.toDoubleOrNull() ?: 0.0)).toString()
                                        }else{
                                            opQtyAlt = it; opQty = ((opQtyAlt.toDoubleOrNull()
                                                ?: 0.0) / (conFactor.toDoubleOrNull() ?: 0.0)).toString()
                                        }
                                    },
                                    placeholder = "0",
                                    isNumber = true
                                )
                            }
                        }
                    }
                    FormField(
                        label = "Op Amount",
                        value = opAmount,
                        onChange = { opAmount = it },
                        placeholder = "0.00",
                        isNumber = true
                    )
                }

                // ── 6. Pricing ────────────────────────────────────────────
                SectionHeader("Pricing", Icons.Default.LocalOffer)
                FormCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField(
                                "Sale Price",
                                salePrice,
                                { salePrice = it },
                                "0.00",
                                isNumber = true
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            FormField(
                                "Purc Price",
                                purchPrice,
                                { purchPrice = it },
                                "0.00",
                                isNumber = true
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField("MRP", mrp, { mrp = it }, "0.00", isNumber = true)
                        }
                        Box(Modifier.weight(1f)) {
                            FormField(
                                "Min Sale Price",
                                minSalePrice,
                                { minSalePrice = it },
                                "0.00",
                                isNumber = true
                            )
                        }
                    }
                    FormField(
                        label = "Self Val Price",
                        value = selfValPrice,
                        onChange = { selfValPrice = it },
                        placeholder = "0.00",
                        isNumber = true
                    )
                }

                // ── 7. Discounts ──────────────────────────────────────────
                SectionHeader("Discounts", Icons.Default.Percent)
                FormCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) {
                            FormField(
                                "Sale Disc %",
                                saleDiscount,
                                { saleDiscount = it },
                                "0.00",
                                isNumber = true
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            FormField(
                                "Purc Disc %",
                                purchDiscount,
                                { purchDiscount = it },
                                "0.00",
                                isNumber = true
                            )
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
                        label = "Description 4",
                        value = desc4,
                        onChange = { desc4 = it },
                        placeholder = "Line 4",
                        imeAction = ImeAction.Done
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Save ──────────────────────────────────────────────────
                Button(
                    onClick = {
                        val item = validateAndBuild() ?: return@Button
                        scope.launch {
                            val existing = db.productsQueries.existingItems(
                                name1 = name.trim(),
                                alias1 = name.trim(),
                                name2 = if (alias == "") name.trim() else alias.trim(),
                                alias2 = if (alias == "") name.trim() else alias.trim()
                            ).executeAsList()
                            if (existing.isEmpty()) {
                                viewModel.createItem(item) {
                                    db.productsQueries.insertItem(
                                        name = state.data?.name,
                                        alias = state.data?.alias,
                                        printName = state.data?.printName,
                                        parentGroup = state.data?.parentGroup,
                                        parentGroupGuid = state.data?.parentGroupGuid?.toDoubleOrNull()
                                            ?: 0.0,
                                        mainUnit = state.data?.mainUnit,
                                        mainUnitGuid = state.data?.mainUnitGuid?.toDoubleOrNull()
                                            ?: 0.0,
                                        opQty = state.data?.opQty?.toDoubleOrNull() ?: 0.0,
                                        opAmount = state.data?.opAmount?.toDoubleOrNull() ?: 0.0,
                                        taxCategoryName = state.data?.taxCategoryName,
                                        taxCategoryGuid = state.data?.taxCategoryGuid?.toDoubleOrNull()
                                            ?: 0.0,
                                        salePrice = state.data?.salePrice?.toDoubleOrNull() ?: 0.0,
                                        purchPrice = state.data?.purchPrice?.toDoubleOrNull()
                                            ?: 0.0,
                                        mrp = state.data?.mrp?.toDoubleOrNull() ?: 0.0,
                                        minSalePrice = state.data?.minSalePrice?.toDoubleOrNull()
                                            ?: 0.0,
                                        selfValPrice = state.data?.selfValPrice?.toDoubleOrNull()
                                            ?: 0.0,
                                        saleDiscount = state.data?.saleDiscount?.toDoubleOrNull()
                                            ?: 0.0,
                                        purchDiscount = state.data?.purchPrice?.toDoubleOrNull()
                                            ?: 0.0,
                                        product_guid = state.data?.productGuid.toString(),
                                        altUnit = state.data?.altUnit,
                                        conFactor = state.data?.conFactor ?: 1.0,
                                        conType = if (state.data?.conType == CON_TYPE_OPTIONS[0]) 1.0 else 2.0
                                    )
                                }
                                showResultDialog = true

                            } else {
                                showDuplicateDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Item", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(8.dp))

                // ── Cancel ────────────────────────────────────────────────
                OutlinedButton(
                    onClick = { nav.pop() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(46.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}