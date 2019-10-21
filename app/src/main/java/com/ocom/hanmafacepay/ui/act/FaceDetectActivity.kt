package com.ocom.hanmafacepay.ui.act

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.text.TextUtils
import com.ocom.hanmafacepay.FaceServiceManager
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.KEY_USER_ID
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.ioToMain
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_face_detect.*
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

    private val disposable = CompositeDisposable()
    //标记是否是定值消费模式
    private var mContantHint: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_face_detect)
        super.onCreate(savedInstanceState)
        mContantHint = intent.getStringExtra(KEY_CONSTANT_HINT)
    }


    override fun onAnalysisFrame(p0: ByteArray?, p1: Camera?) {
        FaceServiceManager.getInstance().iFaceRecoServiceApi ?: return
        val faces = FaceServiceManager.getInstance().getFacesDrawByYuvData(
            p0, mCameraHelper.previewSize.width, mCameraHelper.previewSize.height
        )
        if (faces == null) {
            tv_description.text = mContantHint ?: "检测中...."
            mFaceRectView.clearRect()
        } else {
            mFaceRectView.drawFaceRect(faces, 640, 640)
            tv_description.text = "检测到人脸,请稍等"
            p0?.run {
                if (!mIsRegistering)
                    getUserIdAsync(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.hanma.fcd.CameraUtil.turnOnLight()
        disposable.add(Maybe.timer(10, TimeUnit.SECONDS)
            .ioToMain()
            .subscribe {
                finishWithUserId("")
            }
        )
    }

    override fun onStop() {
        com.hanma.fcd.CameraUtil.turnOffLight()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraHelper.stopCamera()
        disposable.dispose()
    }

    private var mIsRegistering = false


    private fun finishWithUserId(userId: String) {
        val intent = Intent()
        intent.putExtra(KEY_USER_ID, userId)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * 根据yuv查找
     */
    private fun getUserIdAsync(byteArray: ByteArray) = launch {
        //判断是否已经在机器里面注册,如果已经注册,那就直接认证成功
        mIsRegistering = true
        val users = mutableListOf<String>()
        val iw = mCameraHelper.previewSize.width
        val ih = mCameraHelper.previewSize.height
        val result = FaceServiceManager.getInstance()
            .recognizeFacesByYuvData(byteArray, iw, ih, 1, 0.7f, users)
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
            if (TextUtils.equals(intent.action, ACTION_SHUT_DOWN)) {
                this@FaceDetectActivity.finish()
            }
        }
    }
}