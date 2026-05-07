package org.prime.easykarobar.ui.screen.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.deleteDbFile
import org.prime.easykarobar.data.utils.MOBILE_VERSION
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.auth.OnBoardingScreen
import org.prime.easykarobar.ui.screen.distributor.CreateDistributorScreen
import org.prime.easykarobar.ui.screen.distributor.ListDistributorScreen
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyDivider
import org.prime.easykarobar.ui.shared.composables.TallyIconButton
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.StartDate

object SettingScreen : Screen {
    @OptIn(InternalVoyagerApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        BackHandler(true) {
            nav.pop()
        }
        val db = DatabaseHolder.instance
        val queries = db.companyInformationQueries
        val compInfo = queries.getCompanyInformation().executeAsOne()
        var showAlertBox by remember { mutableStateOf(false) }
        var zeroStock by remember { mutableStateOf(true) }

        zeroStock = SharedPrefs.ShowZeroStock.get() ?: true

        val colors = MaterialTheme.colorScheme
        TallyScaffold("Profile", content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceContainerLowest)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.onPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        CompanyName().take(1),
                                        color = colors.onBackground,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 28.sp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = CompanyName(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.onPrimary
                                )
                                Text(
                                    text = "Business Account",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (userRole() == ROLE.ADMIN) {
                        Text(
                            text = "Distributor Management",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ManagementCard(
                                icon = Icons.Default.Add,
                                label = "Create",
                                subtitle = "New Distributor",
                                containerColor = colors.primaryContainer,
                                contentColor = colors.onPrimaryContainer,
                                onClick = { nav.push(CreateDistributorScreen()) },
                                modifier = Modifier.weight(1f)
                            )
                            ManagementCard(
                                icon = Icons.AutoMirrored.Filled.List,
                                label = "View All",
                                subtitle = "Distributors",
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                                onClick = { nav.push(ListDistributorScreen) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                    }

                    // Distributor Management Section


                    Spacer(modifier = Modifier.height(20.dp))
//                    TallyDivider()
//
//                    // Salesman Management Section
//                    Text(
//                        text = "Salesman Management",
//                        style = MaterialTheme.typography.titleSmall.copy(
//                            fontWeight = FontWeight.SemiBold
//                        ),
//                        color = colors.onSurfaceVariant,
//                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
//                    )
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        ManagementCard(
//                            icon = Icons.Default.Add,
//                            label = "Create",
//                            subtitle = "New Salesman",
//                            containerColor = colors.secondaryContainer,
//                            contentColor = colors.onSecondaryContainer,
//                            onClick = { /* TODO: Navigate to CreateSalesmanScreen */ },
//                            modifier = Modifier.weight(1f)
//                        )
//                        ManagementCard(
//                            icon = Icons.Default.List,
//                            label = "View All",
//                            subtitle = "Salesmen",
//                            containerColor = colors.secondary,
//                            contentColor = colors.onSecondary,
//                            onClick = { /* TODO: Navigate to ListSalesmanScreen */ },
//                            modifier = Modifier.weight(1f)
//                        )
//                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    TallyDivider()

                    Text(
                        text = "Account Information",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileItem(Icons.Default.Email, "Email Address", compInfo.T7.toString())
                        ProfileItem(
                            Icons.Default.Person,
                            "Name",
                            SharedPrefs.User.get()?.FirstName ?: "-"
                        )
                        ProfileItem(
                            Icons.Default.Numbers,
                            "Mobile Number",
                            SharedPrefs.User.get()?.Mobile ?: "-"
                        )
                        ProfileItem(Icons.Default.Business, "Company Name", CompanyName())
                        ProfileItem(
                            Icons.Default.LocationOn,
                            "Business Address",
                            compInfo.T3.toString()
                        )
                        ProfileItem(
                            Icons.Default.Store,
                            "Store ID",
                            SharedPrefs.User.get()?.ID.toString()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    TallyDivider()

                    Text(
                        text = "Business Details",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileItem(Icons.Default.DateRange, "Financial Year", StartDate())

                        ProfileItem(Icons.Default.Receipt, "GST Number", compInfo.T4.toString())
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    TallyDivider()

                    Text(
                        text = "Format & Display Settings",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileItem(
                            Icons.Default.CurrencyRupee,
                            "Currency Symbol",
                            compInfo.T9.toString()
                        )
                        ProfileItem(
                            Icons.Default.Numbers,
                            "Quantity Decimal",
                            compInfo.D3.toString()
                        )
                        ProfileItem(
                            Icons.Default.MonetizationOn,
                            "Amount Decimal",
                            compInfo.D4.toString()
                        )
                        ProfileItem(
                            Icons.Default.CalendarToday,
                            "Date Format",
                            compInfo.T8.toString()
                        )
                        ProfileItem(
                            Icons.Default.SyncLock,
                            "Lasy Synced from Software",
                            compInfo.C8.toString()
                        )
                        if (listOf(
                                ROLE.STAFF_MANAGER,
                                ROLE.OFFICE_STAFF
                            ).none { userRole() != it }
                        ) {
                            TallyToggleRow(
                                checked = zeroStock,
                                onCheckedChange = {
                                    SharedPrefs.ShowZeroStock.save(it)
                                    zeroStock = it
                                },
                                title = "Show Zero Stock in billing",
                                desc = "Include items with zero stock in billing"
                            )

                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TallyIconButton("Sign Out", Icons.AutoMirrored.Filled.Logout) {
                    showAlertBox = true
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "v $MOBILE_VERSION",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }


                if (showAlertBox) {
                    TallyAlertBox(
                        title = "Logout?",
                        message = "Do you want to logout?",
                        confirmButtonText = "Logout",
                        cancelButtonText = "Cancel",
                        onConfirm = {
                            deleteDbFile()
                            SharedPrefs.logout()
                            nav.replaceAll(OnBoardingScreen)
                        },
                        onCancel = {
                            showAlertBox = false
                        },
                        onDismiss = {
                            showAlertBox = false
                        },
                    )
                }
            }
        })
    }
}

@Composable
private fun ManagementCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(75.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TallyToggleRow(
    title: String, desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}