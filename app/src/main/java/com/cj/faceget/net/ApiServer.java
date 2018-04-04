package com.cj.faceget.net;


import com.cj.faceget.model.ResponseBean;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @author: yangshuiqiang
 * Time:2017/11/20 16:43
 */

interface ApiServer {

    @POST("/id")
    Flowable<ResponseBean> getId();


    @Multipart
    @POST("/upload")
    Flowable<ResponseBean> updateVideo(@Part("id") RequestBody id,
                                       @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/newmsg")
    Flowable<ResponseBean> newMsg(@FieldMap Map<String, Object> body);

}