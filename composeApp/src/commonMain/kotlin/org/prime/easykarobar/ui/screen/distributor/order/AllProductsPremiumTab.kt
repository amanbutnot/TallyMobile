package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel

object AllProductsPremiumTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Category"
            val icon = rememberVectorPainter(Icons.Default.ShoppingCart)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        
        AllProductsPremiumScreen(isTab = true).AllProductsPremiumContent(cartViewModel)
    }
}
