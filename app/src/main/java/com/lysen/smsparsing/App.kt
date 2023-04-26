package com.lysen.smsparsing

import android.app.Application
import com.lysen.smsparsing.workers.OfflineScheduler

class App: Application() {

    companion object{
        var offlineScheduler: OfflineScheduler? = null
        var mainActivityResumed = false
    }




}