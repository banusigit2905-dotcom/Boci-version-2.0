package com.example.oktshop17

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oktshop17.ui.NavigationSection
import com.example.oktshop17.ui.OktshopViewModel
import com.example.oktshop17.ui.components.NotificationBadge
import com.example.oktshop17.ui.components.RedeemPointsDialog
import com.example.oktshop17.ui.screens.*
import com.example.oktshop17.ui.theme.OKTSHOP17Theme
import kotlinx.coroutines.launch
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OKTSHOP17Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OktshopApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OktshopApp(viewModel: OktshopViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentSection by viewModel.currentSection.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showRedeemDialog by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        val user = currentUser!!
        val isAdmin = user.role == "admin"

        // DB Data for Badges & Points
        val inactiveUsers by viewModel.inactiveResellers.collectAsState()
        val allOrders by viewModel.allOrders.collectAsState()
        val allReturns by viewModel.allReturns.collectAsState()
        val allComplaints by viewModel.allComplaints.collectAsState()
        val allRedemptions by viewModel.allRedemptions.collectAsState()

        val pendingActivations = inactiveUsers.size
        val pendingOrders = remember(allOrders) { allOrders.count { it.status.equals("pending", ignoreCase = true) } }
        val pendingReturns = remember(allReturns) { allReturns.count { it.status.equals("proses", ignoreCase = true) } }
        val pendingComplaints = remember(allComplaints) { allComplaints.count { it.status.equals("proses", ignoreCase = true) } }

        // Current points for reseller
        val myCompletedOrders = remember(allOrders, user) {
            allOrders.filter { it.resellerId == user.uid && it.status.equals("Selesai", ignoreCase = true) }
        }
        val mySpentTotal = remember(myCompletedOrders) { myCompletedOrders.sumOf { it.total } }
        val myCompletedRedeems = remember(allRedemptions, user) {
            allRedemptions.filter { it.resellerId == user.uid && it.status.equals("Selesai", ignoreCase = true) }
        }
        val usedPoints = remember(myCompletedRedeems) { myCompletedRedeems.sumOf { it.points } }
        val myPoints = remember(mySpentTotal, usedPoints) {
            (floor(mySpentTotal.toDouble() / 100.0).toInt() - usedPoints).coerceAtLeast(0)
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "OKTSHOP17",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MENU UTAMA • ${if (isAdmin) "ADMIN" else "RESELLER"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("📊 Dashboard Admin") },
                            selected = currentSection == NavigationSection.ADMIN_DASHBOARD,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_DASHBOARD
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("🔑 Aktivasi Akun ($pendingActivations)") },
                            selected = currentSection == NavigationSection.ADMIN_ACTIVATION,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_ACTIVATION
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("🎁 Penukaran Poin") },
                            selected = currentSection == NavigationSection.ADMIN_REDEEM,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_REDEEM
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("📦 Update Katalog") },
                            selected = currentSection == NavigationSection.ADMIN_CATALOG,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_CATALOG
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("🏆 Peringkat Reseller") },
                            selected = currentSection == NavigationSection.ADMIN_RANKINGS,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_RANKINGS
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("📥 Returan Masuk ($pendingReturns)") },
                            selected = currentSection == NavigationSection.ADMIN_RETURNS,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_RETURNS
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("📢 Keluhan Masuk ($pendingComplaints)") },
                            selected = currentSection == NavigationSection.ADMIN_COMPLAINTS,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.ADMIN_COMPLAINTS
                                scope.launch { drawerState.close() }
                            }
                        )
                    } else {
                        NavigationDrawerItem(
                            label = { Text("📊 Dashboard Reseller") },
                            selected = currentSection == NavigationSection.RESELLER_DASHBOARD,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.RESELLER_DASHBOARD
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("📦 Retur Barang") },
                            selected = currentSection == NavigationSection.RESELLER_RETURNS,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.RESELLER_RETURNS
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("📢 Laporan Keluhan") },
                            selected = currentSection == NavigationSection.RESELLER_COMPLAINTS,
                            onClick = {
                                viewModel.currentSection.value = NavigationSection.RESELLER_COMPLAINTS
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("👤 Profil Akun") },
                        selected = currentSection == NavigationSection.PROFILE,
                        onClick = {
                            viewModel.currentSection.value = NavigationSection.PROFILE
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "OKTSHOP17",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        actions = {
                            if (isAdmin) {
                                NotificationBadge(icon = "🔑", count = pendingActivations, onClick = { viewModel.currentSection.value = NavigationSection.ADMIN_ACTIVATION })
                                NotificationBadge(icon = "📦", count = pendingOrders, onClick = { viewModel.currentSection.value = NavigationSection.ADMIN_DASHBOARD })
                                NotificationBadge(icon = "📥", count = pendingReturns, onClick = { viewModel.currentSection.value = NavigationSection.ADMIN_RETURNS })
                                NotificationBadge(icon = "📢", count = pendingComplaints, onClick = { viewModel.currentSection.value = NavigationSection.ADMIN_COMPLAINTS })
                            } else {
                                Button(
                                    onClick = { showRedeemDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text("🎁 TUKAR POIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            TextButton(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Text("KELUAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (currentSection) {
                        NavigationSection.RESELLER_DASHBOARD -> ResellerDashboardScreen(viewModel = viewModel)
                        NavigationSection.RESELLER_RETURNS -> ResellerReturnsScreen(viewModel = viewModel)
                        NavigationSection.RESELLER_COMPLAINTS -> ResellerComplaintsScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_ACTIVATION -> AdminActivationScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_REDEEM -> AdminRedeemScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_CATALOG -> AdminCatalogScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_RANKINGS -> AdminRankingsScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_RETURNS -> AdminReturnsScreen(viewModel = viewModel)
                        NavigationSection.ADMIN_COMPLAINTS -> AdminComplaintsScreen(viewModel = viewModel)
                        NavigationSection.PROFILE -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }

        if (showRedeemDialog) {
            RedeemPointsDialog(
                currentPoints = myPoints,
                userNama = user.nama,
                userHp = user.hp,
                onSubmitRedeem = { name, wa, pts -> viewModel.submitRedemption(name, wa, pts) },
                onDismiss = { showRedeemDialog = false }
            )
        }
    }
}
