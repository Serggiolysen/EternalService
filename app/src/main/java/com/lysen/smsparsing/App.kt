package com.lysen.smsparsing

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.telephony.TelephonyManager
import com.lysen.smsparsing.enums.NetState
import com.lysen.smsparsing.workers.OfflineScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class App : Application() {

    companion object {
        var offlineScheduler: OfflineScheduler? = null
        var context: Context? = null
        var mainActivityResumed = false
        val netState = MutableStateFlow(NetState.ERROR)
        lateinit var sharedPrefs:SharedPreferences
        fun saveToken(token:String) = sharedPrefs.edit().putString("token", token).apply()
        fun getToken() = sharedPrefs.getString("token", "")
        fun saveImei1(imei:String) = sharedPrefs.edit().putString("imei1", imei).apply()
        fun getImei1() = sharedPrefs.getString("imei1", "")
        fun saveImei2(imei:String) = sharedPrefs.edit().putString("imei2", imei).apply()
        fun getImei2() = sharedPrefs.getString("imei2", "")
        fun saveImei3(imei:String) = sharedPrefs.edit().putString("imei3", imei).apply()
        fun getImei3() = sharedPrefs.getString("imei3", "")
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        context = this
        sharedPrefs = getSharedPreferences("AppConstants", MODE_PRIVATE)
    }



}