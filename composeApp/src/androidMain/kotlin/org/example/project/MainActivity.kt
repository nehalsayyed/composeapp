package org.example.project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            // Permissions denied handling
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestRequiredPermissions()

        setContent {
            MaterialTheme {
                val callState by CallService.callState.collectAsState()
                val currentCall by CallService.currentCall.collectAsState()

                val callerDetails = remember(currentCall) {
                    val handle = currentCall?.details?.handle?.schemeSpecificPart
                    handle ?: "Unknown Caller"
                }

                CallerScreen(
                    callerNumber = callerDetails,
                    callState = callState,
                    onAnswer = { CallService.answerCall() },
                    onDecline = { CallService.rejectCall() }
                )
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}

@Composable
fun CallerScreen(
    callerNumber: String,
    callState: Int,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    val statusText = when (callState) {
        Call.STATE_RINGING -> "INCOMING CALL"
        Call.STATE_ACTIVE -> "CALL IN PROGRESS"
        Call.STATE_DIALING -> "CALLING..."
        Call.STATE_DISCONNECTED -> "CALL ENDED"
        else -> "CONNECTING..."
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF1E293B), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (callerNumber.isNotEmpty()) callerNumber.first().toString() else "?",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callerNumber,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Mute / Speaker Controls (Active during call)
            AnimatedVisibility(visible = callState == Call.STATE_ACTIVE) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallOptionButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        onClick = {
                            isMuted = !isMuted
                            CallService.setMute(isMuted, null)
                        }
                    )
                    CallOptionButton(
                        icon = Icons.Default.VolumeUp,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            CallService.setSpeaker(isSpeakerOn, null)
                        }
                    )
                }
            }

            // Answer & Decline Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (callState == Call.STATE_RINGING) {
                    ActionButton(
                        icon = Icons.Default.CallEnd,
                        backgroundColor = Color(0xFFEF4444),
                        onClick = onDecline
                    )
                    ActionButton(
                        icon = Icons.Default.Call,
                        backgroundColor = Color(0xFF22C55E),
                        onClick = onAnswer
                    )
                } else if (callState == Call.STATE_ACTIVE || callState == Call.STATE_DIALING) {
                    ActionButton(
                        icon = Icons.Default.CallEnd,
                        backgroundColor = Color(0xFFEF4444),
                        onClick = onDecline
                    )
                } else {
                    Text(
                        text = "No active call",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun CallOptionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFF1E293B))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
    }
}
