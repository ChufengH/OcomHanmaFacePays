package com.ocom.faceidentification.module.setting.tradeHistory

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.observability.Injection
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.User
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.ITradeHistoryConstract
import com.ocom.hanmafacepay.ui.adapter.UsersAdapter
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.ui.widget.LoadingDialog
import com.ocom.hanmafacepay.util.DividerItemDecoration
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_user_list.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * 交易历史记录
 */
class UserListFragment : BaseFragment(), UsersAdapter.ContactsAdapterListener {

    private val mAdapter: UsersAdapter by lazy {
        UsersAdapter(context!!, mutableListOf(), this)
    }

    private lateinit var mPresenter: ITradeHistoryConstract.ITradeHistoryPresenter

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel


    companion object {
        fun newInstance(): UserListFragment {
            return UserListFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
        initData()
    }

    override fun setLayout(): Any = R.layout.fragment_user_list

    private fun initViews() {
        recycler_view.also {
            it.layoutManager = LinearLayoutManager(context!!)

            it.itemAnimator = DefaultItemAnimator()
            it.setBackgroundColor(Color.parseColor("#567777"))
            it.addItemDecoration(
                DividerItemDecoration(
                    context!!,
                    DividerItemDecoration.VERTICAL_LIST,
                    36
                )
            )
            it.adapter = mAdapter
        }
    }

    private fun initData() {
        viewModelFactory = Injection.provideViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        observerData()
    }

    private var mDisposable: Disposable? = null

    private fun observerData() {
        mDisposable = viewModel.getAllUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("数据更新:$it")
                mAdapter.switchData(it)
            }
    }

    private val mLoadingDialog by lazy { LoadingDialog(context!!) }

    val disposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun filter(userName: String) {
       mAdapter.filter.filter(userName)
    }

    override fun onContactSelected(contact: User?) {
        ToastUtil.showShortToast("${contact?.name} been clicked!")
    }
}