package com.lysen.smsparsing

interface ImeiSetContract {
    fun onImeiSet(imei:String, token:String, index:Int)
}