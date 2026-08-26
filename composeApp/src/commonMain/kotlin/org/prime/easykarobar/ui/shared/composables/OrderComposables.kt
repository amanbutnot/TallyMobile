package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.ORDERSTATUS
import org.prime.easykarobar.data.model.OrderItemList
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.ui.shared.globalShared.Tdate

@Composable
fun OrderCard(
    orderNo: String,
    orderStatus: String,
    remarks: String? = null,
    orderDate: String,
    billingName: String,
    totalAmount: String,
    items: List<OrderItemList>? = null,
    sundries: List<SundryItem>? = null,
    cancellationDate: String? = null,
    cancelledBy: String? = null,
    cancellationRemarks: String? = null,
    onCancelOrder: (() -> Unit)? = null,
    onHistoryClick: (() -> Unit)? = null,
    onRepeatOrder: (() -> Unit)? = null,
    onStatusChangeClick: (() -> Unit)? = null,
    onDownloadClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    showStatusChange: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Order Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp, fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Order #$orderNo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (onDownloadClick != null) {
                            androidx.compose.material3.IconButton(
                                onClick = onDownloadClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (onShareClick != null) {
                            androidx.compose.material3.IconButton(
                                onClick = onShareClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = orderStatus,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    if (!remarks.isNullOrEmpty()) {
                        OrderField(
                            icon = Icons.Default.Edit, label = "Remarks", value = remarks
                        )
                    }
                    OrderField(
                        icon = Icons.Default.CalendarMonth,
                        label = "Order Date",
                        value = Tdate(orderDate.take(10))
                    )
                    OrderField(
                        icon = Icons.Default.Person,
                        label = "Billing Name",
                        value = billingName
                    )
                }
            }

            if (!items.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order Items", style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp, fontWeight = FontWeight.Bold
                        ), color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${items.size} items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        items.forEachIndexed { index, item ->
                            OrderItemRow(
                                item = item, serialNumber = index + 1
                            )
                            if (index != items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.Bold)

                    if (!items.isNullOrEmpty()) {
                        val itemsSubtotal = items.sumOf {
                            it.item_amount.toDoubleOrNull() ?: 0.0
                        }

                        val itemsDiscount = items.sumOf {
                            it.discount_amt.toDoubleOrNull() ?: 0.0
                        }

                        val taxableSubtotal = itemsSubtotal - itemsDiscount

                        val itemsGst = items.sumOf {
                            (it.taxamt1.toDoubleOrNull() ?: 0.0) +
                                    (it.taxamt2.toDoubleOrNull() ?: 0.0)
                        }

                        val hamaliFromItems = items.sumOf { item ->
                            when (item.UnitName.lowercase()) {
                                "box", "tin" -> item.quantity * 2.0
                                "bag" -> item.quantity * 5.0
                                else -> 0.0
                            }
                        }

                        val sundryTotal = sundries?.sumOf { it.amount } ?: 0.0
                        val calculatedTotal = taxableSubtotal + itemsGst + sundryTotal
                        val serverTotal = totalAmount.toDoubleOrNull() ?: calculatedTotal

                        SummaryRow(
                            label = "Subtotal",
                            value = itemsSubtotal.formatToAmtDec(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )

                        if (itemsDiscount != 0.0) {
                            SummaryRow(
                                label = "Discount",
                                value = "-${itemsDiscount.formatToAmtDec()}",
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        SummaryRow(
                            label = "Taxable Amount",
                            value = taxableSubtotal.formatToAmtDec(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )

                        if (itemsGst != 0.0) {
                            SummaryRow(
                                label = "GST",
                                value = itemsGst.formatToAmtDec(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                        }

                        if (hamaliFromItems > 0.0) {
                            SummaryRow(
                                label = "Hamali",
                                value = hamaliFromItems.formatToAmtDec(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                        }

                        val coupons = sundries?.filter { it.name.lowercase() != "hamali" } ?: emptyList<SundryItem>()

                        if (coupons.isEmpty()) {
                            SummaryRow(
                                label = "Coupon",
                                value = "Not Applied",
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                        } else {
                            for (sundry in coupons) {
                                SummaryRow(
                                    label = "Coupon (${sundry.name})",
                                    value = sundry.amount.formatToAmtDec(),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    valueColor = if (sundry.amount < 0)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SummaryRow(
                            label = "Total Amount",
                            value = serverTotal.formatToAmtDec(),
                            fontWeight = FontWeight.Bold,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    } else {
                        // Simpler summary if items are not available
                        SummaryRow(
                            label = "Total Amount",
                            value = totalAmount,
                            fontWeight = FontWeight.Bold,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (cancellationDate != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancelled",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Order Cancelled",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                        ) {
                            CancellationField(
                                label = "Cancelled on",
                                value = Tdate(cancellationDate),
                                icon = Icons.Default.CalendarMonth
                            )

                            if (!cancelledBy.isNullOrEmpty()) {
                                CancellationField(
                                    label = "Cancelled by",
                                    value = cancelledBy,
                                    icon = Icons.Default.Person
                                )
                            }

                            if (!cancellationRemarks.isNullOrEmpty()) {
                                CancellationField(
                                    label = "Reason",
                                    value = cancellationRemarks,
                                    icon = Icons.Default.Info
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onRepeatOrder != null) {
                        Button(
                            onClick = onRepeatOrder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat Order",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Repeat",
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (onHistoryClick != null) {
                        Button(
                            onClick = onHistoryClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "History",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "History",
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (onCancelOrder != null && orderStatus != ORDERSTATUS.Confirmed.name) {
                        Button(
                            onClick = onCancelOrder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancel Order",
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (showStatusChange && onStatusChangeClick != null &&
                orderStatus != ORDERSTATUS.Delivered.name &&
                orderStatus != ORDERSTATUS.Cancelled.name
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStatusChangeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Change Status")
                }
            }
        }
    }
}

@Composable
fun OrderField(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                ), color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItemList, serialNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = serialNumber.toString(), style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ), color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = item.product_name,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ItemDetailChip(
                        label = "Qty",
                        value = item.quantity.toString(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ItemDetailChip(
                        label = "Unit",
                        value = item.UnitName,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                ItemDetailChip(
                    label = "Amount",
                    value = item.item_amount.toDouble().formatToAmtDec(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ItemDetailChip(
    label: String, value: String, color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
            Text(
                text = value, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = color
            )
        }
    }
}

@Composable
fun CancellationField(
    label: String, value: String, icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = label, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
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
