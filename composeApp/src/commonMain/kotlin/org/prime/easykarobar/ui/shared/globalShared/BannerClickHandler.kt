package org.prime.easykarobar.ui.shared.globalShared

import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.UriHandler
import cafe.adriel.voyager.navigator.Navigator
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumScreen
import org.tally.BANNER_MASTER
import org.tally.FEATURES_MASTER
import org.tally.GetProductsForDis
import org.tally.SLIDE_IMG
import org.tally.SLIDE_MASTER

sealed class BannerClicks(val value: String) {
    object OpenLink : BannerClicks("Open Link")
    object OpenItem : BannerClicks("Open Item")
    object OpenItemGroup : BannerClicks("Open Item Group")
    object SelectItems : BannerClicks("Select items")
}

fun handleBannerClick(
    c1: String?,
    c2: String?,
    nav: Navigator,
    urlProvider: UriHandler,
    showProductInfo: MutableState<Boolean>,
    selectedProduct: MutableState<GetProductsForDis?>
) {
    val db = DatabaseHolder.instance
    when (c1) {
        BannerClicks.OpenLink.value -> {
            try {
                urlProvider.openUri(c2?.toValidUrl() ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        BannerClicks.OpenItem.value -> {
            val product = db.productsQueries.getProductsByGuidsForDis(
                guids = listOf(c2 ?: ""),
                changePrice = SharedPrefs.ChangePrice.get(),
                mapper = ::GetProductsForDis
            ).executeAsOneOrNull()

            if (product != null) {
                selectedProduct.value = product
                showProductInfo.value = true
            }
        }

        BannerClicks.OpenItemGroup.value -> {
            val category = db.productsQueries.productCategoriesForDis(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes()
            ).executeAsList().find { it.GUID == c2 }

            nav.push(
                AllProductsPremiumScreen(
                    categoryName = category?.Name ?: "Category",
                    productCode = c2?.toDoubleOrNull(),
                    isTab = false
                )
            )
        }

        BannerClicks.SelectItems.value -> {
            val guids = c2?.split(",")?.map { it.trim() } ?: emptyList()
            if (guids.isNotEmpty()) {
                nav.push(
                    AllProductsPremiumScreen(
                        categoryName = "Products",
                        productGuids = guids,
                        isTab = false
                    )
                )
            }
        }
    }
}

fun handleBannerClick(
    banner: BANNER_MASTER,
    nav: Navigator,
    urlProvider: UriHandler,
    showProductInfo: MutableState<Boolean>,
    selectedProduct: MutableState<GetProductsForDis?>
) {
    handleBannerClick(banner.C1, banner.C2, nav, urlProvider, showProductInfo, selectedProduct)
}

fun handleBannerClick(
    feature: FEATURES_MASTER,
    nav: Navigator,
    urlProvider: UriHandler,
    showProductInfo: MutableState<Boolean>,
    selectedProduct: MutableState<GetProductsForDis?>
) {
    handleBannerClick(feature.C1, feature.C2, nav, urlProvider, showProductInfo, selectedProduct)
}

fun handleBannerClick(
    slide: SLIDE_MASTER,
    nav: Navigator,
    urlProvider: UriHandler,
    showProductInfo: MutableState<Boolean>,
    selectedProduct: MutableState<GetProductsForDis?>
) {
    handleBannerClick(slide.C1, slide.C2, nav, urlProvider, showProductInfo, selectedProduct)
}

fun handleBannerClick(
    slideImg: SLIDE_IMG,
    nav: Navigator,
    urlProvider: UriHandler,
    showProductInfo: MutableState<Boolean>,
    selectedProduct: MutableState<GetProductsForDis?>
) {
    handleBannerClick(slideImg.C4, slideImg.C5, nav, urlProvider, showProductInfo, selectedProduct)
}
