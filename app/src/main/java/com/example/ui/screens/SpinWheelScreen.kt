package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import com.example.ui.theme.PremiumPrimary
import com.example.ui.theme.PremiumSecondary
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinWheelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    val prizes = listOf(10, 50, 0, 100, 20, 5)
    val colors = listOf(Color(0xFF6C5CE7), Color(0xFFFFD700), Color(0xFFE74C3C), Color(0xFF2ECC71), Color(0xFF9B59B6), Color(0xFF3498DB))
    
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinsLeft by remember { mutableStateOf(10) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var earnedCoins by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spin & Win") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Daily Spins Left: $spinsLeft/10",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Wheel UI
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation.value)
                ) {
                    val sweepAngle = 360f / prizes.size
                    for (i in prizes.indices) {
                        drawArc(
                            color = colors[i],
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            style = Fill
                        )
                        // Add border
                        drawArc(
                            color = Color.White.copy(alpha = 0.2f),
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                
                // Center pin/button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(4.dp, PremiumSecondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PremiumPrimary, modifier = Modifier.size(32.dp))
                }
                
                // Pointer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width / 2 - 15.dp.toPx(), 10.dp.toPx())
                        lineTo(size.width / 2 + 15.dp.toPx(), 10.dp.toPx())
                        lineTo(size.width / 2, 40.dp.toPx())
                        close()
                    }
                    drawPath(path, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = {
                    if (!isSpinning && spinsLeft > 0) {
                        isSpinning = true
                        spinsLeft -= 1
                        
                        scope.launch {
                            val winningIndex = (0 until prizes.size).random()
                            val targetRotation = rotation.value + 360f * 5 + (360f - (winningIndex * (360f / prizes.size)))
                            
                            rotation.animateTo(
                                targetValue = targetRotation,
                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                            )
                            
                            earnedCoins = prizes[winningIndex]
                            isSpinning = false
                            
                            // Show Rewarded Ad when spin finishes
                            activity?.let {
                                AdsManager.showAdMobRewarded(
                                    activity = it,
                                    onRewardEarned = {
                                        viewModel.addCoins(earnedCoins)
                                        showRewardDialog = true
                                    },
                                    onAdDismissed = {
                                        // Fallback if ad fails or user closes early
                                        if (!showRewardDialog && earnedCoins > 0) {
                                           viewModel.addCoins(earnedCoins)
                                           showRewardDialog = true
                                        } else if (earnedCoins == 0) {
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
                },
                enabled = !isSpinning && spinsLeft > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary)
            ) {
                Text("SPIN NOW", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    
    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text(if (earnedCoins > 0) "Congratulations!" else "Oops!") },
            text = { 
                Text(
                    if (earnedCoins > 0) "You won $earnedCoins coins!" else "Better luck next time!",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { showRewardDialog = false }) {
                    Text("Awesome")
                }
            }
        )
    }
}
