package com.ocom.hanmafacepay.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    private var mRootView: View? = null

    private var isViewPrepare = false//视图是否加载完成

    private var hasLoadData = false//是否加载过数据


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        val layoutObject: Any = setLayout()
        rootView = when (layoutObject) {
            is Int -> {
                val viewId: Int = layoutObject
                inflater.inflate(viewId, container, false)
            }
            is View -> layoutObject
            else -> throw ClassCastException("setLayout() 传入参数类型有误")
        }
        mRootView = rootView

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (isUseEventBus()) {
//            EventBus.getDefault().register(this)
//        }
        isViewPrepare = true
        onBindView(mRootView!!, savedInstanceState)
        lazyLoadDataIfPrepared()
    }


    override fun onDestroy() {
        super.onDestroy()
//        if (isUseEventBus()) {
//            EventBus.getDefault().unregister(this)
//        }

    }

    /**
     * fragment是否可见
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            lazyLoadDataIfPrepared()
            showOnUser()
        }
    }


    private fun lazyLoadDataIfPrepared() {
        if (userVisibleHint && isViewPrepare && !hasLoadData) {
            lazyLoad()
            hasLoadData = true
        }
    }


    /**
     * 直接开启一个activity
     */
    fun _startActivity(targetActivityCls: Class<*>) {
        startActivity(Intent(this@BaseFragment.context, targetActivityCls))
    }
    /**
     * fragment添加
     */

    fun addFragmentInFragmentManager(@IdRes containerId:Int, targetFragment:Fragment){
        fragmentManager?.beginTransaction()?.add(containerId,targetFragment)?.commit()
    }
    /**
     * fragment切换
     */

    fun showHideFragment(showFragment: Fragment, hideFragment: BaseFragment) {
        fragmentManager?.beginTransaction()?.show(showFragment)?.hide(hideFragment)?.commit()
    }

    /**
     * 显示fragment
     */
    fun showFragment(showFragment:Fragment){
        fragmentManager?.beginTransaction()?.show(showFragment)?.commit()
    }

    //-----------------------------------------------------------------实现方法
    abstract fun onBindView(rootView: View, @Nullable savedInstanceState: Bundle?)

    abstract fun setLayout(): Any



    //-----------------------------------------------------------------对外公开
    open fun <T : View> bind(@IdRes id: Int): T {
        return mRootView!!.findViewById(id)
    }

    /**
     * 懒加载 或者重新加载
     */
    open fun lazyLoad() {}

    /**
     * 当前fragment可见
     */
    open fun showOnUser(){}

    /**
     * 注册eventBus
     */
    open fun isUseEventBus(): Boolean = false


}