package org.example.project

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CallService : InCallService() {

    companion object {
        private val _currentCall = MutableStateFlow<Call?>(null)
        val currentCall: StateFlow<Call?> = _currentCall

        private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
        val callState: StateFlow<Int> = _callState

        fun answerCall() {
            _currentCall.value?.answer(0)
        }

        fun rejectCall() {
            _currentCall.value?.let { call ->
                if (call.state == Call.STATE_RINGING) {
                    call.reject(false, null)
                } else {
                    call.disconnect()
                }
            }
        }

        fun setMute(isMuted: Boolean, service: CallService?) {
            service?.setMuted(isMuted)
        }

        fun setSpeaker(isSpeakerOn: Boolean, service: CallService?) {
            val route = if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
            service?.setAudioRoute(route)
        }
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            _callState.value = state
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        _currentCall.value = call
        _callState.value = call.state
        call.registerCallback(callback)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
        _currentCall.value = null
        _callState.value = Call.STATE_DISCONNECTED
    }
}
