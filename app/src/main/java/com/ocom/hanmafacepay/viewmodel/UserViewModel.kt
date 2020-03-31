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

import android.graphics.BitmapFactory
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import com.hanma.faceservice.BitMapUtil
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.FaceServiceManager
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.network.entity.PolicyLimit
import com.ocom.hanmafacepay.network.entity.User
import com.ocom.hanmafacepay.persistence.MealSectionDao
import com.ocom.hanmafacepay.persistence.OrderDao
import com.ocom.hanmafacepay.persistence.PolicyDao
import com.ocom.hanmafacepay.persistence.UserDao
import com.ocom.hanmafacepay.ui.widget.ActivityPartnerManager
import com.ocom.hanmafacepay.util.extension.base64ToByteArray
import com.ocom.hanmafacepay.util.extension.log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * View Model for the [UserActivity]
 */
class UserViewModel(
    private val dataSource: UserDao,
    private val mealSectionDao: MealSectionDao,
    private val policyDao: PolicyDao,
    private val orderDao: OrderDao
) : ViewModel(), CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private fun deleteInLocalAsync(userId: String) {
        FaceServiceManager.getInstance().addRunnable {
            FaceServiceManager.getInstance().removeUserFeature(userId)
        }
    }

    private fun registerInLocal(userId: String) {
        val bytes = userId.base64ToByteArray() ?: return
        //只读尺寸就可以了
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count()) ?: return
        val rgbBytes = BitMapUtil.getRGBFromBMP(bitmap)
        FaceServiceManager.getInstance().addRunnable {
            FaceServiceManager.getInstance()
                .registerUserByImage(userId, rgbBytes, bitmap.width, bitmap.height)
        }
        bitmap.recycle()
    }

    fun updateUsers(users: List<User>) {
        users.filter { it.flag == 0 && !TextUtils.isEmpty(it.picture) }.forEach {
            val file = File(FacePayApplication.INSTANCE.filesDir, it.userid)
            val outputStream = FileOutputStream(file)
            try {
                val bytes = it.picture.toByteArray(Charset.forName("UTF-8"))
                outputStream.write(bytes)
                outputStream.flush()
            } catch (e: Exception) {
                log(e.localizedMessage ?: "")
            } finally {
                outputStream.close()
            }
            it.picture = ""
            registerInLocal(it.userid)
        }
        val updateUsers = users.filter { it.needInsertOrUpdate() }
        val deletedUsers = users.filter { it.needDelete() }
        deletedUsers.forEach { deleteInLocalAsync(it.userid) }
        val temp = dataSource.insertDatas(updateUsers)
            .andThen(dataSource.deleteUsers(deletedUsers))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                log("Update all users success!")
                ActivityPartnerManager.dismissDialog()
            }, { e -> e.printStackTrace() })
    }

    fun updateMealLimits(list: List<MealLimit>) {
        mealSectionDao.updateAllMealLimits(list)
    }

    fun insertMealLimit(mealLimit: MealLimit) =
        mealSectionDao.insertData(mealLimit)

    fun insertMealLimits(list: List<MealLimit>) = mealSectionDao.insertDatas(list)

    fun insertPolicy(policy: PolicyLimit) = policyDao.insertData(policy)


    fun updatePolicys(list: List<PolicyLimit>) {
        policyDao.updatePolicyLimits(list)
    }

    // for every emission of the user, get the user name
    fun getAllUsers(): Flowable<List<User>> {
        return dataSource.getAllusers()
    }

    fun getAllMealLimits() = mealSectionDao.getAllMealLimits()

    fun clearOrderByTime(time: String) = orderDao.clearOrderByTimeStamp(time)

    fun deleteUser(userId: String) {
        dataSource.deleteUser(userId)
    }

    fun deleteAllUsers() = dataSource.deleteAllUsers()

    fun deleteOrder(id: String) = orderDao.deleteOrder(id)

    fun insertOrder(order: Order) = orderDao.insertData(order)

    fun getOrderById(id: String) = orderDao.getOrderById(id)

    fun getAllOrders() =
        orderDao.getAllOrders()

    fun getOrdersAfter(time: String) =
        orderDao.getAllOrdersAfter(time)

    fun getOrdersBetween(start: String, end: String) =
        orderDao.getAllOrdersBetween(start, end)

    fun getAllOnlineOrders() =
        orderDao.getAllOnlineOrders()

    fun getAllOfflineOrders() = orderDao.getAllOfflineOrders()

    fun getPolicy(policy: Int) = policyDao.getPolicy(policy)

    fun getPolicyLimitForUser(userId: String): Maybe<PolicyLimit> {
        return dataSource.getUserById(userId)
            .flatMap { policyDao.getPolicy(it.policy) }
    }

    fun getUserByCardNo(cardNo: String): Single<User> {
        return dataSource.getUserByCard(cardNo)
    }

}
