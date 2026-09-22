package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.TallyDatabase
import org.prime.easykarobar.business.viewmodel.WishlistViewModel
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getCategoryImage
import org.prime.easykarobar.ui.shared.globalShared.handleBannerClick
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.utils.EasyMartRefreshableBox
import org.prime.easykarobar.ui.utils.pushEasyMart
import org.tally.BANNER_MASTER
import org.tally.FEATURES_MASTER
import org.tally.GetAllSubCategories
import org.tally.GetBrandsForDis
import org.tally.GetProductsForDis
import org.tally.ProductCategoriesForDis
import org.tally.SLIDE_IMG
import org.tally.SLIDE_MASTER
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.brand_placeholder
import tallymobile.composeapp.generated.resources.category_placeholder
import tallymobile.composeapp.generated.resources.subcategory_placeholder
import kotlin.time.Duration.Companion.milliseconds

private val featureSectionPastelColors = listOf(
    Color(0xFFFFF0F3), // Soft Pastel Rose
    Color(0xFFEBF3FE), // Soft Pastel Sky Blue
    Color(0xFFEAFAF1), // Soft Pastel Mint Green
    Color(0xFFF4ECF7), // Soft Pastel Lavender
    Color(0xFFFEF9E7), // Soft Pastel Warm Yellow
    Color(0xFFE8F8F5), // Soft Pastel Cyan Aqua
    Color(0xFFFDF2E9), // Soft Pastel Peach
    Color(0xFFE8EAF6), // Soft Pastel Periwinkle
    Color(0xFFFCE4EC), // Soft Pastel Blush Pink
    Color(0xFFF1F8E9)  // Soft Pastel Sage Green
)

private data class ScreenData<Slide, Banner, Feature>(
    val sliders: List<Slide>,
    val banners: List<Banner>,
    val features: List<Feature>,
    val categories: List<ProductCategoriesForDis>,
    val subcategories: List<GetAllSubCategories>,
    val brands: List<GetBrandsForDis>,
    val products: List<GetProductsForDis>,
    val isLoading: Boolean = true
)

object CategoryShoppingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        var isTwoPerRow by remember { mutableStateOf(SharedPrefs.ProductLayout.get()) }

        Scaffold(
            containerColor = Color(0xFFF8F9FB),
            topBar = {
                Surface(
                    color = Color(0xFFFF6D00),
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
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Store",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            BadgedBox(
                                badge = {
                                    val count = cartViewModel.getTotalProductCount()
                                    if (count > 0.0) {
                                        Badge(
                                            containerColor = Color(0xFFE53935),
                                            contentColor = Color.White
                                        ) {
                                            val displayCount = if (count == count.toLong()
                                                    .toDouble()
                                            ) count.toLong().toString() else count.toString()
                                            Text(displayCount)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                IconButton(
                                    onClick = { nav.pushEasyMart(CartScreen) }
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = Color.White,
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
                CategoryShoppingContent(isTwoPerRow)
            }
        }
    }

    @Composable
    fun CategoryShoppingContent(isTwoPerRow: Boolean) {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }



        EasyMartRefreshableBox(nav = nav) {
            val db = DatabaseHolder.instance
            val showProductInfo = remember { mutableStateOf(false) }
            val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }
            val urlProvider = LocalUriHandler.current

            var searchQuery by remember { mutableStateOf("") }
            var debouncedSearchQuery by remember { mutableStateOf("") }

            LaunchedEffect(searchQuery) {
                delay(300.milliseconds)
                debouncedSearchQuery = searchQuery
            }

            val screenData by produceState(
                initialValue = ScreenData(
                    sliders = emptyList(),
                    banners = emptyList(),
                    features = emptyList(),
                    categories = emptyList(),
                    subcategories = emptyList(),
                    brands = emptyList(),
                    products = emptyList(),
                    isLoading = true
                )
            ) {
                value = withContext(Dispatchers.IO) {
                    ScreenData(
                        sliders = db.slide_MasterQueries.selectAll().executeAsList(),
                        banners = db.banner_MasterQueries.selectAll().executeAsList(),
                        features = db.features_MasterQueries.selectAll().executeAsList(),
                        categories = db.productsQueries.productCategoriesForDis(
                            filterGroup = filterItemGroups(),
                            groupCodes = itemGroupCodes()
                        ).executeAsList(),
                        subcategories = db.product_CategoryQueries.getAllSubCategories().executeAsList(),
                        brands = db.productsQueries.getBrandsForDis(
                            filterGroup = filterItemGroups(),
                            groupCodes = itemGroupCodes(),
                            productCode = null
                        ).executeAsList(),
                        products = db.productsQueries.getProductsForDis(
                            filterGroup = filterItemGroups(),
                            groupCodes = itemGroupCodes(),
                            productCode = null,
                            changePrice = SharedPrefs.ChangePrice.get(),
                            mapper = ::GetProductsForDis
                        ).executeAsList(),
                        isLoading = false
                    )
                }
            }

            val filteredProducts = remember(screenData.products, debouncedSearchQuery) {
                if (debouncedSearchQuery.isBlank()) {
                    screenData.products
                } else {
                    screenData.products.filter {
                        it.product_name?.contains(debouncedSearchQuery, ignoreCase = true) == true
                    }
                }
            }
            val productsByCategoryId = remember(filteredProducts) {
                filteredProducts.groupBy { it.category_id?.toDouble() }
            }
            val filteredCategories =
                remember(screenData.categories, debouncedSearchQuery, filteredProducts) {
                    if (debouncedSearchQuery.isBlank()) {
                        screenData.categories
                    } else {
                        screenData.categories.filter { category ->
                            val nameMatches = category.Name?.contains(
                                debouncedSearchQuery,
                                ignoreCase = true
                            ) == true
                            val hasProducts =
                                filteredProducts.any { it.category_id == category.GUID?.toDouble() }
                            nameMatches || hasProducts
                        }
                    }
                }

            val filteredSubcategories =
                remember(screenData.subcategories, debouncedSearchQuery) {
                    if (debouncedSearchQuery.isBlank()) {
                        screenData.subcategories
                    } else {
                        screenData.subcategories.filter { subcat ->
                            subcat.CatName.contains(debouncedSearchQuery, ignoreCase = true)
                        }
                    }
                }

            val filteredBrands =
                remember(screenData.brands, debouncedSearchQuery) {
                    if (debouncedSearchQuery.isBlank()) {
                        screenData.brands
                    } else {
                        screenData.brands.filter { brand ->
                            brand.Name?.contains(debouncedSearchQuery, ignoreCase = true) == true
                        }
                    }
                }

            val topSliders = remember(screenData.sliders) {
                screenData.sliders.filter { master -> isTopLevelSlider(master.parent_guid) }
            }

            val categorySlidersMap = remember(screenData.sliders, filteredCategories) {
                filteredCategories.associateWith { category ->
                    screenData.sliders.filter { master ->
                        !isTopLevelSlider(master.parent_guid) && isMatchingCategoryGuid(master.parent_guid, category.GUID)
                    }
                }
            }

            val topBanners = remember(screenData.banners) {
                screenData.banners.filter { banner -> isTopLevelSlider(banner.parent_guid) }
            }

            val categoryBannersMap = remember(screenData.banners, filteredCategories) {
                filteredCategories.associateWith { category ->
                    screenData.banners.filter { banner ->
                        !isTopLevelSlider(banner.parent_guid) && isMatchingCategoryGuid(banner.parent_guid, category.GUID)
                    }
                }
            }

            val featureProductsByFeature = remember(screenData.features, screenData.products) {
                screenData.features.filter { it.C1 != "Open Link" }.associateWith { feature ->
                    when (feature.C1) {
                        "Open Item" -> screenData.products.filter { it.product_id == feature.C2 }
                        "Open Item Group" -> screenData.products.filter { it.category_id == feature.C2?.toDoubleOrNull() }
                        "Select items" -> {
                            val guids = feature.C2?.split(",")?.map { it.trim() } ?: emptyList()
                            screenData.products.filter { it.product_id in guids }
                        }

                        else -> emptyList()
                    }
                }
            }

            val topFeatureProductsByFeature = remember(featureProductsByFeature) {
                featureProductsByFeature.filterKeys { feature ->
                    isTopLevelSlider(feature.parent_guid)
                }
            }

            val categoryFeaturesMap = remember(screenData.features, filteredCategories) {
                filteredCategories.associateWith { category ->
                    screenData.features.filter { feature ->
                        !isTopLevelSlider(feature.parent_guid) && isMatchingCategoryGuid(feature.parent_guid, category.GUID)
                    }
                }
            }

            val wishlistViewModel = nav.rememberNavigatorScreenModel { WishlistViewModel() }
            val wishlistState by wishlistViewModel.listState
            val wishlistGuids = remember(wishlistState.data) {
                wishlistState.data?.mapNotNull { it.item_name }?.toSet() ?: emptySet()
            }

            val cartItems = cartViewModel.cartItems
            val cartGuids = remember(cartItems.size) {
                cartItems.mapNotNull { it.product.product_id }.toSet()
            }

            LaunchedEffect(Unit) {
                wishlistViewModel.getWishlist()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FB))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = {
                            Text(
                                "Search items or categories...",
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6D00),
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color(0xFFFF6D00)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                val listState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(screenData.isLoading) {
                    if (!screenData.isLoading) {
                        listState.scrollToItem(0)
                    }
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(key = "sliders_banners") {
                        if (debouncedSearchQuery.isBlank()) {
                            if (screenData.isLoading) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFFFF6D00))
                                    }
                                }
                            } else {
                                Column {
                                    topSliders.forEach { master ->
                                        var images by remember(master.ID) { mutableStateOf<List<SLIDE_IMG>>(emptyList()) }
                                        LaunchedEffect(master.ID) {
                                            images = withContext(Dispatchers.IO) {
                                                db.slide_MasterQueries.selectImagesBySlideId(master.ID).executeAsList()
                                            }
                                        }
                                        if (images.isNotEmpty()) {
                                            AutoSlidingPager(
                                                images = images,
                                                onImageClick = { slide ->
                                                    handleBannerClick(
                                                        slideImg = slide,
                                                        nav = nav,
                                                        urlProvider = urlProvider,
                                                        showProductInfo = showProductInfo,
                                                        selectedProduct = selectedProduct
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    topBanners.forEach { banner ->
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
                                                    .height(180.dp)
                                                    .clickable {
                                                        handleBannerClick(
                                                            banner = banner,
                                                            nav = nav,
                                                            urlProvider = urlProvider,
                                                            showProductInfo = showProductInfo,
                                                            selectedProduct = selectedProduct
                                                        )
                                                    },
                                                model = banner.C10,
                                                onLoading = { Res.drawable.category_placeholder },
                                                contentDescription = null,
                                                contentScale = ContentScale.FillBounds
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = topFeatureProductsByFeature.keys.toList(),
                        key = { _, feature -> feature.ID }
                    ) { index, feature ->
                        val featureProducts = topFeatureProductsByFeature[feature].orEmpty()
                        if (featureProducts.isNotEmpty()) {
                            val backgroundColor =
                                featureSectionPastelColors[index % featureSectionPastelColors.size]
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = backgroundColor,
                                shadowElevation = 1.dp
                            ) {
                                FeatureSection(
                                    title = feature.CODE,
                                    products = featureProducts,
                                    cartViewModel = cartViewModel,
                                    wishlistViewModel = wishlistViewModel,
                                    isTwoPerRow = isTwoPerRow,
                                    cartGuids = cartGuids,
                                    wishlistGuids = wishlistGuids,
                                    onItemClick = { item ->
                                        selectedProduct.value = item
                                        showProductInfo.value = true
                                    }
                                )
                            }
                        }
                    }

                    item(key = "categories_grid") {
                        if (filteredCategories.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                CategoriesGrid(
                                    categories = filteredCategories,
                                    title = if (debouncedSearchQuery.isBlank()) "All Categories" else "Matching Categories",
                                    onSeeAllClick = {
                                        nav.pushEasyMart(SeeAllCategoriesScreen)
                                    },
                                    onCategoryClick = { category ->
                                        nav.pushEasyMart(
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
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item(key = "subcategories_grid") {
                        if (filteredSubcategories.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                color = Color(0xFFF4ECF7), // Soft Pastel Lavender
                                shadowElevation = 1.dp
                            ) {
                                SubcategoriesGrid(
                                    subcategories = filteredSubcategories,
                                    title = if (debouncedSearchQuery.isBlank()) "All Subcategories" else "Matching Subcategories",
                                    onSeeAllClick = {
                                        nav.pushEasyMart(SeeAllSubcategoriesScreen)
                                    },
                                    onSubcategoryClick = { subcat ->
                                        coroutineScope.launch {
                                            val guids = withContext(Dispatchers.IO) {
                                                db.product_CategoryQueries.getMappingsByGroup(null)
                                                    .executeAsList()
                                                    .filter { it.CatName == subcat.CatName }
                                                    .mapNotNull { it.product_id }
                                            }
                                            nav.pushEasyMart(
                                                AllProductsPremiumScreen(
                                                    categoryName = subcat.CatName,
                                                    productGuids = guids,
                                                    productCode = subcat.GroupCode,
                                                    subCategoryCode = subcat.CatCode,
                                                    isTab = false
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item(key = "brands_grid") {
                        if (filteredBrands.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                color = Color(0xFFEBF3FE), // Soft Pastel Sky Blue
                                shadowElevation = 1.dp
                            ) {
                                BrandsGrid(
                                    brands = filteredBrands,
                                    title = if (debouncedSearchQuery.isBlank()) "All Brands" else "Matching Brands",
                                    onSeeAllClick = {
                                        nav.pushEasyMart(SeeAllBrandsScreen)
                                    },
                                    onBrandClick = { brand ->
                                        coroutineScope.launch {
                                            val guids = withContext(Dispatchers.IO) {
                                                screenData.products
                                                    .filter { it.brand_name == brand.Name }
                                                    .mapNotNull { it.product_id }
                                            }
                                            nav.pushEasyMart(
                                                AllProductsPremiumScreen(
                                                    categoryName = brand.Name,
                                                    productGuids = guids,
                                                    brandName = brand.Name,
                                                    isTab = false
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(
                        items = filteredCategories,
                        key = { category -> category.GUID ?: category.hashCode() }
                    ) { category ->
                        val products = productsByCategoryId[category.GUID?.toDouble()].orEmpty()
                        val categorySliders = categorySlidersMap[category].orEmpty()
                        val categoryBanners = categoryBannersMap[category].orEmpty()
                        val categoryFeatures = categoryFeaturesMap[category].orEmpty()
                        if (products.isNotEmpty() || categorySliders.isNotEmpty() || categoryBanners.isNotEmpty() || categoryFeatures.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                CategoryProductSection(
                                    category = category,
                                    products = products.take(8),
                                    cartViewModel = cartViewModel,
                                    wishlistViewModel = wishlistViewModel,
                                    isTwoPerRow = isTwoPerRow,
                                    cartGuids = cartGuids,
                                    wishlistGuids = wishlistGuids,
                                    categorySliders = categorySliders,
                                    categoryBanners = categoryBanners,
                                    categoryFeatures = categoryFeatures,
                                    featureProductsByFeature = featureProductsByFeature,
                                    db = db,
                                    nav = nav,
                                    urlProvider = urlProvider,
                                    showProductInfo = showProductInfo,
                                    selectedProduct = selectedProduct,
                                    onItemClick = { item ->
                                        selectedProduct.value = item
                                        showProductInfo.value = true
                                    },
                                    onMoreClick = {
                                        nav.pushEasyMart(
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
                            },
                            wishlistViewModel = wishlistViewModel
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun <T> GenericGrid(
        items: List<T>,
        title: String,
        getName: (T) -> String,
        getImageUrl: (T) -> String,
        cardBgColor: Color = Color.White,
        placeholder: DrawableResource = Res.drawable.category_placeholder,
        onSeeAllClick: (() -> Unit)? = null,
        onItemClick: (T) -> Unit
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
            )

            val chunks = remember(items) { items.chunked(3) }
            chunks.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        CategoryItem(
                            modifier = Modifier.weight(1f),
                            name = getName(item),
                            imageUrl = getImageUrl(item),
                            cardBgColor = cardBgColor,
                            placeholder = placeholder,
                            onClick = { onItemClick(item) }
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (onSeeAllClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onSeeAllClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Show All",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF004D40),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun CategoriesGrid(
        categories: List<ProductCategoriesForDis>,
        title: String,
        onSeeAllClick: (() -> Unit)? = null,
        onCategoryClick: (ProductCategoriesForDis) -> Unit
    ) {
        val userId = remember { SharedPrefs.User.get()?.ID.toString() }
        GenericGrid(
            items = categories.take(9), // 3 rows * 3 items
            title = title,
            getName = { it.Name.orEmpty() },
            getImageUrl = { getCategoryImage(userId, it.GUID.toString()) },
            cardBgColor = Color.White,
            onSeeAllClick = onSeeAllClick,
            onItemClick = onCategoryClick
        )
    }

    @Composable
    fun SubcategoriesGrid(
        subcategories: List<GetAllSubCategories>,
        title: String,
        onSeeAllClick: (() -> Unit)? = null,
        onSubcategoryClick: (GetAllSubCategories) -> Unit
    ) {
        GenericGrid(
            items = subcategories.take(9),
            title = title,
            getName = { it.CatName },
            getImageUrl = { "" },
            cardBgColor = Color.White,
            placeholder = Res.drawable.subcategory_placeholder,
            onSeeAllClick = onSeeAllClick,
            onItemClick = onSubcategoryClick
        )
    }

    @Composable
    fun BrandsGrid(
        brands: List<GetBrandsForDis>,
        title: String,
        onSeeAllClick: (() -> Unit)? = null,
        onBrandClick: (GetBrandsForDis) -> Unit
    ) {
        GenericGrid(
            items = brands.take(9),
            title = title,
            getName = { it.Name.orEmpty() },
            getImageUrl = { "" },
            cardBgColor = Color.White,
            placeholder = Res.drawable.brand_placeholder,
            onSeeAllClick = onSeeAllClick,
            onItemClick = onBrandClick
        )
    }

    @Composable
    internal fun CategoryItem(
        modifier: Modifier = Modifier,
        name: String,
        imageUrl: String,
        cardBgColor: Color = Color.White,
        placeholder: DrawableResource = Res.drawable.category_placeholder,
        onClick: () -> Unit
    ) {
        Column(
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
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
                color = cardBgColor,
                border = null
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(cardBgColor, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    fallback = painterResource(placeholder),
                    error = painterResource(placeholder)
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
        wishlistViewModel: WishlistViewModel,
        isTwoPerRow: Boolean,
        cartGuids: Set<String?>,
        wishlistGuids: Set<String?>,
        categorySliders: List<SLIDE_MASTER> = emptyList(),
        categoryBanners: List<BANNER_MASTER> = emptyList(),
        categoryFeatures: List<FEATURES_MASTER> = emptyList(),
        featureProductsByFeature: Map<FEATURES_MASTER, List<GetProductsForDis>> = emptyMap(),
        db: TallyDatabase = DatabaseHolder.instance,
        nav: Navigator,
        urlProvider: UriHandler,
        showProductInfo: MutableState<Boolean>,
        selectedProduct: MutableState<GetProductsForDis?>,
        onItemClick: (GetProductsForDis) -> Unit,
        onMoreClick: () -> Unit
    ) {
        if (products.isEmpty() && categorySliders.isEmpty() && categoryBanners.isEmpty() && categoryFeatures.isEmpty()) return

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
                if (products.isNotEmpty()) {
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
            }

            if (categorySliders.isNotEmpty() || categoryBanners.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                categorySliders.forEach { master ->
                    var images by remember(master.ID) { mutableStateOf<List<SLIDE_IMG>>(emptyList()) }
                    LaunchedEffect(master.ID) {
                        images = withContext(Dispatchers.IO) {
                            db.slide_MasterQueries.selectImagesBySlideId(master.ID).executeAsList()
                        }
                    }
                    if (images.isNotEmpty()) {
                        AutoSlidingPager(
                            images = images,
                            onImageClick = { slide ->
                                handleBannerClick(
                                    slideImg = slide,
                                    nav = nav,
                                    urlProvider = urlProvider,
                                    showProductInfo = showProductInfo,
                                    selectedProduct = selectedProduct
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                categoryBanners.forEach { banner ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clickable {
                                    handleBannerClick(
                                        banner = banner,
                                        nav = nav,
                                        urlProvider = urlProvider,
                                        showProductInfo = showProductInfo,
                                        selectedProduct = selectedProduct
                                    )
                                },
                            model = banner.C10,
                            onLoading = { Res.drawable.category_placeholder },
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (products.isNotEmpty() || categoryFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (categoryFeatures.isNotEmpty()) {
                categoryFeatures.forEachIndexed { index, feature ->
                    val featureProducts = featureProductsByFeature[feature].orEmpty()
                    if (featureProducts.isNotEmpty()) {
                        val backgroundColor =
                            featureSectionPastelColors[index % featureSectionPastelColors.size]
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            color = backgroundColor,
                            shadowElevation = 1.dp
                        ) {
                            FeatureSection(
                                title = feature.CODE,
                                products = featureProducts,
                                cartViewModel = cartViewModel,
                                wishlistViewModel = wishlistViewModel,
                                isTwoPerRow = isTwoPerRow,
                                cartGuids = cartGuids,
                                wishlistGuids = wishlistGuids,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }

            if (products.isNotEmpty()) {
                val chunks =
                    remember(products, isTwoPerRow) { products.chunked(if (isTwoPerRow) 2 else 1) }
                val premiumScreen = remember { AllProductsPremiumScreen(isTab = false) }

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
                                val isInCart = product.product_id in cartGuids
                                val isInWishlist = product.product_id in wishlistGuids

                                Box(modifier = Modifier.weight(1f)) {
                                    premiumScreen.PremiumProductItem(
                                        product = product,
                                        cartViewModel = cartViewModel,
                                        wishlistViewModel = wishlistViewModel,
                                        isInCart = isInCart,
                                        isInWishlist = isInWishlist,
                                        isTwoPerRow = isTwoPerRow,
                                        onClick = { onItemClick(product) }
                                    )
                                }
                            }
                            if (rowItems.size < (if (isTwoPerRow) 2 else 1)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FeatureSection(
        title: String,
        products: List<GetProductsForDis>,
        cartViewModel: CartViewModel,
        wishlistViewModel: WishlistViewModel,
        isTwoPerRow: Boolean,
        cartGuids: Set<String?>,
        wishlistGuids: Set<String?>,
        onItemClick: (GetProductsForDis) -> Unit
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF1A1C1E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val premiumScreen = remember { AllProductsPremiumScreen(isTab = false) }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(
                    items = products,
                    key = { product -> product.product_id ?: product.hashCode() }
                ) { product ->
                    val isInCart = product.product_id in cartGuids
                    val isInWishlist = product.product_id in wishlistGuids

                    Box(modifier = Modifier.width(125.dp)) {
                        premiumScreen.PremiumProductItem(
                            product = product,
                            cartViewModel = cartViewModel,
                            wishlistViewModel = wishlistViewModel,
                            isInCart = isInCart,
                            isInWishlist = isInWishlist,
                            isTwoPerRow = true,
                            onClick = { onItemClick(product) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AutoSlidingPager(
        images: List<SLIDE_IMG>,
        onImageClick: (SLIDE_IMG) -> Unit
    ) {
        val realSize = images.size
        val startPage = Int.MAX_VALUE / 2
        val pagerState = rememberPagerState(
            initialPage = startPage - (startPage % realSize),
            pageCount = { Int.MAX_VALUE }
        )
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000.milliseconds)
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
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
                    val imageIndex = page % realSize
                    val slide = images[imageIndex]
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { onImageClick(slide) },
                        model = slide.C10,
                        onLoading = { Res.drawable.category_placeholder },
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
            Row(
                Modifier
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
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

    private fun isTopLevelSlider(parentGuid: String?): Boolean {
        if (parentGuid.isNullOrBlank()) return true
        val trimmed = parentGuid.trim()
        if (trimmed == "0" || trimmed == "0.0") return true
        val doubleVal = trimmed.toDoubleOrNull()
        return doubleVal == 0.0
    }

    private fun isMatchingCategoryGuid(parentGuid: String?, categoryGuid: String?): Boolean {
        if (parentGuid.isNullOrBlank() || categoryGuid.isNullOrBlank()) return false
        val trimmedParent = parentGuid.trim()
        val trimmedCategory = categoryGuid.trim()
        if (trimmedParent.equals(trimmedCategory, ignoreCase = true)) return true
        val parentDouble = trimmedParent.toDoubleOrNull()
        val categoryDouble = trimmedCategory.toDoubleOrNull()
        return parentDouble != null && categoryDouble != null && parentDouble == categoryDouble
    }
}
