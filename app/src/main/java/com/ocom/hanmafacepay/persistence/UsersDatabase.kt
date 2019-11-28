/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ocom.hanmafacepay.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.network.entity.PolicyLimit
import com.ocom.hanmafacepay.network.entity.User

/**
 * The Room database that contains the Users table
 */
@Database(
    entities = [User::class, PolicyLimit::class, MealLimit::class, Order::class],
    exportSchema = false,
    version = 6
)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun policyDao(): PolicyDao
    abstract fun mealLimitDao(): MealSectionDao
    abstract fun orderDao(): OrderDao

    companion object {

        @Volatile
        private var INSTANCE: UsersDatabase? = null

        fun getInstance(context: Context): UsersDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                UsersDatabase::class.java, "Users.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
