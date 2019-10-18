package com.ocom.hanmafacepay.ui.act.setting.tradeHistory.adapter;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.math.BigDecimal;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 历史记录
 */
@Entity
public class TradeHistory {
    @Id(autoincrement = true)
    private Long id;//本地数据库的用户id

    private String card_number;//物理卡号
    private String logic_card_number;//逻辑卡号
    private String user_name ;//用户姓名
    private Long trade_time ;//用户交易时间
    private boolean trade_status;//交易状态
    private String trade_message;//交易成功或者失败的类型说明
    private String trade_money;//交易的金额 作为字符串存储 拿出来是再进行转换
    @Generated(hash = 1030315065)
    public TradeHistory(Long id, String card_number, String logic_card_number,
            String user_name, Long trade_time, boolean trade_status,
            String trade_message, String trade_money) {
        this.id = id;
        this.card_number = card_number;
        this.logic_card_number = logic_card_number;
        this.user_name = user_name;
        this.trade_time = trade_time;
        this.trade_status = trade_status;
        this.trade_message = trade_message;
        this.trade_money = trade_money;
    }
    @Generated(hash = 120534300)
    public TradeHistory() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCard_number() {
        return this.card_number;
    }
    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }
    public String getUser_name() {
        return this.user_name;
    }
    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
    public Long getTrade_time() {
        return this.trade_time;
    }
    public void setTrade_time(Long trade_time) {
        this.trade_time = trade_time;
    }
    public boolean getTrade_status() {
        return this.trade_status;
    }
    public void setTrade_status(boolean trade_status) {
        this.trade_status = trade_status;
    }
    public String getTrade_message() {
        return this.trade_message;
    }
    public void setTrade_message(String trade_message) {
        this.trade_message = trade_message;
    }
    public String getTrade_money() {
        return this.trade_money;
    }
    public void setTrade_money(String trade_money) {
        this.trade_money = trade_money;
    }
    public String getLogic_card_number() {
        return this.logic_card_number;
    }
    public void setLogic_card_number(String logic_card_number) {
        this.logic_card_number = logic_card_number;
    }



}
