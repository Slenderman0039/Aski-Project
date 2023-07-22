package it.am.gpsmodule.utils

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface UserAPI {

    @POST("postSelect/")
    @FormUrlEncoded
    fun querySelect(@Field("query") query: String): Call <JsonObject>
    @POST("postInsert/")
    @FormUrlEncoded
    fun queryInsert(@Field("query") query: String): Call <JsonObject>
    @POST("postRemove/")
    @FormUrlEncoded
    fun queryDelete(@Field("query") query: String): Call <JsonObject>
    @POST("postUpdate/")
    @FormUrlEncoded
    fun queryUpdate(@Field("query") query: String): Call <JsonObject>

}