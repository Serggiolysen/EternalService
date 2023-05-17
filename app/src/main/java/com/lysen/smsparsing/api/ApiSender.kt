package com.lysen.smsparsing.api

import com.lysen.smsparsing.App
import com.lysen.smsparsing.ServerApi
import com.lysen.smsparsing.TelegramBotApi
import com.lysen.smsparsing.enums.ReportKind
import com.lysen.smsparsing.models.*
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object ApiSender {

    interface ApiCallback {
        fun onApiSuccess()
        fun onApiError()
    }

    val vendor = (android.os.Build.BRAND + " " + android.os.Build.DEVICE + " (" + android.os.Build.MODEL + ")") ?: ""
    val CHAT_ID = "-885907368"


    fun send(
        reportKind: ReportKind, sms_extra_tel: String = "", sms_extra_mess: String = "", sms_extra_date: String = "", apiCallback: ApiCallback = object : ApiCallback {
            override fun onApiSuccess() {}
            override fun onApiError() {}
        }
    ) {
        val date = DateTime(Date()).toString("YY-MM-dd HH:m:ss.SSS")
        val reportText = when (reportKind) {
            ReportKind.ALIVE -> "Service is alive:  ${date}\nDevice:  $vendor"
            ReportKind.SMS -> "Date:  ${date}\nTel:  ${sms_extra_tel}\nMessage:  ${sms_extra_mess}"
            ReportKind.REBOOTED -> "DEVICE IS REBOOTED:  ${date}\nDevice:  $vendor"
            ReportKind.BATTERY_LOW -> "BATTERY LOW:  ${date}\nDevice:  $vendor"
            ReportKind.CHARGER_DISCONNECTED -> "CHARGER DISCONNECTED:  ${date}\nDevice:  $vendor"
            ReportKind.WIFI_LOW -> "WIFI LOW LEVEL:  ${date}\nDevice:  $vendor"
        }

//        Toast.makeText(App.context, reportText, Toast.LENGTH_SHORT).show()

        if (reportKind == ReportKind.SMS)
            sendToApi(sms_extra_mess = sms_extra_mess, sms_extra_tel = sms_extra_tel, date = date, sms_extra_date = sms_extra_date, apiCallback = apiCallback)

        TelegramBotApi.service.send(chat_id = CHAT_ID, text = reportText)?.enqueue(object : Callback<TelegamAnswer> {
            override fun onResponse(call: Call<TelegamAnswer>, response: Response<TelegamAnswer>) {
                println("53ss onResponse  response.body() 1=  " + response.body())
                println("53ss onResponse  response.code() 1=  " + response.code())
            }

            override fun onFailure(call: Call<TelegamAnswer>, throwable: Throwable) {
                println("53ss onFailure  throwable 1=  " + throwable.message)

            }
        })

    }

    fun sendToApi(sms_extra_mess: String = "test", sms_extra_tel: String = "test", date: String = "test", sms_extra_date: String = "test", apiCallback: ApiCallback) {
        val deviceAndSMS = DeviceAndSMS(Device(model = vendor), Sms(body = sms_extra_mess, from = sms_extra_tel, received = date, sent = sms_extra_date))
        val testToken = "d00faf0e989fa356db16"
        ServerApi.service.send(bearer = "Bearer ${App.getToken()}", data = deviceAndSMS)?.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                println("56ss onResponse ServerApi response.code() =  " + response.code())
                if (199 < response.code() && response.code() < 299) {
                    apiCallback.onApiSuccess()
                }
                if (399 < response.code() && response.code() < 499) {
                    apiCallback.onApiError()
                }
            }

            override fun onFailure(call: Call<Void>, throwable: Throwable) {
                println("56ss onFailure ServerApi throwable =  " + throwable.message)
            }
        })
    }
}