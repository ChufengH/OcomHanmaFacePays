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

package com.example.android.observability

import android.content.Context
import com.ocom.hanmafacepay.persistence.UsersDatabase
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import com.ocom.hanmafacepay.persistence.MealSectionDao
import com.ocom.hanmafacepay.persistence.OrderDao
import com.ocom.hanmafacepay.persistence.PolicyDao
import com.ocom.hanmafacepay.persistence.UserDao

/**
 * Enables injection of data sources.
 */
object Injection {

    fun provideUserDataSource(context: Context): UserDao {
        val database = UsersDatabase.getInstance(context)
        return database.userDao()
    }

    fun provideMealLimitDataSource(context: Context): MealSectionDao {
        val database = UsersDatabase.getInstance(context)
        return database.mealLimitDao()
    }

    fun providePolicyDataSource(context: Context): PolicyDao {
        val database = UsersDatabase.getInstance(context)
        return database.policyDao()
    }

    fun provideOrderDataSource(context: Context): OrderDao {
        val database = UsersDatabase.getInstance(context)
        return database.orderDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return ViewModelFactory(dataSource, provideMealLimitDataSource(context),
            providePolicyDataSource(context), provideOrderDataSource(context))
    }
}
