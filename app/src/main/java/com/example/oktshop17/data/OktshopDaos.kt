package com.example.oktshop17.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserById(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'reseller' AND isActive = 0 ORDER BY createdAt DESC")
    fun getInactiveResellers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'reseller' ORDER BY nama ASC")
    fun getAllResellers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = 1 WHERE uid = :uid")
    suspend fun activateUser(uid: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY nama ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE resellerId = :resellerId ORDER BY createdAt DESC")
    fun getOrdersByReseller(resellerId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = 'Selesai' WHERE id = :id")
    suspend fun markOrderCompleted(id: String)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String)
}

@Dao
interface ReturnDao {
    @Query("SELECT * FROM returns ORDER BY createdAt DESC")
    fun getAllReturns(): Flow<List<ReturnEntity>>

    @Query("SELECT * FROM returns WHERE resellerId = :resellerId ORDER BY createdAt DESC")
    fun getReturnsByReseller(resellerId: String): Flow<List<ReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnEntity: ReturnEntity)

    @Query("UPDATE returns SET status = 'Selesai' WHERE id = :id")
    suspend fun markReturnCompleted(id: String)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE resellerId = :resellerId ORDER BY createdAt DESC")
    fun getComplaintsByReseller(resellerId: String): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Query("UPDATE complaints SET status = 'Selesai' WHERE id = :id")
    suspend fun markComplaintCompleted(id: String)
}

@Dao
interface RedemptionDao {
    @Query("SELECT * FROM redemptions ORDER BY createdAt DESC")
    fun getAllRedemptions(): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemptions WHERE resellerId = :resellerId ORDER BY createdAt DESC")
    fun getRedemptionsByReseller(resellerId: String): Flow<List<RedemptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: RedemptionEntity)

    @Query("UPDATE redemptions SET status = 'Selesai' WHERE id = :id")
    suspend fun markRedemptionCompleted(id: String)
}
