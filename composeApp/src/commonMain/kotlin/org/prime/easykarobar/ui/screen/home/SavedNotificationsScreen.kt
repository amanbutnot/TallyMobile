package org.prime.easykarobar.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.prime.easykarobar.business.repository.AuthRepository
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.OnClickAction
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.distributor.order.AllProductsPremiumScreen
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.utils.pushEasyMart
import org.tally.GetProductsForDis

class SavedNotificationsScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val colors = MaterialTheme.colorScheme
        val db = DatabaseHolder.instance
        var notifications by remember { mutableStateOf(emptyList<org.prime.easykarobar.data.model.NotificationPayload>()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            isLoading = true
            val res = AuthRepository.listNotifications()
            if (res?.statuscode == 200 && res.data != null) {
                notifications = res.data.sortedByDescending { it.timestamp }
            }
            isLoading = false
        }

        TallyScaffold(
            title = "Notifications",
            onBack = { nav.pop() },
            showEditIcon = false
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceContainerLowest)
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = colors.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Text(
                                text = "No Notifications Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onSurface
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { item ->
                            val action = item.onClickAction.trim().lowercase().replace('_', ' ').replace('-', ' ')
                            val guid = item.entityGuid ?: ""
                            val cleanGuid = guid.substringBefore(".")
                            val name = item.entityName ?: "Items"

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        println("[WWWW] SavedNotificationsScreen clicked: action=$action, guid=$guid, name=$name")
                                        when (action) {
                                            "open item" -> {
                                                if (guid.isNotBlank()) {
                                                    val product = db.productsQueries.getProductsByGuidsForDis(
                                                        guids = listOf(guid),
                                                        changePrice = SharedPrefs.ChangePrice.get(),
                                                        mapper = ::GetProductsForDis
                                                    ).executeAsOneOrNull()

                                                    nav.pushEasyMart(
                                                        AllProductsPremiumScreen(
                                                            categoryName = product?.product_name ?: name,
                                                            productGuids = listOf(guid),
                                                            isTab = false
                                                        )
                                                    )
                                                }
                                            }
                                            "open item category" -> {
                                                val category = db.productsQueries.productCategoriesForDis(
                                                    filterGroup = 0L,
                                                    groupCodes = emptyList()
                                                ).executeAsList().find { it.GUID == guid || it.GUID?.toDoubleOrNull() == guid.toDoubleOrNull() }

                                                nav.pushEasyMart(
                                                    AllProductsPremiumScreen(
                                                        categoryName = category?.Name ?: name,
                                                        productCode = guid.toDoubleOrNull(),
                                                        isTab = false
                                                    )
                                                )
                                            }
                                            "open item subcategory" -> {
                                                val subCategory = db.product_CategoryQueries.getSubCategory(null).executeAsList()
                                                    .find { it.CatCode?.toString() == guid || it.CatCode?.toString() == cleanGuid || it.CatCode?.toDouble() == guid.toDoubleOrNull() }

                                                nav.pushEasyMart(
                                                    AllProductsPremiumScreen(
                                                        categoryName = subCategory?.CatName ?: name,
                                                        subCategoryCode = guid.toDoubleOrNull(),
                                                        isTab = false
                                                    )
                                                )
                                            }
                                            "open brand" -> {
                                                val brand = db.productsQueries.getAllBrands().executeAsList()
                                                    .find { it.Code?.toString() == guid || it.Code?.toString() == cleanGuid || it.Code?.toDouble() == guid.toDoubleOrNull() || it.Name.equals(name, ignoreCase = true) }

                                                nav.pushEasyMart(
                                                    AllProductsPremiumScreen(
                                                        categoryName = brand?.Name ?: name,
                                                        brandName = brand?.Name ?: name,
                                                        isTab = false
                                                    )
                                                )
                                            }
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val timeStr = runCatching {
                                            val instant = Instant.fromEpochMilliseconds(item.timestamp)
                                            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                            "${dateTime.date} ${dateTime.time.hour}:${dateTime.time.minute.toString().padStart(2, '0')}"
                                        }.getOrDefault("")
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    }

                                    val isImage = item.contentType.equals("image", ignoreCase = true) || !item.imageUrl.isNullOrBlank()
                                    if (!isImage && !item.description.isNullOrBlank()) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurface.copy(alpha = 0.8f)
                                        )
                                    }

                                    if (!item.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = "Notification image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
