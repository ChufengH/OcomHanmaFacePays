package com.ocom.hanmafacepay.mvp.base;

import com.ocom.hanmafacepay.network.RetrofitManagement;
import com.ocom.hanmafacepay.network.entity.BaseResponse;
import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.HttpException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

/**
 * 用于统一处理网络错误
 *
 * @param <T>
 */
public abstract class CallbackWrapper<T extends BaseResponse> extends DisposableObserver<T> {
    //BaseView is just a reference of a View in MVP
    private WeakReference<BaseView> weakReference;

    public CallbackWrapper(BaseView view) {
        this.weakReference = new WeakReference<>(view);
    }

    protected abstract void onSuccess(T t);

    protected abstract void onApiError(RetrofitManagement.APIException e);

    protected void onTimeOut() {
    }

    protected boolean onAllError(Throwable e) {
        return false;
    }

    @Override
    public void onNext(T t) {
        //You can return StatusCodes of different cases from your API and handle it here. I usually include these cases on BaseResponse and iherit it from every Response
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        BaseView view = weakReference.get();
//        if (onAllError(e)) return;
        if (e instanceof HttpException) {
            ResponseBody responseBody = ((HttpException) e).response().errorBody();
            view.onUnknownError(getErrorMessage(responseBody));
        } else if (e instanceof SocketTimeoutException) {
            view.onTimeout();
            onTimeOut();
        } else if (e instanceof IOException) {
            view.onNetworkError();
        } else if (e instanceof RetrofitManagement.APIException) {
            onApiError((RetrofitManagement.APIException) e);
        } else {
            view.onUnknownError(e.getMessage());
        }
    }

    @Override
    public void onComplete() {

    }

    private String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("msg");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
