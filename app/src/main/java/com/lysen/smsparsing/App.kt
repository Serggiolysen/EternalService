package com.lysen.smsparsing

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
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
        var secureId = ""
        val smsStaus = MutableStateFlow(true)

    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        context = this
        secureId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    }



}