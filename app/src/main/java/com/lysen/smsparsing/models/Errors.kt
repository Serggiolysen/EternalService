package com.lysen.smsparsing.models

import com.google.gson.annotations.SerializedName


data class Errors(

    @SerializedName("device") var device: ArrayList<String> = arrayListOf()

)

