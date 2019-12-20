package com.ocom.hanmafacepay.ui.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.network.entity.OrderSummary
import com.ocom.hanmafacepay.util.BigDecimalUtils
import java.util.*
import kotlin.math.max

class OrderHistoryByDayAdapter(
    val dataList: List<OrderSummary>,
    val mealLimits: List<MealLimit>
) :
    RecyclerView.Adapter<OrderHistoryByDayAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dataList.size > position) {
            val summary = dataList[position]
            val list = mutableListOf<String>()
            list.addAll(mealLimits.map {

                val startHour =
                    if (!TextUtils.isEmpty(it.local_start_time))
                        it.local_start_time!!.substring(
                            0,
                            it.local_start_time!!.indexOf(":")
                        ).toInt()
                    else
                        it.start_time.substring(0, 2).toInt()
                val startMin =
                    if (!TextUtils.isEmpty(it.local_start_time))
                        it.local_start_time!!.substring(
                            it.local_start_time!!.indexOf(":") + 1,
                            it.local_start_time!!.length
                        ).toInt()
                    else
                        it.start_time.substring(3, 5).toInt()

                val endHour =
                    if (!TextUtils.isEmpty(it.local_end_time))
                        it.local_end_time!!.substring(0, it.local_end_time!!.indexOf(":")).toInt()
                    else
                        it.end_time.substring(0, 2).toInt()

                val endMin =
                    if (!TextUtils.isEmpty(it.local_end_time))
                        it.local_end_time!!.substring(
                            it.local_end_time!!.indexOf(":") + 1,
                            it.local_end_time!!.length
                        ).toInt()
                    else
                        it.end_time.substring(3, 5).toInt()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = summary.orders[0].timestamp.toLong()
                calendar.set(Calendar.HOUR_OF_DAY, startHour)
                calendar.set(Calendar.MINUTE, startMin)
                //跨夜了,开始时间减一天
                if (startHour > endHour) {
                    calendar.add(Calendar.DATE, -1)
                }
                val startTime = calendar.timeInMillis
                if (startHour > endHour) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.set(Calendar.HOUR_OF_DAY, endHour)
                calendar.set(Calendar.MINUTE, endMin)
                val endTime = calendar.timeInMillis
                val amountInTimeRange = calAmountInTimeRange(summary.orders, startTime, endTime)
                "${it.meal_section}(${it.start_time}至${it.end_time}): ${amountInTimeRange / 100f}元"
            })
            val remainAmount = max(summary.orders.sumBy { it.amount } -
                    list.sumBy {
                        BigDecimalUtils.mul(
                            it.substring(it.lastIndexOf(": ") + 2, it.lastIndex),
                            "100"
                        ).toInt()
                    }, 0)
            list.add("其余时间消费: ${remainAmount / 100f}元")
            holder.bindOrder(summary.orders, summary.title, list)
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHistoryByDayAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_summary_card
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun calAmountInTimeRange(orders: List<Order>, startTime: Long, endTime: Long): Int {
        var sum = 0
        sum = orders.filter { it.timestamp.toLong() >= startTime }
            .filter { it.timestamp.toLong() <= endTime }
            .sumBy { it.amount }
        return sum
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mTitleTv: TextView by lazy { itemView.findViewById<TextView>(R.id.card_title_tv) }
        private val mBreakfastTv: TextView by lazy { itemView.findViewById<TextView>(R.id.breakfast_tv) }
        private val mAfternoonTv: TextView by lazy { itemView.findViewById<TextView>(R.id.afternoon_tv) }
        private val mDinnerTv: TextView by lazy { itemView.findViewById<TextView>(R.id.dinner_tv) }
        private val mEveningTv: TextView by lazy { itemView.findViewById<TextView>(R.id.evening_tv) }
        private val mOtherTv: TextView by lazy { itemView.findViewById<TextView>(R.id.other_tv) }
        private val mTotalTv: TextView by lazy { itemView.findViewById<TextView>(R.id.total_tv) }

        fun bindOrder(orders: List<Order>, title: String, detailList: List<String>) {
            mTitleTv.text = title
            mTotalTv.text = "合计消费: ${orders.sumBy { it.amount } / 100f}元"
            mBreakfastTv.text = detailList[0]
            mAfternoonTv.text = detailList[1]
            mDinnerTv.text = detailList[2]
            mEveningTv.text = detailList[3]
            mOtherTv.text = detailList[4]
        }
    }

}