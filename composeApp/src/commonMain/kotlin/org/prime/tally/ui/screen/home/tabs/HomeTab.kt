package org.prime.tally.ui.screen.home.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.prime.tally.data.expect.DatabaseHolder

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
        val list by queries.selectAll().asFlow().mapToList(Dispatchers.IO)
            .collectAsState(initial = emptyList())


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeadingTitle("Details")
            CompanyInfoCard(
                companyName = "Demo Company",
                address = "44 Arjun Nagar Ambala Cantt",
                financialYear = "2025-2026",
                gstNo = "PMW37XNZKK1Z", modifier = Modifier.clickable {
                    print(list)
                }
            )

            Spacer(Modifier.height(8.dp))
            HeadingTitle("Cards")
            LazyRow(
            ) {
                items(5) { columnIndex ->
                    Column(
                    ) {
                        DashboardCard(
                            name = "Paisa ${columnIndex + 1}",
                            amount = ((columnIndex + 1) * 1000).toString()
                        )
                        DashboardCard(
                            name = "Rupee ${columnIndex + 6}",
                            amount = ((columnIndex + 6) * 1000).toString()
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            HeadingTitle("Create")
            ExpandableGrid()
        }
    }
}


@Composable
fun ExpandableGrid() {
    var expanded by remember { mutableStateOf(false) }

    val cardList = listOf(
        "Order" to Icons.Default.AddShoppingCart,
        "Sale Invoice" to Icons.Default.Description,
        "Sale return" to Icons.Default.ShoppingCart,
        "Receipt" to Icons.Default.Receipt,
        "Payment" to Icons.Default.Payment,
        "Order" to Icons.Default.AddShoppingCart,
        "Sale Invoice" to Icons.Default.Description,
        "Sale return" to Icons.Default.ShoppingCart,
        "Receipt" to Icons.Default.Receipt,
        "Payment" to Icons.Default.Payment,
        "Payment" to Icons.Default.Payment
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
                    if (name.first == "Show More") {
                        expanded = true
                    } else if (name.first == "Show Less") {
                        expanded = false
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
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            //TODO: make a function that changes the currency
            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.25).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
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