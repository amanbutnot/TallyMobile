package org.prime.easykarobar.ui.screen.easymart

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumTab
import org.prime.easykarobar.ui.screen.distributor.order.CartScreen
import org.prime.easykarobar.ui.screen.home.SettingScreen
import org.prime.easykarobar.ui.screen.home.tabs.DistributorCategorySubTab
import org.prime.easykarobar.ui.screen.home.tabs.DistributorHomeSubTab
import org.prime.easykarobar.ui.shared.globalShared.CompanyName

object DmsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }

        val db = DatabaseHolder.instance
        val hideGroup = db.companyConfigurationQueries.hideGroup().executeAsOneOrNull()?.T2.toString() == "Y"
        TabNavigator(if (hideGroup) AllProductsPremiumTab else DistributorHomeSubTab) { tabNavigator ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                CompanyName(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            BadgedBox(badge = {
                                if ((cartViewModel?.getTotalProductCount() ?: 0) > 0) {
                                    Badge(
                                        containerColor = Color(0xFFE53935),
                                        contentColor = Color.White
                                    ) {
                                        Text(cartViewModel?.getTotalProductCount().toString())
                                    }
                                }
                            }) {
                                IconButton(onClick = {
                                    nav.push(CartScreen)
                                }) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "cart",
                                        tint = colors.onBackground
                                    )
                                }
                            }

                            IconButton(onClick = { nav.push(SettingScreen) }) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Settings icon",
                                    tint = colors.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.primary.copy(alpha = 0.2f)
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                DistributorHomeSubTab,
                                DistributorCategorySubTab
                            ).forEach { tab ->
                                val actualTab =
                                    if (tab == DistributorCategorySubTab && hideGroup) AllProductsPremiumTab
                                    else tab

                                val selected = tabNavigator.current == actualTab

                                val colorTint by animateColorAsState(
                                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            tabNavigator.current = actualTab
                                        }
                                ) {
                                    tab.options.icon?.let {
                                        Icon(
                                            painter = it,
                                            contentDescription = tab.options.title,
                                            tint = colorTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = tab.options.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorTint,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    CurrentTab()
                }
            }
        }
    }
}
