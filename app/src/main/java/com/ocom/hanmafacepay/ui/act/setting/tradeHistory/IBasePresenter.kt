package com.ocom.hanmafacepay.ui.act.setting.tradeHistory

import androidx.lifecycle.LifecycleObserver
import io.reactivex.disposables.CompositeDisposable


interface IBasePresenter: LifecycleObserver {
    fun start()

}