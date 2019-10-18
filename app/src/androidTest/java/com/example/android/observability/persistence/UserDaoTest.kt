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

package com.example.android.observability.persistence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.ocom.hanmafacepay.network.entity.User
import com.ocom.hanmafacepay.persistence.UsersDatabase
import com.ocom.hanmafacepay.util.extension.log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Test the implementation of [UserDao]
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: UsersDatabase

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears after test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            UsersDatabase::class.java
        )
            // allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetAllusers() {
        database.userDao().getAllusers()
            .test()
            .assertValue {
                it.isNotEmpty()
            }
    }

    @Test
    fun getUsersWhenNoUserInserted() {
        database.userDao().getUserById("123")
            .test()
            .assertNoValues()
    }

    @Test
    fun updateAllUser() {
        val list = mutableListOf<User>()
        list.add(USER)
        list.add(USER2)
        database.userDao().updataAllUsers(list)
//        Thread.sleep(2000)
        database.userDao().getAllusers().test()
            .assertValue {
                it.size == 1
            }
    }

    @Test
    fun insertAndGetUser() {
        // When inserting a new user in the data source
        database.userDao().insertData(USER).blockingAwait()
        database.userDao().insertData(
            User(
                "", "122",
                "Jack", "0", 0, 1
            )
        ).blockingAwait()

        // When subscribing to the emissions of the user
        database.userDao().getUserById(USER.userid)
            .test()
            .assertValue { it.userid == USER.userid && it.name == USER.name }
//        database.userDao().getAllusers()
//            .test()
//            .assertValue { it.size == 2 }
    }

    @Test
    fun updateAndGetUser() {
        // Given that we have a user in the data source
        database.userDao().insertData(USER).blockingAwait()

        // When we are updating the name of the user
        val updatedUser = User("", USER.userid, "new username", "2", 0, 0)
        database.userDao().insertData(updatedUser).blockingAwait()

        // When subscribing to the emissions of the user
        database.userDao().getUserById(USER.userid)
            .test()
            // assertValue asserts that there was only one emission of the user
            .assertValue { it.userid == USER.userid && it.name == "new username" }
    }

    @Test
    fun testDeleteAll() {
        database.userDao().deleteAllUsers().blockingAwait()
        database.userDao().getAllusers().test()
            .assertEmpty()
    }

    @Test
    fun deleteAndGetUser() {
        // Given that we have a user in the data source
        database.userDao().insertData(USER).blockingAwait()

        //When we are deleting all users
        database.userDao().deleteAllUsers()
        // When subscribing to the emissions of the user
        database.userDao().getUserById(USER.userid)
            .test()
            // check that there's no user emitted
            .assertNoValues()
    }

    companion object {
        private val USER = User("666", "123", "Castle", "2", 1, 0)
        private val USER2 = User("666", "124", "Castle", "2", 0, 0)
        private val disposable: CompositeDisposable = CompositeDisposable()
    }

    @Test
    fun testCount() {
        disposable.add(
            Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                .take(7)
                .flatMap { c ->
                    Observable.just("这是一个测试$c")
                }
                .doOnComplete {
                    log("执行到onComplete")
                }
                .subscribe {
                    if (it.substring(it.lastIndex,it.length).toInt()>4){
                        disposable.dispose()
                    }
                    log("next 结果$it")
                }
        )
        Thread.sleep(1500)
    }


    @Test
    fun testCalendar() {
        val cal = Calendar.getInstance(Locale.CHINA)
        val date = SimpleDateFormat("yyyyMMddHHMMSS").format(cal.time)
        assert(date.length == 14)
    }
}
