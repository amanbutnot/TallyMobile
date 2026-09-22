package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getCategoryImage
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.utils.pushEasyMart
import org.tally.GetAllSubCategories
import org.tally.GetBrandsForDis
import org.tally.GetProductsForDis
import org.tally.ProductCategoriesForDis

object SeeAllSubcategoriesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val db = DatabaseHolder.instance
        val coroutineScope = rememberCoroutineScope()

        val subcategories by produceState<List<GetAllSubCategories>>(initialValue = emptyList()) {
            value = withContext(Dispatchers.IO) {
                db.product_CategoryQueries.getAllSubCategories().executeAsList()
            }
        }

        Scaffold(
            topBar = {
                Surface(color = Color(0xFFFF6D00), shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "All Subcategories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
                            color = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF8F9FB))) {
                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    item {
                        CategoryShoppingScreen.GenericGrid(
                            items = subcategories,
                            title = "",
                            getName = { it.CatName },
                            getImageUrl = { "" },
                            cardBgColor = Color.White,
                            onSeeAllClick = null,
                            onItemClick = { subcat ->
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
            }
        }
    }
}

object SeeAllBrandsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val db = DatabaseHolder.instance
        val coroutineScope = rememberCoroutineScope()

        val brands by produceState<List<GetBrandsForDis>>(initialValue = emptyList()) {
            value = withContext(Dispatchers.IO) {
                db.productsQueries.getBrandsForDis(
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes(),
                    productCode = null
                ).executeAsList()
            }
        }

        val products by produceState<List<GetProductsForDis>>(initialValue = emptyList()) {
            value = withContext(Dispatchers.IO) {
                db.productsQueries.getProductsForDis(
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes(),
                    productCode = null,
                    changePrice = SharedPrefs.ChangePrice.get(),
                    mapper = ::GetProductsForDis
                ).executeAsList()
            }
        }

        Scaffold(
            topBar = {
                Surface(color = Color(0xFFFF6D00), shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "All Brands",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
                            color = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF8F9FB))) {
                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    item {
                        CategoryShoppingScreen.GenericGrid(
                            items = brands,
                            title = "",
                            getName = { it.Name.orEmpty() },
                            getImageUrl = { "" },
                            cardBgColor = Color.White,
                            onSeeAllClick = null,
                            onItemClick = { brand ->
                                coroutineScope.launch {
                                    val guids = withContext(Dispatchers.IO) {
                                        products.filter { it.brand_name == brand.Name }.mapNotNull { it.product_id }
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
            }
        }
    }
}

object SeeAllCategoriesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val nav = navigator.parent?.parent ?: navigator.parent ?: navigator
        val db = DatabaseHolder.instance

        val categories by produceState<List<ProductCategoriesForDis>>(initialValue = emptyList()) {
            value = withContext(Dispatchers.IO) {
                db.productsQueries.productCategoriesForDis(
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes()
                ).executeAsList()
            }
        }

        Scaffold(
            topBar = {
                Surface(color = Color(0xFFFF6D00), shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "All Categories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
                            color = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF8F9FB))) {
                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    item {
                        val userId = remember { SharedPrefs.User.get()?.ID.toString() }
                        CategoryShoppingScreen.GenericGrid(
                            items = categories,
                            title = "",
                            getName = { it.Name.orEmpty() },
                            getImageUrl = { getCategoryImage(userId, it.GUID.toString()) },
                            cardBgColor = Color.White,
                            onSeeAllClick = null,
                            onItemClick = { category ->
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
    }
}
