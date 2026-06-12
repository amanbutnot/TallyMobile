package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cases
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumScreen
import org.prime.easykarobar.ui.screen.distributor.order.CategoryShoppingScreen
import org.prime.easykarobar.ui.screen.distributor.order.MyOrdersScreen
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingDisFilterScreen
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getPCGroupCodes

object DistributorHomeSubTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow.parent?.parent
        val db = DatabaseHolder.instance
        val queries = db.companyInformationQueries
        val compInfo = queries.getCompanyInformation().executeAsOne()
        val configHideGroup = db.companyConfigurationQueries.hideGroup().executeAsOneOrNull()

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeadingTitle("Hi, ${SharedPrefs.DistributorData.get()?.UserName ?: "User"}")

            CompanyInfoCard(
                companyName = CompanyName(),
                address = compInfo.T3.toString(),
                financialYear = Tdate(StartDate()),
                gstNo = compInfo.T4.toString()
            )

            LastSyncedCard(
                lastSyncDateTime = SharedPrefs.LastSync.get().toString(),
                modifier = Modifier.clickable {
                    println(getPCGroupCodes("117.0"))
                })

            Spacer(Modifier.height(8.dp))
            HeadingTitle("Quick Actions")

            val hideGroup = configHideGroup?.T2.toString() == "Y"
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            )
            {
                // First Card - Bill Receivable
                ReportActionCard(
                    title = "Bill Receivable",
                    description = "View outstanding receivables and pending bills",
                    icon = Icons.Default.Receipt,
                    onClick = {
                        nav?.push(OutstandingDisFilterScreen)
                    })

                // Second Card - Ledger
                ReportActionCard(
                    title = "Ledger Report",
                    description = "Access detailed ledger statements and transactions",
                    icon = Icons.Default.Cases,
                    onClick = {
                        nav?.push(LedgerReportFilterScreen(showAccount = false))
                    })
                // Third Card - Raise Order
                ReportActionCard(
                    title = "Raise Order",
                    description = "Create and place a new order",
                    icon = Icons.Default.ShoppingCart,
                    onClick = {
                        nav?.push(if (hideGroup) AllProductsPremiumScreen(isTab = false) else CategoryShoppingScreen)
                    })
                // Fourth Card - View Order
                ReportActionCard(
                    title = "View Order",
                    description = "View your orders",
                    icon = Icons.Default.ShoppingBasket,
                    onClick = {
                        nav?.push(MyOrdersScreen)
                    })
            }
        }
    }
}
