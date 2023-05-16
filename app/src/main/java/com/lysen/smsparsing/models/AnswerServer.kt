package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName

data class AnswerServer(
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("errors"  ) var errors  : Errors? = Errors()
)
