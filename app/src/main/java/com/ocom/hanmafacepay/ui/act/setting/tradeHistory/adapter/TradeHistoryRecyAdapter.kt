package com.ocom.faceidentification.module.setting.tradeHistory.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ocom.faceidentification.common.CommonUtils
import com.ocom.faceidentification.utils.TimeUtils
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.adapter.TradeHistory
import com.ocom.hanmafacepay.util.BigDecimalUtils

class TradeHistoryRecyAdapter(val mOnRefundClick: ((item: TradeHistory) -> Unit)? = null) :
    BaseQuickAdapter<Order, BaseViewHolder>(R.layout.item_setting_trade_list) {


    override fun convert(helper: BaseViewHolder?, item: Order?) {
        helper?.setText(R.id.item_cardNumTv, item?.orderNo)
            ?.setText(R.id.item_userNameTv, item?.user_id)
            ?.setText(R.id.item_timeTv, item?.timestamp?.let { TimeUtils.stampToDate(it.toLong()) })
            ?.setText(R.id.item_tradeStatusTv, if (item?.offline==0)"在线订单" else "离线订单")
            ?.setText(R.id.item_moneyTv, item?.amount?.let { BigDecimalUtils.div(it.toString(), "100").toString() + "元" })
        val tradeStatusTv = helper?.getView<TextView>(R.id.item_tradeStatusTv)
//        val refundBtn = helper?.getView<Button>(R.id.refund_btn)
//        if (CommonProcess.getSettingRefundAllow()) {
//            tradeStatusTv?.visibility = View.GONE
//            refundBtn?.visibility = View.VISIBLE
//            refundBtn?.also {
//                it.setOnClickListener {
//                    item ?: return@setOnClickListener
//                    mOnRefundClick?.invoke(item)
//                }
//            }
//        } else {
//            refundBtn?.visibility = View.GONE
//            tradeStatusTv?.visibility = View.VISIBLE
//        }
        if (item?.offline==0) {
            tradeStatusTv?.setTextColor(CommonUtils.getColor(R.color.green))
        } else {
            tradeStatusTv?.setTextColor(CommonUtils.getColor(R.color.red))
        }
    }
}