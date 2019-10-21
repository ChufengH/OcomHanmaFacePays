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

package com.ocom.hanmafacepay.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ocom.hanmafacepay.persistence.MealSectionDao
import com.ocom.hanmafacepay.persistence.OrderDao
import com.ocom.hanmafacepay.persistence.PolicyDao
import com.ocom.hanmafacepay.persistence.UserDao

/**
 * Factory for ViewModels
 */
class ViewModelFactory(private val dataSource: UserDao,
                       private val mealSectionDao: MealSectionDao,
                       private val policyDao: PolicyDao, private val orderDao: OrderDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(dataSource,mealSectionDao,policyDao,orderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}