package com.example.oktshop17.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oktshop17.data.OrderEntity
import java.text.SimpleDateFormat
import java.util.*

enum class OrderStatusType(val label: String, val icon: String, val color: Color, val bgColor: Color) {
    PENDING("Pending", "⏳", Color(0xFFD97706), Color(0xFFFEF3C7)),
    SHIPPED("Shipped", "🚚", Color(0xFF0284C7), Color(0xFFE0F2FE)),
    DELIVERED("Delivered", "📦", Color(0xFF16A34A), Color(0xFFDCFCE7));

    companion object {
        fun fromString(status: String): OrderStatusType {
            return when {
                status.equals("shipped", ignoreCase = true) -> SHIPPED
                status.equals("delivered", ignoreCase = true) || status.equals("Selesai", ignoreCase = true) -> DELIVERED
                else -> PENDING
            }
        }
    }
}

@Composable
fun OrderStatusBadge(
    statusStr: String,
    modifier: Modifier = Modifier
) {
    val status = OrderStatusType.fromString(statusStr)
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = status.bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = status.icon, fontSize = 11.sp)
            Text(
                text = status.label,
                color = status.color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun OrderStatusTrackerBar(
    currentStatus: OrderStatusType,
    modifier: Modifier = Modifier
) {
    val steps = OrderStatusType.values()
    val currentIndex = currentStatus.ordinal

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            val isPassed = index <= currentIndex
            val isCurrent = index == currentIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) step.color else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPassed) "✓" else "${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = step.label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isPassed) step.color else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            if (index < steps.size - 1) {
                Divider(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 16.dp),
                    color = if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
fun OrderDataTable(
    orders: List<OrderEntity>,
    isAdmin: Boolean = false,
    onStatusChange: ((orderId: String, newStatus: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> orders.filter { OrderStatusType.fromString(it.status) == OrderStatusType.PENDING }
            "Shipped" -> orders.filter { OrderStatusType.fromString(it.status) == OrderStatusType.SHIPPED }
            "Delivered" -> orders.filter { OrderStatusType.fromString(it.status) == OrderStatusType.DELIVERED }
            else -> orders
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_data_table"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Title & Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Past Orders Table",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "${filteredOrders.size} Orders",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Pending", "Shipped", "Delivered")
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders found for '$selectedFilter'",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Table Container with Horizontal Scroll
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        // Data Table Header Row
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Date", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                            Text("Customer", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                            Text("Products", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(140.dp))
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                            Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(100.dp))
                            if (isAdmin) {
                                Text("Action", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Data Table Body Rows
                        filteredOrders.forEachIndexed { index, order ->
                            val isExpanded = expandedOrderId == order.id
                            val currentStatus = OrderStatusType.fromString(order.status)
                            val rowBg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                            Column {
                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .clickable {
                                            expandedOrderId = if (isExpanded) null else order.id
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Date
                                    Text(
                                        text = dateFormat.format(Date(order.createdAt)),
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.width(110.dp)
                                    )

                                    // Customer
                                    Column(modifier = Modifier.width(110.dp)) {
                                        Text(
                                            text = order.customerName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (order.resellerName.isNotBlank() && isAdmin) {
                                            Text(
                                                text = "By: ${order.resellerName}",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Products
                                    Text(
                                        text = order.produk,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(140.dp)
                                    )

                                    // Total
                                    Text(
                                        text = formatRp(order.total),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(90.dp)
                                    )

                                    // Status Badge
                                    Box(modifier = Modifier.width(100.dp)) {
                                        OrderStatusBadge(statusStr = order.status)
                                    }

                                    // Admin Action Dropdown / Buttons
                                    if (isAdmin) {
                                        Box(modifier = Modifier.width(110.dp)) {
                                            var showMenu by remember { mutableStateOf(false) }

                                            OutlinedButton(
                                                onClick = { showMenu = true },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Update", fontSize = 10.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }

                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("⏳ Pending") },
                                                    onClick = {
                                                        onStatusChange?.invoke(order.id, "Pending")
                                                        showMenu = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("🚚 Shipped") },
                                                    onClick = {
                                                        onStatusChange?.invoke(order.id, "Shipped")
                                                        showMenu = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("📦 Delivered") },
                                                    onClick = {
                                                        onStatusChange?.invoke(order.id, "Delivered")
                                                        showMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Expanded Row Details with Status Tracker
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "📍 Status Tracking Progress",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )

                                        OrderStatusTrackerBar(currentStatus = currentStatus)

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📅 Date: ${fullDateFormat.format(Date(order.createdAt))}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "💳 Payment: ${order.metode}",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "📞 Customer Phone: ${order.customerHp}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                if (index < filteredOrders.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
