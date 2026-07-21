package com.example.oktshop17.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oktshop17.data.ProductEntity
import com.example.oktshop17.ui.OktshopViewModel
import com.example.oktshop17.ui.components.OrderDataTable
import com.example.oktshop17.ui.components.StatCard
import com.example.oktshop17.ui.components.StatusChip
import com.example.oktshop17.ui.components.formatRp
import kotlin.math.floor

@Composable
fun AdminDashboardScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()

    val completedOrders = remember(allOrders) { allOrders.filter { it.status.equals("Delivered", ignoreCase = true) || it.status.equals("Selesai", ignoreCase = true) } }
    val admQty = remember(completedOrders) { completedOrders.size }
    val admTotal = remember(completedOrders) { completedOrders.sumOf { it.total } }
    val admPoin = remember(admTotal) { floor(admTotal.toDouble() / 100.0).toLong() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(title = "Total Order Selesai", value = "$admQty Order", accentColor = Color(0xFF0284C7), icon = Icons.Default.Receipt)
                StatCard(title = "Total Omset", value = formatRp(admTotal), accentColor = Color(0xFF16A34A), icon = Icons.Default.PointOfSale)
                StatCard(title = "Total Poin Keluar", value = "$admPoin Poin", accentColor = MaterialTheme.colorScheme.secondary, icon = Icons.Default.Stars)
            }
        }

        item {
            OrderDataTable(
                orders = allOrders,
                isAdmin = true,
                onStatusChange = { orderId, newStatus ->
                    viewModel.updateOrderStatus(orderId, newStatus)
                }
            )
        }
    }
}

@Composable
fun AdminActivationScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val inactiveUsers by viewModel.inactiveResellers.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🔑 Aktivasi Akun Baru",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (inactiveUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Semua akun reseller sudah aktif ✅", color = Color.Gray)
                }
            }
        } else {
            items(inactiveUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ID: ${user.customId}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            Text(text = user.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${user.email} • ${user.hp}", fontSize = 12.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = { viewModel.activateUser(user.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("AKTIFKAN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRedeemScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val redemptions by viewModel.allRedemptions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🎁 Penukaran Poin Masuk",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (redemptions.isEmpty()) {
            item { Text("Belum ada pengajuan penukaran poin", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(redemptions) { red ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Akun: ${red.resellerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Penerima: ${red.redeemName} (${red.wa})", fontSize = 12.sp)
                            Text("Poin: ${red.points} Poin", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                        }

                        if (red.status.equals("proses", ignoreCase = true)) {
                            Button(
                                onClick = { viewModel.markRedemptionCompleted(red.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Selesai", fontSize = 11.sp)
                            }
                        } else {
                            StatusChip(status = red.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCatalogScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val catalog by viewModel.products.collectAsState()

    var editingId by remember { mutableStateOf<String?>(null) }
    var prodNama by remember { mutableStateOf("") }
    var prodHarga by remember { mutableStateOf("") }
    var prodKategori by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingId == null) "📦 Tambah Produk Katalog" else "✏️ Edit Produk Katalog",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = prodNama,
                        onValueChange = { prodNama = it },
                        label = { Text("Nama Produk") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prodHarga,
                        onValueChange = { prodHarga = it },
                        label = { Text("Harga (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prodKategori,
                        onValueChange = { prodKategori = it },
                        label = { Text("Kategori (e.g. Baso Aci, Topping)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val price = prodHarga.toLongOrNull() ?: 0
                                if (prodNama.isNotBlank() && price > 0) {
                                    viewModel.saveProduct(editingId, prodNama, price, prodKategori)
                                    editingId = null
                                    prodNama = ""
                                    prodHarga = ""
                                    prodKategori = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SIMPAN PRODUK")
                        }

                        if (editingId != null) {
                            OutlinedButton(onClick = {
                                editingId = null
                                prodNama = ""
                                prodHarga = ""
                                prodKategori = ""
                            }) {
                                Text("Batal")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(text = "Daftar Katalog Produk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(catalog) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Kategori: ${p.kategori}", fontSize = 12.sp, color = Color.Gray)
                        Text(formatRp(p.harga), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row {
                        IconButton(onClick = {
                            editingId = p.id
                            prodNama = p.nama
                            prodHarga = p.harga.toString()
                            prodKategori = p.kategori
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { viewModel.deleteProduct(p.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRankingsScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val allResellers by viewModel.allResellers.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    val rankings = remember(allResellers, allOrders) {
        val completed = allOrders.filter { it.status.equals("Delivered", ignoreCase = true) || it.status.equals("Selesai", ignoreCase = true) }
        allResellers.map { res ->
            val total = completed.filter { it.resellerId == res.uid }.sumOf { it.total }
            val poin = floor(total.toDouble() / 100.0).toInt()
            Triple(res.nama, poin, total)
        }.sortedByDescending { it.third }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🏆 Peringkat Global Reseller",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(rankings.size) { index ->
            val (nama, poin, total) = rankings[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${index + 1}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.width(36.dp),
                            color = if (index == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Total Belanja: ${formatRp(total)}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Text(
                        text = "$poin Poin",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminReturnsScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val returns by viewModel.allReturns.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "📥 Returan Masuk",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (returns.isEmpty()) {
            item { Text("Belum ada pengajuan retur barang", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(returns) { ret ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ret.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("HP: ${ret.hp}", fontSize = 12.sp, color = Color.Gray)
                            Text("Produk: ${ret.produk}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Alasan: ${ret.alasan}", fontSize = 12.sp, color = Color.DarkGray)
                        }

                        if (ret.status.equals("proses", ignoreCase = true)) {
                            Button(
                                onClick = { viewModel.markReturnCompleted(ret.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Selesai", fontSize = 11.sp)
                            }
                        } else {
                            StatusChip(status = ret.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminComplaintsScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val complaints by viewModel.allComplaints.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "📢 Keluhan Masuk",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (complaints.isEmpty()) {
            item { Text("Belum ada laporan keluhan", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(complaints) { comp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comp.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("HP: ${comp.hp}", fontSize = 12.sp, color = Color.Gray)
                            Text("Pesan: ${comp.pesan}", fontSize = 13.sp, color = Color.DarkGray)
                        }

                        if (comp.status.equals("proses", ignoreCase = true)) {
                            Button(
                                onClick = { viewModel.markComplaintCompleted(comp.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Selesai", fontSize = 11.sp)
                            }
                        } else {
                            StatusChip(status = comp.status)
                        }
                    }
                }
            }
        }
    }
}
