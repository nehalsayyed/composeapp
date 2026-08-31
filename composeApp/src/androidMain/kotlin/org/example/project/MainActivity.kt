package org.example.project

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppTab(val title: String, val icon: ImageVector) {
    FAVORITES("Favorites", Icons.Rounded.Star),
    RECENTS("Recents", Icons.Rounded.History),
    CONTACTS("Contacts", Icons.Rounded.Contacts),
    KEYPAD("Keypad", Icons.Rounded.Dialpad)
}

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Refreshes state */ }

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Default dialer callback */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsAndRole()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    primary = Color(0xFF38BDF8),
                    onBackground = Color(0xFFF8FAFC),
                    onSurface = Color(0xFFF8FAFC)
                )
            ) {
                MainAppScaffold()
            }
        }
    }

    private fun requestPermissionsAndRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            }
        }

        val required = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            required.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}

@Composable
fun MainAppScaffold() {
    val context = LocalContext.current
    val repository = remember { TelephonyRepository(context) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(AppTab.KEYPAD) }
    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var callLogs by remember { mutableStateOf<List<CallLogItem>>(emptyList()) }

    // Telecom state hooks
    val callState by CallService.callState.collectAsState()
    val currentCall by CallService.currentCall.collectAsState()
    val audioState by CallService.audioState.collectAsState()

    fun refreshData() {
        scope.launch {
            try {
                contacts = repository.getContacts()
                callLogs = repository.getCallLogs()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val callerName = remember(currentCall) {
        currentCall?.details?.callerDisplayName
            ?: currentCall?.details?.handle?.schemeSpecificPart
            ?: "Unknown"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF090D16),
                    tonalElevation = 8.dp
                ) {
                    AppTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                refreshData()
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color(0xFF64748B),
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color(0xFF64748B),
                                indicatorColor = Color(0xFF2563EB)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    AppTab.FAVORITES -> FavoritesScreen(
                        contacts = contacts.filter { it.isFavorite },
                        onCall = { CallService.initiateCall(context, it) }
                    )
                    AppTab.RECENTS -> RecentsScreen(
                        callLogs = callLogs,
                        onCall = { CallService.initiateCall(context, it) }
                    )
                    AppTab.CONTACTS -> ContactsScreen(
                        contacts = contacts,
                        onCall = { CallService.initiateCall(context, it) }
                    )
                    AppTab.KEYPAD -> DialpadScreen(
                        onCall = { CallService.initiateCall(context, it) }
                    )
                }
            }
        }

        // Full Screen In-Call UI Overlay
        AnimatedVisibility(
            visible = callState != Call.STATE_DISCONNECTED,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            InCallOverlay(
                callerName = callerName,
                callState = callState,
                audioState = audioState,
                onAnswer = { CallService.answerCall() },
                onDecline = { CallService.rejectOrEndCall() },
                onToggleMute = { CallService.toggleMute() },
                onToggleSpeaker = { CallService.toggleSpeaker() },
                onToggleHold = { CallService.toggleHold() },
                onSendDtmf = { CallService.sendDtmfTone(it) }
            )
        }
    }
}






@Composable
fun DialpadScreen(onCall: (String) -> Unit) {
    var dialString by remember { mutableStateOf("") }

    val keypadButtons = listOf(
        Pair("1", ""), Pair("2", "A B C"), Pair("3", "D E F"),
        Pair("4", "G H I"), Pair("5", "J K L"), Pair("6", "M N O"),
        Pair("7", "P Q R S"), Pair("8", "T U V"), Pair("9", "W X Y Z"),
        Pair("*", ""), Pair("0", "+"), Pair("#", "")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(
                text = dialString.ifEmpty { " " },
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (dialString.isNotEmpty()) {
                Text(
                    text = "Add to Contacts",
                    fontSize = 13.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { /* Add to contacts action */ }
                )
            }
        }

        // 3x4 Grid Keypad
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(keypadButtons) { (digit, sub) ->
                KeypadKey(
                    digit = digit,
                    letters = sub,
                    onClick = { dialString += digit },
                    onLongClick = { if (digit == "0") dialString += "+" }
                )
            }
        }

        // Bottom Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(56.dp))

            // Call Trigger
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .clickable { if (dialString.isNotBlank()) onCall(dialString) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // Backspace Button
            if (dialString.isNotEmpty()) {
                IconButton(
                    onClick = { dialString = dialString.dropLast(1) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.Backspace, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                }
            } else {
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadKey(digit: String, letters: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "")

    Surface(
        shape = CircleShape,
        color = if (isPressed) Color(0xFF334155) else Color(0xFF1E293B),
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = digit, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (letters.isNotEmpty()) {
                Text(text = letters, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun RecentsScreen(callLogs: List<CallLogItem>, onCall: (String) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Recent Calls", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        }
        items(callLogs) { log ->
            val isMissed = log.type == CallLog.Calls.MISSED_TYPE
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCall(log.number) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (log.type) {
                            CallLog.Calls.OUTGOING_TYPE -> Icons.Rounded.CallMade
                            CallLog.Calls.MISSED_TYPE -> Icons.Rounded.CallMissed
                            else -> Icons.Rounded.CallReceived
                        },
                        contentDescription = null,
                        tint = if (isMissed) Color(0xFFEF4444) else Color(0xFF22C55E),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = log.name ?: log.number,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (isMissed) Color(0xFFF87171) else Color.White
                        )
                        Text(
                            text = "${log.number} • ${dateFormat.format(Date(log.timestamp))}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = { onCall(log.number) }) {
                        Icon(Icons.Rounded.Call, contentDescription = "Call Back", tint = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsScreen(contacts: List<ContactItem>, onCall: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { it.name.contains(searchQuery, ignoreCase = true) || it.number.contains(searchQuery) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search contacts...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { contact ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCall(contact.number) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.firstOrNull()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = contact.name, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                            Text(text = contact.number, color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        IconButton(onClick = { onCall(contact.number) }) {
                            Icon(Icons.Rounded.Call, contentDescription = "Call", tint = Color(0xFF22C55E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(contacts: List<ContactItem>, onCall: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Speed Dial & Favorites", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))

        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorite contacts starred yet", color = Color(0xFF64748B), fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contacts) { contact ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCall(contact.number) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.name.firstOrNull()?.toString() ?: "?", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(contact.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(contact.number, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}






@Composable
fun InCallOverlay(
    callerName: String,
    callState: Int,
    audioState: CallAudioState?,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onSendDtmf: (Char) -> Unit
) {
    var durationSeconds by remember { mutableLongStateOf(0L) }
    var showInCallDialpad by remember { mutableStateOf(false) }

    val isRinging = callState == Call.STATE_RINGING
    val isActive = callState == Call.STATE_ACTIVE
    val isHolding = callState == Call.STATE_HOLDING
    val isMuted = audioState?.isMuted ?: false
    val isSpeakerOn = audioState?.route == CallAudioState.ROUTE_SPEAKER

    LaunchedEffect(callState) {
        if (callState == Call.STATE_ACTIVE) {
            durationSeconds = 0L
            while (true) {
                delay(1000L)
                durationSeconds++
            }
        }
    }

    val backgroundBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617)),
        radius = 1600f
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
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x1FFFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    val statusText = when (callState) {
                        Call.STATE_RINGING -> "INCOMING CALL"
                        Call.STATE_DIALING -> "DIALING..."
                        Call.STATE_HOLDING -> "ON HOLD"
                        Call.STATE_ACTIVE -> String.format(Locale.getDefault(), "%02d:%02d", durationSeconds / 60, durationSeconds % 60)
                        else -> "CALLING..."
                    }
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .border(2.dp, Color(0x33FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callerName.firstOrNull()?.toString() ?: "?",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = callerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "HD Voice Call",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // In-Call Controls Deck
            if (isActive || isHolding) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallControl(
                                icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                label = if (isMuted) "Unmute" else "Mute",
                                active = isMuted,
                                onClick = onToggleMute
                            )
                            InCallControl(
                                icon = Icons.Rounded.Dialpad,
                                label = "Keypad",
                                active = showInCallDialpad,
                                onClick = { showInCallDialpad = !showInCallDialpad }
                            )
                            InCallControl(
                                icon = Icons.Rounded.VolumeUp,
                                label = "Speaker",
                                active = isSpeakerOn,
                                onClick = onToggleSpeaker
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallControl(
                                icon = Icons.Rounded.Pause,
                                label = if (isHolding) "Resume" else "Hold",
                                active = isHolding,
                                onClick = onToggleHold
                            )
                            InCallControl(
                                icon = Icons.Rounded.AddCall,
                                label = "Add call",
                                active = false,
                                onClick = { /* Support multi-party calls */ }
                            )
                            InCallControl(
                                icon = Icons.Rounded.RecordVoiceOver,
                                label = "Record",
                                active = false,
                                onClick = { /* Call recording hook */ }
                            )
                        }
                    }
                }
            }

            // Answer / Hangup Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = if (isRinging) Arrangement.SpaceEvenly else Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRinging) {
                    CallActionButton(
                        icon = Icons.Filled.CallEnd,
                        label = "Decline",
                        color = Color(0xFFDC2626),
                        onClick = onDecline
                    )
                    CallActionButton(
                        icon = Icons.Filled.Call,
                        label = "Accept",
                        color = Color(0xFF16A34A),
                        onClick = onAnswer
                    )
                } else {
                    CallActionButton(
                        icon = Icons.Filled.CallEnd,
                        label = "End Call",
                        color = Color(0xFFDC2626),
                        onClick = onDecline
                    )
                }
            }
        }
    }
}

@Composable
fun InCallControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color(0x1FFFFFFF))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = if (active) Color.White else Color(0xFF94A3B8))
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(34.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
    }
}

