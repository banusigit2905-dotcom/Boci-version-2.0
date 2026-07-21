package com.example.oktshop17.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oktshop17.ui.OktshopViewModel
import com.example.oktshop17.ui.components.NewOrderDialog
import com.example.oktshop17.ui.components.OrderDataTable
import com.example.oktshop17.ui.components.StatCard
import com.example.oktshop17.ui.components.StatusChip
import com.example.oktshop17.ui.components.formatRp
import kotlin.math.floor

@Composable
fun ResellerDashboardScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allRedemptions by viewModel.allRedemptions.collectAsState()
    val allResellers by viewModel.allResellers.collectAsState()
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    var showNewOrderDialog by remember { mutableStateOf(false) }

    // Calculate reseller metrics
    val userOrders = remember(allOrders, user) {
        allOrders.filter { it.resellerId == user?.uid }
    }

    val completedOrders = remember(userOrders) {
        userOrders.filter { it.status.equals("Delivered", ignoreCase = true) || it.status.equals("Selesai", ignoreCase = true) }
    }

    val resQty = remember(completedOrders) { completedOrders.sumOf { it.jumlah } }
    val resTotal = remember(completedOrders) { completedOrders.sumOf { it.total } }

    val userCompletedRedeems = remember(allRedemptions, user) {
        allRedemptions.filter { it.resellerId == user?.uid && it.status.equals("Selesai", ignoreCase = true) }
    }
    val usedPoints = remember(userCompletedRedeems) { userCompletedRedeems.sumOf { it.points } }
    val currentPoints = remember(resTotal, usedPoints) {
        (floor(resTotal.toDouble() / 100.0).toInt() - usedPoints).coerceAtLeast(0)
    }

    // Calculate Leaderboard
    val leaderboard = remember(allResellers, allOrders) {
        val completedAll = allOrders.filter { it.status.equals("Delivered", ignoreCase = true) || it.status.equals("Selesai", ignoreCase = true) }
        allResellers.map { res ->
            val totalSpent = completedAll.filter { it.resellerId == res.uid }.sumOf { it.total }
            val points = floor(totalSpent.toDouble() / 100.0).toInt()
            Pair(res.nama, points)
        }.sortedByDescending { it.second }.take(10)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Halo, ${user?.nama ?: "Reseller"}! 👋",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Stat Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "QTY Terjual",
                    value = "$resQty Item",
                    accentColor = Color(0xFF0284C7),
                    icon = Icons.Default.ShoppingBag
                )
                StatCard(
                    title = "Total Belanja",
                    value = formatRp(resTotal),
                    accentColor = Color(0xFF16A34A),
                    icon = Icons.Default.EmojiEvents
                )
                StatCard(
                    title = "Poin Saat Ini",
                    value = "$currentPoints Poin",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Default.Star
                )
            }
        }

        // Riwayat Pesanan Data Table Component
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Dashboard Order Tracking",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = { showNewOrderDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🛒 + PESAN BARU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                OrderDataTable(
                    orders = userOrders,
                    isAdmin = false
                )
            }
        }

        // Leaderboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 Top 10 Reseller Teraktif",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (leaderboard.isEmpty()) {
                        Text("Belum ada data reseller", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            leaderboard.forEachIndexed { index, pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (index == 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${index + 1}.",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(28.dp),
                                            color = if (index == 0) Color(0xFFB45309) else Color.Unspecified
                                        )
                                        Text(
                                            text = pair.first,
                                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "${pair.second} Poin",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewOrderDialog) {
        NewOrderDialog(
            products = products,
            cartItems = cartItems,
            onAddToCart = { prod, q -> viewModel.addToCart(prod, q) },
            onRemoveFromCart = { idx -> viewModel.removeFromCart(idx) },
            onSubmitOrder = { cust, hp, pay -> viewModel.submitOrder(cust, hp, pay, viewModel.getApplication()) },
            onDismiss = { showNewOrderDialog = false }
        )
    }
}
