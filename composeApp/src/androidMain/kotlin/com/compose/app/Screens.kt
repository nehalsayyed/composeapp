

@file:OptIn(ExperimentalMaterial3Api::class)

package com.compose.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen() {
    val tooltipState = rememberTooltipState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = tooltipState,
            tooltip = {
                PlainTooltip { Text("Go to Home") }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "home",
                tint = Color(0xFF0F9D58)
            )
        }
        Text(text = "Home", color = Color.Black)
    }
}

@Composable
fun SearchScreen() {
    val tooltipState = rememberTooltipState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = tooltipState,
            tooltip = {
                PlainTooltip { Text("Search content") }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search",
                tint = Color(0xFF0F9D58)
            )
        }
        Text(text = "Search", color = Color.Black)
    }
}

@Composable
fun ProfileScreen() {
    val tooltipState = rememberTooltipState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = tooltipState,
            tooltip = {
                PlainTooltip { Text("View your profile") }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color(0xFF0F9D58)
            )
        }
        Text(text = "Profile", color = Color.Black)
    }
}
