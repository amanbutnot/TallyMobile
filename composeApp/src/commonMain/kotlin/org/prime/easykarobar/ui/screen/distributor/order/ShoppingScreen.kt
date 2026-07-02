package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.productShareHtml
import org.prime.easykarobar.ui.shared.composables.QuantityTextField
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getCategoryImage
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductsForDis
import org.tally.ProductCategoriesForDis
import org.prime.easykarobar.ui.shared.composables.smartSearch
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.category_placeholder
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
        val nav = LocalNavigator.currentOrThrow
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val showProductInfo = remember { mutableStateOf(false) }
        val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }
        val db = DatabaseHolder.instance
        val list = db.productsQueries.getProductsForDis(
            filterGroup = filterItemGroups(),
            groupCodes = itemGroupCodes(), productCode = null
        ).executeAsList()
        val categoryList = db.productsQueries.productCategoriesForDis(
            filterGroup = filterItemGroups(),
            groupCodes = itemGroupCodes()
        ).executeAsList()


        val filteredProducts =
            smartSearch(
                list = list,
                query = searchQuery,
                selectors = listOf { it.product_name }
            )
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background).systemBarsPadding()
                .navigationBarsPadding()
        ) {
            TopHeader(

                onCartClick = {
                    println(cartViewModel.getAllProducts())
                    nav.push(CartScreen)

                },
                modifier = Modifier.padding(16.dp),
                toggle = cartViewModel.showImage.value,
                onToggle = { cartViewModel.changeShowImage(it) }
            )

            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item {
                        CategoryGrid(
                            categories = categoryList, onCategoryClick = { category ->
                                nav.push(
                                    AllProductScreen(
                                        category.Name.toString(),

                                        productCode = category.GUID?.toDouble() ?: 0.0
                                    )
                                )
                            }, product = filteredProducts, viewModel = cartViewModel
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
                                        productCode = category.GUID?.toDouble() ?: 0.0,
                                    )
                                )
                            }, product = filteredProducts
                        )

                    }
                } else {
                    item {
                        Text(
                            text = "Search Results (${filteredProducts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredProducts.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                ItemCard(
                                    viewModel = cartViewModel,
                                    item = item,
                                    onItemClick = {
                                        selectedProduct.value = item
                                        showProductInfo.value = true
                                    },
                                    onButtonClick = {
                                        if (cartViewModel.isProductInCart(item)) {
                                            cartViewModel.removeProduct(item)
                                        } else {
                                            cartViewModel.addProduct(item)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }
        if (showProductInfo.value) {
            selectedProduct.value?.let {
                ShowProductInfo(
                    showProductInfo = showProductInfo, product = it, cartViewModel = cartViewModel,
                    onButtonClick = {
                        if (cartViewModel.isProductInCart(it)) {
                            cartViewModel.removeProduct(it)
                        } else {
                            cartViewModel.addProduct(it)
                        }
                    },
                )

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShowProductInfo(
        showProductInfo: MutableState<Boolean>,
        product: GetProductsForDis,
        cartViewModel: CartViewModel, onButtonClick: () -> Unit,

        ) {
        val colorScheme = MaterialTheme.colorScheme
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val showImage = cartViewModel.showImage.value

        ModalBottomSheet(
            onDismissRequest = { showProductInfo.value = false },
            sheetState = sheetState,
            containerColor = colorScheme.surface,
            tonalElevation = 2.dp,
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(100)
                ) {
                    Box(Modifier.size(width = 36.dp, height = 4.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(),
                horizontalAlignment = Alignment.Start
            ) {

                val fullUrl = getProductImage(
                    storeId = SharedPrefs.User.get()?.ID.toString(),
                    guid = product.product_id.toString()
                )
                println("Full image url $fullUrl")

                // --- Image collapses fully when OFF
                AnimatedVisibility(
                    visible = showImage,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = product.product_name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                if (showImage) Spacer(Modifier.height(20.dp))

                // --- Product title (clear visual anchor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.product_name.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    var isSharing by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    IconButton(
                        onClick = {
                            scope.launch {
                                handlePdfAction(
                                    fileName = product.product_name ?: "Product",
                                    htmlContent = productShareHtml(
                                        productName = product.product_name ?: "",
                                        price = product.sales_price ?: 0.0,
                                        mrp = product.MRP ?: 0.0,
                                        discount = product.MRP?.takeIf { it != 0.0 }?.let { mrp ->
                                            ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
                                        },
                                        imageUrl = fullUrl,
                                        description = product.product_description
                                    ),
                                    action = PdfAction.Share,
                                    onLoadingChange = { isSharing = it }
                                )
                            }
                        },
                        enabled = !isSharing
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // --- Description (secondary, readable, not overpowering)
                if (!product.product_description.isNullOrBlank()) {
                    Text(
                        text = product.product_description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(18.dp))
                }

                // --- Price block (clean, compact, intentional)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${product.sales_price?.formatToAmtDec()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )

                            if (product.MRP != product.sales_price && product.MRP != 0.0) {
                                Text(
                                    text = "${product.MRP?.formatToAmtDec()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    ),
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (product.MRP != 0.0) {
                            Surface(
                                color = colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            ) {
                                val per = product.MRP?.takeIf { it != 0.0 }?.let { mrp ->
                                    ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
                                }

                                Text(
                                    text = "${per?.formatToAmtDec()}% OFF",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                val inCart = cartViewModel.isProductInCart(product)
                val quantity = cartViewModel.getProductQuantity(product)

                if (inCart) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp)
                            ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { cartViewModel.decreaseQuantity(product) }) {
                            Icon(
                                imageVector = Icons.Default.HorizontalRule,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        QuantityTextField(
                            quantity = quantity,
                            onQuantityChange = {
                                cartViewModel.updateQuantity(product, it)
                            },
                            modifier = Modifier.width(40.dp),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(onClick = { cartViewModel.increaseQuantity(product) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onButtonClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add to cart",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- Close button (neutral, not shouting)
                OutlinedButton(
                    onClick = { showProductInfo.value = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }


    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun TopHeader(
        toggle: Boolean,
        onToggle: (Boolean) -> Unit,
        onCartClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val nav = LocalNavigator.currentOrThrow
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }


        Column(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.pop() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        ""
                    )
                }

                Text(
                    "Products",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.fillMaxWidth().weight(2f)
                )

                ToggleIconButton(
                    isOn = toggle,
                    onToggle = { onToggle(it) },
                    modifier = Modifier.weight(0.5f)
                )

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

@Composable
fun ToggleIconButton(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate background and icon color for smooth transition
    val backgroundColor by animateColorAsState(
        targetValue = if (isOn) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.onError.copy(alpha = 0.05f)
    )
    val iconTint by animateColorAsState(
        targetValue = if (isOn) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    )

    IconButton(
        onClick = { onToggle(!isOn) },
        modifier = modifier
            .size(48.dp) // professional button size
            .background(backgroundColor, CircleShape)
            .clip(CircleShape)
    ) {
        Icon(
            imageVector = if (isOn) Icons.Default.Image else Icons.Default.ImageNotSupported,
            contentDescription = if (isOn) "With Image" else "Without Image",
            tint = iconTint,
            modifier = Modifier.size(24.dp) // icon size proportional to button
        )
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
    product: List<GetProductsForDis>,
    viewModel: CartViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Split list into chunks of 2 (each column has 2 items)
        val columns = categories.chunked(2)
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))


        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(columns) { columnItems ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.width(160.dp) // control column width
                ) {
                    columnItems.forEach { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) },
                            product = product,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
    }
}


@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    category: ProductCategoriesForDis,
    onClick: () -> Unit,
    product: List<GetProductsForDis>,
    viewModel: CartViewModel
) {
    val showImage = viewModel.showImage.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val fullUrl = getCategoryImage(
            SharedPrefs.User.get()?.ID.toString(),
            category.GUID.toString()
        )

        // --- Image only when enabled (no wasted space)
        AnimatedVisibility(
            visible = showImage,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AsyncImage(
                model = fullUrl,
                contentDescription = category.Name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(Res.drawable.splashImage),
                error = painterResource(Res.drawable.category_placeholder)
            )
        }

        if (showImage) Spacer(Modifier.height(8.dp))

        // --- Better styled text
        Text(
            text = category.Name.orEmpty(),
            style = if (showImage)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.titleMedium, // slightly stronger in compact mode
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
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
    val nav = LocalNavigator.currentOrThrow
    val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
    val categoryProducts = product.filter {
        it.category_id?.toDouble() == category.GUID?.toDouble()
    }
    if (categoryProducts.isNotEmpty()) {
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
                val displayItems = categoryProducts.take(5)
                items(displayItems) { item ->
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
                        modifier = Modifier.width(160.dp)
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
    viewModel: CartViewModel,
    modifier: Modifier = Modifier.width(160.dp)
) {
    val showImage = viewModel.showImage.value
    val inCart = viewModel.isProductInCart(item)
    val quantity = viewModel.getProductQuantity(item)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                0.4.dp,
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            val fullUrl = getProductImage(
                storeId = SharedPrefs.User.get()?.ID.toString(),
                guid = item.product_id.toString()
            )

            // --- Image only when enabled (no dead space)
            AnimatedVisibility(
                visible = showImage,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = item.product_name,
                        modifier = Modifier
                            .fillMaxSize()
                            //  .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(Res.drawable.splashImage),
                        onError = { println(it.result.throwable) }
                    )

                    IconButton(
                        onClick = { /* TODO: Wishlist */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showImage) Spacer(Modifier.height(8.dp))

            // --- Product name (stronger hierarchy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.product_name.orEmpty(),
                    style = if (showImage)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                var isSharing by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                IconButton(
                    onClick = {
                        scope.launch {
                            handlePdfAction(
                                fileName = item.product_name ?: "Product",
                                htmlContent = productShareHtml(
                                    productName = item.product_name ?: "",
                                    price = item.sales_price ?: 0.0,
                                    mrp = item.MRP ?: 0.0,
                                    discount = item.MRP?.takeIf { it != 0.0 }?.let { mrp ->
                                        ((mrp - (item.sales_price ?: 0.0)) / mrp) * 100
                                    },
                                    imageUrl = fullUrl,
                                    description = item.product_description
                                ),
                                action = PdfAction.Share,
                                onLoadingChange = { isSharing = it }
                            )
                        }
                    },
                    modifier = Modifier.size(24.dp),
                    enabled = !isSharing
                ) {
                    if (isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


            Spacer(Modifier.height(6.dp))
            val per = item.MRP?.takeIf { it != 0.0 }?.let { mrp ->
                ((mrp - (item.sales_price ?: 0.0)) / mrp) * 100
            }


            // --- Price (visually separated but not screaming)
            Text(
                text = item.sales_price?.formatToAmtDec().toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (per == null) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                    //modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {

                per?.let {
                    Text(
                        text = "${per.toInt()}% OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                }
            }

            Spacer(Modifier.height(10.dp))

            // --- Button that doesn't look like a warning when in cart
            if (inCart) {
                Row(
                    modifier = Modifier.fillMaxWidth().border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    ),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.decreaseQuantity(item) }) {
                        Icon(
                            imageVector = Icons.Default.HorizontalRule,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    QuantityTextField(
                        quantity = quantity,
                        onQuantityChange = {
                            viewModel.updateQuantity(item, it)
                        },
                        modifier = Modifier.width(30.dp),
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = { viewModel.increaseQuantity(item) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Add to cart",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
