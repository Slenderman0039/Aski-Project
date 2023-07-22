package it.am.gpsmodule.utils.fcm

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key="
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification: FCMNotification): Call<ResponseBody>

    companion object {
        fun create(): FCMService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(FCMService::class.java)
        }
    }
}
data class FCMNotification(
    @SerializedName("to") val to: String,
    @SerializedName("notification") val notification: FCMNotificationData,
    @SerializedName("data") val data: Map<String, String>
)

data class FCMNotificationData(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)