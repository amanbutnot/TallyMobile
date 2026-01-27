package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.ui.screen.distributor.order.ShoppingScreen.ItemCard
import org.prime.easykarobar.ui.screen.distributor.order.ShoppingScreen.SearchField
import org.prime.easykarobar.ui.screen.distributor.order.ShoppingScreen.ShowProductInfo
import org.prime.easykarobar.ui.screen.distributor.order.ShoppingScreen.ToggleIconButton
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.tally.GetProductsForDis

data class AllProductScreen(
    val categoryName: String,
    val viewModel: CartViewModel,
    val productCode: Double,
) : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            val nav = LocalNavigator.currentOrThrow
            val showProductInfo = remember { mutableStateOf(false) }
            val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }
            val searchQuery = remember { mutableStateOf("") }
            val db = DatabaseHolder.instance
            val list = db.productsQueries.getProductsForDis(productCode).executeAsList()
            TallyScaffold(
                title = categoryName,
                onBack = { nav.pop() },
                showEditIcon = false,
                onEditClick = {}
            ) { paddingValues ->
                if (list.isEmpty()) {
                    EmptyListPlaceholder(
                        icon = Icons.Default.Inbox,
                        title = "No products",
                        showAddButton = false,
                        onAddClick = { }
                    )
                } else {

                    val filteredList = remember(searchQuery.value, list) {
                        if (searchQuery.value.isBlank()) {
                            list
                        } else {
                            list.filter {
                                it.product_name!!.contains(searchQuery.value, ignoreCase = true)
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            TopHeaderAllProducts(
                                searchQuery = searchQuery.value,
                                onSearchQueryChange = { searchQuery.value = it },
                                onOrdersClick = { nav.push(MyOrdersScreen) },
                                onCartClick = { nav.push(CartScreen(viewModel)) },
                                cartViewModel = viewModel
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        items(filteredList) { product ->
                            ItemCard(
                                product,
                                onItemClick = {
                                    selectedProduct.value = product
                                    showProductInfo.value = true
                                },
                                onButtonClick = {
                                    if (viewModel.isProductInCart(product)) {
                                        viewModel.removeProduct(product)
                                    } else {
                                        viewModel.addProduct(product)
                                    }
                                },
                                viewModel = viewModel,
                            )
                        }
                    }
                }

            }
            if (showProductInfo.value) {
                selectedProduct.value?.let {
                    ShowProductInfo(
                        showProductInfo = showProductInfo,
                        product = it,
                        cartViewModel = viewModel,
                        onButtonClick = {
                            if (viewModel.isProductInCart(it)) {
                                viewModel.removeProduct(it)
                            } else {
                                viewModel.addProduct(it)
                            }
                        })

                }
            }
        }
    }
}

@Composable
fun TopHeaderAllProducts(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOrdersClick: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {

                ToggleIconButton(
                    isOn = cartViewModel.showImage.value,
                    onToggle = { cartViewModel.changeShowImage(it) }
                )

                IconButton(
                    onClick = onOrdersClick,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Your Orders",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                BadgedBox(
                    badge = {
                        val count = cartViewModel.getTotalProductCount()
                        if (count > 0) {
                            Badge { Text("$count") }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Your Cart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
