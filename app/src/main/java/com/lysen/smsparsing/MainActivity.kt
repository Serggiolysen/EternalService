package com.lysen.smsparsing

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lysen.smsparsing.models.SmsObject
import com.lysen.smsparsing.models.TelegamAnswer
import com.lysen.smsparsing.utils.AutoStartHelper
import com.lysen.smsparsing.workers.SMS_Receiver
import com.lysen.smsparsing.workers.SyncWorker
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        val REQUEST_CODE = 123222
        val REQUEST_CODE2 = 12322234
        val SMS_OBJECTS_LIST = "SMS_OBJECTS_LIST"
        val SMS_OBJECTS_NAME = "SMS_OBJECTS_NAME"
        val CHAT_ID = "-934185155"
//        val CHAT_ID = "6036255168"
    }

    private var recycler: RecyclerView? = null
    private var adapter: SmsAdapter? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val SMS_EXTRA_TEL = intent.getStringExtra(SMS_Receiver.SMS_EXTRA_TEL)
            val SMS_EXTRA_MESS = intent.getStringExtra(SMS_Receiver.SMS_EXTRA_MESS)
            val ACTION_TYPE = intent.getStringExtra(SMS_Receiver.ACTION_TYPE)

            context?.let {
                adapter?.updateAdapter(tel = SMS_EXTRA_TEL, mess = SMS_EXTRA_MESS, context = it, recyclerView = recycler)
            }

            val joda = DateTime(Date())
            val date = "${joda.toString("dd")}-${joda.toString("MMM")}-${joda.toString("YYYY")} " +
                    "${joda.toString("HH")}:${joda.toString("mm")}:${joda.toString("ss")}  "


            println("53ss ACTION_TYPE =  " + ACTION_TYPE)


            val vendor = android.os.Build.MODEL
            if (ACTION_TYPE == Intent.ACTION_BOOT_COMPLETED) {
                TelegramBotApi.service.getSend(chat_id = CHAT_ID, text = "DEVICE IS REBOOTED:  ${date}\nDevice:  ${vendor}")?.enqueue(object : Callback<TelegamAnswer> {
                    override fun onResponse(call: Call<TelegamAnswer>, response: Response<TelegamAnswer>) {
                        println("53ss onResponse  response.body() =  " + response.body())
                        println("53ss onResponse  response.code() =  " + response.code())
                    }

                    override fun onFailure(call: Call<TelegamAnswer>, throwable: Throwable) {
                        println("53ss onFailure  throwable =  " + throwable.message)
                    }
                })
            }


            if (SMS_EXTRA_TEL == null || SMS_EXTRA_MESS == null) return

            TelegramBotApi.service.getSend(chat_id = CHAT_ID, text = "Date:  ${date}\nTel:  ${SMS_EXTRA_TEL}\nMessage:  ${SMS_EXTRA_MESS}")?.enqueue(object : Callback<TelegamAnswer> {
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

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val joda = DateTime(Date())
            val date = "${joda.toString("dd")}-${joda.toString("MMM")}-${joda.toString("YYYY")} " +
                    "${joda.toString("HH")}:${joda.toString("mm")}:${joda.toString("ss")}  "
            val vendor = android.os.Build.MODEL

            println("53ss batteryReceiver = " + intent.action)
            TelegramBotApi.service.getSend(chat_id = CHAT_ID, text = "BATTERY LOW:  ${date}\nDevice:  ${vendor}")?.enqueue(object : Callback<TelegamAnswer> {
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

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val joda = DateTime(Date())
            val date = "${joda.toString("dd")}-${joda.toString("MMM")}-${joda.toString("YYYY")} " +
                    "${joda.toString("HH")}:${joda.toString("mm")}:${joda.toString("ss")}  "
            val vendor = android.os.Build.MODEL

            println("53ss DISCONNECTED = " + intent.action)
            TelegramBotApi.service.getSend(chat_id = CHAT_ID, text = "CHARGER DISCONNECTED:  ${date}\nDevice:  ${vendor}")?.enqueue(object : Callback<TelegamAnswer> {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
    }


    private fun requestPermissions() {
        AutoStartHelper.instance.getAutoStartPermission(this)
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                ), REQUEST_CODE
            )
            println("53ss   requestPermissions  1")
        } else {
            println("53ss   requestPermissions  2")
            initWork()
        }
    }

    private fun initWork() {
        val work = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .addTag("WorkManager")
//            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueue(work)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                startActivityForResult(intent, REQUEST_CODE2)
            } else {
                initWork()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Settings.canDrawOverlays(this)) {
            initWork()
        }
    }


    class SmsAdapter : RecyclerView.Adapter<SmsViewHolder>() {
        val itemsList = mutableListOf<SmsObject>()

        fun updateAdapter(tel: String? = null, mess: String? = null, context: Context, recyclerView: RecyclerView?) {
            itemsList.clear()
            val smsObject = if (tel != null && mess != null) SmsObject(tel, mess) else null
            val lisSmsObjects =
                Gson().fromJson<MutableList<SmsObject>>(
                    context.getSharedPreferences(SMS_OBJECTS_NAME, MODE_PRIVATE).getString(SMS_OBJECTS_LIST, null),
                    object : TypeToken<MutableList<SmsObject>>() {}.type
                );
            val dateNow = Date().time
            val diff = 1000 * 60 * 15

            if (lisSmsObjects == null || lisSmsObjects.isEmpty()) {
                smsObject?.let { itemsList.add(it) }
            } else {
                lisSmsObjects.forEachIndexed { index, smsObject ->
                    println("53ss lisSmsObjects it = " + smsObject)
                    if ((dateNow - smsObject.date.time) < diff) {
                        itemsList.add(smsObject)
                    }
                }
                smsObject?.let { itemsList.add(it) }
            }

            context.getSharedPreferences(SMS_OBJECTS_NAME, MODE_PRIVATE).edit().putString(SMS_OBJECTS_LIST, Gson().toJson(itemsList)).apply()
            if (recyclerView == null) return
            notifyDataSetChanged()
            recyclerView?.scrollToPosition(itemCount - 1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
            return SmsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_adapter, parent, false))
        }

        override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
            holder.bind(itemsList[position], itemsList.size - position)
        }

        override fun getItemCount(): Int = itemsList.size
    }

    class SmsViewHolder(itemView: View) : ViewHolder(itemView) {
        val num = itemView.findViewById<AppCompatTextView>(R.id.num)
        val date = itemView.findViewById<AppCompatTextView>(R.id.date)
        val tel = itemView.findViewById<AppCompatTextView>(R.id.tel)
        val mess = itemView.findViewById<AppCompatTextView>(R.id.mess)
        fun bind(smsObject: SmsObject, i: Int) {
            this.num.text = i.toString()
            val joda = DateTime(smsObject.date)
            this.date.text = "Date: ${joda.toString("dd")}-${joda.toString("MMM")}-${joda.toString("YYYY")} " +
                    "${joda.toString("HH")}:${joda.toString("mm")}:${joda.toString("ss")}  "
            this.tel.text = "Tel: " + smsObject.tel
            this.mess.text = "Message: " + smsObject.mess
        }
    }



    override fun onResume() {
        super.onResume()
        val smsFilter = IntentFilter(SMS_Receiver.SMS_ACTION_NAME)
        registerReceiver(receiver, smsFilter)
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_LOW)
        registerReceiver(batteryReceiver, batteryFilter)
        val powerFilter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerReceiver, powerFilter)
        recycler = findViewById(R.id.recycler)
        recycler?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        adapter = SmsAdapter()
        recycler?.adapter = adapter
        adapter?.updateAdapter(context = this, recyclerView = recycler)
        App.mainActivityResumed = true
    }

    override fun onDestroy() {
        super.onDestroy()
        App.mainActivityResumed = false
    }


}