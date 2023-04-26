package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName


data class Result(
    @SerializedName("message_id") var messageId: Long? = 0L,
    @SerializedName("from") var from: From? = From(),
    @SerializedName("chat") var chat: Chat? = Chat(),
    @SerializedName("date") var date: Long? = 0L,
    @SerializedName("text") var text: String? = ""
)