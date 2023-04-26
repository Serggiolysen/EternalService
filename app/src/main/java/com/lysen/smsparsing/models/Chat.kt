package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("id") val id: Long? = 0L,
    @SerializedName("title") val title: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("all_members_are_administrators") val allMembersAreAdministrators: Boolean? = false
)