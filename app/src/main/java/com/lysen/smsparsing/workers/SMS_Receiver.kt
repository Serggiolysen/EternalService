package com.lysen.smsparsing.workers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import com.lysen.smsparsing.App
import com.lysen.smsparsing.App.Companion.mainActivityResumed
import com.lysen.smsparsing.App.Companion.offlineScheduler
import com.lysen.smsparsing.enums.NetState
import kotlinx.coroutines.*
import org.joda.time.DateTime
import java.util.*


class SMS_Receiver : BroadcastReceiver() {

    companion object {
        val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
        val ACTION_TYPE = "ACTION_TYPE"
        val SMS_ACTION_NAME = "SMS_ACTION_NAME"
        val SMS_EXTRA_TEL = "SMS_EXTRA_TEL"
        val SMS_EXTRA_DATE = "SMS_EXTRA_DATE"
        val SMS_EXTRA_MESS = "SMS_EXTRA_MESS"
        val SMS_SLOT = "SMS_SLOT"
    }


    private val subscriptionManager by lazy { App.context?.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }


    override fun onReceive(context: Context?, intent: Intent?) {
        println("53ss   SMS_Receiver intent action = " + intent?.action)

        if (intent?.action?.let { ACTION_SIM_STATE_CHANGED.compareTo(it, ignoreCase = true) } == 0) {
            val intentSIM = Intent(SMS_ACTION_NAME)
            intentSIM.putExtra(SMS_SLOT, intent.action!!)
            context?.sendBroadcast(intentSIM)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (offlineScheduler == null) {
                println("53ss   NotificationReceiver onReceive   offlineScheduler==null")
                val newIntent = Intent()
                if (context == null) return@launch
                println("53ss   NotificationReceiver onReceive   offlineScheduler==null  2")
                newIntent.component = ComponentName(context.packageName, "${context.packageName}.MainActivity")
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                println("53ss   NotificationReceiver onReceive   offlineScheduler==null  3 context = " + context)
                delay(2 * 1000)
                context.startActivity(newIntent)
                CoroutineScope(Dispatchers.IO).launch {
                    do {
                        println("53ss   do  while")
                    } while (!mainActivityResumed)
                    withContext(Dispatchers.Main) {
                        parceSMS(intent, context)
                    }
                }

            } else {
                parceSMS(intent, context)
                if (intent?.action?.let { ACTION_SMS_RECEIVED.compareTo(it, ignoreCase = true) } == 0) {
                    offlineScheduler!!.initScheduler(false)
                    return@launch
                }
                offlineScheduler!!.initScheduler(true)
//                println("53ss   WorkerReceiver ")
            }

        }
    }


    private fun parceSMS(intent: Intent?, context: Context?) {
        val checkNetwork = offlineScheduler?.checkNetwork()
        if (checkNetwork == NetState.WIFI_CELLULAR || checkNetwork == NetState.ERROR || checkNetwork == NetState.OFFLINE) return

        println("53ss   SMS_Receiver 1111 = ")
        if (intent == null || intent.action == null || intent.extras == null || intent.extras!!["pdus"] == null) {
            val intentSms = Intent(SMS_ACTION_NAME)
            intentSms.putExtra(ACTION_TYPE, intent?.action)
            context?.sendBroadcast(intentSms)
            return
        }


        if (ACTION_SMS_RECEIVED.compareTo(intent.action!!, ignoreCase = true) == 0) {
            val pduArray = intent.extras!!["pdus"] as Array<Any>?
            if (pduArray == null || pduArray.isEmpty()) return
            val messages: Array<SmsMessage?> = arrayOfNulls(pduArray.size)
            var initialSMStext = ""
            var initialPhone = ""
            var sentDate = ""
            for (i in pduArray.indices) {
                messages[i] = SmsMessage.createFromPdu(pduArray[i] as ByteArray)
                println("56ss displayMessageBody        " + messages[i]?.displayMessageBody)
                println("56ss messageBody               " + messages[i]?.messageBody)
                println("56ss displayOriginatingAddress " + messages[i]?.displayOriginatingAddress)
                println("56ss emailFrom                 " + messages[i]?.emailFrom)
                println("56ss emailBody                 " + messages[i]?.emailBody)
                println("56ss isEmail                   " + messages[i]?.isEmail)
                println("56ss timestampMillis           " + DateTime(messages[i]?.timestampMillis).toString())
                println("56ss serviceCenterAddress      " + messages[i]?.serviceCenterAddress)
                println("56ss originatingAddress        " + messages[i]?.originatingAddress)
                println("56ss pseudoSubject             " + messages[i]?.pseudoSubject)
                println("56ss messageClass              " + messages[i]?.messageClass?.name)

                if (sentDate.isEmpty()) sentDate = DateTime(messages[i]?.timestampMillis).toString("YY-MM-dd HH:m:ss.SSS")
                initialSMStext = initialSMStext + messages[i]?.displayMessageBody
                if (initialPhone.isEmpty()) initialPhone = messages[i]?.displayOriginatingAddress + ""
            }
            println("53ss initialSMStext " + initialSMStext)
            println("53ss initialPhone " + initialPhone)

            val smsSlot = smsSimRecognizer(intent)
            println("54ss SMS_Receiver ACTION_TYPE " + intent.action!!)
            println("54ss SMS_Receiver SMS_EXTRA_TEL " + initialPhone)
            println("54ss SMS_Receiver SMS_EXTRA_DATE " + sentDate)
            println("54ss SMS_Receiver SMS_EXTRA_MESS " + initialSMStext)
            println("54ss SMS_Receiver SMS_SLOT " + smsSlot.toString())

            val intentSms = Intent(SMS_ACTION_NAME)
            intentSms.putExtra(SMS_EXTRA_TEL, initialPhone)
            intentSms.putExtra(SMS_EXTRA_DATE, sentDate)
            intentSms.putExtra(SMS_EXTRA_MESS, initialSMStext)
            intentSms.putExtra(SMS_SLOT, smsSlot.toString())
            intentSms.putExtra(ACTION_TYPE, intent.action!!)
            context?.sendBroadcast(intentSms)

            // SMS Sender, example: 123456789
//            val sms_from: String? = messages[0]?.getDisplayOriginatingAddress()
//            //Lets check if SMS sender is 123456789
//            if (sms_from.equals(SMS_SENDER, ignoreCase = true)) {
//                val bodyText = StringBuilder()
//                // If SMS has several parts, lets combine it :)
//                for (i in messages.indices) {
//                    bodyText.append(messages[i]?.getMessageBody())
//                }
//                //SMS Body
//                val body = bodyText.toString()
//                // Lets get SMS Code
//                val code = body.replace("[^0-9]".toRegex(), "")
//            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun smsSimRecognizer(intent: Intent): Int {
        var simSlotIndexLastSms = -100
        try {
            val slotIndex = when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ->  intent.getIntExtra(SubscriptionManager.EXTRA_SLOT_INDEX, -100)
                else -> {
                    val subscriptionId = intent.getIntExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, -100)
                    subscriptionManager.activeSubscriptionInfoList?.find { it.subscriptionId == subscriptionId }?.simSlotIndex ?: -100
                }
            }
            simSlotIndexLastSms = if (slotIndex != -100) slotIndex.plus(1) else slotIndex // make it 1-index instead of 0-indexed so corresponds with physical slots 1 and 2
        } catch (e: Exception) {
            println("53ss smsReceiver Exception " + e.message)
        }
        return simSlotIndexLastSms
    }


}