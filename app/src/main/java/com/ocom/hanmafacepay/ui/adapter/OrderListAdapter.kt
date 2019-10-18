package com.ocom.hanmafacepay.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderListAdapter(val dataList: List<Order>, var mCallback: OrderListAdapterView? = null) :
    RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dataList.size > position) {
            val order = dataList[position]
            holder.bindOrder(order)
            holder.itemView.setOnClickListener {
                mCallback?.onRefund(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_order
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mUserIdTv: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_userid) }
        private val mOrderNoTv: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_orderNo) }

        fun bindOrder(order: Order) {
            with(order) {
                val cal = Calendar.getInstance(Locale.CHINA)
                cal.timeInMillis = timestamp.toLong()
                val date = SimpleDateFormat("yyyy年MM月dd日HH:mm").format(cal.time)
                mOrderNoTv.text = "订单号: ${orderNo.substring(0, 9)} 消费金额: ${amount/100f}元"
                mUserIdTv.text = "用户Id: $user_id 消费日期: $date"
            }
        }
    }


    interface OrderListAdapterView {
        fun onRefund(order: Order)
    }
}