package org.example.project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Handle granted / denied states */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()

        setContent {
            val callState by CallService.callState.collectAsState()
            val currentCall by CallService.currentCall.collectAsState()
            val audioState by CallService.audioState.collectAsState()

            val callerName = remember(currentCall) {
                currentCall?.details?.callerDisplayName
                    ?: currentCall?.details?.handle?.schemeSpecificPart
                    ?: "Unknown Caller"
            }

            ProfessionalCallerScreen(
                callerName = callerName,
                callState = callState,
                audioState = audioState,
                onAnswer = { CallService.answerCall() },
                onDeclineOrEnd = { CallService.rejectOrEndCall() },
                onToggleMute = { CallService.toggleMute() },
                onToggleSpeaker = { CallService.toggleSpeaker() }
            )
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
fun ProfessionalCallerScreen(
    callerName: String,
    callState: Int,
    audioState: CallAudioState?,
    onAnswer: () -> Unit,
    onDeclineOrEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    var callDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(callState) {
        if (callState == Call.STATE_ACTIVE) {
            callDurationSeconds = 0L
            while (true) {
                delay(1000L)
                callDurationSeconds++
            }
        } else {
            callDurationSeconds = 0L
        }
    }

    val isRinging = callState == Call.STATE_RINGING
    val isActive = callState == Call.STATE_ACTIVE
    val isMuted = audioState?.isMuted ?: false
    val isSpeakerOn = (audioState?.route == CallAudioState.ROUTE_SPEAKER)

    val backgroundBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF020617)
        ),
        radius = 1400f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Status & Caller Metadata
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                CallStatusBadge(callState = callState, durationSeconds = callDurationSeconds)

                Spacer(modifier = Modifier.height(36.dp))

                PulsingAvatar(
                    letter = callerName.firstOrNull()?.toString() ?: "?",
                    isPulsing = isRinging
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Mobile Audio Call",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Middle Section: In-Call Quick Action Deck
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InCallIconButton(
                            icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            isActive = isMuted,
                            onClick = onToggleMute
                        )
                        InCallIconButton(
                            icon = Icons.Rounded.VolumeUp,
                            label = "Speaker",
                            isActive = isSpeakerOn,
                            onClick = onToggleSpeaker
                        )
                        InCallIconButton(
                            icon = Icons.Rounded.Dialpad,
                            label = "Keypad",
                            isActive = false,
                            onClick = { /* Handle Keypad */ }
                        )
                    }
                }
            }

            // Bottom Section: Answer / Hang Up Triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = if (isRinging) Arrangement.SpaceEvenly else Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRinging) {
                    HeroActionButton(
                        icon = Icons.Filled.CallEnd,
                        label = "Decline",
                        backgroundColor = Color(0xFFDC2626),
                        onClick = onDeclineOrEnd
                    )
                    HeroActionButton(
                        icon = Icons.Filled.Call,
                        label = "Accept",
                        backgroundColor = Color(0xFF16A34A),
                        onClick = onAnswer
                    )
                } else {
                    HeroActionButton(
                        icon = Icons.Filled.CallEnd,
                        label = "End Call",
                        backgroundColor = Color(0xFFDC2626),
                        onClick = onDeclineOrEnd
                    )
                }
            }
        }
    }
}

@Composable
fun CallStatusBadge(callState: Int, durationSeconds: Long) {
    val text = when (callState) {
        Call.STATE_RINGING -> "INCOMING CALL"
        Call.STATE_DIALING -> "CALLING..."
        Call.STATE_ACTIVE -> {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
        Call.STATE_DISCONNECTED -> "CALL ENDED"
        else -> "CONNECTING"
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0x1FFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (callState == Call.STATE_ACTIVE) Color(0xFF4ADE80) else Color(0xFF94A3B8),
            letterSpacing = if (callState == Call.STATE_ACTIVE) 0.5.sp else 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PulsingAvatar(letter: String, isPulsing: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.22f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0x1A22C55E))
            )
        }

        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                    )
                )
                .border(2.dp, Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.uppercase(),
                fontSize = 42.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun InCallIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "buttonPress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0x1FFFFFFF))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun HeroActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "heroPress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
    }
}
