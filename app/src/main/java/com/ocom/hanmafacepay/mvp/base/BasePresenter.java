package com.ocom.hanmafacepay.mvp.base;

/**
 * Presenter层基类
 *
 * @author lp 2016-7-21
 */
public interface BasePresenter<T> {
    void setView(T view);
    void cancelAllRequest();
}
