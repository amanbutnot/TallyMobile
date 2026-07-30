package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.business.viewmodel.WishlistViewModel
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.productListHtml
import org.prime.easykarobar.ui.shared.composables.QuantityTextField
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.prime.easykarobar.ui.shared.globalShared.parseToDoubleList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductsForDis
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.category_placeholder

data class AllProductsPremiumScreen(
    val categoryName: String? = null,
    val productCode: Double? = null,
    val isTab: Boolean,
    val productGuids: List<String>? = null
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = if (isTab) (navigator.parent?.parent ?: navigator.parent) else navigator
        val cartViewModel = nav?.rememberNavigatorScreenModel { CartViewModel() }

        if (cartViewModel != null && nav != null) {
            AllProductsPremiumContent(cartViewModel, nav)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AllProductsPremiumContent(cartViewModel: CartViewModel, nav: Navigator) {
        val db = DatabaseHolder.instance
        val wishlistViewModel = nav.rememberNavigatorScreenModel { WishlistViewModel() }

        LaunchedEffect(Unit) {
            wishlistViewModel.getWishlist()
        }

        val showProductInfo = remember { mutableStateOf(false) }
        val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }
        val scope = rememberCoroutineScope()
        var isSharing by remember { mutableStateOf(false) }

        var showRangeSlider by remember { mutableStateOf(false) }
        var showSortSheet by remember { mutableStateOf(false) }
        var sortOrder by remember { mutableStateOf("Default") }
        var isTwoPerRow by remember { mutableStateOf(SharedPrefs.ProductLayout.get()) }

        val perms = SharedPrefs.Permissions.get()
        val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
        val groupCodes = perms?.ConfigAGRP.parseToDoubleList()

        var currentCategoryCode by remember { mutableStateOf(productCode) }
        var currentCategoryName by remember { mutableStateOf(categoryName ?: "All Products") }
        var currentProductGuids by remember { mutableStateOf(productGuids) }

        val categories = remember {
            db.productsQueries.productCategoriesForDis(
                filterGroup = filterAGRP,
                groupCodes = groupCodes
            ).executeAsList()
        }

        val productList = remember(currentCategoryCode, currentProductGuids) {
            if (currentProductGuids != null) {
                db.productsQueries.getProductsByGuidsForDis(currentProductGuids!!) { product_id, hospital_id, product_name, category_id, unit_id, sales_price, MRP, purchase_price, discount, gst_tax_percentage, product_description, created_at, updated_at, discounted_price ->
                    GetProductsForDis(
                        product_id,
                        hospital_id,
                        product_name,
                        category_id,
                        unit_id,
                        sales_price,
                        MRP,
                        purchase_price,
                        discount,
                        gst_tax_percentage,
                        product_description,
                        created_at,
                        updated_at,
                        discounted_price
                    )
                }.executeAsList()
            } else {
                db.productsQueries.getProductsForDis(
                    filterGroup = filterAGRP,
                    groupCodes = groupCodes,
                    productCode = currentCategoryCode
                ).executeAsList()
            }
        }

        val maxPrice = remember(productList) {
            productList.maxOfOrNull { it.sales_price ?: 0.0 }?.toFloat() ?: 1000f
        }

        var priceRange by remember(maxPrice) {
            mutableStateOf(0f..maxPrice)
        }

        val filteredProducts = remember(priceRange, productList, sortOrder) {
            val filtered = productList.filter { product ->
                val price = product.sales_price ?: 0.0
                val matchesPrice = price >= priceRange.start && price <= priceRange.endInclusive
                matchesPrice
            }

            when (sortOrder) {
                "Price: Low to High" -> filtered.sortedBy { it.sales_price ?: 0.0 }
                "Price: High to Low" -> filtered.sortedByDescending { it.sales_price ?: 0.0 }
                else -> filtered
            }
        }

        Scaffold(
            containerColor = Color(0xFFF8F9FB),
            topBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { nav.pop() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF1A1C1E)
                                )
                            }
                            Text(
                                text = currentCategoryName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color(0xFF1A1C1E),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        handlePdfAction(
                                            fileName = currentCategoryName.replace(" ", "_"),
                                            htmlContent = productListHtml(
                                                categoryName = currentCategoryName,
                                                products = filteredProducts,
                                                storeId = SharedPrefs.User.get()?.ID.toString()
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
                                        Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color(0xFF1A1C1E)
                                    )
                                }
                            }

                            BadgedBox(
                                badge = {
                                    if (cartViewModel.getTotalProductCount() > 0) {
                                        Badge(
                                            containerColor = Color(0xFFE53935),
                                            contentColor = Color.White
                                        ) {
                                            Text(cartViewModel.getTotalProductCount().toString())
                                        }
                                    }
                                },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                IconButton(
                                    onClick = { nav.push(CartScreen) }
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = Color(0xFF1A1C1E),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(bottom = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                CategoryChip(
                                    name = "All",
                                    isSelected = currentCategoryCode == null && currentProductGuids == null,
                                    onClick = {
                                        currentCategoryCode = null
                                        currentCategoryName = "All Products"
                                        currentProductGuids = null
                                    }
                                )
                            }
                            items(categories) { category ->
                                CategoryChip(
                                    name = category.Name ?: "",
                                    isSelected = currentCategoryCode == category.GUID?.toDoubleOrNull() && currentProductGuids == null,
                                    onClick = {
                                        currentCategoryCode = category.GUID?.toDoubleOrNull()
                                        currentCategoryName = category.Name ?: ""
                                        currentProductGuids = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F9FB))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        // High-end Filter and Sort Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterSortButton(
                                text = "Filter Price",
                                icon = Icons.Default.FilterList,
                                isActive = showRangeSlider || priceRange.start > 0f || priceRange.endInclusive < maxPrice,
                                onClick = { showRangeSlider = !showRangeSlider },
                                modifier = Modifier.weight(1f)
                            )

                            FilterSortButton(
                                text = if (sortOrder == "Default") "Sort" else sortOrder,
                                icon = Icons.AutoMirrored.Filled.Sort,
                                isActive = sortOrder != "Default",
                                onClick = { showSortSheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showRangeSlider,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Price Range",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                )
                                Text(
                                    "₹${priceRange.start.toInt()} - ₹${priceRange.endInclusive.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF004D40)
                                    )
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            RangeSlider(
                                value = priceRange,
                                onValueChange = { priceRange = it },
                                valueRange = 0f..maxPrice,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF004D40),
                                    activeTrackColor = Color(0xFF004D40),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.height(24.dp), steps = 10
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "₹0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    "₹${maxPrice.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Results count
                Text(
                    text = "${filteredProducts.size} Products found",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isTwoPerRow) 2 else 1),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isTwoPerRow) 24.dp else 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        val isInCart = cartViewModel.isProductInCart(product)
                        val isInWishlist =
                            wishlistViewModel.listState.value.data?.any { it.item_name == product.product_id } == true

                        PremiumProductItem(
                            product = product,
                            cartViewModel = cartViewModel,
                            wishlistViewModel = wishlistViewModel,
                            isInCart = isInCart,
                            isInWishlist = isInWishlist,
                            isTwoPerRow = isTwoPerRow,
                            onClick = {
                                selectedProduct.value = product
                                showProductInfo.value = true
                            }
                        )
                    }
                }
            }
        }

        if (showProductInfo.value) {
            selectedProduct.value?.let {
                ShoppingScreen.ShowProductInfo(
                    showProductInfo = showProductInfo,
                    product = it,
                    cartViewModel = cartViewModel,
                    onButtonClick = {
                        if (cartViewModel.isProductInCart(it)) {
                            cartViewModel.removeProduct(it)
                        } else {
                            cartViewModel.addProduct(it)
                        }
                    },
                    wishlistViewModel = wishlistViewModel
                )
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color.White,
                dragHandle = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                        )
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Text(
                        "Sort Products By",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    listOf(
                        "Default" to null,
                        "Price: Low to High" to "Low to High",
                        "Price: High to Low" to "High to Low"
                    ).forEach { (option, _) ->
                        val isSelected = sortOrder == option
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    sortOrder = option
                                    showSortSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFF0F9F6) else Color.Transparent,
                            border = if (isSelected) BorderStroke(
                                1.dp,
                                Color(0xFF004D40).copy(0.2f)
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF004D40) else Color(
                                            0xFF475569
                                        )
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF004D40),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FilterSortButton(
        text: String,
        icon: ImageVector,
        isActive: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier
                .height(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) Color(0xFFF0F9F6) else Color.White,
            border = BorderStroke(
                width = 1.dp,
                color = if (isActive) Color(0xFF004D40) else Color(0xFFE2E8F0)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isActive) Color(0xFF004D40) else Color(0xFF64748B)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF004D40) else Color(0xFF475569)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    fun PremiumProductItem(
        product: GetProductsForDis,
        cartViewModel: CartViewModel,
        wishlistViewModel: WishlistViewModel,
        isInCart: Boolean,
        isInWishlist: Boolean,
        isTwoPerRow: Boolean = true,
        onClick: () -> Unit
    ) {
        if (isTwoPerRow) {
            VerticalPremiumProductItem(
                product = product,
                cartViewModel = cartViewModel,
                wishlistViewModel = wishlistViewModel,
                isInCart = isInCart,
                isInWishlist = isInWishlist,
                onClick = onClick
            )
        } else {
            HorizontalPremiumProductItem(
                product = product,
                cartViewModel = cartViewModel,
                wishlistViewModel = wishlistViewModel,
                isInCart = isInCart,
                isInWishlist = isInWishlist,
                onClick = onClick
            )
        }
    }

    @Composable
    private fun VerticalPremiumProductItem(
        product: GetProductsForDis,
        cartViewModel: CartViewModel,
        wishlistViewModel: WishlistViewModel,
        isInCart: Boolean,
        isInWishlist: Boolean,
        onClick: () -> Unit
    ) {
        val quantity = cartViewModel.getProductQuantity(product)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Image only, no overlay ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F5FF),
                    border = null
                ) {
                    AsyncImage(
                        model = getProductImage(
                            storeId = SharedPrefs.User.get()?.ID.toString(),
                            guid = product.product_id.toString()
                        ),
                        contentDescription = product.product_name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit,
                        fallback = painterResource(Res.drawable.category_placeholder),
                        error = painterResource(Res.drawable.category_placeholder)
                    )
                }

                // --- Discount badge (far left) + Wishlist button (far right), same row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val discount = product.MRP?.takeIf { it != 0.0 && it != product.sales_price }?.let { mrp ->
                        ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (discount != null && discount > 0) {
                            Text(
                                text = "${discount.toInt()}% OFF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isInWishlist) {
                                wishlistViewModel.deleteWishlist(product.product_id.toString())
                            } else {
                                wishlistViewModel.addWishlist(
                                    itemGuid = product.product_id.toString(),
                                    groupGuid = product.category_id.toString()
                                )
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Title ---
                Text(
                    text = product.product_name.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // --- Price + MRP ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.sales_price?.formatToAmtDec() ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF1A1C1E)
                    )

                    if (product.MRP != 0.0 && product.MRP != product.sales_price) {
                        Text(
                            text = "${product.MRP?.formatToAmtDec()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = Color(0xFF667085),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isInCart) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                color = Color(0xFF1A1C1E)
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
                    Button(
                        onClick = { cartViewModel.addProduct(product) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HorizontalPremiumProductItem(
        product: GetProductsForDis,
        cartViewModel: CartViewModel,
        wishlistViewModel: WishlistViewModel,
        isInCart: Boolean,
        isInWishlist: Boolean,
        onClick: () -> Unit
    ) {
        val quantity = cartViewModel.getProductQuantity(product)
        val discount = product.MRP?.takeIf { it != 0.0 && it != product.sales_price }?.let { mrp ->
            ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image Section
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8F9FB)
                    ) {
                        AsyncImage(
                            model = getProductImage(
                                storeId = SharedPrefs.User.get()?.ID.toString(),
                                guid = product.product_id.toString()
                            ),
                            contentDescription = product.product_name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit,
                            fallback = painterResource(Res.drawable.category_placeholder),
                            error = painterResource(Res.drawable.category_placeholder)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Content Section
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = product.product_name.orEmpty(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            color = Color(0xFF1A1C1E),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 24.dp) // Space for wishlist button
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Unit Placeholder
                        Surface(
                            color = Color(0xFFF2F4F7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.unit_id?.toString() ?: "Unit",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (product.MRP != 0.0 && product.MRP != product.sales_price) {
                                    Text(
                                        text = "₹${product.MRP?.formatToAmtDec()}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = TextDecoration.LineThrough,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "₹${product.sales_price?.formatToAmtDec()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color(0xFF1A1C1E)
                                )
                            }

                            // Add Button
                            if (isInCart) {
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { cartViewModel.decreaseQuantity(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.HorizontalRule,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = quantity.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { cartViewModel.increaseQuantity(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    onClick = { cartViewModel.addProduct(product) },
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "Add",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Icon(
                                            Icons.Default.Add,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Overlay Elements (Not on top of image)
                if (discount != null && discount > 0) {
                    Text(
                        text = "${discount.toInt()}% OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.TopStart)
                    )
                }

                IconButton(
                    onClick = {
                        if (isInWishlist) {
                            wishlistViewModel.deleteWishlist(product.product_id.toString())
                        } else {
                            wishlistViewModel.addWishlist(
                                itemGuid = product.product_id.toString(),
                                groupGuid = product.category_id.toString()
                            )
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    private fun CategoryChip(
        name: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier.clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Color(0xFF1A1C1E) else Color(0xFFF1F3F5),
            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Text(
                text = name,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF1A1C1E),
                    fontSize = 14.sp
                )
            )
        }
    }
}
