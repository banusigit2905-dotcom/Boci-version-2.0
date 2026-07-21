package com.example.oktshop17.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    val customId: String = "",
    val nama: String = "",
    val email: String = "",
    val hp: String = "",
    val role: String = "reseller", // "reseller" or "admin"
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nama: String = "",
    val harga: Long = 0,
    val kategori: String = "Umum"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resellerId: String = "",
    val resellerName: String = "",
    val customerName: String = "",
    val customerHp: String = "",
    val produk: String = "",
    val total: Long = 0,
    val jumlah: Int = 0,
    val metode: String = "Transfer Bank",
    val status: String = "pending", // "pending", "Selesai"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "returns")
data class ReturnEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resellerId: String = "",
    val nama: String = "",
    val produk: String = "",
    val alasan: String = "",
    val hp: String = "",
    val status: String = "proses", // "proses", "Selesai"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resellerId: String = "",
    val nama: String = "",
    val hp: String = "",
    val pesan: String = "",
    val status: String = "proses", // "proses", "Selesai"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "redemptions")
data class RedemptionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resellerId: String = "",
    val resellerName: String = "",
    val redeemName: String = "",
    val wa: String = "",
    val points: Int = 0,
    val status: String = "proses", // "proses", "Selesai"
    val createdAt: Long = System.currentTimeMillis()
)
