package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warehouse
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.data.enums.MasterEnums
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.ui.screen.masters.MasterListScreen
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog

object MastersTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.ManageAccounts)
            return TabOptions(index = 0u, title = "Masters", icon = icon)
        }

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow.parent
        val tabNav = LocalTabNavigator.current
        var showDeniedDialog by remember { mutableStateOf(false) }
        BackHandler(true) {
            tabNav.current = HomeTab
        }
        Column(
            modifier = Modifier.fillMaxSize().background(colors.background)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Masters",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                MasterButton(
                    Icons.Default.AccountBalance, "Accounts", modifier = Modifier.weight(1f)
                ) {
                    salesmanPermission(
                        "D1",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.ACCOUNTS)) }
                    )

                }

                MasterButton(
                    Icons.Default.Groups2, "Account Groups", modifier = Modifier.weight(1f)
                ) {
                    salesmanPermission(
                        "D2",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.ACCOUNT_GROUP)) }
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MasterButton(
                    Icons.Default.Inventory2, "Items", modifier = Modifier.weight(1f)
                ) {

                    salesmanPermission(
                        "D3",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.ITEMS)) }
                    )
                }


                MasterButton(
                    Icons.Default.Category, "Item Groups", modifier = Modifier.weight(1f)
                ) {

                    salesmanPermission(
                        "D4",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.ITEM_GROUP)) }
                    )
                }


            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                MasterButton(
                    Icons.Default.Straighten, "Units", modifier = Modifier.weight(1f)
                ) {
                    salesmanPermission(
                        "D5",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.ITEM_UNIT)) }
                    )
                }




                MasterButton(
                    Icons.Default.Warehouse, "Material Centers", modifier = Modifier.weight(1f)
                ) {
                    salesmanPermission(
                        "D6",
                        accessDeniedBlock = { showDeniedDialog = true },
                        successBlock = { nav?.push(MasterListScreen(MasterEnums.MATERIAL_CENTER)) }
                    )
                }

            }

            if (showDeniedDialog) {
                PermissionDeniedDialog { showDeniedDialog = false }
            }
        }
    }
}

@Composable
fun MasterButton(
    icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp, pressedElevation = 8.dp
        ),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        onClick = { onClick() }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}