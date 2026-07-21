package com.example.oktshop17.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oktshop17.ui.OktshopViewModel
import com.example.oktshop17.ui.components.StatusChip

@Composable
fun ResellerReturnsScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val allReturns by viewModel.allReturns.collectAsState()

    var prod by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf(user?.hp ?: "") }

    val myReturns = remember(allReturns, user) {
        allReturns.filter { it.resellerId == user?.uid }
    }

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
                        text = "📦 Ajukan Retur Barang",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = prod,
                        onValueChange = { prod = it },
                        label = { Text("Nama Produk & Jumlah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Alasan Retur / Kendala") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hp,
                        onValueChange = { hp = it },
                        label = { Text("No. WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (prod.isNotBlank() && reason.isNotBlank() && hp.isNotBlank()) {
                                viewModel.submitReturn(prod, reason, hp)
                                Toast.makeText(context, "Pengajuan retur berhasil dikirim!", Toast.LENGTH_SHORT).show()
                                prod = ""
                                reason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("KIRIM PENGAJUAN RETUR")
                    }
                }
            }
        }

        item {
            Text(text = "📜 Riwayat Retur Saya", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (myReturns.isEmpty()) {
            item { Text("Belum ada riwayat retur", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(myReturns) { r ->
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
                            Text(r.produk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Alasan: ${r.alasan}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("WA: ${r.hp}", fontSize = 11.sp, color = Color.Gray)
                        }
                        StatusChip(status = r.status)
                    }
                }
            }
        }
    }
}

@Composable
fun ResellerComplaintsScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val allComplaints by viewModel.allComplaints.collectAsState()

    var nama by remember { mutableStateOf(user?.nama ?: "") }
    var hp by remember { mutableStateOf(user?.hp ?: "") }
    var pesan by remember { mutableStateOf("") }

    val myComplaints = remember(allComplaints, user) {
        allComplaints.filter { it.resellerId == user?.uid }
    }

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
                        text = "📢 Laporan Keluhan / Masukan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Pelapor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hp,
                        onValueChange = { hp = it },
                        label = { Text("Nomor HP / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pesan,
                        onValueChange = { pesan = it },
                        label = { Text("Isi Keluhan / Kendala...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nama.isNotBlank() && hp.isNotBlank() && pesan.isNotBlank()) {
                                viewModel.submitComplaint(nama, hp, pesan)
                                Toast.makeText(context, "Keluhan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                                pesan = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("KIRIM LAPORAN KELUHAN")
                    }
                }
            }
        }

        item {
            Text(text = "📜 Riwayat Laporan Keluhan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (myComplaints.isEmpty()) {
            item { Text("Belum ada riwayat keluhan", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(myComplaints) { c ->
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
                            Text(c.pesan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Pelapor: ${c.nama} (${c.hp})", fontSize = 11.sp, color = Color.Gray)
                        }
                        StatusChip(status = c.status)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: OktshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()

    var nama by remember(user) { mutableStateOf(user?.nama ?: "") }
    var hp by remember(user) { mutableStateOf(user?.hp ?: "") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "👤 Profil Akun",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email Anda (Tetap)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = user?.customId ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ID Reseller Anda") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hp,
                    onValueChange = { hp = it },
                    label = { Text("No. WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (nama.isNotBlank() && hp.isNotBlank()) {
                            viewModel.updateProfile(nama, hp)
                            Toast.makeText(context, "Profil Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("UPDATE PROFIL")
                }
            }
        }
    }
}
