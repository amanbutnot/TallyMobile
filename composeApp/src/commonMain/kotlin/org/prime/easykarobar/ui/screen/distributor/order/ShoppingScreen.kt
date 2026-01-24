package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.getCategoryImage
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.tally.GetProductsForDis
import org.tally.ProductCategoriesForDis
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.splashImage


object ShoppingScreen : Screen {


    @Composable
    override fun Content() {


        MyOrderTabContent()

    }

    @Composable
    @OptIn(InternalVoyagerApi::class, ExperimentalMaterial3Api::class)
    fun MyOrderTabContent() {

        var searchQuery by remember { mutableStateOf("") }
        val viewModel: OrderViewModel = viewModel { OrderViewModel() }
        val state by viewModel.orderState
        val cartViewModel: CartViewModel = viewModel { CartViewModel() }
        val nav = LocalNavigator.currentOrThrow
        val showProductInfo = remember { mutableStateOf(false) }
        val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }

        val db = DatabaseHolder.instance
        val list = db.productsQueries.getProductsForDis(groupCode = null).executeAsList()
        val categoryList = db.productsQueries.productCategoriesForDis().executeAsList()


        val filteredProducts = remember(searchQuery, list) {
            if (searchQuery.isBlank()) {
                list
            } else {
                list.filter {
                    it.product_name!!.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background).systemBarsPadding()
        ) {
            TopHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onOrdersClick = {
                    nav.push(MyOrdersScreen)
                },
                onCartClick = {
                    println(cartViewModel.getAllProducts())
                    nav.push(CartScreen(cartViewModel))

                },
                modifier = Modifier.padding(16.dp)
            )



            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategoryGrid(
                        categories = categoryList, onCategoryClick = { category ->
                            nav.push(
                                AllProductScreen(
                                    category.Name.toString(),

                                    cartViewModel, productCode = category.GUID?.toDouble() ?: 0.0
                                )
                            )
                        }, product = filteredProducts
                    )
                }




                items(categoryList) { section ->
                    CategoryItemsSection(
                        category = section, onItemClick = { item ->
                            selectedProduct.value = item
                            showProductInfo.value = true


                        }, onMoreClick = { category ->
                            nav.push(
                                AllProductScreen(
                                    category.Name.toString(),
                                    cartViewModel,
                                    productCode = category.GUID?.toDouble() ?: 0.0,
                                )
                            )
                        }, product = filteredProducts
                    )

                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }
        if (showProductInfo.value) {
            selectedProduct.value?.let {
                ShowProductInfo(showProductInfo, it)

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShowProductInfo(showProductInfo: MutableState<Boolean>, product: GetProductsForDis) {
        val colorScheme = MaterialTheme.colorScheme
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = { showProductInfo.value = false },
            sheetState = sheetState,
            containerColor = colorScheme.surface,
            tonalElevation = 4.dp,
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(width = 32.dp, height = 4.dp)
                    )
                }
            }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    val fullUrl = getProductImage(
                        storeId = SharedPrefs.User.get()?.ID.toString(),
                        guid = product.product_id.toString()
                    )
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = product.product_name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = product.product_name.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = product.product_description.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "₹${product.sales_price}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colorScheme.primary
                                )
                                if (product.MRP != product.sales_price) {
                                    Text(
                                        text = "${product.MRP}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (product.discount.toString() != "0") {
                                Surface(
                                    color = colorScheme.secondary, shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "${product.discount}% OFF",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colorScheme.onSecondary,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp, vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showProductInfo.value = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Close", style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }


    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun TopHeader(
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        onOrdersClick: () -> Unit,
        onCartClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val cartViewModel: CartViewModel = viewModel { CartViewModel() }



        Column(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Products",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.fillMaxWidth().weight(2f)
                )


                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = onOrdersClick, modifier = Modifier.background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Your Orders",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    BadgedBox(
                        badge = {
                            if (cartViewModel.getTotalProductCount() > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text("${cartViewModel.getTotalProductCount()}")
                                }
                            }
                        }) {
                        IconButton(
                            onClick = onCartClick, modifier = Modifier.background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchField(
        query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier
    ) {
        OutlinedTextField(
            maxLines = 1,
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier,
            placeholder = {
                Text(
                    text = "Search products...",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
    }

    @Composable
    private fun CategoryGrid(
        categories: List<ProductCategoriesForDis>,
        onCategoryClick: (ProductCategoriesForDis) -> Unit,
        product: List<GetProductsForDis>
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val nonEmptyCategories = categories


            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (nonEmptyCategories.size > 3) 240.dp else 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nonEmptyCategories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        product = product
                    )
                }
            }
        }
    }


    @Composable
    private fun CategoryCard(
        category: ProductCategoriesForDis,
        onClick: () -> Unit,
        product: List<GetProductsForDis>
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp).clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val fullUrl =
                getCategoryImage(SharedPrefs.User.get()?.ID.toString(), category.GUID.toString())
            AsyncImage(
                model = fullUrl,
                contentDescription = category.Name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onLoading = {}
            )
            Text(
                text = category.Name.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }


    @Composable
    private fun CategoryItemsSection(
        category: ProductCategoriesForDis,
        product: List<GetProductsForDis>,
        onItemClick: (GetProductsForDis) -> Unit,
        onMoreClick: (ProductCategoriesForDis) -> Unit,

        ) {
        val cartViewModel: CartViewModel = viewModel { CartViewModel() }
        val filteredProducts = product.filter {
            it.category_id?.toDouble() == category.GUID?.toDouble()
        }
        println(product)
        println(filteredProducts)
        if (filteredProducts.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.Name.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = {
                            onMoreClick(category)
                        }) {
                        Text(
                            text = "More",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    val filteredProducts =
                        product.take(5)
                    items(filteredProducts) { item ->
                        ItemCard(
                            viewModel = cartViewModel,
                            item = item,
                            onItemClick = { onItemClick(item) },
                            onButtonClick = {
                                if (cartViewModel.isProductInCart(item)) {
                                    cartViewModel.removeProduct(item)
                                } else {

                                    cartViewModel.addProduct(item)
                                }
                            },
                        )
                    }
                }
            }
        }

    }

    @Composable
    fun ItemCard(
        item: GetProductsForDis,
        onItemClick: () -> Unit,
        onButtonClick: () -> Unit,
        viewModel: CartViewModel
    ) {
        Card(
            modifier = Modifier.width(160.dp).clickable { onItemClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp).background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ).border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ), contentAlignment = Alignment.Center
                ) {
                    val fullUrl = getProductImage(
                        storeId = SharedPrefs.User.get()?.ID.toString(),
                        guid = item.product_id.toString()
                    )
                    AsyncImage(
                        model = fullUrl.toString(),
                        contentDescription = item.product_name,
                        modifier = Modifier.wrapContentSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        onError = { println(it.result.throwable) },
                        filterQuality = FilterQuality.Medium,
                        fallback = painterResource(Res.drawable.splashImage)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.product_name.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.product_description.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.sales_price.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onButtonClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(

                        containerColor = if (viewModel.isProductInCart(item)) MaterialTheme.colorScheme.error.copy(
                            alpha = 0.4f
                        ) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isProductInCart(item)) Icons.Default.Remove else Icons.Default.ShoppingCart,
                        contentDescription = "Add to cart",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (viewModel.isProductInCart(item)) "Remove Item" else " Add to Cart ",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
