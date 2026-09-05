package com.example.ui.screens

import android.app.Activity
import android.view.MotionEvent
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdsManager
import com.example.ui.theme.PremiumPrimary
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ScratchCardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var isScratched by remember { mutableStateOf(false) }
    var scratchPath by remember { mutableStateOf(Path()) }
    var earnedCoins by remember { mutableStateOf(0) }
    var showRewardDialog by remember { mutableStateOf(false) }
    
    // Scratching logic
    var currentPath by remember { mutableStateOf<Path?>(null) }
    
    // Random reward when screen loads
    LaunchedEffect(Unit) {
        earnedCoins = listOf(15, 25, 50, 10, 5).random()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scratch & Win") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scratch the card to reveal your prize!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Reward text underneath
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "You Won",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$earnedCoins",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = PremiumPrimary
                    )
                    Text(
                        "Coins",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Scratch layer
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isScratched,
                    exit = fadeOut(tween(500)),
                    modifier = Modifier.matchParentSize()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 0.99f } // Needed for BlendMode.Clear to work in Canvas
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        currentPath = Path().apply {
                                            moveTo(event.x, event.y)
                                        }
                                        true
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        currentPath?.lineTo(event.x, event.y)
                                        scratchPath.addPath(currentPath!!)
                                        currentPath = Path().apply { moveTo(event.x, event.y) }
                                        
                                        // Very simple "is scratched enough" check based on touches
                                        // In a real app, you'd calculate bounding boxes or pixel density
                                        if (scratchPath.getBounds().width > 300f && scratchPath.getBounds().height > 300f) {
                                            if (!isScratched) {
                                                isScratched = true
                                                
                                                // Show Ad when scratched
                                                activity?.let {
                                                    AdsManager.showAdMobRewarded(
                                                        activity = it,
                                                        onRewardEarned = {
                                                            viewModel.addCoins(earnedCoins)
                                                            showRewardDialog = true
                                                        },
                                                        onAdDismissed = {
                                                            if (!showRewardDialog) {
                                                                viewModel.addCoins(earnedCoins)
                                                                showRewardDialog = true
                                                            }
                                                        }
                                                    )
                                                } ?: run {
                                                    viewModel.addCoins(earnedCoins)
                                                    showRewardDialog = true
                                                }
                                            }
                                        }
                                        true
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        currentPath = null
                                        true
                                    }
                                    else -> false
                                }
                            }
                    ) {
                        // Draw the scratch cover
                        drawRect(color = Color(0xFF9E9E9E), size = size)
                        
                        // Draw patterns on cover
                        drawCircle(color = Color(0xFFBDBDBD), radius = 50f, center = Offset(50f, 50f))
                        drawCircle(color = Color(0xFFBDBDBD), radius = 80f, center = Offset(size.width - 50f, size.height - 50f))
                        
                        // Draw the scratch path with BlendMode.Clear to reveal underneath
                        drawPath(
                            path = scratchPath,
                            color = Color.Transparent,
                            style = Stroke(width = 80f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
            }
        }
    }
    
    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRewardDialog = false
                onBack()
            },
            title = { Text("Card Scratched!") },
            text = { 
                Text(
                    "You found $earnedCoins coins! They have been added to your balance.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    showRewardDialog = false
                    onBack()
                }) {
                    Text("Awesome")
                }
            }
        )
    }
}
