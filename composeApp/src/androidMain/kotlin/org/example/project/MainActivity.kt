package org.example.project

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// USE THESE EXACT IMPORTS
import org.tensorflow.lite.gpu.CompatibilityList
import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val isSupported = remember { CompatibilityList().isDelegateSupportedOnThisDevice }
                
                // Fetching CPU Information
                val cpuArch = remember { Build.SUPPORTED_ABIS.joinToString(", ") }
                val cpuCores = remember { getNumberOfCpuCores() }
                val cpuHardware = remember { Build.HARDWARE }
                val cpuBoard = remember { Build.BOARD }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Hardware Information", 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()

                        // --- GPU SECTION ---
                        Text(text = "GPU Status", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isSupported) "✅ GPU SUPPORTED (Mali-G52 / Compatible)" else "❌ CPU ONLY (Fallback)",
                            color = if (isSupported) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- CPU SECTION ---
                        Text(text = "CPU Specifications", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        
                        CpuInfoRow(label = "SoC / Hardware", value = cpuHardware)
                        CpuInfoRow(label = "Board", value = cpuBoard)
                        CpuInfoRow(label = "Architecture", value = cpuArch)
                        CpuInfoRow(label = "Total Cores", value = "$cpuCores Cores")
                    }
                }
            }
        }
    }

    /**
     * Helper function to get the actual number of CPU cores on the device.
     * Fallback relies on Runtime available processors.
     */
    private fun getNumberOfCpuCores(): Int {
        return try {
            val dir = File("/sys/devices/system/cpu/")
            val files = dir.listFiles(FileFilter { 
                Pattern.matches("cpu[0-9]+", it.name) 
            })
            files?.size ?: Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            Runtime.getRuntime().availableProcessors()
        }
    }
}

@Composable
fun CpuInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontWeight = FontWeight.Normal)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}
