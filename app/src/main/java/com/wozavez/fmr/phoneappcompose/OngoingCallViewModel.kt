package com.wozavez.fmr.phoneappcompose

import androidx.lifecycle.ViewModel
import android.telecom.Call
import android.telecom.VideoProfile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import timber.log.Timber
import java.util.Timer
import kotlin.concurrent.timerTask

object OngoingCallViewModel : ViewModel() {
    var state by mutableIntStateOf(0)
    var time by mutableIntStateOf(0)
    private var timer = Timer()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Timber.d(call.toString())
            state = call.details.state
            if (state == Call.STATE_DIALING) time = 0
            if (state == Call.STATE_ACTIVE) {
                timer = Timer()
                timer.scheduleAtFixedRate(timerTask {
                    time++
                }, 0, 1000)
            }
            if (state == Call.STATE_DISCONNECTING) {
                timer.cancel()
            }
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state = it.details.state
            }
            field = value
        }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
}