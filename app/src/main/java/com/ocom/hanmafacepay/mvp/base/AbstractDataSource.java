package com.ocom.hanmafacepay.mvp.base;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * author : asstea
 * desc   : DataSource抽象类，提供对Subcription的操作方法
 */
public abstract class AbstractDataSource<T> {

    protected T mAPIWrapper;
    private CompositeDisposable mSubscription;
    private Disposable mCurrentSubscribe;

    public AbstractDataSource(T aPIWrapper) {
        this.mAPIWrapper = aPIWrapper;
        mSubscription = new CompositeDisposable();
    }

    protected void addSubscription(Disposable subscription) {
        mCurrentSubscribe = subscription;
        if (mSubscription != null && subscription != null) {
            mSubscription.add(subscription);
        }else{
            mSubscription = new CompositeDisposable();
            mSubscription.add(subscription);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void cancelCurrentRequest() {
        if (mCurrentSubscribe != null && !mCurrentSubscribe.isDisposed()) {
            mCurrentSubscribe.dispose();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void cancelAllRequest() {
        if (mSubscription != null && !mSubscription.isDisposed()) {
            mSubscription.dispose();
            mSubscription = null;
        }
    }

}
