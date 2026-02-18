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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceInfoScreen()
                }
            }
        }
    }
}

@Composable
fun DeviceInfoScreen() {
    val context = LocalContext.current
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    // JVM Heap Calculations
    val runtime = Runtime.getRuntime()
    val maxHeap = runtime.maxMemory() / (1024 * 1024)
    val allocatedHeap = runtime.totalMemory() / (1024 * 1024)
    val freeHeap = runtime.freeMemory() / (1024 * 1024)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Ensures content isn't hidden under the status bar
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "📱 Device Info", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfoRow("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
        InfoRow("Android Version", Build.VERSION.RELEASE)
        InfoRow("SDK Level", Build.VERSION.SDK_INT.toString())
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "💾 Memory & Heap", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        InfoRow("Total System RAM", "${memoryInfo.totalMem / (1024 * 1024)} MB")
        InfoRow("Available RAM", "${memoryInfo.availMem / (1024 * 1024)} MB")
        InfoRow("JVM Max Heap", "$maxHeap MB")
        InfoRow("JVM Allocated", "$allocatedHeap MB")
        InfoRow("JVM Free", "$freeHeap MB")
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "⚙️ Hardware", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        InfoRow("CPU ABIs", Build.SUPPORTED_ABIS.joinToString(", "))
        InfoRow("Hardware", Build.HARDWARE)
        InfoRow("Board", Build.BOARD)
        InfoRow("Device", Build.DEVICE)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = value)
    }
}
