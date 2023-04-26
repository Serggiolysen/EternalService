package com.lysen.smsparsing.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lysen.smsparsing.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SyncWorker(private val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = coroutineScope {
        launch(Dispatchers.Main) {
            println("53ss   DataSyncWorker doWork2")
            if (App.offlineScheduler == null) {
                println("250ss   DataSyncWorker doWork 1")
                val offlineScheduler = OfflineScheduler(context)
                App.offlineScheduler = offlineScheduler
                offlineScheduler.initScheduler()
            } else {
                println("250ss   DataSyncWorker doWork 2")
                App.offlineScheduler!!.initScheduler()
            }
        }
        Result.success()
    }
}