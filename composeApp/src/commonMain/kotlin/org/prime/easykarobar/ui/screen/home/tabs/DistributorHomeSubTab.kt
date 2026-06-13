package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumScreen
import org.prime.easykarobar.ui.screen.distributor.order.CategoryShoppingScreen

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
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val db = DatabaseHolder.instance
        val configHideGroup = db.companyConfigurationQueries.hideGroup().executeAsOneOrNull()
        val hideGroup = configHideGroup?.T2.toString() == "Y"

        if (hideGroup) {
            AllProductsPremiumScreen(isTab = true).AllProductsPremiumContent(cartViewModel)
        } else {
            CategoryShoppingScreen.CategoryShoppingContent()
        }
    }
}
