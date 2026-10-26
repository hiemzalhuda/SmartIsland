/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Fork hiemz addition: the interactive Nochi face. Tapping the idle eyes on the pill drops
 * the island down into this big face; tapping the face cycles funny reactions (wink, laugh,
 * surprised, tongue out). It keeps blinking and glancing on its own like the pill eyes.
 */

package com.agupta07505.smartisland.ui.expanded

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.util.SystemAnimScale
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class NochiFaceMood { Normal, Happy, Laugh, Wink, Surprised, Tongue }

/**
 * The expanded island's content when the face is open: a big Nochi face that reacts to taps.
 * Close with the X (or the island's usual outside-tap / swipe-up close).
 */
@Composable
internal fun NochiFaceExpanded(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationsOff = SystemAnimScale.animationsOff()
    var blinking by remember { mutableStateOf(false) }
    var lookX by remember { mutableFloatStateOf(0f) }
    var idleMood by remember { mutableStateOf(NochiFaceMood.Normal) }
    var tapMood by remember { mutableStateOf<NochiFaceMood?>(null) }

    val openFraction by animateFloatAsState(
        targetValue = if (blinking) 0.08f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "nochiFaceBlink"
    )
    val lookAnimated by animateFloatAsState(
        targetValue = lookX,
        animationSpec = tween(durationMillis = 300),
        label = "nochiFaceLook"
    )

    // Idle life: blink, glance sideways, occasionally smile. Tap reactions override it briefly.
    if (!animationsOff) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(2400L, 5200L))
                blinking = true
                delay(130L)
                blinking = false
                when (Random.nextFloat()) {
                    in 0f..0.35f -> {
                        val directions = listOf(-1f, 1f)
                        lookX = directions.random()
                        delay(900L)
                        lookX = 0f
                    }
                    in 0.35f..0.50f -> {
                        idleMood = NochiFaceMood.Happy
                        delay(900L)
                        idleMood = NochiFaceMood.Normal
                    }
                }
            }
        }
    }
    LaunchedEffect(tapMood) {
        if (tapMood != null) {
            delay(1500L)
            tapMood = null
        }
    }

    val mood = tapMood ?: idleMood
    val reactions = listOf(
        NochiFaceMood.Happy, NochiFaceMood.Laugh, NochiFaceMood.Wink,
        NochiFaceMood.Surprised, NochiFaceMood.Tongue
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable { onCollapse() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .clickable {
                    tapMood = reactions.filter { it != tapMood }.random()
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp, 150.dp)) {
                val eyeW = 64.dp.toPx()
                val eyeHFull = 72.dp.toPx()
                val pupilR = 13.dp.toPx()
                val gap = 56.dp.toPx()
                val leftCenter = Offset(size.width / 2f - gap / 2f - eyeW / 2f, 55.dp.toPx())
                val rightCenter = Offset(size.width / 2f + gap / 2f + eyeW / 2f, 55.dp.toPx())
                val centers = listOf(leftCenter, rightCenter)

                // Blush, for the cute factor.
                for (cheekX in listOf(28.dp.toPx(), size.width - 28.dp.toPx())) {
                    drawCircle(
                        color = Color(0xFFF9A8D4).copy(alpha = 0.45f),
                        radius = 11.dp.toPx(),
                        center = Offset(cheekX, 102.dp.toPx())
                    )
                }

                for ((index, center) in centers.withIndex()) {
                    val isWinkEye = mood == NochiFaceMood.Wink && index == 0
                    if (mood == NochiFaceMood.Happy || mood == NochiFaceMood.Laugh || isWinkEye) {
                        // Upper-half arc: ^^
                        drawArc(
                            color = Color.White,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(center.x - eyeW / 2f, center.y - eyeHFull * 0.35f),
                            size = Size(eyeW, eyeHFull * 0.7f),
                            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                        )
                    } else {
                        val eyeH = (eyeHFull * openFraction).coerceAtLeast(1f)
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(center.x - eyeW / 2f, center.y - eyeH / 2f),
                            size = Size(eyeW, eyeH)
                        )
                        if (openFraction > 0.5f) {
                            val pupil = Offset(center.x + lookAnimated * 8.dp.toPx(), center.y)
                            drawCircle(color = Color(0xFF111111), radius = pupilR, center = pupil)
                            drawCircle(
                                color = Color.White,
                                radius = pupilR * 0.3f,
                                center = pupil + Offset(-pupilR * 0.35f, -pupilR * 0.35f)
                            )
                        }
                    }
                }

                // Mouth, per mood.
                val mouthCenter = Offset(size.width / 2f, 122.dp.toPx())
                when (mood) {
                    NochiFaceMood.Normal -> drawArc(
                        color = Color.White,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(mouthCenter.x - 16.dp.toPx(), mouthCenter.y - 8.dp.toPx()),
                        size = Size(32.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    NochiFaceMood.Happy, NochiFaceMood.Wink -> drawArc(
                        color = Color.White,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(mouthCenter.x - 22.dp.toPx(), mouthCenter.y - 11.dp.toPx()),
                        size = Size(44.dp.toPx(), 22.dp.toPx()),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    NochiFaceMood.Laugh -> drawOval(
                        color = Color(0xFF111111),
                        topLeft = Offset(mouthCenter.x - 20.dp.toPx(), mouthCenter.y - 10.dp.toPx()),
                        size = Size(40.dp.toPx(), 28.dp.toPx())
                    )
                    NochiFaceMood.Surprised -> drawCircle(
                        color = Color(0xFF111111),
                        radius = 10.dp.toPx(),
                        center = mouthCenter
                    )
                    NochiFaceMood.Tongue -> {
                        drawArc(
                            color = Color.White,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(mouthCenter.x - 20.dp.toPx(), mouthCenter.y - 10.dp.toPx()),
                            size = Size(40.dp.toPx(), 20.dp.toPx()),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawOval(
                            color = Color(0xFFF472B6),
                            topLeft = Offset(mouthCenter.x - 7.dp.toPx(), mouthCenter.y + 2.dp.toPx()),
                            size = Size(14.dp.toPx(), 18.dp.toPx())
                        )
                    }
                }
            }
        }
        Text(
            text = "Ketuk aku!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
