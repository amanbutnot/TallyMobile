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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.prime.easykarobar.business.viewmodel.distributor.CartItem
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.CreateOrderRequest
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.QuantityTextField
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.convertCouponDate
import org.prime.easykarobar.ui.shared.globalShared.getProductImage

data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val discountType: String, // "flat" or "percent"
    val discountValue: Double,
    val minOrderValue: Double
)

data class DeliveryChargeConfig(
    val minAmount: Double,
    val maxAmount: Double,
    val charge: Double
)

fun getDeliveryChargeConfigs(): List<DeliveryChargeConfig> {
    return try {
        DatabaseHolder.instance.configMasterQueries.selectDeliveryCharges().executeAsList().map {
            DeliveryChargeConfig(
                minAmount = it.C1?.toDoubleOrNull() ?: 0.0,
                maxAmount = it.C2?.toDoubleOrNull() ?: 0.0,
                charge = it.C3?.toDoubleOrNull() ?: 0.0
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun calculateDeliveryCharge(totalAmount: Double, configs: List<DeliveryChargeConfig>): Double {
    val config = configs.find { totalAmount >= it.minAmount && totalAmount <= it.maxAmount }
    return config?.charge ?: 0.0
}

object CartScreen : Screen {
    @Composable
    override fun Content() {

        val nav = LocalNavigator.currentOrThrow
        val viewModel = nav.rememberNavigatorScreenModel { CartViewModel() }
        val state = viewModel.cartItems

        TallyScaffold(
            title = "My Orders",
            onBack = { nav.pop() },
            showEditIcon = false,
            onEditClick = {},
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues).navigationBarsPadding()
            ) {
                if (state.isEmpty()) {
                    EmptyListPlaceholder(
                        icon = Icons.Default.ShoppingBag,
                        title = "No Items in cart",
                        onAddClick = { nav.pop() }
                    )
                } else {
                    CartContent(viewModel.getAllProducts())
                }
            }
        }
    }
}

@Composable
private fun CartContent(
    list: List<CartItem>,
) {
    val nav = LocalNavigator.currentOrThrow
    val viewModel = nav.rememberNavigatorScreenModel { CartViewModel() }

    var couponText by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var remarks by remember { mutableStateOf("") }
    var deliveryDay by remember { mutableStateOf("Today") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    var pickupDay by remember { mutableStateOf("Today") }
    var pickupStartTime by remember { mutableStateOf("") }
    var pickupEndTime by remember { mutableStateOf("") }

    var isDelivery by remember { mutableStateOf(true) }

    val availableCoupons = remember {
        try {
            DatabaseHolder.instance.coupon_MasterQueries.selectAll().executeAsList().map {
                val discType = it.DISC_TYPE?.lowercase() ?: "flat"
                val expDateFormatted = convertCouponDate(it.EXP_DATE)
                val expDisplay = if (expDateFormatted != null) "Expires: ${Tdate(expDateFormatted)}" else ""

                Coupon(
                    code = it.CODE,
                    title = when (discType) {
                        "percentage" -> "${it.DISC_PER.formatToAmtDec(0)}% OFF"
                        "flat" -> "₹${it.DISC_PER.formatToAmtDec(0)} OFF"
                        "cashback" -> "₹${it.DISC_PER.formatToAmtDec(0)} Cashback"
                        else -> "₹${it.DISC_PER.formatToAmtDec(0)} OFF"
                    },
                    description = when (discType) {
                        "percentage" -> "${it.DISC_PER.formatToAmtDec(0)}% off on orders above ₹${it.BILL_VAL.formatToAmtDec(0)}. $expDisplay"
                        "flat" -> "Flat ₹${it.DISC_PER.formatToAmtDec(0)} off on orders above ₹${it.BILL_VAL.formatToAmtDec(0)}. $expDisplay"
                        "cashback" -> "₹${it.DISC_PER.formatToAmtDec(0)} cashback on orders above ₹${it.BILL_VAL.formatToAmtDec(0)}. $expDisplay"
                        else -> "Discount on orders above ₹${it.BILL_VAL.formatToAmtDec(0)}. $expDisplay"
                    }.trim(),
                    discountType = if (discType == "percentage") "percent" else "flat",
                    discountValue = it.DISC_PER,
                    minOrderValue = it.BILL_VAL
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val totalDiscountedPrice = list.sumOf {
        val discounted = it.product.discounted_price ?: 0.0
        discounted * it.quantity.value
    }

    var couponError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(list) { product ->
            CartProductItem(product = product)
        }

        item {
            CouponSection(
                couponText = couponText,
                onCouponTextChange = {
                    couponText = it
                    couponError = null
                },
                onApplyCoupon = { code ->
                    val coupon = availableCoupons.find { it.code.trim().equals(code.trim(), true) }
                    if (coupon != null) {
                        if (totalDiscountedPrice >= coupon.minOrderValue) {
                            appliedCoupon = coupon
                            couponError = null
                        } else {
                            couponError = "Minimum order value of ₹${coupon.minOrderValue.formatToAmtDec(0)} required"
                        }
                    } else {
                        couponError = "Invalid coupon code"
                    }
                },
                availableCoupons = availableCoupons,
                onSelectCoupon = {
                    if (it != null) {
                        if (totalDiscountedPrice >= it.minOrderValue) {
                            appliedCoupon = it
                            couponError = null
                        } else {
                            couponError = "Minimum order value of ₹${it.minOrderValue.formatToAmtDec(0)} required"
                        }
                    } else {
                        appliedCoupon = null
                        couponError = null
                    }
                },
                appliedCoupon = appliedCoupon
            )
            if (couponError != null) {
                Text(
                    text = couponError!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            CartSummary(
                products = list,
                appliedCoupon = appliedCoupon
            )
        }

        item {
            OrderTypeSelection(
                isDelivery = isDelivery,
                onTypeSelected = { isDelivery = it }
            )
        }

        if (isDelivery) {
            item {
                OrderTimeSection(
                    title = "Delivery Time",
                    deliveryDay = deliveryDay,
                    onDaySelected = { deliveryDay = it },
                    startTime = startTime,
                    onStartTimeChange = { startTime = it },
                    endTime = endTime,
                    onEndTimeChange = { endTime = it }
                )
            }
        } else {
            item {
                OrderTimeSection(
                    title = "Pickup Time",
                    deliveryDay = pickupDay,
                    onDaySelected = { pickupDay = it },
                    startTime = pickupStartTime,
                    onStartTimeChange = { pickupStartTime = it },
                    endTime = pickupEndTime,
                    onEndTimeChange = { pickupEndTime = it }
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                TallyTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    placeholder = "Your Remarks",
                    isPassword = false,
                    isNumber = false,
                    label = "Remarks",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                ConfirmOrderButton(
                    products = list,
                    cartViewModel = viewModel,
                    appliedCoupon = appliedCoupon,
                    remarks = remarks,
                    deliveryDay = deliveryDay,
                    startTime = startTime,
                    endTime = endTime,
                    pickupDay = pickupDay,
                    pickupStartTime = pickupStartTime,
                    pickupEndTime = pickupEndTime,
                    isDelivery = isDelivery
                )
            }
        }
    }
}

@Composable
fun OrderTypeSelection(
    isDelivery: Boolean,
    onTypeSelected: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Order Type",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTypeSelected(true) }
                ) {
                    RadioButton(
                        selected = isDelivery,
                        onClick = { onTypeSelected(true) }
                    )
                    Text("Delivery", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTypeSelected(false) }
                ) {
                    RadioButton(
                        selected = !isDelivery,
                        onClick = { onTypeSelected(false) }
                    )
                    Text("Pickup", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun OrderTimeSection(
    title: String,
    deliveryDay: String,
    onDaySelected: (String) -> Unit,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDaySelected("Today") }
                ) {
                    RadioButton(
                        selected = deliveryDay == "Today",
                        onClick = { onDaySelected("Today") }
                    )
                    Text("Today", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDaySelected("Tomorrow") }
                ) {
                    RadioButton(
                        selected = deliveryDay == "Tomorrow",
                        onClick = { onDaySelected("Tomorrow") }
                    )
                    Text("Tomorrow", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TallyTimePickerRow(
                    label = "Start Time",
                    selectedTime = startTime,
                    onTimeSelected = onStartTimeChange,
                    modifier = Modifier.weight(1f)
                )
                TallyTimePickerRow(
                    label = "End Time",
                    selectedTime = endTime,
                    onTimeSelected = onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyTimePickerRow(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedTime.isNotEmpty())
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            onClick = { showPicker = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Select time",
                        tint = if (selectedTime.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = selectedTime.ifEmpty { "Select" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (selectedTime.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selectedTime.isEmpty())
                            FontWeight.Normal
                        else
                            FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showPicker) {
        TallyTimePicker(
            onTimeSelected = { time ->
                onTimeSelected(time)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyTimePicker(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val hour = timePickerState.hour
                val minute = timePickerState.minute
                val amPm = if (hour < 12) "AM" else "PM"
                val h = if (hour % 12 == 0) 12 else hour % 12
                val formattedTime = "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
                onTimeSelected(formattedTime)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CartProductItem(
    product: CartItem,
) {
    val nav = LocalNavigator.currentOrThrow
    val viewModel = nav.rememberNavigatorScreenModel { CartViewModel() }

    val factor = product.product.con_factor ?: 1.0
    val conType = product.product.con_type ?: 1.0
    val selectedUnit = product.selectedUnit.value

    val currentListPrice =
        if (selectedUnit == product.product.main_unit || product.product.alt_unit.isNullOrBlank()) {
            product.product.sales_price ?: 0.0
        } else {
            val price = if (conType == 1.0) (product.product.sales_price
                ?: 0.0) / factor else (product.product.sales_price ?: 0.0) * factor
            kotlin.math.round(price * 100.0) / 100.0
        }

    val currentDiscountedPrice =
        if (selectedUnit == product.product.main_unit || product.product.alt_unit.isNullOrBlank()) {
            product.product.discounted_price ?: currentListPrice
        } else {
            val price = if (conType == 1.0) (product.product.discounted_price
                ?: 0.0) / factor else (product.product.discounted_price ?: 0.0) * factor
            kotlin.math.round(price * 100.0) / 100.0
        }

    val currentMrp =
        if (selectedUnit == product.product.main_unit || product.product.alt_unit.isNullOrBlank()) {
            product.product.MRP ?: 0.0
        } else {
            val price = if (conType == 1.0) (product.product.MRP ?: 0.0) / factor else (product.product.MRP
                ?: 0.0) * factor
            kotlin.math.round(price * 100.0) / 100.0
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                val fullUrl = getProductImage(
                    storeId = SharedPrefs.User.get()?.ID.toString(),
                    guid = product.product.product_id.toString()
                )
                //TODO:IMAGE
                AsyncImage(
                    model = fullUrl,
                    contentDescription = product.product.product_name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.product.product_name.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentDiscountedPrice.formatToAmtDec(),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (currentMrp != 0.0) {
                            Text(
                                text = currentMrp.formatToAmtDec(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )

                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.product.discount.toString() != "0.0") {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${product.product.discount?.formatToAmtDec()}% OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = selectedUnit,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = MaterialShapes.Arch.toShape()
                            )
                            .clickable { viewModel.decreaseQuantity(product.product) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HorizontalRule,
                            contentDescription = "Decrease quantity",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    QuantityTextField(
                        quantity = product.quantity.value,
                        onQuantityChange = {
                            viewModel.updateQuantity(product.product, it)
                        },
                        modifier = Modifier.width(40.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialShapes.Slanted.toShape()
                                    )
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = MaterialShapes.Arch.toShape()
                            )
                            .clickable { viewModel.addProduct(product.product) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                val validationMessage = viewModel.getValidationMessage(product)
                if (validationMessage != null) {
                    Text(
                        text = validationMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = { viewModel.removeProduct(product.product) },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CartSummary(
    products: List<CartItem>,
    appliedCoupon: Coupon? = null
) {

//    val totalMrp = products.sumOf {
//        val mrp = it.product.MRP ?: 0.0
//        mrp * it.quantity.value
//    }
    val totalMrp = products.sumOf {
        val factor = it.product.con_factor ?: 1.0
        val conType = it.product.con_type ?: 1.0
        val selectedUnit = it.selectedUnit.value

        val mrp =
            if (it.product.MRP == 0.0) it.product.sales_price ?: 0.0 else it.product.MRP ?: 0.0

        val currentMrp =
            if (selectedUnit == it.product.main_unit || it.product.alt_unit.isNullOrBlank()) {
                mrp
            } else {
                val price = if (conType == 1.0) mrp / factor else mrp * factor
                kotlin.math.round(price * 100.0) / 100.0
            }
        currentMrp * it.quantity.value
    }


    val totalDiscountedPrice = products.sumOf {
        val factor = it.product.con_factor ?: 1.0
        val conType = it.product.con_type ?: 1.0
        val selectedUnit = it.selectedUnit.value

        val currentDiscountedPrice =
            if (selectedUnit == it.product.main_unit || it.product.alt_unit.isNullOrBlank()) {
                it.product.discounted_price ?: 0.0
            } else {
                val price = if (conType == 1.0) (it.product.discounted_price
                    ?: 0.0) / factor else (it.product.discounted_price ?: 0.0) * factor
                kotlin.math.round(price * 100.0) / 100.0
            }
        currentDiscountedPrice * it.quantity.value
    }

    val deliveryConfigs = remember { getDeliveryChargeConfigs() }
    val deliveryCharge = calculateDeliveryCharge(totalDiscountedPrice, deliveryConfigs)
    val minOrderAmount = deliveryConfigs.minOfOrNull { it.minAmount } ?: 0.0
    val isBelowMinOrder = totalDiscountedPrice < minOrderAmount

    val totalSavings = (totalMrp - totalDiscountedPrice).toDouble()

    val totalGst = products.sumOf {
        val factor = it.product.con_factor ?: 1.0
        val conType = it.product.con_type ?: 1.0
        val selectedUnit = it.selectedUnit.value

        val currentDiscountedPrice =
            if (selectedUnit == it.product.main_unit || it.product.alt_unit.isNullOrBlank()) {
                it.product.discounted_price ?: 0.0
            } else {
                val price = if (conType == 1.0) (it.product.discounted_price
                    ?: 0.0) / factor else (it.product.discounted_price ?: 0.0) * factor
                kotlin.math.round(price * 100.0) / 100.0
            }

        val gstPercentage = it.product.gst_tax_percentage ?: 0.0
        val gstPerItem = (currentDiscountedPrice * gstPercentage) / 100.0
        gstPerItem * it.quantity.value
    }

    val totalBeforeCoupon = (totalDiscountedPrice + totalGst).toDouble()

    val totalHamali = products.sumOf { cartItem ->
        val unitName = cartItem.selectedUnit.value
        val quantity = cartItem.quantity.value
        when (unitName.lowercase()) {
            "box", "tin" -> quantity * 2.0
            "bag" -> quantity * 5.0
            else -> 0.0
        }
    }

    val couponDiscount = if (appliedCoupon != null && totalDiscountedPrice >= appliedCoupon.minOrderValue) {
        if (appliedCoupon.discountType == "flat") {
            appliedCoupon.discountValue
        } else {
            (totalDiscountedPrice * appliedCoupon.discountValue) / 100.0
        }
    } else 0.0

    val finalTotal = totalBeforeCoupon - couponDiscount + totalHamali + deliveryCharge
    val totalSavingsCombined = totalSavings + couponDiscount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            SummaryRow(
                label = "Items (${products.size})",
                value = totalMrp.formatToAmtDec(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (totalSavings != 0.0) {
                SummaryRow(
                    label = "Discount",
                    value = "-${totalSavings?.formatToAmtDec()}",
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }


            SummaryRow(
                label = "Subtotal",
                value = "${totalDiscountedPrice?.formatToAmtDec()}",
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = "GST",
                value = "${totalGst?.formatToAmtDec()}",
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                // valueColor = MaterialTheme.colorScheme.tertiary
            )

            if (totalHamali > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "Hamali",
                    value = totalHamali.formatToAmtDec(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
            }

            if (deliveryCharge > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "Delivery Charges",
                    value = deliveryCharge.formatToAmtDec(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
            }

            if (appliedCoupon != null) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "Coupon (${appliedCoupon.code})",
                    value = "-${couponDiscount.formatToAmtDec()}",
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .padding(vertical = 12.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            SummaryRow(
                label = "Total Amount",
                value = "${finalTotal?.formatToAmtDec()}",
                textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                valueColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ Inclusive of all taxes",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (totalSavingsCombined > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 You saved ${totalSavingsCombined?.formatToAmtDec()} on this order!",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isBelowMinOrder) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ Minimum order amount should be ₹${minOrderAmount.formatToAmtDec(0)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun ConfirmOrderButton(
    products: List<CartItem>,
    cartViewModel: CartViewModel? = null,
    appliedCoupon: Coupon? = null,
    remarks: String = "",
    deliveryDay: String = "",
    startTime: String = "",
    endTime: String = "",
    pickupDay: String = "",
    pickupStartTime: String = "",
    pickupEndTime: String = "",
    isDelivery: Boolean = true
) {
    val totalDiscountedPrice = products.sumOf {
        val factor = it.product.con_factor ?: 1.0
        val conType = it.product.con_type ?: 1.0
        val selectedUnit = it.selectedUnit.value

        val currentDiscountedPrice =
            if (selectedUnit == it.product.main_unit || it.product.alt_unit.isNullOrBlank()) {
                it.product.discounted_price ?: 0.0
            } else {
                val price = if (conType == 1.0) (it.product.discounted_price
                    ?: 0.0) / factor else (it.product.discounted_price ?: 0.0) * factor
                kotlin.math.round(price * 100.0) / 100.0
            }
        currentDiscountedPrice * it.quantity.value
    }

    val totalGst = products.sumOf {
        val factor = it.product.con_factor ?: 1.0
        val conType = it.product.con_type ?: 1.0
        val selectedUnit = it.selectedUnit.value

        val currentDiscountedPrice =
            if (selectedUnit == it.product.main_unit || it.product.alt_unit.isNullOrBlank()) {
                it.product.discounted_price ?: 0.0
            } else {
                val price = if (conType == 1.0) (it.product.discounted_price
                    ?: 0.0) / factor else (it.product.discounted_price ?: 0.0) * factor
                kotlin.math.round(price * 100.0) / 100.0
            }

        val gstPercentage = it.product.gst_tax_percentage ?: 0.0
        val gstPerItem = (currentDiscountedPrice * gstPercentage) / 100.0
        gstPerItem * it.quantity.value
    }

    val totalBeforeCoupon = (totalDiscountedPrice + totalGst)

    val totalHamali = products.sumOf { cartItem ->
        val unitName = cartItem.selectedUnit.value
        val quantity = cartItem.quantity.value
        when (unitName.lowercase()) {
            "box", "tin" -> quantity * 2.0
            "bag" -> quantity * 5.0
            else -> 0.0
        }
    }

    val deliveryConfigs = remember { getDeliveryChargeConfigs() }
    val deliveryCharge = calculateDeliveryCharge(totalDiscountedPrice, deliveryConfigs)
    val minOrderAmount = deliveryConfigs.minOfOrNull { it.minAmount } ?: 0.0
    val isBelowMinOrder = totalDiscountedPrice < minOrderAmount

    val couponDiscount = if (appliedCoupon != null && totalDiscountedPrice >= appliedCoupon.minOrderValue) {
        if (appliedCoupon.discountType == "flat") {
            appliedCoupon.discountValue
        } else {
            (totalDiscountedPrice * appliedCoupon.discountValue) / 100.0
        }
    } else 0.0

    val finalTotal = totalBeforeCoupon - couponDiscount + totalHamali + deliveryCharge

    val orderViewModel: OrderViewModel = viewModel { OrderViewModel() }
    val orderDataState by orderViewModel.createOrderState
    val nav = LocalNavigator.currentOrThrow
    var showConfirmDialog by remember { mutableStateOf(false) }

    var showPincodeDialog by remember { mutableStateOf(false) }
    var pincodeValue by remember { mutableStateOf("") }
    var showPickupDialog by remember { mutableStateOf(false) }
    var showMinOrderAlert by remember { mutableStateOf(false) }
    var showCartValidationAlert by remember { mutableStateOf(false) }
    var currentIsDelivery by remember { mutableStateOf(isDelivery) }

    LaunchedEffect(isDelivery) {
        currentIsDelivery = isDelivery
    }

    val isCartValid = cartViewModel?.isCartValid() ?: true

    Button(
        onClick = {
            if (!isCartValid) {
                showCartValidationAlert = true
            } else if (isBelowMinOrder) {
                showMinOrderAlert = true
            } else if (currentIsDelivery) {
                showPincodeDialog = true
            } else {
                showConfirmDialog = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Confirm Order",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

    if (showPincodeDialog) {
        TallyAlertBox(
            title = "Enter Pincode",
            message = "Please enter your pincode to check delivery availability",
            confirmButtonText = "Check",
            cancelButtonText = "Cancel",
            onConfirm = {
                if (pincodeValue.isNotEmpty()) {
                    val exists = DatabaseHolder.instance.configMasterQueries
                        .selectConfigMasterC1ByType(configType = 1L)
                        .executeAsList()
                        .any { it.C1 == pincodeValue }

                    if (exists) {
                        showPincodeDialog = false
                        showConfirmDialog = true
                    } else {
                        showPincodeDialog = false
                        showPickupDialog = true
                    }
                }
            },
            onCancel = { showPincodeDialog = false },
            onDismiss = { showPincodeDialog = false },
            content = {
                TallyTextField(
                    value = pincodeValue,
                    onValueChange = { pincodeValue = it },
                    placeholder = "Enter Pincode",
                    isPassword = false,
                    isNumber = true,
                    label = "Pincode"
                )
            }
        )
    }

    if (showPickupDialog) {
        TallyAlertBox(
            title = "Delivery Not Available",
            message = "Delivery is not available for this pincode. Do you want to place the order for pickup instead?",
            confirmButtonText = "Yes, Pickup",
            cancelButtonText = "No",
            onConfirm = {
                currentIsDelivery = false
                showPickupDialog = false
                showConfirmDialog = true
            },
            onCancel = { showPickupDialog = false },
            onDismiss = { showPickupDialog = false }
        )
    }

    if (showMinOrderAlert) {
        TallyAlertBox(
            title = "Minimum Order Amount",
            message = "Your order should be greater than ₹${minOrderAmount.formatToAmtDec(0)} to place an order.",
            confirmButtonText = "Ok",
            cancelButtonText = "",
            onConfirm = { showMinOrderAlert = false },
            onCancel = { showMinOrderAlert = false },
            onDismiss = { showMinOrderAlert = false }
        )
    }

    if (showCartValidationAlert) {
        TallyAlertBox(
            title = "Invalid Order Quantities",
            message = "Some items in your cart have invalid quantities. Please check the messages underneath each item.",
            confirmButtonText = "Ok",
            cancelButtonText = "",
            onConfirm = { showCartValidationAlert = false },
            onCancel = { showCartValidationAlert = false },
            onDismiss = { showCartValidationAlert = false }
        )
    }

    if (showConfirmDialog) {
        val itemsList = products.map { cartItem ->
            val product = cartItem.product
            val quantity = cartItem.quantity.value
            val factor = product.con_factor ?: 1.0
            val conType = product.con_type ?: 1.0
            val selectedUnit = cartItem.selectedUnit.value

            val basePrice = product.sales_price?.toDouble() ?: 0.0
            val currentListPrice =
                if (selectedUnit == product.main_unit || product.alt_unit.isNullOrBlank()) {
                    basePrice
                } else {
                    val price = if (conType == 1.0) basePrice / factor else basePrice * factor
                    kotlin.math.round(price * 100.0) / 100.0
                }

            val discountedPrice =
                if (selectedUnit == product.main_unit || product.alt_unit.isNullOrBlank()) {
                    product.discounted_price ?: currentListPrice
                } else {
                    val price = if (conType == 1.0) (product.discounted_price
                        ?: 0.0) / factor else (product.discounted_price ?: 0.0) * factor
                    kotlin.math.round(price * 100.0) / 100.0
                }

            val calculatedAltQty = if (selectedUnit == product.alt_unit) {
                quantity
            } else {
                if (conType == 1.0) { // 1 Main = factor Alt
                    quantity * factor
                } else { // 1 Alt = factor Main => 1 Main = 1/factor Alt
                    quantity / factor
                }
            }

            org.prime.easykarobar.data.model.items(
                item_id = product.hospital_id?.toInt() ?: 0,
                productName = product.product_name.toString(),
                quantity = quantity,
                price = currentListPrice,
                discount_percent = product.discount?.toDouble() ?: 0.0,
                tax_amount = product.gst_tax_percentage.toDouble(),
                net_amount = discountedPrice * quantity,
                selected_unit = selectedUnit,
                con_factor = factor,
                con_type = conType,
                alt_qty = calculatedAltQty
            )
        }
        TallyAlertBox(
            title = "Confirm Order",
            message = "Do you want to confirm your order",
            confirmButtonText = "Yes",
            cancelButtonText = "No",
            onConfirm = {
                showConfirmDialog = false

                val sundriesList = mutableListOf<SundryItem>()
                if (appliedCoupon != null) {
                    sundriesList.add(
                        SundryItem(
                            name = appliedCoupon.code,
                            amount = -couponDiscount,
                            rate = if (appliedCoupon.discountType == "percent") appliedCoupon.discountValue else 0.0,
                            percentValue = if (appliedCoupon.discountType == "percent") appliedCoupon.discountValue else 0.0,
                            srno = sundriesList.size + 1,
                            guid = "",
                            i1 = 0,
                            i2 = 0,
                            d2 = 0
                        )
                    )
                }

                products.forEach { cartItem ->
                    val unitName = cartItem.selectedUnit.value
                    val quantity = cartItem.quantity.value
                    val rate = when (unitName.lowercase()) {
                        "box", "tin" -> 2.0
                        "bag" -> 5.0
                        else -> 0.0
                    }
                    if (rate > 0) {
                        sundriesList.add(
                            SundryItem(
                                name = "hamali",
                                amount = rate * quantity,
                                rate = rate,
                                percentValue = 0.0,
                                srno = sundriesList.size + 1,
                                guid = "",
                                i1 = 0,
                                i2 = 0,
                                d2 = 0
                            )
                        )
                    }
                }

                if (deliveryCharge > 0) {
                    sundriesList.add(
                        SundryItem(
                            name = "delivery charges",
                            amount = deliveryCharge,
                            rate = 0.0,
                            percentValue = 0.0,
                            srno = sundriesList.size + 1,
                            guid = "",
                            i1 = 0,
                            i2 = 0,
                            d2 = 0
                        )
                    )
                }

                val timeRemarks = if (currentIsDelivery) {
                    "Delivery: $deliveryDay, Time: $startTime - $endTime"
                } else {
                    "Pickup: $pickupDay, Time: $pickupStartTime - $pickupEndTime"
                }
                val finalRemarks = if (remarks.isNotEmpty()) "$remarks | $timeRemarks" else timeRemarks

                orderViewModel.createOrder(
                    CreateOrderRequest(
                        billing_guid = SharedPrefs.DistributorData.get()?.ledger_GUID.toString(),
                        remarks = finalRemarks,
                        billing_name = SharedPrefs.DistributorData.get()?.ledger_name.toString(),
                        total_amt = finalTotal.toString(),
                        items = itemsList,
                        sundries = sundriesList
                    )
                )
            },
            onCancel = { showConfirmDialog = false },
            onDismiss = { showConfirmDialog = false },
        )
    }
    if (orderDataState.isLoading) {
        TallyLoadingDialog("Creating your order")
    }
    if (orderDataState.success) {
        TallyResultDialog(
            message = "${orderDataState.message}\n${if (orderDataState.success) orderDataState.data?.VoucherNumber else ""}",
            onDone = {
                cartViewModel?.emptyList()
                nav.pop()
            },
            isSuccess = orderDataState.success,
            confirmText = "Ok"
        )
    }
}


@Composable
fun CouponSection(
    couponText: String,
    onCouponTextChange: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    availableCoupons: List<Coupon>,
    onSelectCoupon: (Coupon?) -> Unit,
    appliedCoupon: Coupon?
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Offers & Coupons",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Manual entry field
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                TallyTextField(
                    value = couponText,
                    onValueChange = onCouponTextChange,
                    placeholder = "Enter coupon code",
                    isPassword = false,
                    isNumber = false,
                    label = "Coupon Code",
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "APPLY",
                    color = if (couponText.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable(enabled = couponText.isNotEmpty()) {
                            onApplyCoupon(couponText)
                            focusManager.clearFocus()
                        }
                        .padding(16.dp)
                )
            }
        }

        if (appliedCoupon != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "'${appliedCoupon.code}' applied",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "REMOVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onSelectCoupon(null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Available Coupons",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 4.dp)
        ) {
            items(availableCoupons) { coupon ->
                CouponItem(
                    coupon = coupon,
                    isSelected = appliedCoupon?.code == coupon.code,
                    onApply = {
                        if (appliedCoupon?.code == coupon.code) {
                            onSelectCoupon(null)
                        } else {
                            onSelectCoupon(coupon)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CouponItem(
    coupon: Coupon,
    isSelected: Boolean,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isSelected) "APPLIED" else "APPLY",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onApply() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = coupon.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = coupon.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}
@Composable
fun SummaryRow(
    label: String,
    value: String,
    textStyle: TextStyle,
    fontWeight: FontWeight = FontWeight.Normal,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = textStyle,
            fontWeight = fontWeight,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = textStyle,
            fontWeight = fontWeight,
            color = valueColor
        )
    }
}

data class CartSummaryItem(
    val productId: Int,
    val name: String,
    val mrp: Double,
    val salesPrice: Double,
    val discountedPrice: Double,
    val discountPercent: Double,
    val gstPercent: Double,
    val quantity: Double
)

fun CartItem.toSummaryItem(): CartSummaryItem {
    val p = product

    return CartSummaryItem(
        productId = p.hospital_id?.toInt() ?: 0,
        name = p.product_name.orEmpty(),
        mrp = p.MRP ?: 0.0,
        salesPrice = p.sales_price ?: 0.0,
        discountedPrice = p.discounted_price ?: 0.0,
        discountPercent = p.discount ?: 0.0,
        gstPercent = p.gst_tax_percentage ?: 0.0,
        quantity = quantity.value
    )
}

@Composable
fun CartSummaryShow(
    items: List<CartSummaryItem>,
) {
    val totalMrp = items.sumOf { (if (it.mrp == 0.0) it.salesPrice else it.mrp) * it.quantity }
    val totalDiscounted = items.sumOf { it.discountedPrice * it.quantity }
    val totalSavings = totalMrp - totalDiscounted
    val totalGst = items.sumOf {
        ((it.discountedPrice * it.gstPercent) / 100.0) * it.quantity
    }
    val finalTotal = totalDiscounted + totalGst


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Order Summary", fontWeight = FontWeight.Bold)

            SummaryRow(
                "Items (${items.size})",
                totalMrp.formatToAmtDec(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
            SummaryRow(
                "Discount",
                "-${totalSavings.formatToAmtDec()}",
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
            SummaryRow(
                "Subtotal",
                totalDiscounted.formatToAmtDec(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
            SummaryRow(
                "GST",
                totalGst.formatToAmtDec(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = "Total Amount",
                value = finalTotal.formatToAmtDec(),
                fontWeight = FontWeight.Bold,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

        }
    }

}
