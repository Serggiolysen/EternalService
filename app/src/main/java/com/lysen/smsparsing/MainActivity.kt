package com.lysen.smsparsing

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lysen.smsparsing.api.ApiSender
import com.lysen.smsparsing.enums.NetState
import com.lysen.smsparsing.enums.ReportKind
import com.lysen.smsparsing.models.SmsObject
import com.lysen.smsparsing.utils.AutoStartHelper
import com.lysen.smsparsing.workers.SMS_Receiver
import com.lysen.smsparsing.workers.SyncWorker
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ApiSender.ApiCallback, TokenSendContract {

    companion object {
        val REQUEST_CODE = 123222
        val REQUEST_CODE2 = 12322234
        val SMS_OBJECTS_LIST = "SMS_OBJECTS_LIST"
        val SMS_OBJECTS_NAME = "SMS_OBJECTS_NAME"
    }

    private var recycler: RecyclerView? = null
    private var adapter: SmsAdapter? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var SMS_EXTRA_TEL = ""
    private var SMS_EXTRA_DATE = ""
    private var SMS_EXTRA_MESS = ""
    private var ACTION_TYPE = ""
    private var SMS_SLOT = "-1"
    lateinit var iv_wifi: AppCompatImageView
    lateinit var tv_version: AppCompatTextView
    lateinit var tv_sim1: AppCompatTextView
    lateinit var iv_danger_sim1: AppCompatImageView
    lateinit var tv_sim2: AppCompatTextView
    lateinit var iv_danger_sim2: AppCompatImageView
    lateinit var tv_sim3: AppCompatTextView
    lateinit var iv_danger_sim3: AppCompatImageView
    lateinit var simViewsList: List<AppCompatTextView>
    lateinit var simImagesList: List<AppCompatImageView>

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            ACTION_TYPE = intent.getStringExtra(SMS_Receiver.ACTION_TYPE) ?: ""
            SMS_EXTRA_TEL = intent.getStringExtra(SMS_Receiver.SMS_EXTRA_TEL) ?: ""
            SMS_EXTRA_DATE = intent.getStringExtra(SMS_Receiver.SMS_EXTRA_DATE) ?: ""
            SMS_EXTRA_MESS = intent.getStringExtra(SMS_Receiver.SMS_EXTRA_MESS) ?: ""
            SMS_SLOT = intent.getStringExtra(SMS_Receiver.SMS_SLOT) ?: ""


            println("54ss receiver updateAdapter smsSlot  =  " + SMS_SLOT)

            if (ACTION_TYPE == Intent.ACTION_BOOT_COMPLETED) ApiSender.send(reportKind = ReportKind.REBOOTED)
            if (SMS_SLOT == SMS_Receiver.ACTION_SIM_STATE_CHANGED) getSimInfo()

            if (SMS_EXTRA_TEL.isEmpty() || SMS_EXTRA_MESS.isEmpty()) return

            ApiSender.send(reportKind = ReportKind.SMS, sms_extra_tel = SMS_EXTRA_TEL,
                sms_extra_mess = SMS_EXTRA_MESS, sms_extra_date = SMS_EXTRA_DATE, apiCallback = this@MainActivity)
        }
    }

    override fun onApiSuccess() {
        println("56ss onSuccess() =  ")
        if (SMS_EXTRA_TEL.isEmpty() || SMS_EXTRA_MESS.isEmpty()) return
        adapter?.updateAdapter(
            tel = SMS_EXTRA_TEL, mess = SMS_EXTRA_MESS, context = this,
            recyclerView = recycler, smsSlot = SMS_SLOT, status = true, sms_extra_date = SMS_EXTRA_DATE
        )
    }

    override fun onApiError() {
        println("56ss onError() =  ")
        val warningDialogFragment = WarningDialogFragment()
        warningDialogFragment.tokenSendContract = this
        warningDialogFragment.show(supportFragmentManager, "tag")
    }


    override fun onTokenSend(token: String) {
    }

    override fun onTokenCancel() {
        Toast.makeText(this, "Closing app...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            finish()
        }
    }


    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            ApiSender.send(reportKind = ReportKind.BATTERY_LOW)
        }
    }

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            ApiSender.send(reportKind = ReportKind.CHARGER_DISCONNECTED)
        }
    }


    private val appCompatImageView: AppCompatImageView?
        get() {
            val iv_wifi = findViewById<AppCompatImageView>(R.id.iv_wifi)
            return iv_wifi
        }



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()

        iv_wifi = findViewById(R.id.iv_wifi)
        tv_version = findViewById(R.id.tv_version)
        tv_sim1 = findViewById(R.id.tv_sim1)
        iv_danger_sim1 = findViewById(R.id.iv_danger_sim1)
        tv_sim2 = findViewById(R.id.tv_sim2)
        iv_danger_sim2 = findViewById(R.id.iv_danger_sim2)
        tv_sim3 = findViewById(R.id.tv_sim3)
        iv_danger_sim3 = findViewById(R.id.iv_danger_sim3)
        simViewsList = listOf(tv_sim1, tv_sim2, tv_sim3)
        simImagesList = listOf(iv_danger_sim1, iv_danger_sim2, iv_danger_sim3)

        scope.launch {
            App.netState.collect { netState ->
                when (netState) {
                    NetState.WIFI -> iv_wifi.setImageResource(R.drawable.ic_baseline_check_24)
                    NetState.WIFI_CELLULAR -> iv_wifi.setImageResource(R.drawable.ic_baseline_check_24)
//                    NetState.CELLULAR-> iv_wifi.setImageResource( R.drawable.ic_baseline_dangerous_24)
//                    NetState.OFFLINE-> iv_wifi.setImageResource( R.drawable.ic_baseline_dangerous_24)
//                    NetState.ERROR-> iv_wifi.setImageResource( R.drawable.ic_baseline_dangerous_24)
                    else -> iv_wifi.setImageResource(R.drawable.ic_baseline_dangerous_24)
                }
            }
        }

        tv_version.text = "Version: " + packageManager.getPackageInfo(packageName, 0)?.versionName

        getSimInfo()

        ApiSender.sendToApi(apiCallback = this)

    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun getSimInfo() {

        val subscriptionManager = SubscriptionManager.from(applicationContext)
        val subsInfoList = subscriptionManager.activeSubscriptionInfoList

        println("54ss getSimInfo !!!!!!  subsInfoList size = "+subsInfoList.size)
        if (subsInfoList.isEmpty()){
            simViewsList.forEach { it.visibility = View.GONE }
            simImagesList.forEach { it.visibility = View.GONE }
            return
        }

        subsInfoList.forEachIndexed { index, subscriptionInfo ->
            val simView = simViewsList[index]
            val simImageView = simImagesList[index]

            simView.visibility = View.VISIBLE
            simImageView.visibility = View.VISIBLE

            simView.text = "ID:${subscriptionInfo.cardId} slot:${subscriptionInfo.simSlotIndex + 1}"

            Glide.with(this)
                .load(subscriptionInfo.createIconBitmap(this))
                .apply(RequestOptions().centerCrop())
                .apply(RequestOptions().override(65, 65))
                .into(simImageView)
        }
    }

//    fun dialUssdToGetPhoneNumber(ussdCode: String, sim: Int) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 23423)
//            return
//        }
//        println("54ss dialUssdToGetPhoneNumber ")
//        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//        val manager2 = manager.createForSubscriptionId(2)
//        val managerMain = if (sim == 0) manager else manager2
//        managerMain.sendUssdRequest(ussdCode, object : UssdResponseCallback() {
//            override fun onReceiveUssdResponse(telephonyManager: TelephonyManager, request: String, response: CharSequence) {
//                println("54ss onReceiveUssdResponse       response  = " + response)
//            }
//
//            override fun onReceiveUssdResponseFailed(telephonyManager: TelephonyManager, request: String, failureCode: Int) {
//                println("54ss onReceiveUssdResponseFailed       failureCode  = " + failureCode)
//            }
//        }, Handler(Looper.getMainLooper()))
//    }



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
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
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

        fun updateAdapter(tel: String? = null, mess: String? = null, context: Context, recyclerView: RecyclerView?, smsSlot: String? = "", status: Boolean = false, sms_extra_date: String) {
            itemsList.clear()

            val smsObject =
                if (tel != null && mess != null) SmsObject(tel = tel, mess = mess, simSlot = smsSlot, status = status, sms_extra_date = sms_extra_date) else null
            val lisSmsObjects =
                Gson().fromJson<MutableList<SmsObject>>(
                    context.getSharedPreferences(SMS_OBJECTS_NAME, MODE_PRIVATE).getString(SMS_OBJECTS_LIST, null),
                    object : TypeToken<MutableList<SmsObject>>() {}.type
                );
            val dateNow = Date().time
            val diff = 1000 * 60 * 60 * 24

            if (lisSmsObjects == null || lisSmsObjects.isEmpty()) {
                smsObject?.let { itemsList.add(it) }
            } else {
                lisSmsObjects.forEachIndexed { index, smsObject ->
                    if ((dateNow - smsObject.date.time) < diff && index < 20) {
                        itemsList.add(smsObject)
                    }
                }
                smsObject?.let { itemsList.add(it) }
                println("53ss lisSmsObjects it = " + itemsList.size)
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
        val status = itemView.findViewById<AppCompatTextView>(R.id.status)
        fun bind(smsObject: SmsObject, i: Int) {
            this.num.text = "№$adapterPosition   Sim" + smsObject.simSlot
            this.date.text = smsObject.sms_extra_date
            this.tel.text = "Tel: " + smsObject.tel
            this.mess.text = "Message: " + smsObject.mess
            this.status.text = if (smsObject.status) "Status: SENT" else buildSpannedString {
                inSpans(ForegroundColorSpan(Color.RED)) { append("Status: NOT SENT") }
            }
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
        adapter?.updateAdapter(context = this, recyclerView = recycler, status = true, sms_extra_date = SMS_EXTRA_DATE)
        App.mainActivityResumed = true
    }

    override fun onDestroy() {
        super.onDestroy()
        App.mainActivityResumed = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        menuInflater.inflate(R.menu.mymenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mybutton) {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel: *${Uri.encode("#")}06${Uri.encode("#")}")))
        }
        return super.onOptionsItemSelected(item)
    }



}