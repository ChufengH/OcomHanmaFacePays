package com.ocom.hanmafacepay.network.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 所有响应的基础类，ret为0标记是否成功
 * @param ret 如果为0那么成功，否则失败
 * @param msg 错误信息，如果ret不为0那么返回错误信息，否则应该为空
 * @param timestamp 时间戳
 * @param sign 应该与请求的sign保持一致，格式为SHA256(device_no+ timestamp + 秘钥)
 * @param device_no 机具号，应该与请求的机具号保持一致
 */
open class BaseResponse(
    val ret: Int = -1, val msg: String = "", val timestamp: String = "",
    val sign: String = "", val device_no: String = ""
) {
    fun isSuccess() = ret == 0
}

/**
 * 支付响应
 * @param amount 实际消费金额
 * @param queryCount 查询次数
 * @param cash_account 现金余额
 * @param subsidy_account 补贴余额
 */
data class PayResponse(val amount: Int, val cash_account: Int = 0, val subsidy_account: Int = 0) :
    BaseResponse()

/**
 * 心跳响应
 * @param riskUpdateFlag 风险控制标记，为1的时候需要调用拉取风控接口
 * @param usersUpdateFlag 人员更新标记，为1的场子需要同步人员信息
 */
data class HeartBeatResponse(
    @SerializedName("fk_update_flag") val riskUpdateFlag: Int,
    @SerializedName("users_flag") val usersUpdateFlag: Int,
    @SerializedName("soft_update_flag") val softUpdateFlag: Int,
    @SerializedName("download_url") val downloadUrl: String
) : BaseResponse() {
    fun needControlRisk() = riskUpdateFlag == 1
    fun needUpdateUsers() = usersUpdateFlag == 1
    fun needUpdateSoft() = softUpdateFlag == 1
}

@Entity(tableName = "meal_limit")
data class MealLimit(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val meal_section: String, val end_time: String, val start_time: String, val amount: Int
) {
    /**
     * 判断开始时间和结束时间
     */
    private fun compareTime(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val startHour = start_time.substring(0, 2).toInt()
        val startMin = start_time.substring(3, 5).toInt()
        val endHour = end_time.substring(0, 2).toInt()
        val endMin = end_time.substring(3, 5).toInt()
        //有可能过夜
        if (startHour < endHour || (startHour == endHour && startMin < endMin)) {
            if (currentHour > startHour || (currentHour == startHour && currentMinute >= startMin)) {
                if (currentHour < endHour || (currentHour == endHour && currentMinute <= endMin))
                    return true
            }
        } else if (startHour > endHour) {
            if (currentHour < endHour || (currentHour == endHour && currentMinute <= endMin)
                || (currentHour > startHour || (currentHour == startHour && currentMinute >= startMin))
            ) {
                return true
            }
        }
        return false
    }

    //根据当前时间和消费金额判断当前是否能够进行消费
    fun isInRange(amount: Int): Boolean {
        return compareTime() && amount <= this.amount
    }
}

@Entity(
    tableName = "policy"
)
data class PolicyLimit(
    @PrimaryKey(autoGenerate = false)
    val policy: Int,
    //离线消费总额度
    val amount: String,
    //离线消费最大允许金额
    var order_amount: Int,
    //离线消费限制次数
    var order_num: Int,
    //实际消费的金额
    var real_amount: Int = 0,
    //实际消费的订单数
    var real_num: Int = 0
)

data class RiskControlResponse(
    val meal_section_para: List<MealLimit>,
    val policy_limit: List<PolicyLimit>
) :
    BaseResponse()

/**
 * 人员信息
 * @param picture: "", 头像，直接传回base64字符串
 * @param userid: "1",
 * @param name: "张帅",
 * @param job_number: "1"
 * @param flag:0,     // 0: 新增或更新、 1： 刪除,
 * @param policy:1   //消费策略
 */
@Entity(
    tableName = "users"
)
data class User(
    var picture: String,
    @PrimaryKey
    val userid: String,
    val name: String,
    val job_number: String,
    val flag: Int,//标记更新或者删除
    val policy: Int,
    val card_no: String = ""
) {
    fun needInsertOrUpdate() = flag == 0
    fun needDelete() = flag == 1
}

@Entity(
    tableName = "orders"
)
data class Order(
    @PrimaryKey
    val orderNo: String,
    val user_id: String,
    val amount: Int,
    val timestamp: String,
    //数据库标记
    val offline: Int = 0
)

/**
 * 订单结果包装类
 */
data class OrderSummary(val title: String, val orders: List<Order>)

/**
 * 人员信息更新响应
 * @param users 更新的员工信息
 */
data class UsersListResponse(val users: List<User>) : BaseResponse()