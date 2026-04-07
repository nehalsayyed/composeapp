package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
// USE THESE EXACT IMPORTS
import org.tensorflow.lite.gpu.CompatibilityList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Use the CompatibilityList to check your Mali-G52 GPU
                val isSupported = remember { CompatibilityList().isDelegateSupportedOnThisDevice }
                
                Surface {
                    Text(
                        text = if (isSupported) "✅ GPU SUPPORTED" else "❌ CPU ONLY",
                        color = if (isSupported) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
        }
    }
}
