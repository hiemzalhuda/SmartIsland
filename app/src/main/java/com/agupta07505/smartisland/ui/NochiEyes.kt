/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Fork hiemz addition: Nochi-style idle eyes for the collapsed pill.
 * Inspired by tini's Nochi (eyes in the notch): the eyes blink, glance
 * sideways at random, and occasionally smile (^^) or squint calmly.
 * There is no mouse cursor on a phone, so "following the cursor" is
 * replaced by the random glances. The camera hole stays clear because
 * the two eyes sit left and right of the pill's centre, like the idle
 * widget's left / right slots.
 */

package com.agupta07505.smartisland.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.util.SystemAnimScale
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class NochiMood { Normal, Happy, Squint }

/**
 * Two eyes drawn on the black pill. Static (open, centred) when system
 * animations are off; otherwise blinking, glancing and mood-switching.
 */
@Composable
internal fun NochiEyes(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    /** Fork hiemz: gap between the inner edges of the two eyes, in dp (portrait pill only). */
    gapDp: Float = 24f
) {
    val animationsOff = SystemAnimScale.animationsOff()
    var blinking by remember { mutableStateOf(false) }
    var lookX by remember { mutableFloatStateOf(0f) }
    var mood by remember { mutableStateOf(NochiMood.Normal) }

    val openFraction by animateFloatAsState(
        targetValue = if (blinking) 0.08f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "nochiBlink"
    )
    val lookAnimated by animateFloatAsState(
        targetValue = lookX,
        animationSpec = tween(durationMillis = 300),
        label = "nochiLook"
    )

    if (!animationsOff) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(2400L, 5200L))
                blinking = true
                delay(130L)
                blinking = false
                when (Random.nextFloat()) {
                    in 0f..0.35f -> {
                        // Glance sideways, then back to centre.
                        val directions = listOf(-1f, 1f)
                        lookX = directions.random()
                        delay(900L)
                        lookX = 0f
                    }
                    in 0.35f..0.50f -> {
                        // Happy eyes (^^), like Nochi's hover smile.
                        mood = NochiMood.Happy
                        delay(900L)
                        mood = NochiMood.Normal
                    }
                    in 0.50f..0.62f -> {
                        // Calm squint, like Nochi during focus.
                        mood = NochiMood.Squint
                        delay(1000L)
                        mood = NochiMood.Normal
                    }
                }
            }
        }
    }

    // Compact (landscape circle, 26 dp) must fit inside the circle.
    // Portrait: the canvas grows with the gap so the hole fits between the eyes.
    val eyeWPortrait = 20.dp
    val eyeMarginPortrait = 6.dp
    val totalWidth = if (compact) 24.dp else (gapDp + 2 * eyeWPortrait.value + 2 * eyeMarginPortrait.value).dp
    val totalHeight = if (compact) 12.dp else 26.dp
    Canvas(modifier = modifier.size(totalWidth, totalHeight)) {
        val eyeW = if (compact) 11.dp.toPx() else eyeWPortrait.toPx()
        val eyeHFull = if (compact) 12.dp.toPx() else 22.dp.toPx()
        val pupilR = if (compact) 2.5.dp.toPx() else 4.5.dp.toPx()
        val centers = if (compact) {
            listOf(
                Offset(size.width * 0.28f, size.height / 2f),
                Offset(size.width * 0.72f, size.height / 2f)
            )
        } else {
            val margin = eyeMarginPortrait.toPx()
            listOf(
                Offset(margin + eyeW / 2f, size.height / 2f),
                Offset(size.width - margin - eyeW / 2f, size.height / 2f)
            )
        }
        for (center in centers) {
            if (mood == NochiMood.Happy) {
                // Upper-half arc: ^^
                drawArc(
                    color = Color.White,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - eyeW / 2f, center.y - eyeHFull * 0.35f),
                    size = Size(eyeW, eyeHFull * 0.7f),
                    style = Stroke(width = (if (compact) 1.5.dp else 2.5.dp).toPx(), cap = StrokeCap.Round)
                )
            } else {
                val squint = if (mood == NochiMood.Squint) 0.45f else 1f
                val eyeH = (eyeHFull * openFraction * squint).coerceAtLeast(1f)
                drawOval(
                    color = Color.White,
                    topLeft = Offset(center.x - eyeW / 2f, center.y - eyeH / 2f),
                    size = Size(eyeW, eyeH)
                )
                // Pupil, shifted sideways by the glance; hidden while blinking.
                if (openFraction > 0.5f) {
                    val pupil = Offset(
                        center.x + lookAnimated * (if (compact) 2.dp.toPx() else 4.dp.toPx()),
                        center.y
                    )
                    drawCircle(color = Color(0xFF111111), radius = pupilR, center = pupil)
                    drawCircle(
                        color = Color.White,
                        radius = pupilR * 0.3f,
                        center = pupil + Offset(-pupilR * 0.35f, -pupilR * 0.35f)
                    )
                }
            }
        }
    }
}
