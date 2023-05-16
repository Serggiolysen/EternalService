package com.lysen.smsparsing

import com.lysen.smsparsing.models.AnswerServer
import com.lysen.smsparsing.models.DeviceAndSMS
import com.lysen.smsparsing.models.Errors
import com.lysen.smsparsing.models.TelegamAnswer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ServerApi {

    @Headers("Content-Type: application/json")
    @POST(".")
    fun send(@Header("Authorization") bearer: String, @Body data: DeviceAndSMS): Call<Void>?

    @Headers("Content-Type: application/json")
    @POST(".")
    fun sendEmpyCheck(@Header("Authorization") bearer: String, @Body data: DeviceAndSMS): Call<AnswerServer>?

    companion object {

        private val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (message.isEmpty()) return
                println("56ss ServerApi HttpLoggingInterceptor message = " + message)
            }
        }).also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }
        private val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://providerservice.digitalpostpaid.com/api/external/app/sms_webhook/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var service: ServerApi = retrofit.create(ServerApi::class.java)

    }
}