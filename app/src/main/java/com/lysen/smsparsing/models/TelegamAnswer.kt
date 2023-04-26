package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName


data class TelegamAnswer(
    @SerializedName("ok") var ok: Boolean? = false,
    @SerializedName("result") var result: Result? = Result()
)