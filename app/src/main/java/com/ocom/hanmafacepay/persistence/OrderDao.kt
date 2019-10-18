package com.ocom.hanmafacepay.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ocom.hanmafacepay.network.entity.Order
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Data Access Object for the users table.
 */
@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE orderNo = :id")
    fun getOrderById(id: String): Flowable<Order>

    @Query("SELECT * FROM orders WHERE offline = 1")
    fun getAllOfflineOrders(): Flowable<List<Order>>

    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flowable<List<Order>>

    @Query("SELECT * FROM orders WHERE timestamp >= :timeStamp")
    fun getAllOrdersAfter(timeStamp: String): Flowable<List<Order>>

    @Query("SELECT * FROM orders WHERE timestamp >= :startTime AND timestamp<=:endTime")
    fun getAllOrdersBetween(startTime: String, endTime: String): Flowable<List<Order>>

    @Query("SELECT * FROM orders WHERE offline = 0")
    fun getAllOnlineOrders(): Flowable<List<Order>>

    @Query("DELETE FROM orders WHERE orderNo = :id")
    fun deleteOrder(id: String): Completable

    @Query("DELETE FROM orders WHERE timestamp < :timeStamp")
    fun clearOrderByTimeStamp(timeStamp: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(user: Order): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDatas(user: List<Order>): Completable

    /**
     * Delete all users.
     */
    @Query("DELETE FROM orders")
    fun deleteAllOrders(): Completable
}
