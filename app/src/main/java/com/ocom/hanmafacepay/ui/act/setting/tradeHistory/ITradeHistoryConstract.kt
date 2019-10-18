package com.ocom.hanmafacepay.ui.act.setting.tradeHistory

import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.adapter.TradeHistory
import io.reactivex.Observable

/**
 * 交易历史
 */
interface ITradeHistoryConstract {
    interface ITradeHistoryPresenter : IBasePresenter {
        /**
         * 获取
         */
        fun getTradeHistory()
    }


    interface ITradeHistoryView : IBaseView<ITradeHistoryPresenter> {
        /**
         * 获取数据完成 isUpdate 是否更新 是则刷新 否则直接添加
         */
        fun upDateHistoryDone(isUpdate: Boolean, data: List<Order>?)


    }


    interface ITradeHistoryModel : IBaseModel {

        /**
         * 获取
         */
        fun getAllTradeHistory(): Observable<List<TradeHistory>>

        /**
         * 分页查找
         * nowIndex 当前第几页
         * pageNumber 每一页的数量
         */
        fun getHistroyByLimit(nowIndex: Int, pageNumber: Int): Observable<List<TradeHistory>>
    }
}