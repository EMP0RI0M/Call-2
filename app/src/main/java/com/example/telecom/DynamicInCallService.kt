package com.example.telecom

import android.telecom.Call
import android.telecom.InCallService

class DynamicInCallService : InCallService() {
    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        // TODO: Monitor call state, trigger pipeline
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        // TODO: Clean up pipeline
    }
}
