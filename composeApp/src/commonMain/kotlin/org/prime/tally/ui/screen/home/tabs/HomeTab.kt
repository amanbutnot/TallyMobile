package org.prime.tally.ui.screen.home.tabs

import CurrentDate
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cases
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.screen.attendance.AttendanceScreen
import org.prime.tally.ui.screen.attendance.formatAddress
import org.prime.tally.ui.screen.attendance.getPlaceFromCoordinates
import org.prime.tally.ui.screen.home.isAdmin
import org.prime.tally.ui.screen.reports.ledger.LedgerReportFilterScreen
import org.prime.tally.ui.screen.reports.outstanding.OutstandingDisFilterScreen
import org.prime.tally.ui.screen.reports.outstanding.OutstandingReportScreen
import org.prime.tally.ui.screen.reports.registers.RegisterReportScreen
import org.prime.tally.ui.screen.transactions.SingleEntryReceipt
import org.prime.tally.ui.screen.transactions.sale.SaleScreen
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.globalShared.CompanyName
import org.prime.tally.ui.shared.globalShared.StartDate
import org.prime.tally.ui.shared.globalShared.Tdate

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
        val nav = LocalNavigator.currentOrThrow.parent

        val reportList = queries.dashboardReportData(StartDate(), CurrentDate()).executeAsList()

        val filteredReportList = reportList.map { report ->
            val newRepType = when (report.RecType) {
                1L -> "Pending Receivables"
                2L -> "Pending Payables"
                3L -> "Sales Summary"
                4L -> "Purchase Overview"
                5L -> "Customer Receipts"
                6L -> "Vendor Payments"
                else -> report.RepType
            }
            report.copy(RepType = newRepType)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAdmin()) {

                HeadingTitle("Details")
            } else {
                HeadingTitle("Hi, ${SharedPrefs.DistributorData.get()?.UserName ?: "User"}")
            }
            CompanyInfoCard(
                companyName = CompanyName(),
                address = compInfo.T3.toString(),
                financialYear = Tdate(StartDate()),
                gstNo = compInfo.T4.toString()
            )

            if (isAdmin()) {
                Spacer(Modifier.height(8.dp))
                HeadingTitle("Data")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    filteredReportList.chunked(3).take(2).forEach { rowItems ->
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            items(rowItems) { item ->
                                DashboardCard(
                                    name = item.RepType,
                                    amount = if (item.PenAmt != null) item.PenAmt.toString() else "-",
                                    onClick = {
                                        when (item.RecType) {
                                            1L -> nav?.push(
                                                OutstandingReportScreen(
                                                    name = "Bill Receivable",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate(),
                                                    cm1 = ""
                                                )
                                            )

                                            2L -> nav?.push(
                                                OutstandingReportScreen(
                                                    name = "Bill Payable",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate(),
                                                    cm1 = ""
                                                )
                                            )

                                            3L -> nav?.push(
                                                RegisterReportScreen(
                                                    name = "Sales",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate()
                                                )
                                            )

                                            4L -> nav?.push(
                                                RegisterReportScreen(
                                                    name = "Purchase",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate()
                                                )
                                            )

                                            5L -> nav?.push(
                                                RegisterReportScreen(
                                                    name = "Receipt",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate()
                                                )
                                            )

                                            6L -> nav?.push(
                                                RegisterReportScreen(
                                                    name = "Payment",
                                                    startDate = StartDate(),
                                                    endDate = CurrentDate()
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                HeadingTitle("Create")
                ExpandableGrid()
            } else {
                HeadingTitle("Reports")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MasterButton(
                        icon = Icons.Default.Receipt, modifier = Modifier.weight(1f),
                        title = "Bill Receivable",
                        onClick = {
                            nav?.push(
                                OutstandingDisFilterScreen
                            )
                        }
                    )
                    MasterButton(
                        icon = Icons.Default.Cases, modifier = Modifier.weight(1f),
                        title = "Ledger",
                        onClick = {
                            nav?.push(LedgerReportFilterScreen(showAccount = false))
                        }
                    )
                }
            }


        }
    }
}


@Composable
fun ExpandableGrid() {
    var expanded by remember { mutableStateOf(false) }
    val nav = LocalNavigator.currentOrThrow.parent
    val scope = rememberCoroutineScope()
    var showLocationPopup by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }

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

    if (showLoading) {
        TallyLoadingDialog("Getting Location")
    }


    val cardList = listOf(
        "Receipt" to Icons.Default.Receipt,
        "Payment" to Icons.Default.Payment,
        "Journal" to Icons.Default.AddShoppingCart,
        "Sale Order" to Icons.Default.Description,
        "Sale Invoice" to Icons.Default.ShoppingCart,

        "Check In/Out" to Icons.Default.LocationCity,
        "Attendance" to Icons.Default.LocationOn,

        "Sale Return" to Icons.Default.ShoppingCart,
        "Purchase Order" to Icons.Default.AddShoppingCart,
        "Purchase Invoice" to Icons.Default.ShoppingCart,
        "Purchase Return" to Icons.Default.Receipt,
        "Stock Transfer" to Icons.Default.Payment,
        "Contra" to Icons.Default.Payment
    )

    val displayList = if (expanded) {
        cardList + ("Show Less" to Icons.Default.ExpandLess)
    } else {
        cardList.take(5) + ("Show More" to Icons.Default.ExpandMore)
    }


    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .heightIn(max = 400.dp)
            .padding(8.dp).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(displayList.size) { index ->
            val name = displayList[index]


            CreateCard(
                name = name.first,
                icon = name.second,
                modifier = Modifier.clickable {
                    when (name.first) {
                        "Show More" -> expanded = true
                        "Show Less" -> expanded = false

                        "Receipt" -> nav?.push(SingleEntryReceipt(name.first, vchType = 14))
                        "Payment" -> nav?.push(SingleEntryReceipt(name.first, vchType = 19))
                        "Journal" -> nav?.push(SingleEntryReceipt(name.first, vchType = 16))
                        "Sale Order" -> nav?.push(SaleScreen(name = name.first, vchType = 12))
                        "Sale Return" -> nav?.push(SaleScreen(name = name.first, vchType = 3))
                        "Sale Invoice" -> nav?.push(SaleScreen(name = name.first, vchType = 9))
                        "Check In/Out" -> scope.launch {
                            showLoading = true
                            try {
                                val locator = Locator.mobile()
                                val geolocator = Geolocator(locator)

                                when (val result =
                                    geolocator.current(Priority.HighAccuracy)) {
                                    is GeolocatorResult.Success -> {
                                        val c = result.data.coordinates
                                        val lat = c.latitude
                                        val lon = c.longitude
                                        val place =
                                            getPlaceFromCoordinates(lat, lon)
                                        val address =
                                            place?.let { formatAddress(it) }
                                                ?: "Address not found"
                                        nav?.push(
                                            AttendanceScreen(
                                                lat, lon, address,
                                                isAttendance = false
                                            )
                                        )
                                    }

                                    is GeolocatorResult.Error -> {
                                        showLocationPopup = true
                                    }
                                }
                            } finally {
                                showLoading = false
                            }
                        }
                        "Attendance" -> scope.launch {
                            showLoading = true
                            try {
                                val locator = Locator.mobile()
                                val geolocator = Geolocator(locator)

                                when (val result =
                                    geolocator.current(Priority.HighAccuracy)) {
                                    is GeolocatorResult.Success -> {
                                        val c = result.data.coordinates
                                        val lat = c.latitude
                                        val lon = c.longitude
                                        val place =
                                            getPlaceFromCoordinates(lat, lon)
                                        val address =
                                            place?.let { formatAddress(it) }
                                                ?: "Address not found"
                                        nav?.push(
                                            AttendanceScreen(
                                                lat, lon, address,
                                                isAttendance = true
                                            )
                                        )
                                    }

                                    is GeolocatorResult.Error -> {
                                        showLocationPopup = true
                                    }
                                }
                            } finally {
                                showLoading = false
                            }
                        }
                        "Purchase Order" -> nav?.push(SaleScreen(name = name.first, vchType = 13))
                        "Purchase Invoice" -> nav?.push(SaleScreen(name = name.first, vchType = 2))
                        "Purchase Return" -> nav?.push(SaleScreen(name = name.first, vchType = 10))
                        "Stock Transfer" -> nav?.push(SaleScreen(name = name.first, vchType = 7))
                        "Contra" -> nav?.push(SingleEntryReceipt(name.first, vchType = 15))
                    }
                }
            )
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
fun DashboardCard(
    name: String,
    amount: String,
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(100.dp) // increased height for 2-line names
            .padding(4.dp).clickable { onClick() },
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // centers both vertically and horizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, // ensures multi-line is centered
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.25).sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun CreateCard(
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp).wrapContentHeight()
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
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    letterSpacing = (-0.25).sp
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