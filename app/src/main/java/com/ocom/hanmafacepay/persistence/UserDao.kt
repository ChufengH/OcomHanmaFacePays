package com.ocom.hanmafacepay.persistence

import androidx.room.*
import com.ocom.hanmafacepay.network.entity.User
import io.reactivex.*

/**
 * Data Access Object for the users table.
 */
@Dao
interface UserDao {

    @Transaction
    fun updataAllUsers(users: List<User>) {
        users.filter { it.needDelete() }.forEach {
            deleteUser(it.userid).blockingAwait()
        }
        users.filter { it.needInsertOrUpdate() }.let {
            insertDatas(it).blockingAwait()
        }
    }

    @Delete
    fun deleteUsers(users: List<User>): Completable

    @Query("SELECT * FROM Users WHERE card = :card_no")
    fun getUserByCard(card_no: String): Single<User>

    @Query("SELECT * FROM Users WHERE userid = :id")
    fun getUserById(id: String): Maybe<User>

    @Query("SELECT * FROM Users")
    fun getAllusers(): Flowable<List<User>>

    @Query("DELETE FROM Users WHERE userid = :id")
    fun deleteUser(id: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(user: User): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDatas(user: List<User>): Completable

    /**
     * Delete all users.
     */
    @Query("DELETE FROM Users")
    fun deleteAllUsers(): Completable
}
