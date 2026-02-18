package org.example.project

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Device : Screen("device", "Device", Icons.Default.Info)
    object Memory : Screen("memory", "Memory", Icons.Default.Settings)
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Device) }
    val items = listOf(Screen.Device, Screen.Memory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                is Screen.Device -> DeviceTab()
                is Screen.Memory -> MemoryTab()
            }
        }
    }
}

@Composable
fun DeviceTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "📱 Device Hardware", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfoRow("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
        InfoRow("Android Version", Build.VERSION.RELEASE)
        InfoRow("SDK Level", Build.VERSION.SDK_INT.toString())
        InfoRow("Hardware", Build.HARDWARE)
        InfoRow("Board", Build.BOARD)
        InfoRow("CPU ABIs", Build.SUPPORTED_ABIS.joinToString(", "))
    }
}

@Composable
fun MemoryTab() {
    val context = LocalContext.current
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val runtime = Runtime.getRuntime()
    val maxHeap = runtime.maxMemory() / (1024 * 1024)
    val allocatedHeap = runtime.totalMemory() / (1024 * 1024)
    val freeHeap = runtime.freeMemory() / (1024 * 1024)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "💾 Memory & Heap", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfoRow("Total System RAM", "${memoryInfo.totalMem / (1024 * 1024)} MB")
        InfoRow("Available RAM", "${memoryInfo.availMem / (1024 * 1024)} MB")
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        
        InfoRow("JVM Max Heap", "$maxHeap MB")
        InfoRow("JVM Allocated", "$allocatedHeap MB")
        InfoRow("JVM Free", "$freeHeap MB")
        
        Text(
            text = "Note: Your DOTNET_GCHeapHardLimit logic may affect how the runtime allocates these pools.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = value)
    }
}
