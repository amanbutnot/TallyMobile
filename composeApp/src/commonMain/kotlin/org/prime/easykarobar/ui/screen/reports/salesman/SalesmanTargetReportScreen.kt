package org.prime.easykarobar.ui.screen.reports.salesman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import getMonthRange
import kotlinx.coroutines.launch
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.salesmanReportHtml
import org.prime.easykarobar.ui.screen.home.ROLE
import org.prime.easykarobar.ui.screen.home.userRole
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetSalesmanTargets

data class SalesmanTargetReportScreen(val month: String, val year: Int) : Screen {
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val list = remember { mutableStateOf<List<GetSalesmanTargets>>(emptyList()) }
        val isLoading = remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val db = DatabaseHolder.instance
        val range = getMonthRange(month, year)
        if (shareLoading) {
            TallyLoadingDialog("Generating Pdf")
        }
        LaunchedEffect(Unit) {
            isLoading.value = true
            list.value = db.salesManTargetQueries.getSalesmanTargets(
                trMonth = month.take(3).lowercase().replaceFirstChar { it.uppercase() },
                trYear = year.toString(),
                fromDate = range.first,
                toDate = range.second,
                salesmanName = if (userRole() == ROLE.SALESMAN) SharedPrefs.User.get()?.FirstName else null,
            ).executeAsList()
            println(list.value)
            isLoading.value = false
        }


        val salesmanData = list.value.map {
            SalesmanData(
                name = it.SalesmanName.toString(),
                targetQty = it.TargetQty ?: 0.0,
                achievedQty = it.AchQty ?: 0.0,
                balanceQty = (it.TargetQty ?: 0.0) - (it.AchQty ?: 0.0),
                targetAmt = it.TargetAmt ?: 0.0,
                achievedAmt = it.AchAmt ?: 0.0,
                balanceAmt = (it.TargetAmt ?: 0.0) - (it.AchAmt ?: 0.0),
            )
        }
        val totalTargetQty = salesmanData.sumOf { it.targetQty }
        val totalAchQty = salesmanData.sumOf { it.achievedQty }
        val totalBalQty = salesmanData.sumOf { it.balanceQty }

        val totalTargetAmt = salesmanData.sumOf { it.targetAmt }
        val totalAchAmt = salesmanData.sumOf { it.achievedAmt }
        val totalBalAmt = salesmanData.sumOf { it.balanceAmt }


        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "SalesmanWiseReport",
                            htmlContent = salesmanReportHtml(
                                title = "Target for ($month $year)",
                                rows = salesmanData,
                                totalTargetQty = totalTargetQty,
                                totalAchQty = totalAchQty,
                                totalBalQty = totalBalQty,
                                totalTargetAmt = totalTargetAmt,
                                totalAchAmt = totalAchAmt,
                                totalBalAmt = totalBalAmt,
                            ),
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            ),
            MenuItemData(
                title = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "SalesmanWiseReport",
                            htmlContent = salesmanReportHtml(
                                title = "Target for ($month $year)",
                                rows = salesmanData,
                                totalTargetQty = totalTargetQty,
                                totalAchQty = totalAchQty,
                                totalBalQty = totalBalQty,
                                totalTargetAmt = totalTargetAmt,
                                totalAchAmt = totalAchAmt,
                                totalBalAmt = totalBalAmt,
                            ),
                            action = PdfAction.Share,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            )
        )
        TallyReportScaffold(
            title = "Target for ($month $year)",
            showBurgerMenu = true,
            menuItems = menuItems,
            showBottomBar = true,
            bottomBarContent = { TotalBottomBar(salesmanData) },
            content = { paddingValues ->
                if (isLoading.value) {
                    TallyCircularLoader()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(salesmanData) { salesman ->
                            SalesmanItem(salesman)
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        )
    }
}

data class SalesmanData(
    val name: String,
    val targetQty: Double,
    val achievedQty: Double,
    val balanceQty: Double,
    val targetAmt: Double,
    val achievedAmt: Double,
    val balanceAmt: Double
)

@Composable
fun SalesmanItem(salesman: SalesmanData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with name
            Text(
                text = salesman.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity Section
            MetricSection(
                title = "Quantity",
                metrics = listOf(
                    MetricData("Target", salesman.targetQty.formatToQtyDec(), Color(0xFF2196F3)),
                    MetricData(
                        "Achieved",
                        salesman.achievedQty.formatToQtyDec(),
                        Color(0xFF4CAF50)
                    ),
                    MetricData("Balance", salesman.balanceQty.formatToQtyDec(), Color(0xFFFF9800))
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Amount Section
            MetricSection(
                title = "Amount",
                metrics = listOf(
                    MetricData("Target", salesman.targetAmt.formatToAmtDec(), Color(0xFF2196F3)),
                    MetricData(
                        "Achieved",
                        salesman.achievedAmt.formatToAmtDec(),
                        Color(0xFF4CAF50)
                    ),
                    MetricData("Balance", salesman.balanceAmt.formatToAmtDec(), Color(0xFFFF9800))
                )
            )
        }
    }
}

data class MetricData(val label: String, val value: String, val color: Color)

@Composable
fun MetricSection(title: String, metrics: List<MetricData>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            metrics.forEach { metric ->
                MetricCard(metric)
            }
        }
    }
}

@Composable
fun RowScope.MetricCard(metric: MetricData) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = metric.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = metric.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = metric.color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TotalBottomBar(salesmanData: List<SalesmanData>) {
    val totalTargetQty = salesmanData.sumOf { it.targetQty }
    val totalAchievedQty = salesmanData.sumOf { it.achievedQty }
    val totalBalanceQty = salesmanData.sumOf { it.balanceQty }
    val totalTargetAmt = salesmanData.sumOf { it.targetAmt }
    val totalAchievedAmt = salesmanData.sumOf { it.achievedAmt }
    val totalBalanceAmt = salesmanData.sumOf { it.balanceAmt }

    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Total Targets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity Totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TotalMetric("Target Qty", totalTargetQty.formatToQtyDec(), Color(0xFF2196F3))
                TotalMetric("Achieved Qty", totalAchievedQty.formatToQtyDec(), Color(0xFF4CAF50))
                TotalMetric("Balance Qty", totalBalanceQty.formatToQtyDec(), Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Amount Totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TotalMetric("Target Amt", totalTargetAmt.formatToAmtDec(), Color(0xFF2196F3))
                TotalMetric("Achieved Amt", totalAchievedAmt.formatToAmtDec(), Color(0xFF4CAF50))
                TotalMetric("Balance Amt", totalBalanceAmt.formatToAmtDec(), Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun RowScope.TotalMetric(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}