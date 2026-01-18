package org.prime.tally.ui.screen.home.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.tally.data.model.salesmanPermission
import org.prime.tally.ui.screen.home.Dashboard
import org.prime.tally.ui.screen.reports.godown.GodownClosingStockListScreen
import org.prime.tally.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.tally.ui.screen.reports.outstanding.OutstandingSelectScreen
import org.prime.tally.ui.screen.reports.productReport.ProductReportScreen
import org.prime.tally.ui.screen.reports.registers.RegisterSelectScreen
import org.prime.tally.ui.screen.reports.salesman.SalesmanTargetFilterScreen
import org.prime.tally.ui.screen.reports.stock.ParameterReportScreen
import org.prime.tally.ui.screen.reports.stock.StockReportScreen
import org.prime.tally.ui.screen.reports.trialBalance.TrialBalanceScreen
import org.prime.tally.ui.shared.composables.PermissionDeniedDialog

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
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onBackground,
                modifier = Modifier.padding(20.dp).padding(bottom = 8.dp),
            )

            val reports = listOf(
                Report.Ledger,
                Report.Outstanding,
                Report.TrialBalance,
                Report.Registers,
                Report.StockReport,
                Report.ProductStock,
                Report.ParameterReport,
//                Report.PendingOrders,
//                Report.Quotations,
                Report.GoDownWiseClosingStock,
                Report.SalesmanWise,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(reports) { report ->
                    ReportButton(
                        icon = report.icon,
                        title = report.title,
                        onClick = {
                            //TODO: add appropriate screens
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
                                    ProductReportScreen(
                                        null,
                                        isMain = true
                                    )
                                )

                                Report.Registers -> nav?.push(RegisterSelectScreen)
                                Report.StockReport -> {
                                    salesmanPermission(
                                        "D11",
                                        accessDeniedBlock = { showDeniedDialog = true },
                                        successBlock = { nav?.push(StockReportScreen) }
                                    )

                                }
                                //TODO: make D value for parameter report
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
                                        successBlock = {
                                            nav?.push(
                                                GodownClosingStockListScreen
                                            )
                                        }
                                    )

                                }

                                Report.SalesmanWise -> {
                                    nav?.push(SalesmanTargetFilterScreen)
                                }

                            }
                        }
                    )
                }
            }

        }
    }
}


@Composable
fun ReportButton(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        onClick = { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium, overflow = TextOverflow.Ellipsis, maxLines = 2
            )
        }
    }
}

sealed class Report(val title: String, val icon: ImageVector) {
    object Ledger : Report("Ledger", Icons.Default.AccountBalance)
    object Outstanding : Report("Outstanding", Icons.Default.AssignmentLate)
    object TrialBalance : Report("Trial Balance", Icons.Default.Scale)
    object Registers : Report("Registers", Icons.AutoMirrored.Default.ListAlt)
    object StockReport : Report("Stock Report", Icons.Default.Inventory)
    object PendingOrders : Report("Pending Orders", Icons.Default.ShoppingCart)
    object Quotations : Report("Quotations", Icons.Default.Description)
    object GoDownWiseClosingStock : Report("Godown Wise Closing Stock", Icons.Default.Description)
    object ProductStock : Report("Barcode Report", Icons.Default.QrCodeScanner)
    object ParameterReport : Report("Parameter Report", Icons.Default.ProductionQuantityLimits)
    object SalesmanWise : Report("Salesman Wise Target", Icons.Default.TrackChanges)
}
