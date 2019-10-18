package com.ocom.hanmafacepay.mvp.base;

public abstract class AbstractPresenter<DataSource extends AbstractDataSource, View> implements BasePresenter<View> {

    protected final DataSource mDataSource;
    protected View mView;

    public AbstractPresenter(DataSource dataSource) {
        mDataSource = dataSource;
    }

    @Override
    public void cancelAllRequest() {
        if (mDataSource != null) {
            mDataSource.cancelAllRequest();
        }
    }

    public void onDestroy() {
        cancelAllRequest();
    }

    public void cancelCurrentRequest() {
        if (mDataSource != null) {
            mDataSource.cancelCurrentRequest();
        }
    }

    @Override
    public void setView(View view) {
        mView = view;
    }

}
