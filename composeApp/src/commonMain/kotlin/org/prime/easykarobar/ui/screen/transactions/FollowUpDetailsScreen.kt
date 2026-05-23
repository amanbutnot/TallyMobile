package org.prime.easykarobar.ui.screen.transactions

import CurrentDate
import TallyDatePickerRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.FollowupViewmodel
import org.prime.easykarobar.data.model.PostFollowup
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold

data class FollowUpDetailsScreen(
    val accountName: String,
    val actCode: String,
    val ledgerBalance: String,
    val outstandingBalance: String
) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        var date by remember { mutableStateOf(CurrentDate()) }
        var remarks by remember { mutableStateOf("") }
        val viewmodel: FollowupViewmodel = viewModel { FollowupViewmodel() }
        val state by viewmodel.dataState

        if (state.isLoading) {
            TallyLoadingDialog("Posting followup")
        }
        if (state.success) {
            TallyResultDialog(
                message = state.message ?: "unexpected error",
                onDone = { nav.pop() },
                isSuccess = state.success,
                confirmText = "Ok"
            )
        }
        TallyScaffold(title = "Follow Up Details", onBack = { nav.pop() }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Account: $accountName",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BalanceCard(
                        modifier = Modifier.weight(1f),
                        label = "Ledger",
                        amount = ledgerBalance
                    )
                    BalanceCard(
                        modifier = Modifier.weight(1f),
                        label = "Outstanding",
                        amount = outstandingBalance
                    )
                }

                TallyDatePickerRow(
                    label = "Follow Up Date",
                    selectedDate = date,
                    onDateSelected = { date = it },
                    defaultDate = CurrentDate()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Remarks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("Enter follow up remarks...") },
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TallyButton(
                    label = "Send",
                    onClick = {
                        viewmodel.postFollowup(
                            PostFollowup(
                                ActCode = actCode,
                                nextfollowup = date,
                                status = "Pending",
                                Remarks = remarks
                            )
                        ) {

                        }

                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun BalanceCard(modifier: Modifier, label: String, amount: String) {
        Column(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    MaterialTheme.shapes.medium
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
