package com.lysen.smsparsing.models

import java.util.*

data class SmsObject(val tel: String? = "", val mess: String? = "", val date: Date = Date(), val simSlot: String? = "", val status: Boolean = false, val sms_extra_date: String ="")
