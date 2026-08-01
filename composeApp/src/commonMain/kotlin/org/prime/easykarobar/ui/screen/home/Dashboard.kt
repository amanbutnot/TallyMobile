package org.prime.easykarobar.ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.prime.easykarobar.business.viewmodel.AuthViewModel
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.callPhone
import org.prime.easykarobar.data.expect.getDeviceId
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.auth.SelectCompanyScreen
import org.prime.easykarobar.ui.screen.distributor.order.CartScreen
import org.prime.easykarobar.ui.screen.home.tabs.CallSupportTab
import org.prime.easykarobar.ui.screen.home.tabs.DistributorCategorySubTab
import org.prime.easykarobar.ui.screen.home.tabs.DistributorHomeSubTab
import org.prime.easykarobar.ui.screen.home.tabs.DistributorReportSubTab
import org.prime.easykarobar.ui.screen.home.tabs.HomeTab
import org.prime.easykarobar.ui.screen.home.tabs.MastersTab
import org.prime.easykarobar.ui.screen.home.tabs.ReportingTab
import org.prime.easykarobar.ui.screen.home.tabs.SettingsTab
import org.prime.easykarobar.ui.screen.home.tabs.TransactionTab
import org.prime.easykarobar.ui.screen.home.tabs.WhatsAppSupportTab
import org.prime.easykarobar.ui.screen.home.tabs.WishlistTab
import org.prime.easykarobar.ui.screen.startup.GoogleDriveDownloadScreen
import org.prime.easykarobar.ui.shared.globalShared.CompanyName

object Dashboard : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val nav = LocalNavigator.currentOrThrow
        val db = DatabaseHolder.instance
        val queries = db.companyInformationQueries
        val list = queries.selectAll().executeAsList()
        val loginData = SharedPrefs.LoginData.get()
        val hasCompanies = loginData?.list != null
        val viewModel: AuthViewModel = viewModel { AuthViewModel() }
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val deviceId = getDeviceId()

        val initialTab = if (userRole() == ROLE.DISTRIBUTOR || SharedPrefs.IsEasyMart.get()) DistributorHomeSubTab else HomeTab

        TabNavigator(initialTab) { tabNavigator ->
            Scaffold(
                topBar = {
                    if (!(SharedPrefs.IsEasyMart.get() && tabNavigator.current == SettingsTab)) {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (hasCompanies && userRole() != ROLE.DISTRIBUTOR && !SharedPrefs.IsEasyMart.get()) {
                                                Modifier.clickable {
                                                    nav.push(
                                                        SelectCompanyScreen(
                                                            loginData!!.username,
                                                            loginData.password,
                                                            loginData.list
                                                        )
                                                    )
                                                }
                                            } else Modifier
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        CompanyName(),
                                        color = if (hasCompanies)
                                            colors.onBackground
                                        else
                                            colors.onBackground.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ), maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )

                                    if (hasCompanies && userRole() != ROLE.DISTRIBUTOR) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = colors.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            },

                            actions = {
                                if (userRole() != ROLE.DISTRIBUTOR && !SharedPrefs.IsEasyMart.get()) {
                                    IconButton(onClick = {
                                        viewModel.userLogin(
                                            LoginRequest(
                                                Username = loginData?.username ?: "",
                                                Password = loginData?.password ?: "",
                                                DeviceId = deviceId
                                            ),
                                            onSuccess = {
                                                nav.push(GoogleDriveDownloadScreen)
                                            }, onListSuccess = { companyList ->
                                                SharedPrefs.LoginInfo.save(
                                                    loginData?.username?.trim() ?: ""
                                                )
                                                SharedPrefs.LoginData.save(
                                                    SharedPrefs.LoginDataModel(
                                                        username = loginData?.username?.trim() ?: "",
                                                        password = loginData?.password?.trim() ?: "",
                                                        list = companyList,
                                                    )
                                                )
                                                nav.push(
                                                    SelectCompanyScreen(
                                                        loginData?.username?.trim() ?: "",
                                                        loginData?.password?.trim() ?: "",
                                                        companyList
                                                    )
                                                )
                                            }
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.CloudSync,
                                            contentDescription = "Cloud Sync",
                                            tint = colors.onBackground
                                        )
                                    }
                                }
                                if (userRole() == ROLE.DISTRIBUTOR || SharedPrefs.IsEasyMart.get()) {
                                    BadgedBox(
                                        badge = {
                                            if ((cartViewModel?.getTotalProductCount() ?: 0) > 0) {
                                                Badge(
                                                    containerColor = Color(0xFFE53935),
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        cartViewModel?.getTotalProductCount().toString()
                                                    )
                                                }
                                            }
                                        },
                                    ) {
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
                                }
                                if (!SharedPrefs.IsEasyMart.get()) {
                                    IconButton(onClick = { nav.push(SettingScreen) }) {
                                        Icon(
                                            if (userRole() == ROLE.DISTRIBUTOR) Icons.Default.Person else Icons.Default.Settings,
                                            contentDescription = "Settings icon",
                                            tint = colors.onBackground
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                           //     containerColor = colors.primary.copy(alpha = 0.2f)
                                containerColor = Color(0xFFFF6D00),
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    }
                },
                bottomBar = {
                    val role = userRole()
                    if (role == ROLE.ADMIN || role == ROLE.SALESMAN || role == ROLE.DISTRIBUTOR) {
                        val tabs = if (role == ROLE.DISTRIBUTOR || SharedPrefs.IsEasyMart.get()) {
                            val distributorTabs = mutableListOf<Tab>(
                                DistributorHomeSubTab,
                                DistributorCategorySubTab,
                                WishlistTab
                            )
                            if (org.prime.easykarobar.BuildKonfig.STORE_ID.isEmpty()) {
                                distributorTabs.add(DistributorReportSubTab)
                            }
                            if (SharedPrefs.IsEasyMart.get()) {
                                distributorTabs.add(SettingsTab)
                                distributorTabs.add(WhatsAppSupportTab)
                                distributorTabs.add(CallSupportTab)
                            }
                            distributorTabs
                        } else {
                            listOf(HomeTab, MastersTab, TransactionTab, ReportingTab)
                        }
                        BottomTabBar(
                            tabs = tabs,
                            tabNavigator = tabNavigator
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
fun BottomTabBar(
    tabs: List<Tab>,
    tabNavigator: TabNavigator
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.navigationBarsPadding()
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            TabNavigationItem(
                tab = tab,
                selected = tabNavigator.current == tab,
                onClick = {
                    when (tab) {
                        is WhatsAppSupportTab -> uriHandler.openUri("https://wa.me/919850228878")
                        is CallSupportTab -> callPhone("9850228878")
                        else -> tabNavigator.current = tab
                    }
                }
            )
        }
    }
}

@Composable
fun TabNavigationItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    )

    val iconScale by animateFloatAsState(targetValue = if (selected) 1.15f else 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        tab.options.icon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = tab.options.title,
                tint = colorTint,
                modifier = Modifier.scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.options.title,
            style = MaterialTheme.typography.labelMedium,
            color = colorTint
        )
    }
}

fun userRole(): ROLE {
    return when (SharedPrefs.User.get()?.role) {
        "admin" -> ROLE.ADMIN
        "salesman" -> ROLE.SALESMAN
        "distributor" -> ROLE.DISTRIBUTOR
        "staff-manager" -> ROLE.STAFF_MANAGER
        "office-staff" -> ROLE.OFFICE_STAFF
        "delivery" -> ROLE.DELIVERY
        else -> {
            ROLE.ADMIN
        }
    }
}

enum class ROLE { ADMIN, SALESMAN, DISTRIBUTOR, STAFF_MANAGER, OFFICE_STAFF, DELIVERY }
