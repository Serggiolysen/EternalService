//package com.lysen.smsparsing.workers
//
//import android.content.BroadcastReceiver
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import com.lysen.smsparsing.App.Companion.offlineScheduler
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//
//class WorkerReceiver : BroadcastReceiver() {
//    val CHANEL_ID = "MAIN_CHANEL_ID_654"
//    override fun onReceive(context: Context?, intent: Intent?) {
//        println("53ss   WorkerReceiver intent action = " + intent?.action)
//        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
//            println("53ss   WorkerReceiver intent action 2222 = " + intent?.action)
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            if (offlineScheduler == null) {
//                println("53ss   NotificationReceiver onReceive   offlineScheduler==null")
//                val newIntent = Intent()
//                if (context == null) return@launch
//                println("53ss   NotificationReceiver onReceive   offlineScheduler==null  2")
//                newIntent.component = ComponentName(context.packageName, "${context.packageName}.MainActivity")
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                println("53ss   NotificationReceiver onReceive   offlineScheduler==null  3 context = " + context)
//                delay(2 * 1000)
//                context.startActivity(newIntent)
//
////                while (offlineScheduler == null){
////                    Intent(context, ForegroundService::class.java).also { intent ->
////                        context.startForegroundService(intent)
////                    }
////                    delay(1000)
////                }
//
////
////                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////
////                val syncChanel = NotificationChannel(CHANEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
////                notificationManager.createNotificationChannel(syncChanel)
////
////                val fullScreenIntent = Intent(context, MainActivity::class.java)
////                val fullScreenPendingIntent = PendingIntent.getActivity(
////                    context, 0, fullScreenIntent, PendingIntent.FLAG_MUTABLE
////                )
////
////                val notificationBuilder =
////                    NotificationCompat.Builder(context, CHANEL_ID)
//////                        .setSmallIcon(R.drawable.notification_icon)
////                        .setContentTitle("Incoming ")
////                        .setContentText("555-1234")
////                        .setPriority(NotificationCompat.PRIORITY_HIGH)
////                        .setCategory(NotificationCompat.CATEGORY_CALL)
////
////                        // Use a full-screen intent only for the highest-priority alerts where you
////                        // have an associated activity that you would like to launch after the user
////                        // interacts with the notification. Also, if your app targets Android 10
////                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
////                        // order for the platform to invoke this notification.
////                        .setFullScreenIntent(fullScreenPendingIntent, true)
////
////                val incomingCallNotification = notificationBuilder.build()
////
////                notificationManager.notify(223433456, incomingCallNotification)
//            } else {
//                offlineScheduler!!.initScheduler()
//                println("53ss   WorkerReceiver ")
//            }
//        }
//
//
//
//    }
//}