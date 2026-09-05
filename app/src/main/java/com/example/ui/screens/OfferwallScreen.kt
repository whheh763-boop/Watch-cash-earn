package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferwallScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offerwalls & Surveys") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Complete surveys and offers to earn big rewards!", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            ActionCard(
                title = "CPX Research",
                subtitle = "High paying surveys",
                icon = Icons.Default.PlayArrow,
                onClick = { uriHandler.openUri("https://www.cpx-research.com/") }
            )
            ActionCard(
                title = "Torox (OfferToro)",
                subtitle = "App installs & games",
                icon = Icons.Default.PlayArrow,
                onClick = { uriHandler.openUri("https://torox.io/") }
            )
            ActionCard(
                title = "BitLabs Surveys",
                subtitle = "Daily premium surveys",
                icon = Icons.Default.PlayArrow,
                onClick = { uriHandler.openUri("https://bitlabs.ai/") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Offers credit automatically via Postback API. Please wait up to 24 hours for coins to reflect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
