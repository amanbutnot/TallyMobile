package org.prime.tally.ui.screen.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import org.prime.tally.business.viewmodel.transactions.SingleEntryViewModel
import org.prime.tally.data.model.transactions.TranListRequest
import org.prime.tally.data.model.transactions.TranListResponse
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyDivider
import org.prime.tally.ui.shared.composables.TallyScaffold


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.ui.shared.globalShared.Tdate

data class SingleEntryListScreen(
    val startDate: String,
    val endDate: String,
    val vchType: Int,
    val name: String
) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        TallyScaffold(
            title = "$name List",
            showBottomBar = false,
            bottomBarContent = {},
            content = { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    val viewmodel: SingleEntryViewModel = viewModel { SingleEntryViewModel() }
                    val state by viewmodel.listState

                    LaunchedEffect(
                        Unit
                    ) {
                        viewmodel.listTrans(
                            tranListRequest = TranListRequest(
                                VchType = vchType,
                                StartDate = startDate,
                                EndDate = endDate
                            )
                        )
                    }

                    when {
                        state.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                TallyCircularLoader()
                            }
                        }

                        state.error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {


                                Text(
                                    state.error.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                            }
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                state.data?.let {
                                    itemsIndexed(it) { index, item ->
                                        ListItem(item) {
                                            nav.push(
                                                SingleEntryReceipt(
                                                    name = name,
                                                    vchType = vchType,
                                                    existingTransaction = item
                                                )
                                            )
                                        }
                                        if (index < state.data!!.lastIndex)
                                            TallyDivider()
                                    }
                                }
                            }
                        }
                    }

                }

            }
        )
    }
}


@Composable
private fun ListItem(listState: TranListResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row - Voucher No and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voucher Number
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Voucher",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = listState.VchNo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Tdate(listState.TranDate.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Narration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Narration",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = listState.Narration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${listState.D2}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

//@Composable
//private fun ListItem(listState: TranListResponse) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//    ) {
//
//
//            // Content Column
//            Column(
//                modifier = Modifier.padding(8.dp),
//                verticalArrangement = Arrangement.spacedBy(6.dp)
//            ) {
//                // Voucher Number
//                Text(
//                    text = "Vch No: " +listState.VchNo,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//                // Date
//                Text(
//                    text = "Date: " +listState.TranDate.take(10),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                // Narration
//                Text(
//                    text = "Narration: " +listState.Narration,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                // Amount
//                Text(
//                    text = "Amount: ${listState.D2}",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    }

