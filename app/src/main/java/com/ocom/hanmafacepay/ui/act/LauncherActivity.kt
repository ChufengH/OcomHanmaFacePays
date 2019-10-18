package com.ocom.hanmafacepay.ui.act

import android.Manifest
import android.os.Bundle
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.base.BaseActivity
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


/**
 * 启动模块 以后可能要初始化加载某些部件
 */
class LauncherActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {


    companion object {
        const val PERMISSION_REQUEST_CODE = 0x00000001 //必要权限申请
    }

    private val permissions = arrayListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)


    override fun onActivityCreat(savedInstanceState: Bundle?) {
        jump()//跳转
    }

    override fun setAttachLayoutRes(): Int = R.layout.activity_luncher

    /**
     * 根据设备注册情况跳转
     */
    private fun jump() {
        if (EasyPermissions.hasPermissions(
                this@LauncherActivity,
                permissions[0],
                permissions[1],
                permissions[2]
            )) {


           startWithFinish(HomeActivity::class.java)
            //判断设备号和单位号是否初始化
///*            if (CommonProcess.getDeviceCode().isNullOrEmpty() &&//设备号
//                CommonProcess.getGroupCode().isNullOrEmpty() //单位号
//            ) {
//                startWithFinish(RegistDeviceActivity::class.java)
//            } else {
//                startWithFinish(HomeActivity::class.java)
//            }*/
        } else {
            EasyPermissions.requestPermissions(
                this, "必要权限申请，未通过权限则无法开启应用", PERMISSION_REQUEST_CODE,
                permissions[0],
                permissions[1],
                permissions[2]
            )
        }
    }


    /**
     * 权限申请拒绝的回调
     *
     * @param requestCode 申请权限时的请求码
     * @param perms 申请拒绝的权限集合
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) { //跳转
        //如果有一些权限被永久的拒绝, 就需要转跳到 设置-->应用-->对应的App下去开启权限
        if (requestCode == PERMISSION_REQUEST_CODE && EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //处理权限名字字符串
            val sb = StringBuffer()
            for (str in perms) {
                sb.append(str)
                sb.append("\n")
            }
            sb.replace(sb.length - 2, sb.length, "")
            AppSettingsDialog.Builder(this)
                .setTitle("权限申请")
                .setRationale("此功能需要摄像，本地存储空间访问权限，否则无法打开应用，是否打开设置")
                .setPositiveButton("是")
                .setNegativeButton("关闭")
                .build()
                .show()
        }
    }



    /**
     * 权限申请成功的回调
     *
     * @param requestCode 申请权限时的请求码
     * @param perms 申请成功的权限集合
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == PERMISSION_REQUEST_CODE){
            jump()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}