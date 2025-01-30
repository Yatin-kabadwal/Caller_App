package com.bot.calling_app

import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log

class CallConnection : Connection() {

    init {
        setConnectionCapabilities(CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD)
        setAudioModeIsVoip(true)
    }

    override fun onAnswer() {
        Log.d("CallConnection", "Call answered")
        setActive()
    }

    override fun onReject() {
        Log.d("CallConnection", "Call rejected")
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        Log.d("CallConnection", "Call ended")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }
}
