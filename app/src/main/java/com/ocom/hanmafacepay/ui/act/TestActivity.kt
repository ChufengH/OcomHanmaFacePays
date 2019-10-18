package com.ocom.hanmafacepay.ui.act

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.const.SIGN
import com.ocom.hanmafacepay.const.TIME_STAMP
import com.ocom.hanmafacepay.mvp.datasource.HomeDataSource
import com.ocom.hanmafacepay.mvp.datasource.IHomeView
import com.ocom.hanmafacepay.network.entity.*

class TestActivity : AppCompatActivity(), IHomeView {
    override fun onPaySuccess(response: PayResponse, order: Order) {
    }

    override fun onCancelOrderFailed(erroMsg: String) {
    }


    override fun onPayFailed(erroMsg: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mDataSource by lazy{ HomeDataSource(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        mDataSource.startHeartBeat()
    }


    override fun onUpdateUsers(usersListResponse: UsersListResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpdateRiskInfo(riskControlResponse: RiskControlResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
