package com.lysen.smsparsing.workers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.lysen.smsparsing.App.Companion.mainActivityResumed
import com.lysen.smsparsing.App.Companion.offlineScheduler
import kotlinx.coroutines.*


class SMS_Receiver : BroadcastReceiver() {

    companion object {
        val ACTION = "android.provider.Telephony.SMS_RECEIVED"
        val ACTION_TYPE = "ACTION_TYPE"
        val SMS_ACTION_NAME = "SMS_ACTION_NAME"
        val SMS_EXTRA_TEL = "SMS_EXTRA_TEL"
        val SMS_EXTRA_MESS = "SMS_EXTRA_MESS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        println("53ss   SMS_Receiver intent action = " + intent?.action)

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
                offlineScheduler!!.initScheduler()
//                println("53ss   WorkerReceiver ")

            }
        }

    }


    private fun parceSMS(intent: Intent?, context: Context?) {
        println("53ss   SMS_Receiver 1111 = ")
        if (intent == null || intent.action == null || intent.extras == null || intent.extras!!["pdus"] == null) {
            val intentSms = Intent(SMS_ACTION_NAME)
            intentSms.putExtra(ACTION_TYPE, intent?.action)
            context?.sendBroadcast(intentSms)
            return
        }

        if (ACTION.compareTo(intent.action!!, ignoreCase = true) == 0) {
            val pduArray = intent.extras!!["pdus"] as Array<Any>?
            val messages: Array<SmsMessage?> = arrayOfNulls(pduArray!!.size)
            for (i in pduArray.indices) {
                messages[i] = SmsMessage.createFromPdu(pduArray[i] as ByteArray)
                println("53ss $i " + messages[i]?.displayMessageBody)
//                println("53ss $i " + messages[i]?.messageBody)
                println("53ss $i " + messages[i]?.displayOriginatingAddress)

                val intentSms = Intent(SMS_ACTION_NAME)
                intentSms.putExtra(SMS_EXTRA_TEL, messages[i]?.displayOriginatingAddress)
                intentSms.putExtra(SMS_EXTRA_MESS, messages[i]?.displayMessageBody)
                intentSms.putExtra(ACTION_TYPE, intent.action!!)
                context?.sendBroadcast(intentSms)
            }
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
}