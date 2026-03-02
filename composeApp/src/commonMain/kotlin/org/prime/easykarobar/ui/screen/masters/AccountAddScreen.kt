package org.prime.easykarobar.ui.screen.masters

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.masters.AccountViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.transactions.TransactionBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.agrpGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterAGRPGroups


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
//  TallyDropdownWithCustom — standard list + "Other (type manually)" fallback
// ─────────────────────────────────────────────────────────────────────────────
private const val CUSTOM_OPTION = "Other (type manually)…"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TallyDropdownWithCustom(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    isError: Boolean = false
) {
    val isCustom = selected.isNotEmpty() && selected !in options
    var expanded by remember { mutableStateOf(false) }
    var customText by remember(isCustom) { mutableStateOf(if (isCustom) selected else "") }
    var showCustom by remember(isCustom) { mutableStateOf(isCustom) }

    val displayValue = when {
        showCustom -> CUSTOM_OPTION
        selected.isEmpty() -> ""
        else -> selected
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayValue,
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
                        onClick = {
                            showCustom = false
                            customText = ""
                            onSelect(opt)
                            expanded = false
                        },
                        leadingIcon = if (opt == selected && !showCustom) ({
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }) else null
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            CUSTOM_OPTION,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    onClick = {
                        showCustom = true
                        onSelect("")
                        expanded = false
                    }
                )
            }
        }

        if (showCustom) {
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it; onSelect(it) },
                label = { Text("Enter $label", style = MaterialTheme.typography.bodySmall) },
                placeholder = {
                    Text("Type your $label…", style = MaterialTheme.typography.bodySmall)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TallyDropdown — simple fixed list (Dr / Cr)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TallyDropdown(
    label: String,
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
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
            modifier = modifier
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
//  TallyToggle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TallyToggle(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (value) "Enabled" else "Disabled",
                style = MaterialTheme.typography.bodySmall,
                color = if (value)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    isError: Boolean = false, modifier: Modifier = Modifier,
    errorMessage: String = "Required"
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
//  Validation Dialog
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
//  Static data
// ─────────────────────────────────────────────────────────────────────────────
private val COUNTRIES = listOf(
    "India", "USA", "UK", "UAE", "Singapore", "Australia", "Canada"
)

private val INDIAN_STATES = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
    "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
    "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
    "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
    "Delhi", "Puducherry", "Jammu & Kashmir", "Ladakh"
)

private val DR_CR_OPTIONS = listOf("Dr", "Cr")

// ─────────────────────────────────────────────────────────────────────────────
//  AccountAddScreen
// ─────────────────────────────────────────────────────────────────────────────
object AccountAddScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val scroll = rememberScrollState()
        val scope = rememberCoroutineScope()
        val viewmodel: AccountViewModel = viewModel { AccountViewModel() }
        val dataState by viewmodel.dataState

        // ── Form state ─────────────────────────────────────────────────────
        var name by remember { mutableStateOf("") }
        var alias by remember { mutableStateOf("") }
        var printName by remember { mutableStateOf("") }

        // parentGroup = Pair(LedgerGroupMaster.Name, LedgerGroupMaster.GUID)
        var parentGroup by remember { mutableStateOf("") }
        var parentGroupGUID by remember { mutableStateOf("") }
        var showParentGroupSheet by remember { mutableStateOf(false) }
        var showDuplicateDialog by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }
        val parentGroupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var openingBalance by remember { mutableStateOf("") }
        var drCr by remember { mutableStateOf(DR_CR_OPTIONS[0]) }
        var gstNo by remember { mutableStateOf("") }
        var address1 by remember { mutableStateOf("") }
        var address2 by remember { mutableStateOf("") }
        var address3 by remember { mutableStateOf("") }
        var address4 by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("India") }
        var state by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var whatsapp by remember { mutableStateOf("") }
        var itPan by remember { mutableStateOf("") }
        var maintainBillByBill by remember { mutableStateOf(false) }
        var saleCreditDays by remember { mutableStateOf("") }
        var purchaseCreditDays by remember { mutableStateOf("") }
        var pincode by remember { mutableStateOf("") }
        var station by remember { mutableStateOf("") }

        // ── Validation state ───────────────────────────────────────────────
        var showDialog by remember { mutableStateOf(false) }
        var dialogMissing by remember { mutableStateOf(listOf<String>()) }
        var dialogExtraMsg by remember { mutableStateOf<String?>(null) }
        var attempted by remember { mutableStateOf(false) }

        val nameError = attempted && name.isBlank()
        val aliasEqName = alias.isNotBlank() && alias.trim().equals(name.trim(), ignoreCase = true)
        val aliasError = attempted && aliasEqName

        val countryError = attempted && country.isBlank()
        val stateError = attempted && state.isBlank()

        fun validate(): Boolean {
            attempted = true
            val missing = mutableListOf<String>()
            if (name.isBlank()) missing += "Name"
            if (parentGroup.isBlank()) missing += "Parent Group"
            if (country.isBlank()) missing += "Country"
            if (state.isBlank()) missing += "State"

            if (aliasEqName) {
                dialogExtraMsg = "Name and Alias cannot be the same."
                dialogMissing = missing
                showDialog = true
                return false
            }

            if (missing.isNotEmpty()) {
                dialogExtraMsg = null
                dialogMissing = missing
                showDialog = true
                return false
            }
            return true
        }

        // ── Validation dialog ──────────────────────────────────────────────
        if (showDialog) {
            RequiredFieldsDialog(
                missingFields = dialogMissing,
                extraMessage = dialogExtraMsg,
                onDismiss = { showDialog = false }
            )
        }


        // ── Scaffold ───────────────────────────────────────────────────────
        TallyScaffold(
            title = "Add Account",
            showEditIcon = false,
            onEditClick = {},
            onBack = { nav.pop() }
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
                SectionHeader("Basic Information", Icons.Default.Person)
                FormCard {
                    FormField(
                        label = "Name *",
                        value = name,
                        onChange = { v ->
                            name = v
                            // Auto-mirror into printName until user manually edits it
                            if (printName.isEmpty() || printName == name.dropLast(1)) {
                                printName = v
                            }
                        },
                        placeholder = "Enter account name",
                        isError = nameError,
                        errorMessage = "Name is required"
                    )
                    FormField(
                        label = "Alias",
                        value = alias,
                        onChange = { alias = it },
                        placeholder = "Short alias",
                        isError = aliasError,
                        errorMessage = "Alias cannot be the same as Name"
                    )
                    FormField(
                        label = "Print Name",
                        value = printName,
                        onChange = { printName = it },
                        placeholder = "Name for printing",
                        imeAction = ImeAction.Done
                    )
                }
                val db = DatabaseHolder.instance
                val list = db.ledgerGroupMasterQueries.selectAll(
                    filterGroup = filterAGRPGroups(),
                    groupCodes = agrpGroupCodes()
                ).executeAsList()
                var showBottomSheet by remember { mutableStateOf(false) }
                val Bstate = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // ── 2. Group & Ledger ─────────────────────────────────────
                SectionHeader("Group & Ledger", Icons.Default.AccountTree)
                FormCard {

                    // Read-only trigger field — tapping opens TransactionBottomSheet
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    {

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showBottomSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = parentGroup.ifEmpty { "Choose an account" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (parentGroup.isEmpty())
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        TransactionBottomSheet(
                            showBottomSheet = showBottomSheet,
                            list = list.map { Pair(it.Name.toString(), it.GUID.toString()) },
                            onSelected = {
                                it.let {
                                    parentGroup = it.first
                                    parentGroupGUID = it.second
                                }
                            },
                            onDismiss = { showBottomSheet = false },
                            bottomSheetState = Bstate, title = "Select Account Group"
                        )

                        TallyTextField(
                            label = "Op Bal",
                            value = openingBalance,
                            onValueChange = {
                                openingBalance = it
                            },
                            placeholder = "0.00",
                            isNumber = true, isPassword = false,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )

                        TallyDropdown(
                            "Dr / Cr",
                            DR_CR_OPTIONS,
                            drCr,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
                        ) { drCr = it }


                    }

                }

                // ── 3. Tax Information ────────────────────────────────────
                SectionHeader("Tax Information", Icons.AutoMirrored.Filled.ReceiptLong)
                FormCard {
                    FormField("GST Number", gstNo, { gstNo = it }, "22AAAAA0000A1Z5")
                    FormField("IT PAN", itPan, { itPan = it }, "AAAAA9999A")
                }

                // ── 4. Address Details ────────────────────────────────────
                SectionHeader("Address Details", Icons.Default.LocationOn)
                FormCard {
                    FormField("Address Line 1", address1, { address1 = it }, "Street / Building")
                    FormField("Address Line 2", address2, { address2 = it }, "Area / Locality")
                    FormField("Address Line 3", address3, { address3 = it }, "City")
                    FormField("Address Line 4", address4, { address4 = it }, "Landmark (optional)")

                    TallyDropdownWithCustom(
                        label = "Country *",
                        options = COUNTRIES,
                        selected = country,
                        onSelect = { country = it; state = "" },
                        isError = countryError
                    )
                    TallyDropdownWithCustom(
                        label = "State *",
                        options = INDIAN_STATES,
                        selected = state,
                        onSelect = { state = it },
                        isError = stateError
                    )

                    FormField("Pincode", pincode, { pincode = it }, "400001", isNumber = true)
                    FormField("Station", station, { station = it }, "Station name")
                }

                // ── 5. Contact Information ────────────────────────────────
                SectionHeader("Contact Information", Icons.Default.ContactPhone)
                FormCard {
                    FormField(
                        "Mobile No.",
                        mobile,
                        { mobile = it },
                        "+91 XXXXX XXXXX",
                        isNumber = true
                    )
                    FormField(
                        "WhatsApp No.",
                        whatsapp,
                        { whatsapp = it },
                        "+91 XXXXX XXXXX",
                        isNumber = true
                    )
                    FormField("Email", email, { email = it }, "example@domain.com")
                }

                // ── 6. Credit Settings ────────────────────────────────────
                SectionHeader("Credit Settings", Icons.Default.CreditCard)
                FormCard {
                    TallyToggle("Maintain Bill by Bill", maintainBillByBill) {
                        maintainBillByBill = it
                    }


                    AnimatedVisibility(visible = maintainBillByBill, content = {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FormField(
                                "Sale Credit Days", saleCreditDays, { saleCreditDays = it },
                                "e.g. 30", isNumber = true
                            )
                            FormField(
                                "Purchase Credit Days",
                                purchaseCreditDays,
                                { purchaseCreditDays = it },
                                "e.g. 45",
                                isNumber = true,
                                imeAction = ImeAction.Done
                            )
                        }
                    })

                }

                Spacer(Modifier.height(24.dp))

                // ── Save ──────────────────────────────────────────────────
                Button(
                    onClick = {
                        if (validate()) {
                            val account = AccountModel(
                                name = name.trim(),
                                alias = alias.trim(),
                                printName = printName.trim().ifBlank { name.trim() },
                                parentGroupName = parentGroup,   // LedgerGroupMaster.Name
                                parentGroupGuid = parentGroupGUID,  // LedgerGroupMaster.GUID (FK)
                                openingBalance = openingBalance.trim(),
                                drCr = drCr,
                                gstNo = gstNo.trim(),
                                itPan = itPan.trim(),
                                addressLine1 = address1.trim(),
                                addressLine2 = address2.trim(),
                                addressLine3 = address3.trim(),
                                addressLine4 = address4.trim(),
                                country = country.trim(),
                                state = state.trim(),
                                pincode = pincode.trim(),
                                station = station.trim(),
                                mobileNo = mobile.trim(),
                                email = email.trim(),
                                whatsappNo = whatsapp.trim(),
                                maintainBillByBill = if (maintainBillByBill) 1 else 0,
                                saleCreditDays = if (maintainBillByBill) saleCreditDays.trim() else "",
                                purchaseCreditDays = if (maintainBillByBill) purchaseCreditDays.trim() else "",
                            )

                            println(account)
                            scope.launch {
                                val existing = db.ledgerMasterQueries.existingAccounts(
                                    name1 = name.trim(),
                                    alias1 = name.trim(),
                                    name2 = if (alias == "") name.trim() else alias.trim(),
                                    alias2 = if (alias == "") name.trim() else alias.trim()
                                ).executeAsList()
                                if (existing.isEmpty()) {
                                    viewmodel.createAccount(account) {
                                        db.ledgerMasterQueries.insertLedger(
                                            code = dataState.data?.ledger_guid?.toLong(),
                                            name = dataState.data?.name?.trim(),
                                            alias = dataState.data?.alias?.trim(),
                                            groupName = dataState.data?.parentGroupName,
                                            groupCode = dataState.data?.parentGroupGuid?.toDoubleOrNull()
                                                ?: 0.0,
                                            opBal = dataState.data?.openingBalance?.toDoubleOrNull()
                                                ?: 0.0,
                                            address1 = dataState.data?.addressLine1,
                                            address2 = dataState.data?.addressLine2,
                                            address3 = dataState.data?.addressLine3,
                                            address4 = dataState.data?.addressLine4,
                                            country = dataState.data?.country,
                                            state = dataState.data?.state,
                                            gstin = dataState.data?.gstNo,
                                            email = dataState.data?.email,
                                            mobileNo = dataState.data?.mobileNo,
                                            alterId = 0,
                                            guid = dataState.data?.ledger_guid.toString(),
                                            panNo = dataState.data?.itPan
                                        )
                                        showResultDialog = true
                                    }
                                } else {
                                    showDuplicateDialog = true
                                }
                            }
                            //TODO: Send Request
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
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save Account", style = MaterialTheme.typography.labelLarge)
                }
                if (dataState.isLoading) {
                    TallyLoadingDialog("Creating your account")
                }
                if (showResultDialog) {
                    TallyResultDialog(
                        message = dataState.message ?: "Error Occurred",
                        onDone = { nav.pop() },
                        isSuccess = dataState.success,
                        confirmText = "OK"
                    )
                }
                if (showDuplicateDialog) {
                    TallyResultDialog(
                        message = "Account with this name or alias already exists",
                        onDone = { showDuplicateDialog = false },
                        isSuccess = dataState.success,
                        confirmText = "OK"
                    )
                }

            }
        }
    }
}