package org.prime.tally.ui.screen.home.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.tally.ui.screen.attendance.AttendanceListScreen
import org.prime.tally.ui.screen.transactions.SingleEntryFilterScreen

object TransactionTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.AutoMirrored.Default.ReceiptLong)
            return TabOptions(index = 1u, title = "Transaction", icon = icon)
        }

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val tabNav = LocalTabNavigator.current
        val nav = LocalNavigator.currentOrThrow.parent
        val colors = MaterialTheme.colorScheme

        val entryList = listOf(
            TransactionType("Receipt", Icons.Default.Receipt),
            TransactionType("Payment", Icons.Default.Payment),
            TransactionType("Journal", Icons.Default.AccountBalance),
            TransactionType("Contra", Icons.Default.Money),
            TransactionType("Check in/out", Icons.Default.Work),
            TransactionType("Attendance", Icons.Default.Work)
        )

        val inventoryList = listOf(
            TransactionType("Sale Order", Icons.Default.Receipt),
            TransactionType("Sale Return", Icons.Default.Payment),
            TransactionType("Sale Invoice", Icons.Default.AccountBalance),
            TransactionType("Purchase Order", Icons.Default.Money),
            TransactionType("Purchase Return", Icons.Default.Work),
            TransactionType("Purchase Invoice", Icons.Default.Work),
            TransactionType("Stock Transfer", Icons.Default.Work)
        )

        val othersList = listOf(
            TransactionType("Check in/out", Icons.Default.Work),
            TransactionType("Attendance", Icons.Default.Work)
        )


        BackHandler(true) {
            tabNav.current = HomeTab
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                //        .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tabNav.current.options.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create and manage your transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
            }

            // Transaction Types Card
            TopCard(
                colors,
                title = "Inventory",
                tranList = inventoryList, onClick = { index, type ->
                    when (index) {
                        0 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 12
                            )
                        )

                        1 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 3
                            )
                        )

                        2 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 9
                            )
                        )

                        3 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 13
                            )
                        )

                        4 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 10
                            )
                        )

                        5 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 2
                            )
                        )

                        6 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 7
                            )
                        )

                    }
                }
            )
            TopCard(
                colors,
                title = "Entry Types",
                tranList = entryList, onClick = { index, type ->
                    when (index) {
                        0 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 14
                            )
                        )

                        1 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 19
                            )
                        )

                        2 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 16
                            )
                        )

                        3 -> nav?.push(
                            SingleEntryFilterScreen(
                                type.name,
                                vchType = 15
                            )
                        )
                        4 -> {
                            nav?.push(
                                AttendanceListScreen(
                                    isCheckIn = true,
                                    name = "Check In/Out"
                                )
                            )
                        }

                        5 -> {
                            nav?.push(
                                AttendanceListScreen(
                                    isCheckIn = false,
                                    name = "Attendance Filter"
                                )
                            )
                        }


                    }
                }
            )
//            TopCard(
//                colors,
//                title = "Others", tranList = othersList, onClick = { index, _ ->
//                    when (index) {
//                        0 -> {
//                            nav?.push(
//                                AttendanceListScreen(
//                                    isCheckIn = true,
//                                    name = "Check In/Out"
//                                )
//                            )
//                        }
//
//                        1 -> {
//                            nav?.push(
//                                AttendanceListScreen(
//                                    isCheckIn = false,
//                                    name = "Attendance Filter"
//                                )
//                            )
//                        }
//
//                    }
//
//                }
//            )

        }
    }
}

@Composable
private fun TopCard(
    colors: ColorScheme,
    title: String,
    tranList: List<TransactionType>,
    onClick: (Int, TransactionType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = BorderStroke(
            1.dp,
            colors.outline.copy(alpha = 0.12f)
        )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                itemsIndexed(tranList) { index, type ->
                    TransactionButton(
                        icon = type.icon,
                        title = type.name,
                        onClick = {
                            onClick(index, type)
                        }
                    )
                }
            }
        }
    }
}

data class TransactionType(
    val name: String,
    val icon: ImageVector
)

@Composable
fun TransactionButton(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.size(width = 85.dp, height = 75.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        ),
        border = BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}