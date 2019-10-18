package com.ocom.hanmafacepay.network;

import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.*;
import okio.Buffer;

import java.io.IOException;

public class ParamsInterceptor implements Interceptor {
    private static final String TAG = "RetrofitManagement";
    private final MediaType MediaType_Json = MediaType.parse("application/json");

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request orgRequest = chain.request();
        RequestBody body = orgRequest.body();
        //收集请求参数，方便调试
        if (body != null) {
            RequestBody newBody = body;
            if (body instanceof FormBody) {
                newBody = addParamsToFormBody((FormBody) body);
//                Log.d("ParamsInterceptor","Form Body: "+ body.toString());
            } else if (body instanceof MultipartBody) {
//                Log.d("ParamsInterceptor","Multipart Body: "+ body.toString());
                newBody = addParamsToMultipartBody((MultipartBody) body);
            } else {
                String decodeContent = decodeContent(orgRequest);
//                Log.d("ParamsInterceptor","DecodeContent: "+ decodeContent);
                if (decodeContent.length() == 0) {
                    //newBody = RequestBody.create(MediaType_Json, decodeContent);
                    //} else {
                    //newBody = body;
                    newBody = addParamsToFormBody();
                }
            }

            Request newRequest = orgRequest.newBuilder()
                    .url(orgRequest.url())
                    //.header("app", "android")
                    .method(orgRequest.method(), newBody)
                    .build();

            return chain.proceed(newRequest);
        }
        return chain.proceed(orgRequest);
    }

    private RequestBody addParamsToFormBody() {
        FormBody.Builder builder = new FormBody.Builder();
        //添加id，city参数
//        if (CommonAppCache.get().getToken() != null) {
//            builder.add("token", CommonAppCache.get().getToken());
//        }
        return builder.build();
    }

    private MultipartBody addParamsToMultipartBody(MultipartBody body) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
//        if (CommonAppCache.get().getToken() != null) {
//            builder.addFormDataPart("token", CommonAppCache.get().getToken());
//        }
//        builder.addFormDataPart("device_no",ConstantValueKt)
        //添加原请求体
        for (int i = 0; i < body.size(); i++) {
            builder.addPart(body.part(i));
        }

        return builder.build();
    }

    /**
     * 为FormBody类型请求体添加参数
     *
     * @param body
     * @return
     */
    private FormBody addParamsToFormBody(FormBody body) {
        FormBody.Builder builder = new FormBody.Builder();

        //添加id，city参数
//        if (CommonAppCache.get().getToken() != null) {
//            builder.add("token", CommonAppCache.get().getToken());
//        }

        //添加原请求体
        for (int i = 0; i < body.size(); i++) {
            builder.addEncoded(body.encodedName(i), body.encodedValue(i));
        }

        return builder.build();
    }

    @NonNull
    private String decodeContent(Request orgRequest) {
        String content = StringDecoder.decodeString(bodyToString(orgRequest)).toString();
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start != -1 && end != -1) {
            content = content.substring(start, end + 1);
        }
        return content;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "";
        }
    }

}
