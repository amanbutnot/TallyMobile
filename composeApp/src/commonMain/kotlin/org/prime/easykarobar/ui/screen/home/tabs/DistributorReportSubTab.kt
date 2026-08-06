package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.ui.screen.distributor.order.MyOrdersScreen
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingDisFilterScreen

object DistributorReportSubTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Reports"
            val icon = rememberVectorPainter(Icons.Default.Assessment)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val colors = MaterialTheme.colorScheme

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = "Reports",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "View your business reports",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ReportButton(
                    icon = Icons.Default.AccountBalance,
                    title = "Ledger",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav.push(LedgerReportFilterScreen(showAccount = false))
                    }
                )

                ReportButton(
                    icon = Icons.Default.WarningAmber,
                    title = "Outstanding",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav.push(OutstandingDisFilterScreen(name = "Bill Receivable"))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {


                ReportButton(
                    icon = Icons.Default.ShoppingCart,
                    title = "My Orders",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav.push(MyOrdersScreen())
                    }
                )
            }
        }
    }
}
