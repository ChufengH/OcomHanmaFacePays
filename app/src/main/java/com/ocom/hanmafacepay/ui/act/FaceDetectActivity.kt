package com.ocom.hanmafacepay.ui.act

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Camera
import android.net.ConnectivityManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import com.example.android.observability.Injection
import com.ocom.faceidentification.module.tencent.setting.TencentSettingActivity
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.FaceServiceManager
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.const.KEY_USER_ID
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_face_detect.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * 人脸识别首页
 */
class FaceDetectActivity : BaseCameraActivity(), CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        const val REQUEST_FACE_DETECT = 0x1004
        const val ACTION_SHUT_DOWN = "ACTION_SHUT_DOWN"
        const val ACTION_CHANGE_HINT = "ACTION_CHANGE_HINT"
        const val ACTION_CARD_NO_SCANNED = "ACTION_CARD_NO_SCANNED"
        const val KEY_CONSTANT_HINT = "KEY_CONSTANT_HINT"
        fun start(context: Context, constantHint: String) {
            val intent = Intent(context, FaceDetectActivity::class.java).apply {
                putExtra(
                    KEY_CONSTANT_HINT,
                    constantHint
                )
            }
            context.startActivity(intent)
        }
    }

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()
    //标记是否是定值消费模式
    private var mContantHint: String? = null;
    private val mBroadcastReceiver = FaceDetectBroadcastReceiver()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mContantHint = intent?.getStringExtra(KEY_CONSTANT_HINT)
    }


    override fun onAnalysisFrame(p0: ByteArray?, p1: Camera?) {
        FaceServiceManager.getInstance().iFaceRecoServiceApi ?: return
        p1 ?: return
        mCameraHelper ?: return
        val faces = FaceServiceManager.getInstance().getFacesDrawByYuvData(
            p0, mCameraHelper?.previewSize?.width ?: 640, mCameraHelper?.previewSize?.height ?: 480
        )
        if (faces == null) {
//            tv_description.text = mContantHint ?: "检测中...."
            tv_description.text =  "检测中...."
            mFaceRectView.clearRect()
        } else {
            //不在支付的时候才识别
            if (!mIsPaying) {
                mFaceRectView.drawFaceRect(faces, 640, 480)
                tv_description.text = "检测到人脸,请稍等"
                p0?.run {
                    if (!mIsRegistering)
                        getUserIdAsync(this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.hanma.fcd.CameraUtil.turnOnLight()
        FacePayApplication.INSTANCE.startSeekCard()
        registerBroadcast()
        if (mContantHint.isNullOrEmpty()) {
            disposable.add(Maybe.timer(10, TimeUnit.SECONDS)
                .ioToMain()
                .subscribe {
                    finishWithUserId("")
                }
            )
        }
    }

    override fun onStop() {
        com.hanma.fcd.CameraUtil.turnOffLight()
        unregisterReceiver(mBroadcastReceiver)
        FacePayApplication.INSTANCE.stopSeekCard()
        disposable.clear()
//        mCameraHelper.stopPreview()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraHelper?.stopCamera()
        disposable.dispose()
    }

    private var mIsRegistering = false
    private var mIsPaying = false

    override fun onResume() {
        super.onResume()
        mIsPaying = false
        disposable.add(Maybe.timer(2, TimeUnit.SECONDS).subscribe {
            mIsUsingCard = false
        })
        readTTs("开始验证")
    }

    private fun finishWithUserId(userId: String) {
        //如果定值消费,那么直接跳消费
        if (mContantHint != null) {
            mIsPaying = true;
            TencentPayActivity.jump4PayFace(
                this, CommonProcess.getSettingConstantMoney(),
                userId
            )
        } else {
            val intent = Intent()
            intent.putExtra(KEY_USER_ID, userId)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    /**
     * 根据yuv查找
     */
    private fun getUserIdAsync(byteArray: ByteArray) = launch {
        //判断是否已经在机器里面注册,如果已经注册,那就直接认证成功
        mIsRegistering = true
        val users = mutableListOf<String>()
        val result = FaceServiceManager.getInstance()
            .recognizeFacesByYuvData(byteArray, 640, 480, 1, 0.6f, users)
        if (result == 1 && users.isNotEmpty()) {
            log("识别成功!$users[0]", TAG = FaceDetectActivity::class.java.simpleName)
            finishWithUserId(users[0])
        }
        mIsRegistering = false
    }

    private inner class FaceDetectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            when (intent.action) {
                ACTION_SHUT_DOWN -> {
                    this@FaceDetectActivity.finish()
                }
                ACTION_CHANGE_HINT -> {
                    this@FaceDetectActivity.mContantHint = intent.getStringExtra(KEY_CONSTANT_HINT)
                }
                ACTION_CARD_NO_SCANNED -> {
                    onCardNoScanned(intent.getStringExtra(KEY_CONSTANT_HINT));
                }
            }
        }
    }

    private var mIsUsingCard = false

    private fun onCardNoScanned(card_no: String?) {
        if (mIsUsingCard)
            return
        if (card_no.isNullOrEmpty()) {
//            readTTs("请重新刷卡")
            return
        }
        mIsUsingCard = true
        ToastUtil.showLongToast("识别到卡号$card_no")
        disposable.add(
            viewModel.getUserByCardNo(card_no)
                .subscribeOn(Schedulers.io())
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe({
                    //                    readTTs("刷卡成功")
                    finishWithUserId(it.userid)
                }, {
                    readTTs("请重新刷卡")
                    disposable.add(Maybe.timer(2, TimeUnit.SECONDS).subscribe {
                        mIsUsingCard = false
                    })
                })
        )

    }

    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        finish()
    }

    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {
    }

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        mContantHint = intent?.getStringExtra(KEY_CONSTANT_HINT)
//        tv_description.text = mContantHint ?: "检测中...."
        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        btn_back.setOnClickListener { onBackPressed() }
        btn_setting.setOnClickListener {
            //设置
            if (mIsAdmin) {
                start(TencentSettingActivity::class.java)
            } else {
                mPwdInputDialog.show()
            }
        }
    }

    private fun registerBroadcast() {
        val filter = IntentFilter(ACTION_SHUT_DOWN).apply {
            addAction(ACTION_CHANGE_HINT)
            addAction(ACTION_CARD_NO_SCANNED)
        }
        registerReceiver(mBroadcastReceiver, filter)
    }

    override fun setAttachLayoutRes(): Int {
        return R.layout.activity_face_detect;
    }

    //    override fun onBackPressed() {
//        if (!TextUtils.isEmpty(mContantHint)) {
//            return
//        }
//        super.onBackPressed()
//    }
    private val mPwdInputDialog: AlertDialog by lazy { createPasswordInputDialog() }
    private val mSettingDialog: AlertDialog by lazy { createPasswordSettingDialog() }
    private var mInputPassword: String? = null//输入的密码
    private var mPwdInputTv: EditText? = null //输入密码的editText
    private var mIsAdmin = false

    private fun createPasswordInputDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@FaceDetectActivity, R.style.lightDialog)
        builder.setTitle("验证管理权限")
        val viewInflated =
            LayoutInflater.from(this@FaceDetectActivity)
                .inflate(R.layout.dialog_input_admin, null)
        mPwdInputTv = viewInflated.findViewById(R.id.tv_input_password) as EditText
        mPwdInputTv!!.setSelection(mPwdInputTv!!.text.length)
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            mPwdInputDialog.dismiss()
            mInputPassword = mPwdInputTv?.text.toString()
            mPwdInputTv?.setText("")
            mIsAdmin = TextUtils.equals(mInputPassword, CommonProcess.settingPassword)
            if (mIsAdmin) {
                start(TencentSettingActivity::class.java)
                mIsAdmin = false
            } else {
                ToastUtil.showLongToastCenter("密码错误!")
            }
        }

        builder.setNeutralButton("修改密码") { _, _ ->
            mPwdInputDialog.dismiss()
            mSettingDialog.show()
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    private fun createPasswordSettingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@FaceDetectActivity, R.style.lightDialog)
        builder.setTitle("修改密码")
        val viewInflated =
            LayoutInflater.from(this@FaceDetectActivity)
                .inflate(R.layout.dialog_input_set_pwd, null)
        val oldTv = viewInflated.findViewById(R.id.tv_old_password) as EditText
        val newTv = viewInflated.findViewById(R.id.tv_new_password) as EditText
        oldTv.hint = "请输入旧密码"
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            checkPassword(oldTv, newTv)
        }
        builder.setNeutralButton("默认密码") { _, _ ->
            CommonProcess.settingPassword = "123321"
            ToastUtil.showLongToastCenter("密码已恢复!")
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    private fun checkPassword(oldTv: EditText, newTv: EditText) {
        if (TextUtils.isEmpty(oldTv.text.toString())) {
            ToastUtil.showShortToast("请输入正确的旧密码!")
        } else {
            if (TextUtils.isEmpty(newTv.text.toString()) || newTv.text.length < 6) {
                ToastUtil.showShortToast("请输入新密码,最短为6位!")
            } else {
                if (!TextUtils.equals(oldTv.text.toString(), CommonProcess.settingPassword)) {
                    ToastUtil.showShortToast("旧密码不正确!")
                } else {
                    ToastUtil.showLongToastCenter("密码设置成功!")
                    CommonProcess.settingPassword = newTv.text.toString()
                }
            }
        }
    }
}
