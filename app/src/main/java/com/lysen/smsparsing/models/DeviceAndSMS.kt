package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class DeviceAndSMS(
    @SerializedName("device" ) var device : Device? = null,
    @SerializedName("sms"    ) var sms    : Sms?    = null
)
