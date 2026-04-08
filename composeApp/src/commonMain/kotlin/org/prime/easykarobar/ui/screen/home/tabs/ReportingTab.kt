package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person4
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.ui.screen.home.Dashboard
import org.prime.easykarobar.ui.screen.reports.godown.GodownClosingStockListScreen
import org.prime.easykarobar.ui.screen.reports.godown.MCBatchNoReport
import org.prime.easykarobar.ui.screen.reports.godown.MCSerialNoReport
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingSelectScreen
import org.prime.easykarobar.ui.screen.reports.pendingOrder.OrderReportSelectScreen
import org.prime.easykarobar.ui.screen.reports.productReport.BatchNoReport
import org.prime.easykarobar.ui.screen.reports.productReport.ProductReportScreen
import org.prime.easykarobar.ui.screen.reports.productReport.SerialNumberReport
import org.prime.easykarobar.ui.screen.reports.registers.RegisterSelectScreen
import org.prime.easykarobar.ui.screen.reports.salesman.SalesmanTargetFilterScreen
import org.prime.easykarobar.ui.screen.reports.stock.BatchNumberStockReport
import org.prime.easykarobar.ui.screen.reports.stock.ParameterReportScreen
import org.prime.easykarobar.ui.screen.reports.stock.SerialNumberStockReport
import org.prime.easykarobar.ui.screen.reports.stock.StockReportScreen
import org.prime.easykarobar.ui.screen.reports.trialBalance.TrialBalanceScreen
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog

object ReportingTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Assessment)
            return TabOptions(index = 2u, title = "Reporting", icon = icon)
        }

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {

        val tabNav = LocalTabNavigator.current
        BackHandler(true) {
            tabNav.current = HomeTab
        }
        var showDeniedDialog by remember { mutableStateOf(false) }

        if (showDeniedDialog) {
            PermissionDeniedDialog { showDeniedDialog = false }
        }

        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow.parent

        val reportGroups = listOf(
            ReportGroup(
                "Accounting",
                listOf(Report.Ledger, Report.Outstanding, Report.TrialBalance, Report.Registers)
            ),
            ReportGroup(
                "Inventory",
                listOf(
                    Report.GoDownWiseClosingStock,
                    Report.Order,
                    Report.SalesmanWise,
                    Report.SalesmanGroupWise,
                    Report.ProductStock
                )
            ),
            ReportGroup(
                "Stock",
                listOf(Report.StockReport)
            ),
            ReportGroup("Parameter", listOf(Report.ParameterReport)),
            ReportGroup("Batch", listOf(Report.BatchNumberWise, Report.BatchNumberReport, Report.MCBatchNumberReport)),
            ReportGroup(
                "Serial Number",
                listOf(Report.SerialNumberWise, Report.SerialNumberReport, Report.MCSerialNumberReport)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()  .padding(20.dp),
            ) {
                Text(
                    text = tabNav.current.options.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "View your reports",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
            }

            // ── Report grid ─────────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                reportGroups.forEach { group ->
                    // Section header
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .background(
                                        color = colors.primary,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = group.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 0.8.dp,
                                color = colors.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    items(group.reports) { report ->
                        ReportButton(
                            icon = report.icon,
                            title = report.title,
                            onClick = {
                                when (report) {
                                    Report.Ledger -> {
                                        salesmanPermission(
                                            "D7",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(LedgerReportFilterScreen()) }
                                        )
                                    }

                                    Report.Outstanding -> nav?.push(OutstandingSelectScreen)
                                    Report.PendingOrders -> nav?.push(Dashboard)
                                    Report.Quotations -> nav?.push(Dashboard)
                                    Report.ProductStock -> nav?.push(
                                        ProductReportScreen(null, isMain = true)
                                    )

                                    Report.Registers -> nav?.push(RegisterSelectScreen)
                                    Report.StockReport -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(StockReportScreen) }
                                        )
                                    }

                                    Report.SerialNumberReport -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SerialNumberStockReport(true, "0", true)
                                                )
                                            }
                                        )
                                    }

                                    Report.BatchNumberReport -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    BatchNumberStockReport(true, "0", true)
                                                )
                                            }
                                        )
                                    }

                                    Report.SerialNumberWise -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SerialNumberReport(
                                                        isMain = true,
                                                        isDirect = true,
                                                        godownCode = "0"
                                                    )
                                                )
                                            }
                                        )
                                    }

                                    Report.BatchNumberWise -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    BatchNoReport(
                                                        isMain = true,
                                                        isDirect = true,
                                                        godownCode = "0"
                                                    )
                                                )
                                            }
                                        )
                                    }

                                    Report.ParameterReport -> {
                                        salesmanPermission(
                                            "D11",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(ParameterReportScreen) }
                                        )
                                    }

                                    Report.TrialBalance -> {
                                        salesmanPermission(
                                            "D10",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(TrialBalanceScreen) }
                                        )
                                    }

                                    Report.GoDownWiseClosingStock -> {
                                        salesmanPermission(
                                            "D12",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(GodownClosingStockListScreen) }
                                        )
                                    }

                                    Report.MCSerialNumberReport -> {
                                        salesmanPermission(
                                            "D12",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(MCSerialNoReport) }
                                        )
                                    }

                                    Report.MCBatchNumberReport -> {
                                        salesmanPermission(
                                            "D12",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = { nav?.push(MCBatchNoReport) }
                                        )
                                    }

                                    Report.SalesmanWise -> nav?.push(SalesmanTargetFilterScreen(false))
                                    Report.SalesmanGroupWise -> nav?.push(SalesmanTargetFilterScreen(true))
                                    Report.Order -> nav?.push(OrderReportSelectScreen)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Data ────────────────────────────────────────────────────────────────────

private data class ReportGroup(val title: String, val reports: List<Report>)

// ── Report card ──────────────────────────────────────────────────────────────

@Composable
fun ReportButton(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .height(90.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon container with tinted background
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = colors.primary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = colors.primaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    }
}

// ── Report definitions (unchanged) ──────────────────────────────────────────

sealed class Report(val title: String, val icon: ImageVector) {

    object Ledger : Report("Ledger", Icons.Default.AccountBalance)
    object Outstanding : Report("Outstanding", Icons.Default.WarningAmber)
    object TrialBalance : Report("Trial Balance", Icons.Default.Balance)
    object Registers : Report("Registers", Icons.AutoMirrored.Default.ListAlt)
    object StockReport : Report("Stock Report", Icons.Default.Inventory2)
    object SerialNumberReport : Report("Item Summary", Icons.Default.Numbers)
    object BatchNumberReport : Report("Batch Summary", Icons.Default.Layers)
    object SerialNumberWise : Report("Item Summary", Icons.Default.Tag)
    object BatchNumberWise : Report("Item Summary", Icons.Default.ViewModule)
    object PendingOrders : Report("Pending Orders", Icons.Default.PendingActions)
    object Quotations : Report("Quotations", Icons.Default.RequestQuote)
    object GoDownWiseClosingStock : Report("Godown Stock", Icons.Default.Warehouse)
    object MCSerialNumberReport : Report("Godown Summary", Icons.Default.ConfirmationNumber)
    object MCBatchNumberReport : Report("Godown Summary", Icons.Default.Layers)
    object ProductStock : Report("Barcode Report", Icons.Default.QrCodeScanner)
    object ParameterReport : Report("Parameter Report", Icons.Default.Tune)
    object SalesmanWise : Report("Salesman Target", Icons.Default.Person)
    object SalesmanGroupWise : Report("Salesman Group Wise Target", Icons.Default.Person4)
    object Order : Report("Order Report", Icons.AutoMirrored.Filled.ReceiptLong)
}