package org.example.project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.TelecomManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class CallService : InCallService() {

    companion object {
        private var serviceRef: WeakReference<CallService>? = null

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
            val isMuted = _audioState.value?.isMuted ?: false
            serviceRef?.get()?.setMuted(!isMuted)
        }

        fun toggleSpeaker() {
            val route = _audioState.value?.route ?: CallAudioState.ROUTE_EARPIECE
            val target = if (route == CallAudioState.ROUTE_SPEAKER) {
                CallAudioState.ROUTE_EARPIECE
            } else {
                CallAudioState.ROUTE_SPEAKER
            }
            serviceRef?.get()?.setAudioRoute(target)
        }

        fun toggleHold() {
            _currentCall.value?.let { call ->
                if (call.state == Call.STATE_HOLDING) {
                    call.unhold()
                } else if (call.state == Call.STATE_ACTIVE) {
                    call.hold()
                }
            }
        }

        fun sendDtmfTone(digit: Char) {
            _currentCall.value?.playDtmfTone(digit)
            _currentCall.value?.stopDtmfTone()
        }

        fun initiateCall(context: Context, phoneNumber: String) {
            val uri = Uri.fromParts("tel", phoneNumber, null)
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            try {
                telecomManager?.placeCall(uri, null)
            } catch (e: SecurityException) {
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            _callState.value = state
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        _audioState.value = audioState
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        serviceRef = WeakReference(this)
        _currentCall.value = call
        _callState.value = call.state
        _audioState.value = callAudioState
        call.registerCallback(callback)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
        _currentCall.value = null
        _callState.value = Call.STATE_DISCONNECTED
        _audioState.value = null
        if (serviceRef?.get() == this) {
            serviceRef = null
        }
    }
}
