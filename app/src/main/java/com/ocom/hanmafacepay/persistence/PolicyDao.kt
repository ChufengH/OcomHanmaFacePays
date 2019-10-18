package com.ocom.hanmafacepay.persistence

import androidx.room.*
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.PolicyLimit
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * Data Access Object for the users table.
 */
@Dao
interface PolicyDao {

    @Transaction
    fun updatePolicyLimits(list: List<PolicyLimit>){
        deleteAllPolicy().blockingAwait()
        insertDatas(list).blockingAwait()
    }

    @Query("SELECT * FROM policy WHERE policy = :policy")
    fun getPolicy(policy: Int): Maybe<PolicyLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPolicyLimit(policyLimit: PolicyLimit): Completable


    @Query("DELETE FROM policy")
    fun deleteAllPolicy():Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(policyLimit: PolicyLimit): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDatas(policyLimit: List<PolicyLimit>): Completable

}
