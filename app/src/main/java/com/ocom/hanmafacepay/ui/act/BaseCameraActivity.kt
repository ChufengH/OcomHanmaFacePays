package com.ocom.hanmafacepay.ui.act

import android.Manifest
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hanma.faceservice.RealandApplication
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.util.extension.REQUEST_ALL_PERMISSION
import com.ocom.hanmafacepay.util.extension.hasPermissions
import com.ocom.hanmafacepay.util.extension.hideBottomMenuUI
import com.ocom.hanmafacepay.util.extension.requestPermissionsEx
import com.ocom.hanmafacepay.ui.widget.FaceRectView
import dou.helper.CameraHelper
import dou.helper.CameraParams
import dou.utils.DeviceUtil
import java.lang.ref.WeakReference

abstract class BaseCameraActivity : AppCompatActivity() {

    /**
     * 默认相机参数
     */
    private val mCameraParams by lazy {
        CameraParams().apply {
            // 优先使用的camera Id,
            firstCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
            surfaceView = mCameraPreviewView
            preview_width = 640
            preview_height = 640

            if (DeviceUtil.getModel() == "Nexus 6") {
                camera_ori_front = 180
                RealandApplication.reverse_180 = true
            }
            previewFrameListener = PreviewFrameListener.apply { mActivity = WeakReference(this@BaseCameraActivity) }
        }
    }

    lateinit var mCameraHelper: CameraHelper

    /**
     * 预览Surface
     */
    private val mCameraPreviewView by lazy { findViewById<SurfaceView>(R.id.camera_preview) }
    val mFaceRectView: FaceRectView by lazy {findViewById<FaceRectView>(R.id.face_rect_view)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.run { addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        hideBottomMenuUI()
        requestPermissionsEx(*PERMISSIONS){ mCameraHelper = CameraHelper(this, mCameraParams) }
    }

    /**
     * 实际分析画面，识别人脸的逻辑函数，目前每五帧识别一帧
     */
    abstract fun onAnalysisFrame(p0: ByteArray?, p1: Camera?)

    /**
     * 相机预览回调，在这里按间隔取图片进行识别
     */
    object PreviewFrameListener : CameraHelper.PreviewFrameListener {
        var mActivity: WeakReference<BaseCameraActivity>? = null
        var frameCount = 0

        override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
            //过滤一些帧
            frameCount++
            if (frameCount < 5) return
            frameCount = 0
            mActivity?.get()?.run { onAnalysisFrame(p0, p1) }
        }
    }

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.ACCESS_WIFI_STATE,
//        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CAMERA
    )

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== REQUEST_ALL_PERMISSION) {
            if (hasPermissions(*PERMISSIONS))
                mCameraHelper = CameraHelper(this, mCameraParams)
            else
                requestPermissionsEx(*PERMISSIONS)
        }
    }

}