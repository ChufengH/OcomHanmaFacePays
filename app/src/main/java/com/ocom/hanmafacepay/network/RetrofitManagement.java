package com.ocom.hanmafacepay.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.ocom.hanmafacepay.FacePayApplication;
import com.ocom.hanmafacepay.network.entity.BaseResponse;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitManagement {
    private static final String TAG = "RetrofitManagement";

    @SuppressWarnings("unused")
    private final MediaType MediaType_image = MediaType.parse("image/*");
    private final Map<Class, Object> service = new ConcurrentHashMap<>();
    private boolean DEBUG = false;
    private Retrofit retrofit;
    private HttpCallback mHttpCallback;
    public static final String DEFAULT_BASE_HOST = "http://61.129.251.161:6006/";
    private static String baseUrl = DEFAULT_BASE_HOST;
    //测试用,每10次请求切换一次base以模拟离线模式
    private static int mOfflineCount = 0;

    public static RetrofitManagement getINSTANCES() {
        return RetrofitUtilInnerClass.INSTANCES;
    }

    @SuppressWarnings("unused")
    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    @SuppressWarnings("unchecked")
    <T> T getService(Class<T> clz) {
        T value;
        if (service.containsKey(clz)) {
            final Object s = service.get(clz);
            if (s == null) {
                value = getRetrofit(baseUrl).create(clz);
                service.put(clz, value);
            } else {
                value = (T) s;
            }
        } else {
            value = getRetrofit(baseUrl).create(clz);
            service.put(clz, value);
        }
        return value;
    }

    public void changeBaseUrl(String url) {
        SharedPreferences preferences = FacePayApplication.INSTANCE.getSharedPreferences("temp_file", Context.MODE_PRIVATE);
        baseUrl = url;
        service.put(ApiService.class, getRetrofit(baseUrl).create(ApiService.class));
        preferences.edit().putString("sp_setting_base_host", url).commit();
    }

    public void resetDefaultBaseUrl() {
        SharedPreferences preferences = FacePayApplication.INSTANCE.getSharedPreferences("temp_file", Context.MODE_PRIVATE);
        baseUrl = DEFAULT_BASE_HOST;
        service.put(ApiService.class, getRetrofit(baseUrl).create(ApiService.class));
        preferences.edit().putString("sp_setting_base_host", DEFAULT_BASE_HOST).commit();
    }

    /**
     * 设置okhttp各项参数
     */
    private OkHttpClient getDownloadOkHttpClient(DownloadResponseBody.DownloadListener downloadListener) {
        return new OkHttpClient.Builder()
                .addInterceptor(new DownloadInterceptor(downloadListener))
                .connectTimeout(60L, TimeUnit.SECONDS)
                .readTimeout(60L, TimeUnit.SECONDS)
                .writeTimeout(60L, TimeUnit.SECONDS)
                .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER) //设置所有证书通过 不安全
                .build();
    }

    public Retrofit getDownloadRetrofit(DownloadResponseBody.DownloadListener downloadListener) {
        retrofit = new Retrofit.Builder()
                .baseUrl(DEFAULT_BASE_HOST)
                .client(getDownloadOkHttpClient(downloadListener))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit;

    }


    private Retrofit getRetrofit(String url) {
//        if (retrofit == null) {
        ParamsInterceptor paramsInterceptor = new ParamsInterceptor();
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .addInterceptor(paramsInterceptor);
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
//        }
        return retrofit;
    }

    /**
     * 对网络接口返回的Response进行分割操作
     */
    private <T extends BaseResponse> Observable<T> flatResponse(final T response) {
        return Observable.create(
                new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> observableEmitter) throws Exception {
                        if (response.isSuccess()) {
                            observableEmitter.onNext(response);
                        } else {
                            if (!observableEmitter.isDisposed()) {
                                try {
                                    observableEmitter.onError(new APIException(308, response.getMsg()));
                                } catch (CompositeException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                        }
                        if (!observableEmitter.isDisposed()) {
                            observableEmitter.onComplete();
                        }
                    }
                }
        );
    }


    <T extends BaseResponse> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::flatResponse);
    }

    /**
     * 当APIService中接口的注解为{@link retrofit2.http.Multipart}时，参数为{@link RequestBody}
     * 生成对应的RequestBody
     */
    @SuppressWarnings("unused")
    private RequestBody createRequestBody(int param) {
        return RequestBody.create(MediaType.parse("text/plain"), String.valueOf(param));
    }

    @SuppressWarnings("unused")
    private RequestBody createRequestBody(long param) {
        return RequestBody.create(MediaType.parse("text/plain"), String.valueOf(param));
    }

    @SuppressWarnings("unused")
    private RequestBody createRequestBody(String param) {
        return RequestBody.create(MediaType.parse("text/plain"), param);
    }

    @SuppressWarnings("unused")
    private RequestBody createRequestBody(File param) {
        return RequestBody.create(MediaType.parse("image/*"), param);
    }

    @SuppressWarnings("unused")
    public void setHttpCallback(HttpCallback httpCallback) {
        mHttpCallback = httpCallback;
    }

    ///**
    // * 已二进制传递图片文件，对图片文件进行了压缩
    // *
    // * @param path 文件路径
    // * @return
    // */
    //private RequestBody createPictureRequestBody(String path) {
    //    Bitmap bitmap = ClippingPictureUtils.decodeResizeBitmapSd(path, 400, 800);
    //    return RequestBody.create(MediaType.parse("image/*"), ClippingPictureUtils.bitmapToBytes(bitmap));
    //}

    public static class APIException extends Exception {

        public static final String NET_ERROR = "网络错误";

        public int code;
        private String message;

        public APIException(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public RetrofitManagement() {
        SharedPreferences preferences = FacePayApplication.INSTANCE.getSharedPreferences("temp_file", Context.MODE_PRIVATE);
        baseUrl = preferences.getString("sp_setting_base_host", DEFAULT_BASE_HOST);
    }

    private static class RetrofitUtilInnerClass {
        private static final RetrofitManagement INSTANCES = new RetrofitManagement();
    }

}
