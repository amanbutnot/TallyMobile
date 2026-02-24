package org.prime.easykarobar.ui.screen.home.tabs

import CurrentDate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cases
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.hasSalesmanPermission
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.attendance.AttendanceScreen
import org.prime.easykarobar.ui.screen.distributor.order.MyOrdersScreen
import org.prime.easykarobar.ui.screen.distributor.order.ShoppingScreen
import org.prime.easykarobar.ui.screen.home.ROLE
import org.prime.easykarobar.ui.screen.home.userRole
import org.prime.easykarobar.ui.screen.masters.AccountAddScreen
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingDisFilterScreen
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingReportScreen
import org.prime.easykarobar.ui.screen.reports.registers.RegisterReportScreen
import org.prime.easykarobar.ui.screen.transactions.SingleEntryReceipt
import org.prime.easykarobar.ui.screen.transactions.sale.SaleScreen
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.accountGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.agrpGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.configBroker
import org.prime.easykarobar.ui.shared.globalShared.filterAGRPGroups
import org.prime.easykarobar.ui.shared.globalShared.filterAccountGroups
import org.prime.easykarobar.ui.shared.globalShared.filterBroker
import org.prime.easykarobar.ui.shared.globalShared.getPCGroupCodes
import kotlin.math.absoluteValue

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return TabOptions(index = 0u, title = "Home", icon = icon)
        }

    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance
        val queries = db.companyInformationQueries
        val compInfo = queries.getCompanyInformation().executeAsOne()
        var showDeniedDialog by remember { mutableStateOf(false) }
        val nav = LocalNavigator.currentOrThrow.parent


        val reportList = queries.dashboardReportData(
            StartDate(), CurrentDate(), filterCm3 = filterBroker(),
            cm3 = configBroker(),
            groupFilter = filterAGRPGroups(),
            GroupCode = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            excludeFilter = filterAccountGroups(),
            GUID = accountGroupCodes(),
            GroupCode_ = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            GUID_ = accountGroupCodes(),
            GroupCode__ = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            GUID__ = accountGroupCodes(),
            GroupCode___ = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            GUID___ = accountGroupCodes(),
            GroupCode____ = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            GUID____ = accountGroupCodes(),
            GroupCode_____ = agrpGroupCodes().map { it.toDoubleOrNull()?:0.0 },
            GUID_____ = accountGroupCodes()
        ).executeAsList()

        val filteredReportList = reportList
            .filter { report -> report.RecType !in listOf(4L, 6L) }
            .map { report ->
                val newRepType = when (report.RecType) {
                    1L -> "Pending Receivables"
                    2L -> "Pending Payables"
                    3L -> "Sales Summary"
                    5L -> "Customer Receipts"
                    else -> report.RepType
                }
                report.copy(RepType = newRepType)
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            if (userRole() == ROLE.DISTRIBUTOR) {
                HeadingTitle("Hi, ${SharedPrefs.DistributorData.get()?.UserName ?: "User"}")
            }
            CompanyInfoCard(
                companyName = CompanyName(),
                address = compInfo.T3.toString(),
                financialYear = Tdate(StartDate()),
                gstNo = compInfo.T4.toString()
            )
            //    Spacer(Modifier.height(8.dp))
            LastSyncedCard(
                lastSyncDateTime = SharedPrefs.LastSync.get().toString(),
                modifier = Modifier.clickable {
                    println(getPCGroupCodes("117.0"))
                }
            )
            if (userRole() == ROLE.ADMIN || userRole() == ROLE.SALESMAN) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    filteredReportList.take(6).chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                InfoStatCard(
                                    title = item.RepType,
                                    amount = when (item.RecType) {
                                        1L -> {
                                            if (hasSalesmanPermission("D8")) {
                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
                                            } else "X"
                                        }

                                        2L -> {
                                            if (hasSalesmanPermission("D9")) {
                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
                                            } else "X"
                                        }

                                        3L -> {
                                            if (hasSalesmanPermission("D13")) {
                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
                                            } else "X"
                                        }

//                                        4L -> {
//                                            if (hasSalesmanPermission("D14")) {
//                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
//                                            } else "X"
//                                        }

                                        5L -> {
                                            if (hasSalesmanPermission("D15")) {
                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
                                            } else "X"
                                        }

//                                        6L -> {
//                                            if (hasSalesmanPermission("D16")) {
//                                                item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
//                                            } else "X"
//                                        }

                                        else -> item.PenAmt?.absoluteValue?.formatToAmtDec() ?: "-"
                                    },
                                    onClick = {
                                        when (item.RecType) {
                                            1L -> salesmanPermission(
                                                flag = "D8",
                                                accessDeniedBlock = { showDeniedDialog = true },
                                                successBlock = {
                                                    nav?.push(
                                                        OutstandingReportScreen(
                                                            name = "Bill Receivable",
                                                            startDate = StartDate(),
                                                            endDate = CurrentDate(),
                                                            cm1 = ""
                                                        )
                                                    )
                                                }
                                            )

                                            2L -> salesmanPermission(
                                                flag = "D9",
                                                accessDeniedBlock = { showDeniedDialog = true },
                                                successBlock = {
                                                    nav?.push(
                                                        OutstandingReportScreen(
                                                            name = "Bill Payable",
                                                            startDate = StartDate(),
                                                            endDate = CurrentDate(),
                                                            cm1 = ""
                                                        )
                                                    )
                                                }
                                            )

                                            3L -> salesmanPermission(
                                                flag = "D13",
                                                accessDeniedBlock = { showDeniedDialog = true },
                                                successBlock = {
                                                    nav?.push(
                                                        RegisterReportScreen(
                                                            name = "Sales",
                                                            startDate = StartDate(),
                                                            endDate = CurrentDate()
                                                        )
                                                    )
                                                }
                                            )

//                                            4L -> salesmanPermission(
//                                                flag = "D14",
//                                                accessDeniedBlock = { showDeniedDialog = true },
//                                                successBlock = {
//                                                    nav?.push(
//                                                        RegisterReportScreen(
//                                                            name = "Purchase",
//                                                            startDate = StartDate(),
//                                                            endDate = CurrentDate()
//                                                        )
//                                                    )
//                                                }
//                                            )

                                            5L -> salesmanPermission(
                                                flag = "D15",
                                                accessDeniedBlock = { showDeniedDialog = true },
                                                successBlock = {
                                                    nav?.push(
                                                        RegisterReportScreen(
                                                            name = "Receipt",
                                                            startDate = StartDate(),
                                                            endDate = CurrentDate()
                                                        )
                                                    )
                                                }
                                            )

//                                            6L -> salesmanPermission(
//                                                flag = "D16",
//                                                accessDeniedBlock = { showDeniedDialog = true },
//                                                successBlock = {
//                                                    nav?.push(
//                                                        RegisterReportScreen(
//                                                            name = "Payment",
//                                                            startDate = StartDate(),
//                                                            endDate = CurrentDate()
//                                                        )
//                                                    )
//                                                }
//                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    buttonText = "View",
                                    iconContent = {
                                        Icon(
                                            Icons.Default.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }

                            // Add spacer if odd number of items in last row
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HeadingTitle("Create")
                ExpandableGrid()
            } else if (userRole() == ROLE.DISTRIBUTOR) {
                Spacer(Modifier.height(8.dp))
                HeadingTitle("Quick Actions")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // First Card - Bill Receivable
                    ReportActionCard(
                        title = "Bill Receivable",
                        description = "View outstanding receivables and pending bills",
                        icon = Icons.Default.Receipt,
                        onClick = {
                            nav?.push(OutstandingDisFilterScreen)
                        }
                    )

                    // Second Card - Ledger
                    ReportActionCard(
                        title = "Ledger Report",
                        description = "Access detailed ledger statements and transactions",
                        icon = Icons.Default.Cases,
                        onClick = {
                            nav?.push(LedgerReportFilterScreen(showAccount = false))
                        }
                    )
                    // Second Card - Ledger
                    ReportActionCard(
                        title = "Raise Order",
                        description = "Create and place a new order",
                        icon = Icons.Default.ShoppingCart,
                        onClick = {
                            nav?.push(ShoppingScreen)
                        }
                    )  // Second Card - Ledger
                    ReportActionCard(
                        title = "View Order",
                        description = "View your orders",
                        icon = Icons.Default.ShoppingBasket,
                        onClick = {
                            nav?.push(MyOrdersScreen)
                        }
                    )
                }
            } else {
                HeadingTitle("Create")
                ExpandableGrid()
            }

            if (showDeniedDialog) {
                PermissionDeniedDialog { showDeniedDialog = false }
            }
        }
    }
}

@Composable
fun ReportActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Arrow Indicator
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}


@Composable
fun ExpandableGrid() {
    val nav = LocalNavigator.currentOrThrow.parent
    val scope = rememberCoroutineScope()

    var showLocationPopup by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }


    val cardList = listOf(
        "Sale Order" to Icons.Default.Description,
        "Sale Invoice" to Icons.Default.ShoppingCart,
        "Sale Return" to Icons.Default.ShoppingCart,
        "Receipt" to Icons.Default.Receipt,
        "Payment" to Icons.Default.Payment,
        "Journal" to Icons.Default.AddShoppingCart,
        "Purchase Order" to Icons.Default.AddShoppingCart,
        "Purchase Invoice" to Icons.Default.ShoppingCart,
        "Purchase Return" to Icons.Default.Receipt,
        "Attendance" to Icons.Default.LocationOn,
        "Check In/Out" to Icons.Default.LocationCity,
        "Contra" to Icons.Default.Payment,
        "Account" to Icons.Default.AccountBox
    )

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cardList.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CreateCard(
                            name = item.first,
                            icon = item.second,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    when (item.first) {

                                        "Receipt" -> salesmanPermission(
                                            "D17",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SingleEntryReceipt(
                                                        item.first,
                                                        vchType = 14
                                                    )
                                                )
                                            })

                                        "Payment" -> salesmanPermission(
                                            "D18",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SingleEntryReceipt(
                                                        item.first,
                                                        vchType = 19
                                                    )
                                                )
                                            })

                                        "Journal" -> salesmanPermission(
                                            "D19",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SingleEntryReceipt(
                                                        item.first,
                                                        vchType = 16
                                                    )
                                                )
                                            })

                                        "Sale Order" -> salesmanPermission(
                                            "D20",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 12))
                                            })

                                        "Sale Invoice" -> salesmanPermission(
                                            "D21",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 9))
                                            })

                                        "Check In/Out" -> salesmanPermission(
                                            "D22",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                scope.launch {
                                                    showLoading = true
                                                    try {
                                                        val geolocator =
                                                            Geolocator(Locator.mobile())

                                                        runCatching { geolocator.lastLocation() }

                                                        val result = withTimeoutOrNull(20000) {
                                                            geolocator.current(Priority.HighAccuracy)
                                                        }

                                                        when (result) {
                                                            is GeolocatorResult.Success -> {
                                                                val c = result.data.coordinates

                                                                nav?.push(
                                                                    AttendanceScreen(
                                                                        c.latitude,
                                                                        c.longitude,

                                                                        isAttendance = false
                                                                    )
                                                                )
                                                            }

                                                            else -> showLocationPopup = true
                                                        }

                                                    } catch (e: Exception) {
                                                        showLocationPopup = true
                                                    } finally {
                                                        showLoading = false
                                                    }
                                                }
                                            }
                                        )

                                        "Attendance" -> salesmanPermission(
                                            "D23",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                scope.launch {
                                                    showLoading = true
                                                    try {
                                                        val geolocator =
                                                            Geolocator(Locator.mobile())

                                                        runCatching { geolocator.lastLocation() }

                                                        val result = withTimeoutOrNull(20000) {
                                                            geolocator.current(Priority.HighAccuracy)
                                                        }

                                                        when (result) {
                                                            is GeolocatorResult.Success -> {
                                                                val c = result.data.coordinates

                                                                nav?.push(
                                                                    AttendanceScreen(
                                                                        c.latitude,
                                                                        c.longitude,

                                                                        isAttendance = true
                                                                    )
                                                                )
                                                            }

                                                            else -> showLocationPopup = true
                                                        }

                                                    } catch (e: Exception) {
                                                        showLocationPopup = true
                                                    } finally {
                                                        showLoading = false
                                                    }
                                                }
                                            }
                                        )


                                        "Sale Return" -> salesmanPermission(
                                            "D24",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 3))
                                            })

                                        "Purchase Order" -> salesmanPermission(
                                            "D25",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 13))
                                            })

                                        "Purchase Invoice" -> salesmanPermission(
                                            "D26",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 2))
                                            })

                                        "Purchase Return" -> salesmanPermission(
                                            "D27",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(SaleScreen(item.first, vchType = 10))
                                            })

                                        "Contra" -> salesmanPermission(
                                            "D28",
                                            accessDeniedBlock = { showDeniedDialog = true },
                                            successBlock = {
                                                nav?.push(
                                                    SingleEntryReceipt(
                                                        item.first,
                                                        vchType = 15
                                                    )
                                                )
                                            })
                                        "Account"->{nav?.push(AccountAddScreen)}
                                    }
                                }
                        )
                    }
                }

                // Fill empty slots if row has less than 3 items (keeps grid aligned)
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (showLocationPopup) {
            AlertDialog(
                onDismissRequest = { showLocationPopup = false },
                title = { Text("Location is Off") },
                text = { Text("Please enable location to continue.") },
                confirmButton = {
                    Button(onClick = { showLocationPopup = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showDeniedDialog) {
            PermissionDeniedDialog { showDeniedDialog = false }
        }

        if (showLoading) {
            TallyLoadingDialog("Getting Location")
        }

    }
}

@Composable
private fun HeadingTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
    )
}


@Composable
fun CreateCard(
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp).height(100.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )


            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.25).sp, textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CompanyInfoCard(
    companyName: String,
    address: String,
    financialYear: String,
    gstNo: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape
                        ), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Company",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.DateRange, label = "Financial Year", value = financialYear
                )

                InfoRow(
                    icon = Icons.Default.Badge, label = "GST Number", value = gstNo
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LastSyncedCard(
    lastSyncDateTime: String, // Format: "2024-01-09 14:30:45" or use LocalDateTime
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Last Synced",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = lastSyncDateTime,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Optional: Add a sync status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun InfoStatCard(
    title: String,
    amount: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                colors.primary.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .background(colors.surface)
            .padding(12.dp)
    ) {


        // Compact content
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
            //.padding(end = 40.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                color = colors.onSurface
            )

            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(6.dp),
                color = colors.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}