package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getCategoryImage
import org.prime.easykarobar.ui.shared.globalShared.handleBannerClick
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.toValidUrl
import org.tally.BANNER_MASTER
import org.tally.GetProductsForDis
import org.tally.ProductCategoriesForDis
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.category_placeholder
import kotlin.time.Duration.Companion.milliseconds

object CategoryShoppingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }

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
                                text = "Store",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color(0xFF1A1C1E),
                                modifier = Modifier.weight(1f)
                            )

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
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                CategoryShoppingContent()
            }
        }
    }

    @Composable
    fun CategoryShoppingContent() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val db = DatabaseHolder.instance
        val showProductInfo = remember { mutableStateOf(false) }
        val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }
        val sliderMaster = db.slide_MasterQueries.selectAll().executeAsOneOrNull()
        val sliderImages = if (sliderMaster != null) {
            db.slide_MasterQueries.selectImagesBySlideId(sliderMaster.ID).executeAsList()
        } else {
            emptyList()
        }
        val bannerImages = db.banner_MasterQueries.selectAll().executeAsList()
        val urlProvider = LocalUriHandler.current
        val realSize = if (sliderImages.isEmpty()) 1 else sliderImages.size
        val startPage = Int.MAX_VALUE / 2

        val pagerState = rememberPagerState(
            initialPage = startPage - (startPage % realSize),
            pageCount = { Int.MAX_VALUE }
        )

        LaunchedEffect(Unit) {
            while (true) {
                delay(3000.milliseconds)
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1
                )
            }
        }


        var searchQuery by remember { mutableStateOf("") }

        val categoryList = remember {
            db.productsQueries.productCategoriesForDis(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes()
            ).executeAsList()
        }

        val productList = remember {
            db.productsQueries.getProductsForDis(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes(),
                productCode = null
            ).executeAsList()
        }

        val filteredCategories = remember(searchQuery, categoryList) {
            if (searchQuery.isEmpty()) categoryList
            else categoryList.filter { it.Name?.contains(searchQuery, ignoreCase = true) == true }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F4F7)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search products, categories...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF667085)
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF667085),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF1A1C1E)
                        ),
                        singleLine = true
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = 8.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 4.dp
                        ) {
                            HorizontalPager(state = pagerState) { page ->
                                if (sliderImages.isNotEmpty()) {
                                    val imageIndex = page % realSize
                                    val slide = sliderImages[imageIndex]
                                    AsyncImage(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clickable {
                                                handleBannerClick(
                                                    slideImg = slide,
                                                    nav = nav,
                                                    urlProvider = urlProvider,
                                                    showProductInfo = showProductInfo,
                                                    selectedProduct = selectedProduct
                                                )
                                            },
                                        model = slide.C10,
                                        onLoading = { Res.drawable.category_placeholder },
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        model = Res.drawable.category_placeholder,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Row(
                            Modifier
                                .padding(bottom = 12.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(realSize) { iteration ->
                                val color =
                                    if (pagerState.currentPage % realSize == iteration) Color.White else Color.White.copy(
                                        alpha = 0.5f
                                    )
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(6.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    bannerImages.forEach {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 4.dp
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp).clickable {
                                        handleBannerClick(
                                            banner = it,
                                            nav = nav,
                                            urlProvider = urlProvider,
                                            showProductInfo = showProductInfo,
                                            selectedProduct = selectedProduct
                                        )
                                    },
                                model = it.C10,
                                onLoading = { Res.drawable.category_placeholder },
                                contentDescription = null,
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                }
                // Categories Grid at Top
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        CategoriesGrid(
                            categories = filteredCategories,
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category Sections with Products
                items(filteredCategories) { category ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        CategoryProductSection(
                            category = category,
                            products = productList.filter { it.category_id?.toDouble() == category.GUID?.toDouble() },
                            cartViewModel = cartViewModel,
                            onItemClick = { item ->
                                selectedProduct.value = item
                                showProductInfo.value = true
                            },
                            onMoreClick = {
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
                    }
                )
            }
        }
    }

    @Composable
    fun CategoriesGrid(
        categories: List<ProductCategoriesForDis>,
        onCategoryClick: (ProductCategoriesForDis) -> Unit
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = "All Categories",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
            )

            // Using a simple grid-like layout for all categories
            val chunks = categories.chunked(4)
            chunks.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { category ->
                        CategoryItem(
                            modifier = Modifier.weight(1f),
                            name = category.Name.orEmpty(),
                            imageUrl = getCategoryImage(
                                SharedPrefs.User.get()?.ID.toString(),
                                category.GUID.toString()
                            ),
                            onClick = { onCategoryClick(category) }
                        )
                    }
                    // Fill empty slots
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    @Composable
    internal fun CategoryItem(
        modifier: Modifier = Modifier,
        name: String,
        imageUrl: String,
        onClick: () -> Unit
    ) {
        Column(
            modifier = modifier
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F5FF),
                border = null
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit,
                    fallback = painterResource(Res.drawable.category_placeholder),
                    error = painterResource(Res.drawable.category_placeholder)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1A1C1E)
            )
        }
    }

    @Composable
    private fun CategoryProductSection(
        category: ProductCategoriesForDis,
        products: List<GetProductsForDis>,
        cartViewModel: CartViewModel,
        onItemClick: (GetProductsForDis) -> Unit,
        onMoreClick: () -> Unit
    ) {
        if (products.isEmpty()) return

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.Name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF1A1C1E)
                )

                TextButton(onClick = onMoreClick) {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF004D40),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Using 2xN grid-like layout for products under categories
            val chunks = products.chunked(2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                chunks.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                AllProductsPremiumScreen(null, null, false).PremiumProductItem(
                                    product = product,
                                    cartViewModel = cartViewModel,
                                    onClick = { onItemClick(product) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

