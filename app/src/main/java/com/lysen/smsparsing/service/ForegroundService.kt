//package com.lysen.smsparsing
//
//import android.app.*
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.security.AccessController.getContext
//
//
//
//class ForegroundService : Service() {
//    private val CHANEL_ID = "MAIN_CHANEL_ID_654"
//    private val REQUEST_CODE = 110
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        showNotification()
//        return START_STICKY
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        CoroutineScope(Dispatchers.Main).launch {
//            while (true) {
//                delay(3 * 1000)
//                println("53ss   NForegroundService   offlineScheduler  111 App.offlineScheduler = " + App.offlineScheduler)
//                if (App.offlineScheduler == null) {
//                    println("53ss   NForegroundService   offlineScheduler==null")
//                    val newIntent = Intent()
//                    newIntent.component = ComponentName(packageName, "${packageName}.MainActivity")
//                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(newIntent)
//                }
//            }
//        }
//    }
//
//    private fun showNotification() {
//        println("53ss   isServiceRunning()  ====== "+isServiceRunning())
//        if (isServiceRunning() && App.offlineScheduler == null) {
//            println("53ss   NForegroundService   offlineScheduler==null")
//            val newIntent = Intent()
//            newIntent.component = ComponentName(packageName, "${packageName}.MainActivity")
//            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(newIntent)
//        } else if (isServiceRunning()){
//            println("53ss ForegroundService  showNotification")
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val syncChanel = NotificationChannel(CHANEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(syncChanel)
//            val fullScreenIntent = Intent(this, MainActivity::class.java)
//            val fullScreenPendingIntent = PendingIntent.getActivity(
//                this, REQUEST_CODE, fullScreenIntent, PendingIntent.FLAG_MUTABLE
//            )
//            val notification =
//                NotificationCompat.Builder(this, CHANEL_ID)
////                        .setSmallIcon(R.drawable.notification_icon)
//                    .setContentTitle("Incoming ")
//                    .setContentText("555-1234")
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                    .setFullScreenIntent(fullScreenPendingIntent, true)
//                    .build()
//            startForeground(REQUEST_CODE, notification)
//        }
//    }
//
//    private fun isServiceRunning(serviceClassName: String? = "ForegroundService"): Boolean {
//        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        val services: List<ActivityManager.RunningServiceInfo> = activityManager.getRunningServices(Int.MAX_VALUE)
//        for (runningServiceInfo in services) {
//            if (runningServiceInfo.service.className == serviceClassName) {
//                return true
//            }
//        }
//        return false
//    }
//}