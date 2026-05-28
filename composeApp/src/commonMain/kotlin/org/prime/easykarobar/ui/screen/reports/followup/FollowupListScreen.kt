package org.prime.easykarobar.ui.screen.reports.followup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.business.viewmodel.FollowupViewmodel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.FollowupData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.Tdate

data class FollowupListScreen(val accountName: String, val actCode: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewmodel: FollowupViewmodel = viewModel { FollowupViewmodel() }
        val state by viewmodel.listState
        val db = DatabaseHolder.instance

        LaunchedEffect(Unit) {
            viewmodel.getFollowupList()
        }

        val filteredList = remember(state.data, actCode) {
            val list = state.data ?: emptyList()
            if (actCode.isNotEmpty()) {
                list.filter { it.ActCode == actCode }
            } else {
                list
            }
        }

        TallyScaffold(
            title = if (accountName.isNotEmpty()) "$accountName Followups" else "Followup List",
            onBack = { nav.pop() }
        ) { paddingValues ->
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error ?: "Error fetching followups")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No followups found")
                                }
                            }
                        } else {
                            items(filteredList) { item ->
                                FollowupItem(item, db)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FollowupItem(item: FollowupData, db: TallyDatabase) {
        val accountNameFromDb = remember(item.ActCode) {
            db.ledgerMasterQueries.selectNameFromGuid(item.ActCode).executeAsOneOrNull()?.Name
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = accountNameFromDb ?: "Account: ${item.ActCode}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date: ${Tdate(item.followupdate.split(" ").first())}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Status: ${item.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.status == "Pending") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Next Followup: ${Tdate(item.nextfollowup)}", style = MaterialTheme.typography.bodyMedium)
                if (!item.remarks.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Remarks: ${item.remarks}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
