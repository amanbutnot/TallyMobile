package org.prime.easykarobar.ui.screen.transactions.sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.prime.easykarobar.ui.screen.masters.INDIAN_STATES
import org.prime.easykarobar.ui.screen.masters.TallyDropdownWithCustom
import org.prime.easykarobar.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingCard(
    showShippingDetails: Boolean,
    onShowChange: () -> Unit,

    billingShipping: Boolean,
    onBillingShippingChange: (Boolean) -> Unit,

    partyName: String,
    onPartyNameChange: (String) -> Unit,

    address1: String,
    onAddressChange1: (String) -> Unit,
    address2: String,
    onAddressChange2: (String) -> Unit,
    address3: String,
    onAddressChange3: (String) -> Unit,
    address4: String,
    onAddressChange4: (String) -> Unit,

    state: String,
    onStateChange: (String) -> Unit,

    mobileNo: String,
    onMobileChange: (String) -> Unit,

    email: String,
    onEmailChange: (String) -> Unit,

    itPan: String,
    onPanChange: (String) -> Unit,

    gstIn: String,
    onGstChange: (String) -> Unit,

    selectedBilling: String,
    onbillingShippingSelected: (String) -> Unit,
    adharNo: String,
    onAdharChange: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "Billing/Shipping Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(onClick = onShowChange) {

            Icon(
                imageVector =
                    if (showShippingDetails)
                        Icons.Default.RemoveCircleOutline
                    else
                        Icons.Default.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (showShippingDetails) "Remove" else "Add",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    AnimatedVisibility(
        visible = showShippingDetails,
        enter = fadeIn(animationSpec = tween(300)) +
                expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) +
                shrinkVertically(animationSpec = tween(300))
    ) {

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),

            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),

            elevation = CardDefaults.elevatedCardElevation(2.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Billing/Shipping Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                var showBillingSheet by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onBillingShippingChange(false) },

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = !billingShipping,
                        onClick = { onBillingShippingChange(false) }
                    )

                    Text("As Per Party Master")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            onBillingShippingChange(true)
                            showBillingSheet = true
                        },

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = billingShipping,
                        onClick = {
                            onBillingShippingChange(true)
                            showBillingSheet = true
                        }
                    )

                    Column {
                        Text("Billing/Shipping Details")
                        if (billingShipping && selectedBilling.isNotEmpty()) {
                            Text(
                                text = selectedBilling,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                TransactionOneBottomSheet(
                    showBottomSheet = showBillingSheet,
                    list = listOf(
                        "Un-Registered",
                        "Registered",
                        "Composition",
                        "Govt. Body",
                        "UIN Holder"
                    ),
                    onSelected = {
                        onbillingShippingSelected(it)
                        showBillingSheet = false
                    },
                    onDismiss = { showBillingSheet = false },
                    bottomSheetState = rememberModalBottomSheetState(),
                    title = "Billing Shipping Details",
                )


                TallyTextField(
                    value = partyName,
                    onValueChange = onPartyNameChange,
                    label = "Party Name",
                    placeholder = "Enter party name",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = address1,
                    onValueChange = onAddressChange1,
                    label = "Address 1",
                    placeholder = "Enter Address",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = address2,
                    onValueChange = onAddressChange2,
                    label = "Address 2",
                    placeholder = "Enter Address",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = address3,
                    onValueChange = onAddressChange3,
                    label = "Address 3",
                    placeholder = "Enter Address",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = address4,
                    onValueChange = onAddressChange4,
                    label = "Address 4",
                    placeholder = "Enter Address",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyDropdownWithCustom(
                    label = "State *",
                    options = INDIAN_STATES,
                    selected = state,
                    onSelect = onStateChange ,
                    isError = false
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TallyTextField(
                        value = mobileNo,
                        onValueChange = onMobileChange,
                        label = "Mobile No.",
                        placeholder = "Enter Mobile no.",
                        isPassword = false,
                        isNumber = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                TallyTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "Enter Email",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = itPan,
                    onValueChange = onPanChange,
                    label = "IT Pan",
                    placeholder = "Enter IT Pan",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = adharNo,
                    onValueChange = onAdharChange,
                    label = "Adhar No.",
                    placeholder = "Enter Adhar Number",
                    isPassword = false,
                    isNumber = false,
                    modifier = Modifier.fillMaxWidth()
                )

                TallyTextField(
                    value = gstIn,
                    onValueChange = onGstChange,
                    label = "GSTIN",
                    placeholder = "Enter GSTIN/UIN",
                    isPassword = false,
                    isNumber = false, imeAction = ImeAction.Done,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionalFieldCard(
    showOptionalField: Boolean,
    onShowChange: () -> Unit,

    optionalFields: List<String>,
    onFieldChange: (index: Int, value: String) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "Optional Fields",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(onClick = onShowChange) {

            Icon(
                imageVector =
                    if (showOptionalField)
                        Icons.Default.RemoveCircleOutline
                    else
                        Icons.Default.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (showOptionalField) "Remove" else "Add",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    AnimatedVisibility(
        visible = showOptionalField,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
    ) {

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.elevatedCardElevation(2.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Optional Fields",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                optionalFields.forEachIndexed { index, value ->
                    TallyTextField(
                        value = value,
                        onValueChange = { onFieldChange(index, it) },
                        label = "Optional Field ${index + 1}",
                        placeholder = "Optional Field ${index + 1}",
                        isPassword = false,
                        isNumber = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
