package com.compose.app

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import composeapk.composeapp.generated.resources.Res
import composeapk.composeapp.generated.resources.compose_multiplatform

@Composable
fun AnimatedProgressDialog(showDialog: Boolean, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading...", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "Info") },
            label = { Text("Info") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
    }
}

@Composable
fun SideDrawerContent(onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Menu", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Profile", modifier = Modifier.clickable { onItemClick("Profile") })
        Spacer(modifier = Modifier.height(8.dp))
        Text("Settings", modifier = Modifier.clickable { onItemClick("Settings") })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        var showContent by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var selectedTab by remember { mutableStateOf(0) }

        LaunchedEffect(showContent) {
            if (showContent) {
                showDialog = true
                delay(2000)
                showDialog = false
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SideDrawerContent { item ->
                    println("Clicked: $item")
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(selectedTab = selectedTab) {
                        selectedTab = it
                    }
                },
                topBar = {
                    TopAppBar(
                        title = { Text("Compose App") },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .safeContentPadding()
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = { showContent = !showContent }) {
                        Text("Click me!")
                    }

                    AnimatedProgressDialog(showDialog = showDialog, onDismiss = { showDialog = false })

                    AnimatedVisibility(showContent && !showDialog) {
                        val greeting = remember { Greeting().greet() }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(painterResource(Res.drawable.compose_multiplatform), null)
                            Text("Compose: $greeting")
                        }
                    }

                    if (selectedTab == 1) {
                        Text("Info Tab Content", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
