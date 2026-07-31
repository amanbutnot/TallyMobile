package org.prime.easykarobar.ui.screen.easymart

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.WishlistViewModel
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.QuantityTextField
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.tally.GetProductsForDis
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.category_placeholder

object WishlistScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val viewModel = nav.rememberNavigatorScreenModel { WishlistViewModel() }
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val state = viewModel.listState.value

        // Fetch products from local DB to match with wishlist items
        val allProducts = remember {
            DatabaseHolder.instance.productsQueries.getProductsForDis(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes(),
                productCode = null
            ).executeAsList()
        }

        LaunchedEffect(Unit) {
            viewModel.getWishlist()
        }


            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (!state.error.isNullOrEmpty()) {
                    Text(
                        text = state.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.data.isNullOrEmpty()) {
                    Text(
                        "Your wishlist is empty",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.data,
                            key = { it.id ?: it.hashCode() }
                        ) { wishItem ->
                            val product = allProducts.find { it.product_id == wishItem.item_name }
                            if (product != null) {
                                WishlistCard(
                                    product = product,
                                    cartViewModel = cartViewModel,
                                    onDelete = {
                                        product.product_id?.let { viewModel.deleteWishlist(it) }
                                    },
                                    onClick = {
                                        // Maybe navigate to product details if available
                                    }
                                )
                            } else {
                                // If product not in local DB, show a simplified card or skip
                                Text("Product ID: ${wishItem.item_name} not found locally")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WishlistCard(
        product: GetProductsForDis,
        cartViewModel: CartViewModel,
        onDelete: () -> Unit,
        onClick: () -> Unit
    ) {
        val storeId = SharedPrefs.User.get()?.ID.toString()
        val imageUrl = getProductImage(storeId, product.product_id.toString())
        val isInCart = cartViewModel.isProductInCart(product)
        val quantity = cartViewModel.getProductQuantity(product)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null, onLoading = { Res.drawable.category_placeholder }, onError = { Res.drawable.category_placeholder },
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.product_name ?: "Unknown Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Price: ${product.sales_price?.formatToAmtDec()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isInCart) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(36.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { cartViewModel.decreaseQuantity(product) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HorizontalRule,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        QuantityTextField(
                            quantity = quantity,
                            onQuantityChange = { cartViewModel.updateQuantity(product, it) },
                            modifier = Modifier.width(30.dp),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(
                            onClick = { cartViewModel.increaseQuantity(product) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { cartViewModel.addProduct(product) }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
