package org.prime.easykarobar.ui.screen.home.tabs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumScreen
import org.prime.easykarobar.ui.screen.distributor.order.CategoryShoppingScreen
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes

object DistributorCategorySubTab : Tab {
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
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val db = DatabaseHolder.instance

        val categoryList = remember {
            db.productsQueries.productCategoriesForDis(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes()
            ).executeAsList()
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                CategoryShoppingScreen.CategoriesGrid(
                    categories = categoryList,
                    onCategoryClick = { category ->
                        nav.push(
                            AllProductsPremiumScreen(
                                categoryName = category.Name,
                                productCode = category.GUID?.toDouble() ?: 0.0,
                                isTab = false
                            )
                        )
                    }
                )
            }
        }
    }
}
