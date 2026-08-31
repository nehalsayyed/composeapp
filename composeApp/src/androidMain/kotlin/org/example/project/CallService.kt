package org.example.project

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class CallService : InCallService() {

    companion object {
        private var serviceInstance: WeakReference<CallService>? = null

        private val _currentCall = MutableStateFlow<Call?>(null)
        val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()

        private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
        val callState: StateFlow<Int> = _callState.asStateFlow()

        private val _audioState = MutableStateFlow<CallAudioState?>(null)
        val audioState: StateFlow<CallAudioState?> = _audioState.asStateFlow()

        fun answerCall() {
            _currentCall.value?.answer(0)
        }

        fun rejectOrEndCall() {
            _currentCall.value?.let { call ->
                if (call.state == Call.STATE_RINGING) {
                    call.reject(false, null)
                } else {
                    call.disconnect()
                }
            }
        }

        fun toggleMute() {
            val currentMuted = _audioState.value?.isMuted ?: false
            serviceInstance?.get()?.setMuted(!currentMuted)
        }

        fun toggleSpeaker() {
            val currentRoute = _audioState.value?.route ?: CallAudioState.ROUTE_EARPIECE
            val targetRoute = if (currentRoute == CallAudioState.ROUTE_SPEAKER) {
                CallAudioState.ROUTE_EARPIECE
            } else {
                CallAudioState.ROUTE_SPEAKER
            }
            serviceInstance?.get()?.setAudioRoute(targetRoute)
        }
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            _callState.value = state
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        _audioState.value = audioState
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        serviceInstance = WeakReference(this)
        _currentCall.value = call
        _callState.value = call.state
        _audioState.value = callAudioState
        call.registerCallback(callCallback)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        _currentCall.value = null
        _callState.value = Call.STATE_DISCONNECTED
        _audioState.value = null
        if (serviceInstance?.get() == this) {
            serviceInstance = null
        }
    }
}
