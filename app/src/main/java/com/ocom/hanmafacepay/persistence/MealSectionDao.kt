package com.ocom.hanmafacepay.persistence

import androidx.room.*
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.User
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Data Access Object for the users table.
 */
@Dao
interface MealSectionDao{

    @Transaction
    fun updateAllMealLimits(list: List<MealLimit>){
        deleteAllMealLimits().blockingAwait()
        insertDatas(list).blockingAwait()
    }

    @Query("SELECT * FROM meal_limit")
    fun getAllMealLimits(): Maybe<List<MealLimit>>

    @Query("DELETE FROM meal_limit")
    fun deleteAllMealLimits():Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(mealSectionDao: MealLimit): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDatas(mealSectionDao: List<MealLimit>): Completable
}
