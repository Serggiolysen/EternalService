package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class Sms(
    @SerializedName("body"     ) var body     : String? = null,
    @SerializedName("from"     ) var from     : String? = null,
    @SerializedName("received" ) var received : String? = null,
    @SerializedName("sent"     ) var sent     : String? = null
)
