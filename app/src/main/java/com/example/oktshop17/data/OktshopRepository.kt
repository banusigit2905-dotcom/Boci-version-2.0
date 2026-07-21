package com.example.oktshop17.data

import kotlinx.coroutines.flow.Flow

class OktshopRepository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val productDao = db.productDao()
    val orderDao = db.orderDao()
    val returnDao = db.returnDao()
    val complaintDao = db.complaintDao()
    val redemptionDao = db.redemptionDao()

    suspend fun seedInitialDataIfEmpty() {
        // Seed default Admin user if not existing
        val admin = userDao.getUserByEmail("admin@oktshop17.com")
        if (admin == null) {
            userDao.insertUser(
                UserEntity(
                    uid = "admin_default_uid",
                    customId = "ADM001",
                    nama = "Admin OKTSHOP17",
                    email = "admin@oktshop17.com",
                    hp = "0895345452412",
                    role = "admin",
                    isActive = true
                )
            )
        }

        // Seed demo Reseller if not existing
        val reseller = userDao.getUserByEmail("febi@oktshop17.com")
        if (reseller == null) {
            userDao.insertUser(
                UserEntity(
                    uid = "reseller_febi_uid",
                    customId = "febi12345",
                    nama = "Febi Reseller",
                    email = "febi@oktshop17.com",
                    hp = "081234567890",
                    role = "reseller",
                    isActive = true
                )
            )
        }

        // Seed products if catalog empty
        val existingProducts = productDao.getAllProducts()
        // We do a quick check via direct query or seeding if needed
        productDao.insertProduct(ProductEntity(id = "p1", nama = "Boci Original Pedas", harga = 15000, kategori = "Baso Aci"))
        productDao.insertProduct(ProductEntity(id = "p2", nama = "Boci Milk Cheese Extra", harga = 18000, kategori = "Baso Aci"))
        productDao.insertProduct(ProductEntity(id = "p3", nama = "Boci Super Jumbo Komplit", harga = 22000, kategori = "Baso Aci"))
        productDao.insertProduct(ProductEntity(id = "p4", nama = "Cuanki Crunchy Special", harga = 12000, kategori = "Topping"))
        productDao.insertProduct(ProductEntity(id = "p5", nama = "Pilus Cikur Gurih (Pack)", harga = 8000, kategori = "Topping"))
    }
}
