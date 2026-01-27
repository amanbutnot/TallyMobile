package org.prime.easykarobar.ui.screen.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.DistributorViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.DistributorRequest
import org.prime.easykarobar.ui.screen.transactions.SelectLedgerRow
import org.prime.easykarobar.ui.screen.transactions.TransactionLedgerBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters

data class CreateDistributorScreen(
    val isEdit: Boolean = false,
    val distributor: DistributorRequest? = null
) :
    Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var name by remember { mutableStateOf("") }
        var number by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var selectedAccount by remember { mutableStateOf("") }
        var selectedName by remember { mutableStateOf("") }
        var selectedNumber by remember { mutableStateOf("") }
        var selectedGUID by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf("active") }
        var showBottomSheet by remember { mutableStateOf(false) }
        var showSuccessDialog by remember { mutableStateOf(false) }
        val db = DatabaseHolder.instance
        val list = getLedgerMasters(db)

        val viewModel: DistributorViewModel = viewModel { DistributorViewModel() }
        val state by viewModel.dataState
        val nav = LocalNavigator.currentOrThrow


        if (isEdit && distributor != null) {
            selectedAccount = distributor.ledger_name
            selectedGUID = distributor.ledger_guid
            name = distributor.distributor_name
            number = distributor.mobile_no
            selectedStatus = distributor.status.toString()
        }

        if (state.isLoading) {
            TallyLoadingDialog("Creating your distributor")
        }

        if (state.error != null) {
            TallyResultDialog(
                state.error ?: "Unexpected Error",
                onDone = { viewModel.clearError() },
                isSuccess = state.success,
                confirmText = "Try Again"
            )
        }
        if (showSuccessDialog) {
            TallyResultDialog(
                state.message ?: "Created",
                onDone = { showSuccessDialog = false },
                isSuccess = state.success,
                confirmText = "Done"
            )
        }
        TallyScaffold(
            title = if (isEdit) "Edit Distributor" else "Create Distributor",
            onBack = { nav.pop() },
            showEditIcon = false,
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize().padding(paddingValues)
                    .background(colors.background),
                contentAlignment = Alignment.Center
            )
            {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Section
                        Icon(
                            if (isEdit) Icons.Default.Edit else Icons.Default.PersonAddAlt,
                            contentDescription = "App icon",
                            tint = colors.primary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = if (isEdit) "Edit Distributor" else "Create Distributor",
                            style = type.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        Spacer(Modifier.height(12.dp))



                        SelectLedgerRow(
                            selectedAccount = selectedAccount,
                            onShowBottomSheet = { showBottomSheet = true },
                            title = "Select Ledger",
                            enabled = true
                        )
                        Spacer(Modifier.height(16.dp))

                        TallyTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Name",
                            isPassword = false,
                            isNumber = false,
                            label = "Name",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        TallyTextField(
                            value = number,
                            onValueChange = { number = it },
                            placeholder = "Phone number",
                            isPassword = false,
                            isNumber = true,
                            label = "Phone",
                            modifier = Modifier.fillMaxWidth()
                        )


                        TransactionLedgerBottomSheet(
                            showBottomSheet = showBottomSheet,
                            list = list,
                            onSelected = {
                                it.let {
                                    selectedAccount = it.Name.toString()
                                    selectedGUID = it.GUID.toString()
                                    selectedName = it.Name.toString()
                                    selectedNumber = it.MobileNo.toString()
                                    name = selectedName
                                    number = selectedNumber
                                }
                            },
                            onDismiss = { showBottomSheet = false },
                            bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            title = "Ledger Name",
                        )
                        Spacer(Modifier.height(16.dp))

                        TallyTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Create password",
                            isPassword = true,
                            isNumber = false,
                            label = "Password",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        TallyTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Confirm password",
                            isPassword = true,
                            isNumber = false,
                            label = "Confirm Password",
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

                        val isFormValid = name.isNotEmpty() &&
                                number.isNotEmpty() &&
                                password.isNotEmpty() &&
                                confirmPassword.isNotEmpty() &&
                                selectedAccount.isNotEmpty() &&
                                selectedGUID.isNotEmpty() &&
                                password == confirmPassword

                        TallyButton(
                            label = if(isEdit) "Update Distributor" else "Create Distributor",
                            onClick = {
                                if(isEdit){
                                    viewModel.updateDistributor(
                                        DistributorRequest(
                                            distributor_id = distributor?.distributor_id,
                                            distributor_name = name,
                                            mobile_no = number,
                                            password = password,
                                            ledger_name = selectedAccount,
                                            ledger_guid = selectedGUID,
                                            status = selectedStatus
                                        )
                                    ) {
                                    nav.pop()
                                    }
                                }else{

                                    viewModel.createDistributor(
                                        DistributorRequest(
                                            distributor_name = name,
                                            mobile_no = number,
                                            password = password,
                                            ledger_name = selectedAccount,
                                            ledger_guid = selectedGUID,
                                            status = selectedStatus
                                        )
                                    ) {
                                        name = ""
                                        number = ""
                                        password = ""
                                        confirmPassword = ""
                                        selectedAccount = ""
                                        selectedGUID = ""
                                        showSuccessDialog = true
                                    }
                                }
                                // Handle sign up
                            },
                            backgroundColor = colors.primary,
                            contentColor = colors.onPrimary,
                            enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}