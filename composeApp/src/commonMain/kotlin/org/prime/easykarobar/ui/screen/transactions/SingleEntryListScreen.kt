package org.prime.easykarobar.ui.screen.transactions


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.transactions.SingleEntryViewModel
import org.prime.easykarobar.data.model.transactions.TranListRequest
import org.prime.easykarobar.data.model.transactions.TranListResponse
import org.prime.easykarobar.ui.printing.EntryTypesHtml
import org.prime.easykarobar.ui.printing.entryTypesHtml
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyDivider
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getNameFromGUID
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction

data class SingleEntryListScreen(
    val startDate: String, val endDate: String, val vchType: Int, val name: String
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
                    val scope = rememberCoroutineScope()
                    val state by viewmodel.listState

                    LaunchedEffect(
                        Unit
                    ) {
                        viewmodel.listTrans(
                            tranListRequest = TranListRequest(
                                VchType = vchType, StartDate = startDate, EndDate = endDate
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
                                        ListItem(
                                            item,
                                            onDownload = { data ->
                                                scope.launch {
                                                    handlePdfAction(
                                                        fileName = item?.billed_vchno
                                                            ?.takeIf { it.isNotEmpty() }
                                                            ?.replace("/", "_")
                                                            ?: CompanyName(),
                                                        htmlContent = entryTypesHtml(
                                                            voucherNo = item?.billed_vchno
                                                                ?.takeIf { it.isNotEmpty() }
                                                                ?: item.VchNo,
                                                            date = item.TranDate,
                                                            data = EntryTypesHtml(
                                                                ledger = item.C1,
                                                                settlement = item.C2,
                                                                amount = item.D2
                                                            ),
                                                            title = name
                                                        ),
                                                        action = PdfAction.Download,
                                                        onLoadingChange = {}
                                                    )
                                                }
                                            },
                                            onShare = { data ->
                                                scope.launch {
                                                    handlePdfAction(
                                                        fileName = item?.billed_vchno
                                                            ?.takeIf { it.isNotEmpty() }
                                                            ?.replace("/", "_")
                                                            ?: CompanyName(),
                                                        htmlContent = entryTypesHtml(
                                                            voucherNo = item.billed_vchno
                                                                .takeIf { it.isNotEmpty() }
                                                                ?: item.VchNo,
                                                            date = item.TranDate,
                                                            data = EntryTypesHtml(
                                                                ledger = item.C1,
                                                                settlement = item.C2,
                                                                amount = item.D2
                                                            ),
                                                            title = name
                                                        ),
                                                        action = PdfAction.Share,
                                                        onLoadingChange = {}
                                                    )
                                                }
                                            }) {
                                            nav.push(
                                                SingleEntryReceipt(
                                                    name = name,
                                                    vchType = vchType,
                                                    existingTransaction = item,
                                                    showPdc = vchType == 19 || vchType == 14
                                                )
                                            )
                                        }
                                        if (index < state.data!!.lastIndex) TallyDivider()
                                    }
                                }
                            }
                        }
                    }

                }
            })
    }
}


@Composable
private fun ListItem(
    listState: TranListResponse,
    onDownload: (TranListResponse) -> Unit,
    onShare: (TranListResponse) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
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

            // Party Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Party Name:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getNameFromGUID(listState.CM1),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settlement Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Settlement Mode:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getNameFromGUID(listState.CM2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download Button
                OutlinedButton(
                    onClick = { onDownload(listState) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Share Button
                Button(
                    onClick = { onShare(listState) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}