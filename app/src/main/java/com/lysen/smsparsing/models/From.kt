package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class From(
    @SerializedName("id") val id: Long? = 0,
    @SerializedName("is_bot") val isBot: Boolean? = false,
    @SerializedName("first_name") val firstName: String? = "",
    @SerializedName("username") val username: String? = ""
)