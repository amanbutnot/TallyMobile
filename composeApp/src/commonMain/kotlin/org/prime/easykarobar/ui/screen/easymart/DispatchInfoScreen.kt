package org.prime.easykarobar.ui.screen.easymart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.masters.AccountViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.home.Dashboard
import org.prime.easykarobar.ui.screen.masters.AccountModel
import org.prime.easykarobar.ui.screen.masters.INDIAN_STATES
import org.prime.easykarobar.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

data class DispatchInfoScreen(val mobile: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        val scope = rememberCoroutineScope()
        val accountViewModel: AccountViewModel = viewModel { AccountViewModel() }
        val dataState by accountViewModel.dataState

        val db = DatabaseHolder.instance

        var name by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var pincode by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var gst by remember { mutableStateOf("") }
        var mobileNumber by remember { mutableStateOf(mobile) }

        var showStateSheet by remember { mutableStateOf(false) }
        val stateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var nameError by remember { mutableStateOf(false) }
        var addressError by remember { mutableStateOf(false) }
        var mobileError by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }

        TallyScaffold(
            title = "Sign Up Information",
            showNavigationIcon = false,
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Provide Sign Up Details",
                        style = type.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    Text(
                        text = "Please enter your information for orders.",
                        style = type.bodyMedium,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TallyTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        placeholder = "Enter your Name",
                        isPassword = false,
                        isNumber = false,
                        label = "Name *",
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nameError) {
                        Text("Name is mandatory", color = colors.error, style = type.labelSmall)
                    }

                    TallyTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            mobileError = false
                        },
                        placeholder = "Enter Mobile Number",
                        isPassword = false,
                        isNumber = true,
                        label = "Mobile *",
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mobileError) {
                        Text("Mobile is mandatory", color = colors.error, style = type.labelSmall)
                    }

                    TallyTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = false
                        },
                        placeholder = "Enter full address",
                        isPassword = false,
                        isNumber = false,
                        label = "Address *",
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (addressError) {
                        Text("Address is mandatory", color = colors.error, style = type.labelSmall)
                    }

                    TallyTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        placeholder = "Enter Pincode",
                        isPassword = false,
                        isNumber = true,
                        label = "Pincode",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // State Selection with Searchable Bottom Sheet
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "State",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showStateSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
                                    text = state.ifEmpty { "Select State" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (state.isEmpty())
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
                    }

                    TransactionOneBottomSheet(
                        showBottomSheet = showStateSheet,
                        list = INDIAN_STATES,
                        onSelected = {
                            state = it
                            showStateSheet = false
                        },
                        onDismiss = { showStateSheet = false },
                        bottomSheetState = stateSheetState,
                        title = "Select State"
                    )

                    TallyTextField(
                        value = gst,
                        onValueChange = { gst = it },
                        placeholder = "Enter GSTIN (Optional)",
                        isPassword = false,
                        isNumber = false,
                        label = "GSTIN",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TallyButton(
                        label = "Save & Continue",
                        onClick = {
                            if (name.isBlank()) nameError = true
                            if (mobileNumber.isBlank()) mobileError = true
                            if (address.isBlank()) addressError = true

                            if (!nameError && !mobileError && !addressError) {
                                SharedPrefs.DispatchInfo.save(
                                    name = name,
                                    mobile = mobileNumber,
                                    address = address,
                                    pincode = pincode,
                                    state = state
                                )

                                val sundryDebtorGroup = db.ledgerGroupMasterQueries.getChildrenByGroupName(
                                    GroupName = listOf("Sundry Debtors"),
                                    GUID = listOf("")
                                ).executeAsOneOrNull() ?: db.ledgerGroupMasterQueries.simpleSelectAll().executeAsList().firstOrNull { it.Name == "Sundry Debtors" }

                                val account = AccountModel(
                                    name = name.trim(),
                                    alias = "",
                                    printName = name.trim(),
                                    parentGroupName = sundryDebtorGroup?.Name ?: "Sundry Debtors",
                                    parentGroupGuid = sundryDebtorGroup?.GUID ?: "",
                                    openingBalance = "0.00",
                                    drCr = "Dr",
                                    gstNo = gst.trim(),
                                    itPan = "",
                                    addressLine1 = address.trim(),
                                    addressLine2 = "",
                                    addressLine3 = "",
                                    addressLine4 = "",
                                    country = "India",
                                    state = state.trim(),
                                    pincode = pincode.trim(),
                                    station = "",
                                    mobileNo = mobileNumber.trim(),
                                    email = "",
                                    whatsappNo = mobileNumber.trim(),
                                    maintainBillByBill = 0,
                                    saleCreditDays = "",
                                    purchaseCreditDays = "",
                                )

                                scope.launch {
                                    accountViewModel.createAccount(account) {
                                        showResultDialog = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (dataState.isLoading) {
                        TallyLoadingDialog("Creating your account")
                    }

                    if (showResultDialog) {
                        TallyResultDialog(
                            message = dataState.message ?: "Error Occurred",
                            onDone = {
                                showResultDialog = false
                                nav.replaceAll(Dashboard)
                            },
                            isSuccess = dataState.success,
                            confirmText = "OK"
                        )
                    }
                }
            }
        )
    }
}
