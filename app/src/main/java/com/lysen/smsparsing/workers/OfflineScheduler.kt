package com.lysen.smsparsing.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lysen.smsparsing.App
import com.lysen.smsparsing.api.ApiSender
import com.lysen.smsparsing.enums.NetState
import com.lysen.smsparsing.enums.ReportKind
import java.io.File
import java.util.Date


class OfflineScheduler(private val context: Context) {

    val alarmManager: AlarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val SCHEDULER_INTERVAL = 60_000

    fun initScheduler(needTosend: Boolean) {
        if (App.getToken()?.isEmpty() == true) return
        if (checkNetwork() == NetState.WIFI_CELLULAR || checkNetwork() == NetState.ERROR || checkNetwork() == NetState.OFFLINE) return
        sendServiceIsAlive(needTosend)
        checkVersion()
        println("53ss   -------------------------------------------------------------------------------------------------")
        val intent = Intent(context, SMS_Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 111, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SCHEDULER_INTERVAL, pendingIntent)
    }

    private fun checkVersion() {
        val date = Date().time / 1000
        println("1122ss  checkVersion  sec = " + date)
        println("1122ss  checkVersion  hours = " + (date / 3600) % 10)
        if ((date / 3600) % 10 != 0L) return
        val storageRef = Firebase.storage.reference.child("files")
        val codeVersion = App.context?.packageManager?.getPackageInfo(App.context?.packageName ?: "", PackageManager.GET_ACTIVITIES)?.versionCode ?: 0
        println("1122ss  storageRef = " + storageRef)
        storageRef.listAll().addOnSuccessListener { resultList ->
            resultList.items.forEach { storageRef ->
                val fileName = storageRef.name
                val fileVersion = fileName.replace("\\D".toRegex(), "").toInt()
                println("1122ss fileName = $fileName  codeVersion = $codeVersion fileVersion = $fileVersion")
                if (codeVersion != 0 && codeVersion < fileVersion) {
                    val file = File(App.context?.getExternalFilesDir("download"), fileName)
                    val fileURI = FileProvider.getUriForFile(context, App.context?.applicationContext?.packageName + ".provider", file)
//                if (!file.exists()) file.mkdir()
//                file.createNewFile()
////                println("1122ss  file = " + file.get)
//                println("1122ss  file exists() = " + file.exists())
//                println("1122ss  file = " + file.path)
//                println("1122ss  fileURI = " + fileURI)
//                println("1122ss  fileURI = " + fileURI.path)
//                install(file,fileURI)
                    storageRef.getFile(file).addOnSuccessListener {
                        println("1122ss  success bytesTransferred = " + it.bytesTransferred)
                        install(file, fileURI)
                    }.addOnFailureListener {
                        println("1122ss  filure = " + it.message)
                    }.addOnProgressListener {
                        println("1122ss  metadata = " + it.bytesTransferred)
                    }
                }
            }
        }
    }

    private fun install(downloadedAPK: File, uri: Uri) {
        val installApplicationIntent = Intent(Intent.ACTION_VIEW)
        downloadedAPK.setReadable(true)
        installApplicationIntent.setDataAndType(uri, "application/vnd.android.package-archive")
        installApplicationIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        installApplicationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        installApplicationIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(installApplicationIntent)
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
        val network = connMgr.activeNetwork ?: println("54ss ! WIFI + ! CELLULAR  ").also { App.netState.value = NetState.OFFLINE }
            .also { return NetState.OFFLINE }
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

