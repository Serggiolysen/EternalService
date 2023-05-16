package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("model" ) var model : String? = null
)
