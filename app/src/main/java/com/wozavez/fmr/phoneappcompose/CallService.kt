package com.wozavez.fmr.phoneappcompose

import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {

    override fun onCallAdded(call: Call) {
        //OngoingCall.call = call
        OngoingCallViewModel.call = call
        CallActivity.start(this, call)
    }

    override fun onCallRemoved(call: Call) {
        //OngoingCall.call = null
        OngoingCallViewModel.call = null
    }
}