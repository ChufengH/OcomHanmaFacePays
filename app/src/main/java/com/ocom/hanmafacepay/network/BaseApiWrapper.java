package com.ocom.hanmafacepay.network;

import com.ocom.hanmafacepay.network.entity.BaseResponse;
import io.reactivex.ObservableTransformer;

public class BaseApiWrapper {

    protected <A> A getService(Class<A> clz) {
        return RetrofitManagement.getINSTANCES().getService(clz);
    }


    protected <T extends BaseResponse> ObservableTransformer<T, T> applySchedulers() {
        return RetrofitManagement.getINSTANCES().applySchedulers();
    }
}
