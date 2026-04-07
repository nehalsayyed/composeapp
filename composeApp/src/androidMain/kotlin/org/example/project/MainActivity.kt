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
import androidx.compose.ui.unit.dp
import com.google.ai.edge.litert.gpu.CompatibilityList
import com.google.ai.edge.litert.gpu.GpuDelegate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GpuDetectionScreen()
                }
            }
        }
    }
}

@Composable
fun GpuDetectionScreen() {
    // 1. Initialize the Compatibility Check
    val compatList = remember { CompatibilityList() }
    
    // 2. Check if the GPU is supported on this specific hardware (Vivo Y20G)
    val isSupported = compatList.isDelegateSupportedOnThisDevice
    
    // 3. Get the best options (ML Drift / OpenCL vs OpenGL)
    val gpuOptions = if (isSupported) {
        "Best Options: ${compatList.bestOptionsForThisDevice}"
    } else {
        "Falling back to CPU"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LiteRT Hardware Check", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSupported) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isSupported) "✅ GPU ACCELERATION SUPPORTED" else "❌ GPU NOT SUPPORTED",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSupported) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Device: ${android.os.Build.MODEL}")
                Text("SoC: ${android.os.Build.BOARD}")
                Text(gpuOptions)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (isSupported) {
            Text(
                "Since you are on Android 13, LiteRT will use the ML Drift engine for maximum speed.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


