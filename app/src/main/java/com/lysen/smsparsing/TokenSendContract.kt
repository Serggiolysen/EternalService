package com.lysen.smsparsing

interface TokenSendContract {
    fun onTokenSend(token:String)
    fun onTokenCancel()

}