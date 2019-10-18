package com.ocom.hanmafacepay.network;

import retrofit2.Response;

public interface HttpCallback {

    void loginOut();

    <T> void appForceUpdate(Response<T> response);

}
