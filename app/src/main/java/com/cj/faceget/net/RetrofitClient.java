package com.cj.faceget.net;

import com.cj.faceget.model.ResponseBean;
import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author: yangshuiqiang
 * Time:2017/11/20 16:42
 */

public class RetrofitClient {

    private volatile static RetrofitClient mRetrofitClient;

    private static final long TIME_OUT = 1000 * 60;

    private ApiServer mApiServer;

    private RetrofitClient() {
        //用Chrome 调试
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();

        mApiServer = new retrofit2.Retrofit.Builder()
                .baseUrl("http://192.168.100.75:5800")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build().create(ApiServer.class);
    }

    public static RetrofitClient get() {
        if (mRetrofitClient == null) {
            synchronized (RetrofitClient.class) {
                if (mRetrofitClient == null) {
                    mRetrofitClient = new RetrofitClient();
                }
            }
        }
        return mRetrofitClient;
    }

    public static void init() {
        RetrofitClient.get();
    }

    public Flowable<ResponseBean> getId() {
        return mApiServer.getId().subscribeOn(Schedulers.io());
    }

    public Flowable<Boolean> updateImage(final String id, final File[] files) {
        return Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull FlowableEmitter<Boolean> e) throws Exception {
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                for (File file : files) {
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), file);
                    RequestBody multiBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), fileBody)
                            .addFormDataPart("id", id).build();
                    Request request = new Request.Builder()
                            .url("http://192.168.100.75:5800/upload")
                            .post(multiBody).build();
                    ResponseBody body = client.newCall(request).execute().body();
                    if (body != null) {
                        ResponseBean responseBean = new Gson().fromJson(body.string(), ResponseBean.class);
                        if (!responseBean.isResult()) {
                            throw new Exception(responseBean.getMsg());
                        }
                    }
                }
                e.onNext(true);
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io());
    }

    public Flowable<ResponseBean> updateVideo(String id, File file) {
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("audio/mp4"), file);
        MultipartBody.Part f = MultipartBody.Part.createFormData("file", file.getName(), photoRequestBody);
        RequestBody idBody = RequestBody.create(MediaType.parse("multipart/form-data"), id);//用户的id
        return mApiServer.updateVideo(idBody, f).subscribeOn(Schedulers.io());
    }

    public Flowable<ResponseBean> newMsg(String id, String name, String department) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("name", name);
        hashMap.put("department", department);
        return mApiServer.newMsg(hashMap).subscribeOn(Schedulers.io());
    }
}