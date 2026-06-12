package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.QuantityTextField
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.prime.easykarobar.ui.shared.globalShared.parseToDoubleList
import org.tally.GetProductsForDis
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.category_placeholder

data class AllProductsPremiumScreen(
    val categoryName: String? = null,
    val productCode: Double? = null,
    val isTab: Boolean
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = if (isTab) (navigator.parent?.parent ?: navigator.parent) else navigator
        val cartViewModel = nav?.rememberNavigatorScreenModel { CartViewModel() }

        Scaffold(
            containerColor = Color.White,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { (nav ?: navigator).pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1A1C1E)
                            )
                        }
                        Text(
                            text = categoryName ?: "All Products",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color(0xFF1A1C1E),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        BadgedBox(
                            badge = {
                                if ((cartViewModel?.getTotalProductCount() ?: 0) > 0) {
                                    Badge(
                                        containerColor = Color(0xFFE53935),
                                        contentColor = Color.White
                                    ) {
                                        Text(cartViewModel?.getTotalProductCount().toString())
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { (nav ?: navigator).push(CartScreen) }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color(0xFF1A1C1E)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                if (cartViewModel != null) {
                    AllProductsPremiumContent(cartViewModel)
                }
            }
        }
    }

    @Composable
    fun AllProductsPremiumContent(cartViewModel: CartViewModel) {
        val db = DatabaseHolder.instance
        val showProductInfo = remember { mutableStateOf(false) }
        val selectedProduct = remember { mutableStateOf<GetProductsForDis?>(null) }

        var searchQuery by remember { mutableStateOf("") }
        var selectedPriceRange by remember { mutableStateOf<Pair<Double, Double>?>(null) }

        val perms = SharedPrefs.Permissions.get()
        val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
        val groupCodes = perms?.ConfigAGRP.parseToDoubleList()

        val productList = remember(productCode) {
            db.productsQueries.getProductsForDis(
                filterGroup = filterAGRP,
                groupCodes = groupCodes,
                productCode = productCode
            ).executeAsList()
        }

        val priceRanges = remember(productList) {
            val maxPrice = productList.maxOfOrNull { it.sales_price ?: 0.0 } ?: 0.0
            val ranges = mutableListOf<Pair<Double, Double>>()
            if (maxPrice > 0) {
                val step = when {
                    maxPrice <= 250 -> 50.0
                    maxPrice <= 1000 -> 250.0
                    maxPrice <= 5000 -> 1000.0
                    maxPrice <= 20000 -> 5000.0
                    else -> 10000.0
                }
                var current = 0.0
                while (current < maxPrice) {
                    ranges.add(current to (current + step))
                    current += step
                }
            }
            ranges
        }

        val filteredProducts = remember(searchQuery, selectedPriceRange, productList) {
            productList.filter { product ->
                val matchesSearch = searchQuery.isEmpty() || product.product_name?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true
                val matchesPrice = selectedPriceRange == null || (
                        (product.sales_price ?: 0.0) >= selectedPriceRange!!.first &&
                                (product.sales_price ?: 0.0) <= selectedPriceRange!!.second
                        )
                matchesSearch && matchesPrice
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Clean Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F4F7)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search products...",
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

            if (priceRanges.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F4F7)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedPriceRange?.let { "Price: ${it.first.toInt()} - ${it.second.toInt()}" } ?: "All Prices",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1A1C1E),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF667085)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Prices") },
                            onClick = {
                                selectedPriceRange = null
                                expanded = false
                            }
                        )
                        priceRanges.forEach { range ->
                            DropdownMenuItem(
                                text = { Text("${range.first.toInt()} - ${range.second.toInt()}") },
                                onClick = {
                                    selectedPriceRange = range
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    PremiumProductItem(
                        product = product,
                        cartViewModel = cartViewModel,
                        onClick = {
                            selectedProduct.value = product
                            showProductInfo.value = true
                        }
                    )
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
    fun PremiumProductItem(
        product: GetProductsForDis,
        cartViewModel: CartViewModel,
        onClick: () -> Unit
    ) {
        val inCart = cartViewModel.isProductInCart(product)
        val quantity = cartViewModel.getProductQuantity(product)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
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

                if (product.MRP != 0.0 && product.MRP != product.sales_price) {
                    val discount = product.MRP?.takeIf { it != 0.0 }?.let { mrp ->
                        ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
                    }
                    if (discount != null && discount > 0) {
                        Surface(
                            color = Color(0xFFE53935),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "${discount.toInt()}% OFF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${product.sales_price?.formatToAmtDec()}",
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

            if (inCart) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .border(
                            1.dp,
                            Color(0xFF004D40),
                            RoundedCornerShape(8.dp)
                        ),
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
                            tint = Color(0xFF004D40)
                        )
                    }
                    QuantityTextField(
                        quantity = quantity,
                        onQuantityChange = {
                            cartViewModel.updateQuantity(product, it)
                        },
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
                            tint = Color(0xFF004D40)
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
                        containerColor = Color(0xFF004D40),
                        contentColor = Color.White
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
