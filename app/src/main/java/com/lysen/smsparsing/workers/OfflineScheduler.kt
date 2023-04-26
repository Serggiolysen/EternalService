package com.lysen.smsparsing.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.lysen.smsparsing.MainActivity
import com.lysen.smsparsing.TelegramBotApi
import com.lysen.smsparsing.models.TelegamAnswer
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class OfflineScheduler(private val context: Context) {

    fun initScheduler() {
        sendServiceIsAlive()
        println("53ss   -------------------------------------------------------------------------------------------------")
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SMS_Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 111, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60_000, pendingIntent)
    }

    fun sendServiceIsAlive() {
        val joda = DateTime(Date())
        val date = "${joda.toString("dd")}-${joda.toString("MMM")}-${joda.toString("YYYY")} " +
                "${joda.toString("HH")}:${joda.toString("mm")}:${joda.toString("ss")}  "
        val vendor = android.os.Build.MODEL
        TelegramBotApi.service.getSend(chat_id = MainActivity.CHAT_ID, text = "Service is alive:  ${date}\nDevice:  ${vendor}")?.enqueue(object : Callback<TelegamAnswer> {
            override fun onResponse(call: Call<TelegamAnswer>, response: Response<TelegamAnswer>) {
                println("53ss onResponse  response.body() =  " + response.body())
                println("53ss onResponse  response.code() =  " + response.code())
            }

            override fun onFailure(call: Call<TelegamAnswer>, throwable: Throwable) {
                println("53ss onFailure  throwable =  " + throwable.message)
            }
        })
    }

}