package com.ocom.hanmafacepay.ui.act.setting.settings.connect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.NetworkUtils
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.network.RetrofitManagement
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.util.ToastUtil
import kotlinx.android.synthetic.main.fragment_tencent_connect.*


class TencentConnectFragment : BaseFragment() {
    companion object {
        fun newInstance(): TencentConnectFragment {
            return TencentConnectFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
    }

    override fun setLayout(): Any = R.layout.fragment_tencent_connect

    private fun initViews() {
        connect_statusRefreshBtn.setOnClickListener {
            //刷新
            setNetInfo()
        }

        connect_settingBtn.setOnClickListener {
            //设置网络
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        setNetInfo()

    }

    private fun setNetInfo() {
        //------------------------------------------------网络连接状态
        when (NetworkUtils.isConnected()) {
            true -> {
                connect_connectStatusTv.text = getString(R.string.connected)
                connect_connectStatusTv.setTextColor(ColorUtils.getColor(R.color.common_container_background_green))
            }
            false -> {
                connect_connectStatusTv.text = getString(R.string.unconnect)
                connect_connectStatusTv.setTextColor(ColorUtils.getColor(R.color.common_container_background_orange))
            }
        }
        ll_net_host.setOnClickListener {
            mHostInputDialog.show()
        }
        //------------------------------------------------网络类型
        connect_netTypeTv.text = NetworkUtils.getNetworkType().name
        //------------------------------------------------ip地址
        connect_ipAddressTv.text = NetworkUtils.getIPAddress(true)
        //------------------------------------------------网关
        connect_gatewayTv.text = NetworkUtils.getGatewayByWifi()
        //------------------------------------------------子网掩码
        connect_netMaskTv.text = NetworkUtils.getNetMaskByWifi()
        //------------------------------------------------服务器host
        connect_netHostTv.text = CommonProcess.getBaseHost()
    }

    private val mHostInputDialog: AlertDialog by lazy { createPasswordInputDialog() }
    private var mHostInputTv: EditText? = null //输入密码的editText
    private var mInputHost: String? = null//输入的密码

    private fun createPasswordInputDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@TencentConnectFragment.context, R.style.lightDialog)
        builder.setTitle("修改服务器地址")
        val viewInflated =
            LayoutInflater.from(this@TencentConnectFragment.context)
                .inflate(R.layout.dialog_input_admin, null)
        mHostInputTv = viewInflated.findViewById(R.id.tv_input_password) as EditText
        mHostInputTv?.filters = arrayOf(InputFilter.LengthFilter(60));
        mHostInputTv?.inputType = InputType.TYPE_TEXT_VARIATION_URI
        mHostInputTv?.hint = "请输入服务器地址"
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            mHostInputDialog.dismiss()
            mInputHost = mHostInputTv?.text.toString()
            mHostInputTv?.setText("")
            if (!TextUtils.isEmpty(mInputHost) && isValidUrl(mInputHost!!)) {
                if (!mInputHost!!.startsWith("http:")) {
                    mInputHost = "http://$mInputHost"
                }
                if (!mInputHost!!.endsWith("/")) {
                    mInputHost += "/"
                }
                RetrofitManagement.getINSTANCES().changeBaseUrl(mInputHost)
                connect_netHostTv.text = CommonProcess.getBaseHost()
            } else {
                ToastUtil.showLongToastCenter("请输入正确的服务器地址!")
            }
        }
        builder.setNeutralButton("默认地址") { _, _ ->
            RetrofitManagement.getINSTANCES().resetDefaultBaseUrl()
            connect_netHostTv.text = CommonProcess.getBaseHost()
        }

        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    /**
     * This is used to check the given URL is valid or not.
     * @param url
     * @return true if url is valid, false otherwise.
     */
    private fun isValidUrl(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.toLowerCase())
        return m.matches()
        return true;
    }
}