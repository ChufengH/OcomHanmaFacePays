package com.castle.serialport

import android.util.Log
import com.ocom.hanmafacepay.const.SERIAL_PORT_NAME_CARD_READER
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executors

object SerialPortManager {

    private val mPublishSubscribe = PublishSubject.create<Pair<String, Array<out String>>>()

    init {
        Log.d("SerialPortManager", "开始加载库")
        System.loadLibrary("mserialport")
        val scheduler = Schedulers.from(Executors.newFixedThreadPool(5))
        val d = mPublishSubscribe
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .doFinally { scheduler.shutdown() }
            .subscribe {
                sendMessage(it.first, it.second)
            }
    }


    fun closeAll() {
        closeSerialPort(SERIAL_PORT_NAME_CARD_READER)
    }


    /**
     * 发送批量命令
     * @param path 串口路径
     * @param command 命令数组,由字符串组成
     */
    fun sendMessageEx(path: String, vararg command: String) {
        mPublishSubscribe.onNext(Pair(path, command))
    }

    /**
     * 打开一个读串口,用于监听数据
     * @param path 串口路径,通常为/dev/tty*开头
     * @param baudrate 串口拨特率,底层在打开串口时会检测一次默认波特率
     * @param listener 读数据监听,为空的话就为只写接口
     */
    external fun openSerialPort(path: String, baudrate: Int, listener: OnReadListener? = null)

    /**
     * 关闭串口
     * @param path 串口路径,通常为/dev/tty*开头
     * @param 如果成功,那么返回0,否则为失败
     */
    external fun closeSerialPort(path: String)

    /**
     * 设置读串口的周期时间,越长的周期可以处理越长的数据,同时响应也会变慢,此方法需要在串口开启后调用,否则将不起任何作用
     * 此方法可以临时在读数据前调用以灵活调整响应时间
     * @param timeinterval 轮循读串口的时间,单位为纳秒 参考时间(键盘-100, 短码扫码头-5000, 长码扫玛头-50000)
     * @param path 串口路径,通常为/dev/tty*开头
     */
    external fun setReadTimeInterval(path: String, timeInterval: Int)
    /**
     * 发送消息给指定串口, 底层已经为串口读写专门开启线程,上层可以直接调用,无需切换线程
     * @param path 串口路径,通常为/dev/tty*开头
     * @param msg 要发送给串口的消息, 直接传入hexString即可, 底层会将其转换成为16进制byte数组
     * @param flags 标记, 1->只写,2->只读,3->读写
     * @return 如果失败,底层会直接抛出异常
     */
    external fun sendMessage(path: String, msg: Array<out String>)

    interface OnReadListener {
        fun onDataReceived(msg: ByteArray)
    }
}
