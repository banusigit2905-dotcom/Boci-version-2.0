package com.example.oktshop17.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oktshop17.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CartItem(
    val product: ProductEntity,
    val qty: Int
) {
    val subtotal: Long get() = product.harga * qty
}

enum class NavigationSection {
    RESELLER_DASHBOARD,
    RESELLER_RETURNS,
    RESELLER_COMPLAINTS,
    ADMIN_DASHBOARD,
    ADMIN_ACTIVATION,
    ADMIN_REDEEM,
    ADMIN_CATALOG,
    ADMIN_RANKINGS,
    ADMIN_RETURNS,
    ADMIN_COMPLAINTS,
    PROFILE
}

class OktshopViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OktshopRepository

    val currentUser = MutableStateFlow<UserEntity?>(null)
    val currentSection = MutableStateFlow(NavigationSection.RESELLER_DASHBOARD)
    val authError = MutableStateFlow<String?>(null)

    // DB Flows
    val products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val allResellers = MutableStateFlow<List<UserEntity>>(emptyList())
    val inactiveResellers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val allReturns = MutableStateFlow<List<ReturnEntity>>(emptyList())
    val allComplaints = MutableStateFlow<List<ComplaintEntity>>(emptyList())
    val allRedemptions = MutableStateFlow<List<RedemptionEntity>>(emptyList())

    // Cart
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = OktshopRepository(db)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()

            // Collect DB updates
            launch { repository.productDao.getAllProducts().collect { products.value = it } }
            launch { repository.userDao.getAllResellers().collect { allResellers.value = it } }
            launch { repository.userDao.getInactiveResellers().collect { inactiveResellers.value = it } }
            launch { repository.orderDao.getAllOrders().collect { allOrders.value = it } }
            launch { repository.returnDao.getAllReturns().collect { allReturns.value = it } }
            launch { repository.complaintDao.getAllComplaints().collect { allComplaints.value = it } }
            launch { repository.redemptionDao.getAllRedemptions().collect { allRedemptions.value = it } }
        }
    }

    // AUTH ACTIONS
    fun login(emailInput: String, passInput: String) {
        val email = emailInput.trim().lowercase()
        viewModelScope.launch {
            authError.value = null
            if (email.isEmpty()) {
                authError.value = "Email tidak boleh kosong"
                return@launch
            }

            var user = repository.userDao.getUserByEmail(email)
            // Fast switch/demo helper: if email is admin@oktshop17.com or contains admin, give admin user
            if (user == null && (email.contains("admin") || email == "admin@oktshop17.com")) {
                user = UserEntity(
                    uid = "admin_default_uid",
                    customId = "ADM001",
                    nama = "Admin OKTSHOP17",
                    email = "admin@oktshop17.com",
                    hp = "0895345452412",
                    role = "admin",
                    isActive = true
                )
                repository.userDao.insertUser(user)
            } else if (user == null) {
                authError.value = "Akun tidak ditemukan. Silakan DAFTAR terlebih dahulu."
                return@launch
            }

            if (user.role != "admin" && !user.isActive) {
                authError.value = "Akun Anda (${user.customId.ifEmpty { "User" }}) belum aktif. Silakan hubungi Admin via WhatsApp untuk aktivasi."
                return@launch
            }

            currentUser.value = user
            if (user.role == "admin") {
                currentSection.value = NavigationSection.ADMIN_DASHBOARD
            } else {
                currentSection.value = NavigationSection.RESELLER_DASHBOARD
            }
        }
    }

    fun quickLoginAsAdmin() {
        login("admin@oktshop17.com", "admin123")
    }

    fun quickLoginAsReseller() {
        login("febi@oktshop17.com", "febi123")
    }

    fun register(nama: String, emailInput: String, pass: String, hp: String, context: Context) {
        val email = emailInput.trim().lowercase()
        viewModelScope.launch {
            authError.value = null
            if (nama.isBlank() || email.isBlank() || hp.isBlank()) {
                authError.value = "Semua kolom wajib diisi"
                return@launch
            }

            val existing = repository.userDao.getUserByEmail(email)
            if (existing != null) {
                authError.value = "Email sudah terdaftar"
                return@launch
            }

            val cleanNama = nama.replace("\\s".toRegex(), "").take(4).lowercase()
            val randomNum = (10000..99999).random()
            val customId = "$cleanNama$randomNum"

            val newUser = UserEntity(
                uid = UUID.randomUUID().toString(),
                customId = customId,
                nama = nama,
                email = email,
                hp = hp,
                role = "reseller",
                isActive = false
            )
            repository.userDao.insertUser(newUser)

            val adminWA = "62895345452412"
            val waMsg = "Halo Admin, saya ingin aktivasi akun OKTSHOP17.\nNama: $nama\nEmail: $email\nNo. HP: $hp\nID User: $customId"

            Toast.makeText(context, "Pendaftaran Berhasil!\nID USER: $customId", Toast.LENGTH_LONG).show()

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$adminWA?text=${Uri.encode(waMsg)}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun logout() {
        currentUser.value = null
        cartItems.value = emptyList()
    }

    // CART
    fun addToCart(product: ProductEntity, qty: Int) {
        if (qty <= 0) return
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(qty = existing.qty + qty)
        } else {
            currentList.add(CartItem(product, qty))
        }
        cartItems.value = currentList
    }

    fun removeFromCart(index: Int) {
        val list = cartItems.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            cartItems.value = list
        }
    }

    fun clearCart() {
        cartItems.value = emptyList()
    }

    // SUBMIT ORDER
    fun submitOrder(customerName: String, customerHp: String, paymentMethod: String, context: Context) {
        val user = currentUser.value ?: return
        val cart = cartItems.value
        if (cart.isEmpty()) return

        viewModelScope.launch {
            val totalAmount = cart.sumOf { it.subtotal }
            val totalQty = cart.sumOf { it.qty }
            val summary = cart.joinToString(", ") { "${it.product.nama} (${it.qty}x)" }

            val newOrder = OrderEntity(
                id = UUID.randomUUID().toString(),
                resellerId = user.uid,
                resellerName = user.nama,
                customerName = customerName,
                customerHp = customerHp,
                produk = summary,
                total = totalAmount,
                jumlah = totalQty,
                metode = paymentMethod,
                status = "pending"
            )
            repository.orderDao.insertOrder(newOrder)

            val waText = "PESANAN BARU\nReseller: ${user.nama}\nPenerima: $customerName\nHP: $customerHp\nMetode: $paymentMethod\n\nDetail:\n" +
                    cart.mapIndexed { idx, item -> "  ${idx + 1}. ${item.product.nama} (${item.qty}x) = Rp ${item.subtotal}" }.joinToString("\n") +
                    "\n\nTOTAL: Rp $totalAmount"

            clearCart()

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/62895345452412?text=${Uri.encode(waText)}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // REDEEM POINTS
    fun submitRedemption(redeemName: String, wa: String, points: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.redemptionDao.insertRedemption(
                RedemptionEntity(
                    resellerId = user.uid,
                    resellerName = user.nama,
                    redeemName = redeemName,
                    wa = wa,
                    points = points,
                    status = "proses"
                )
            )
        }
    }

    // RETURNS
    fun submitReturn(produk: String, alasan: String, hp: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.returnDao.insertReturn(
                ReturnEntity(
                    resellerId = user.uid,
                    nama = user.nama,
                    produk = produk,
                    alasan = alasan,
                    hp = hp,
                    status = "proses"
                )
            )
        }
    }

    // COMPLAINTS
    fun submitComplaint(nama: String, hp: String, pesan: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.complaintDao.insertComplaint(
                ComplaintEntity(
                    resellerId = user.uid,
                    nama = nama,
                    hp = hp,
                    pesan = pesan,
                    status = "proses"
                )
            )
        }
    }

    // ADMIN ACTIONS
    fun activateUser(uid: String) {
        viewModelScope.launch {
            repository.userDao.activateUser(uid)
        }
    }

    fun markOrderCompleted(id: String) {
        viewModelScope.launch {
            repository.orderDao.updateOrderStatus(id, "Delivered")
        }
    }

    fun updateOrderStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            repository.orderDao.updateOrderStatus(id, newStatus)
        }
    }

    fun markReturnCompleted(id: String) {
        viewModelScope.launch {
            repository.returnDao.markReturnCompleted(id)
        }
    }

    fun markComplaintCompleted(id: String) {
        viewModelScope.launch {
            repository.complaintDao.markComplaintCompleted(id)
        }
    }

    fun markRedemptionCompleted(id: String) {
        viewModelScope.launch {
            repository.redemptionDao.markRedemptionCompleted(id)
        }
    }

    fun saveProduct(id: String?, nama: String, harga: Long, kategori: String) {
        viewModelScope.launch {
            val prod = ProductEntity(
                id = id.takeIf { !it.isNull_or_blank_safe() } ?: UUID.randomUUID().toString(),
                nama = nama,
                harga = harga,
                kategori = kategori.ifEmpty { "Umum" }
            )
            repository.productDao.insertProduct(prod)
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.productDao.deleteProduct(id)
        }
    }

    fun updateProfile(nama: String, hp: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(nama = nama, hp = hp)
            repository.userDao.updateUser(updated)
            currentUser.value = updated
        }
    }

    private fun String?.isNull_or_blank_safe(): Boolean = this.isNullOrBlank()
}
