package com.lysen.smsparsing.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.lysen.smsparsing.api.ApiSender
import com.lysen.smsparsing.App
import com.lysen.smsparsing.enums.NetState
import com.lysen.smsparsing.enums.ReportKind



class OfflineScheduler(private val context: Context) {

    val alarmManager: AlarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val SCHEDULER_INTERVAL = 60_000

    fun initScheduler(needTosend: Boolean) {
        if (checkNetwork() == NetState.WIFI_CELLULAR || checkNetwork() == NetState.ERROR || checkNetwork() == NetState.OFFLINE ) return
        sendServiceIsAlive(needTosend)
        println("53ss   -------------------------------------------------------------------------------------------------")
        val intent = Intent(context, SMS_Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 111, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SCHEDULER_INTERVAL, pendingIntent)
    }

    private fun sendServiceIsAlive(needTosend: Boolean = true) {
        if (!needTosend) return
        ApiSender.send(reportKind = ReportKind.ALIVE)
    }

    fun checkNetwork(): NetState {
        if (connMgr == null) {
//            println("54ss ERROR  ")
            App.netState.value = NetState.ERROR
            return NetState.ERROR
        }
        val network = connMgr.activeNetwork ?: println("54ss ! WIFI + ! CELLULAR  ").also { App.netState.value = NetState.OFFLINE }.also { return NetState.OFFLINE }
        val capabilities = connMgr.getNetworkCapabilities(network as Network) ?: return NetState.ERROR
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//            println("54ss WIFI +  CELLULAR  ")
            App.netState.value = NetState.WIFI_CELLULAR
            return NetState.WIFI_CELLULAR
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//            println("54ss WIFI +  ! CELLULAR  ")
            App.netState.value = NetState.WIFI
            return NetState.WIFI
        } else if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//            println("54ss ! WIFI +  CELLULAR  ")
            App.netState.value = NetState.CELLULAR
            return NetState.CELLULAR
        }
        val bandwidth = capabilities.linkDownstreamBandwidthKbps
        if (bandwidth < 500) {
//            println("54ss WIFI_LOW  ")
            ApiSender.send(reportKind = ReportKind.WIFI_LOW)
            App.netState.value = NetState.WIFI_LOW
            return NetState.WIFI_LOW
        }
        return NetState.ERROR
    }

}

