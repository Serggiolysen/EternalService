package com.lysen.smsparsing

import com.lysen.smsparsing.models.TelegamAnswer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TelegramBotApi {

    @GET("sendMessage")
    fun getSend(@Query("chat_id") chat_id: String?, @Query("text") text: String?): Call<TelegamAnswer>?

    companion object {
        val BOT_TOKEN = "5865697033:AAH3JWbC9I0dpA9G8FPuWdVQeyt5LSeZEHk"
//        val BOT_TOKEN = "6036255168:AAFEJY3KAPyebEd-woJTK3pv_sPFWEUzzhs"

        private val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (message.isEmpty() ) return
                println("53ss HttpLoggingInterceptor message = " + message)
            }
        }).also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }

        private val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.telegram.org/bot$BOT_TOKEN/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var service: TelegramBotApi = retrofit.create(TelegramBotApi::class.java)
    }
}