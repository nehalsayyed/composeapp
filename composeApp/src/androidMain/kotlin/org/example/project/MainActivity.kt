package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// MODERN LITERT IMPORTS
import com.google.ai.edge.litert.gpu.CompatibilityList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GpuChecker()
                }
            }
        }
    }
}

@Composable
fun GpuChecker() {
    // Use produceState to run the check without blocking the UI thread
    val gpuStatus by produceState(initialValue = "Checking Hardware...") {
        val compatList = CompatibilityList()
        val isSupported = compatList.isDelegateSupportedOnThisDevice
        
        value = if (isSupported) {
            "✅ LiteRT GPU: ACTIVE\n(Mali-G52 Optimized)"
        } else {
            "❌ LiteRT GPU: NOT SUPPORTED\n(Using CPU Fallback)"
        }
        compatList.close()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Acceleration Status", style = MaterialTheme.typography.headlineSmall)
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (gpuStatus.contains("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            )
        ) {
            Text(
                text = gpuStatus,
                modifier = Modifier.padding(20.dp),
                color = if (gpuStatus.contains("✅")) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Android 13 | Vivo Y20G", style = MaterialTheme.typography.bodySmall)
    }
}
