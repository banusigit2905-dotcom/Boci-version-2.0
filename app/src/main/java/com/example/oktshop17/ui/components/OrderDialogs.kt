package com.example.oktshop17.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.oktshop17.data.ProductEntity
import com.example.oktshop17.ui.CartItem
import java.text.NumberFormat
import java.util.Locale

fun formatRp(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace(",00", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderDialog(
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    onAddToCart: (ProductEntity, Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onSubmitOrder: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = remember(products) {
        listOf("Semua") + products.map { it.kategori }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "Semua") products else products.filter { it.kategori == selectedCategory }
    }

    var selectedProduct by remember(filteredProducts) { mutableStateOf(filteredProducts.firstOrNull()) }
    var quantityInput by remember { mutableStateOf("1") }

    // Step 2 state
    var customerName by remember { mutableStateOf("") }
    var customerHp by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }
    var isPaymentDropdownExpanded by remember { mutableStateOf(false) }

    val totalCartAmount = cartItems.sumOf { it.subtotal }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (step == 1) "🛒 Form Pemesanan" else "👤 Data Penerima & Pembayaran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (step == 1) {
                    // Category selector
                    Text(text = "Kategori Produk", style = MaterialTheme.typography.labelMedium)
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            Tab(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    selectedProduct = if (cat == "Semua") products.firstOrNull() else products.firstOrNull { it.kategori == cat }
                                },
                                text = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Product selection
                    if (filteredProducts.isNotEmpty()) {
                        Text(text = "Pilih Produk", style = MaterialTheme.typography.labelMedium)
                        var isProdDropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = isProdDropdownExpanded,
                            onExpandedChange = { isProdDropdownExpanded = !isProdDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.let { "${it.nama} - ${formatRp(it.harga)}" } ?: "Pilih Produk",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProdDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isProdDropdownExpanded,
                                onDismissRequest = { isProdDropdownExpanded = false }
                            ) {
                                filteredProducts.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.nama} (${formatRp(p.harga)})") },
                                        onClick = {
                                            selectedProduct = p
                                            isProdDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quantity input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantityInput,
                                onValueChange = { quantityInput = it },
                                label = { Text("Jumlah (Qty)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val q = quantityInput.toIntOrNull() ?: 1
                                    selectedProduct?.let { onAddToCart(it, q) }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah")
                            }
                        }
                    } else {
                        Text("Belum ada produk di kategori ini.", color = Color.Gray, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cart Items list
                    Text(
                        text = "Keranjang (${cartItems.size} produk)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Keranjang masih kosong", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(cartItems.size) { idx ->
                                val item = cartItems[idx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.product.nama, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${item.qty}x @ ${formatRp(item.product.harga)} = ${formatRp(item.subtotal)}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(
                                        onClick = { onRemoveFromCart(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Total Belanja: ${formatRp(totalCartAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { if (cartItems.isNotEmpty()) step = 2 },
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier.testTag("btn_continue_order")
                        ) {
                            Text("Lanjutkan →")
                        }
                    }
                } else {
                    // Step 2: Customer & Payment
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Customer / Penerima") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerHp,
                        onValueChange = { customerHp = it },
                        label = { Text("No. WhatsApp Customer") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Metode Pembayaran", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = isPaymentDropdownExpanded,
                        onExpandedChange = { isPaymentDropdownExpanded = !isPaymentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPaymentDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isPaymentDropdownExpanded,
                            onDismissRequest = { isPaymentDropdownExpanded = false }
                        ) {
                            listOf("Transfer Bank", "COD (Bayar di Tempat)", "E-Wallet (Dana/OVO/ShopeePay)").forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        isPaymentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = { step = 1 }) { Text("← Kembali") }
                        Button(
                            onClick = {
                                onSubmitOrder(customerName, customerHp, paymentMethod)
                                onDismiss()
                            },
                            enabled = customerName.isNotBlank() && customerHp.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.testTag("btn_submit_order_final")
                        ) {
                            Text("Kirim Pesanan (WA)")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemPointsDialog(
    currentPoints: Int,
    userNama: String,
    userHp: String,
    onSubmitRedeem: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val pointOptions = listOf(25000, 50000, 100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000)
    var selectedAmount by remember { mutableStateOf(25000) }

    var recipientName by remember { mutableStateOf(userNama) }
    var recipientWa by remember { mutableStateOf(userHp) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "🎁 Penukaran Poin",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Poin Anda Saat Ini: $currentPoints Poin",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    Text(text = "Pilih Jumlah Poin Penukaran", style = MaterialTheme.typography.labelMedium)
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedAmount} Poin",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            pointOptions.forEach { amount ->
                                DropdownMenuItem(
                                    text = { Text("${amount} Poin") },
                                    onClick = {
                                        selectedAmount = amount
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (currentPoints < selectedAmount) {
                                    errorMessage = "Poin Anda ($currentPoints) tidak mencukupi untuk penukaran $selectedAmount Poin."
                                } else {
                                    errorMessage = null
                                    step = 2
                                }
                            }
                        ) {
                            Text("Lanjutkan →")
                        }
                    }
                } else {
                    Text(text = "Konfirmasi Data Penerima", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Nama Penerima") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recipientWa,
                        onValueChange = { recipientWa = it },
                        label = { Text("Nomor WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = { step = 1 }) { Text("← Kembali") }
                        Button(
                            onClick = {
                                onSubmitRedeem(recipientName, recipientWa, selectedAmount)
                                onDismiss()
                            },
                            enabled = recipientName.isNotBlank() && recipientWa.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Tukar Sekarang")
                        }
                    }
                }
            }
        }
    }
}
