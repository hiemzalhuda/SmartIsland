/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.data.SmartIslandSettings

/**
 * Invisible touch target over the pill's spot while the pill is hidden, so tapping the area reveals the pill (auto-
 * hide), brings back put-away idle cards, or opens the shortcuts. Fork hiemz: while the round idle dot is active a
 * tap on it opens the widget island instead (see [IdleDotVisual] for the dot itself).
 */
@Composable
internal fun HiddenPillTouchTarget(
    settings: SmartIslandSettings,
    layout: IslandLayout,
    cb: IslandCallbacks,
    ui: IslandInteractionState
) {
    if (layout.isHiding && !cb.currentExpanded) {
        // Keyed on dotActive: the pill can already be hidden (auto-hide / hide-when-idle) when the dot's 10 s
        // wait finishes, so the tap handler must pick up the fresh value instead of the one from first composition.
        val dotActive = settings.roundIdleDot && ui.isIdleDot
        Box(
            modifier = Modifier
                .width(settings.width.dp)
                .height(settings.height.dp)
                .graphicsLayer {
                    translationX = layout.collapsedMainOffset.toPx()
                }
                .pointerInput(dotActive) {
                    detectTapGestures {
                        if (dotActive) {
                            // Open the widget island when it has something to show; otherwise just
                            // bring the pill back (e.g. idle widgets off, weather not loaded yet).
                            val frame = ui.idleFrameNow
                            val data = frame?.data
                            val opensIsland = frame != null && data != null &&
                                com.agupta07505.smartisland.model.WidgetIslandContent.hasContent(
                                    kind = frame.kind,
                                    charging = data.charging,
                                    nextAlarmMs = data.nextAlarmMs,
                                    nowMs = System.currentTimeMillis(),
                                    hasWeather = data.weather != null,
                                    notificationCount = data.notificationSummary.total,
                                    modeActive = data.mode.active
                                )
                            if (opensIsland) {
                                ui.onWidgetGlyphTap?.invoke()
                            } else {
                                ui.isIdleDot = false
                                ui.touch()
                            }
                        } else if (settings.autoHidePill && ui.isAutoHidden) {
                            ui.isAutoHidden = false
                            ui.touch()
                        } else if (cb.currentNotifications.isEmpty() && cb.currentHasIdleStoredCards) {
                            cb.currentOnRevealIdleCards()
                        } else if (cb.currentNotifications.isNotEmpty()) {
                            cb.currentOnToggle()
                        }
                    }
                }
        )
    }
}

/** Fork hiemz: the round idle dot itself, at the pill's spot. */
@Composable
internal fun IdleDotVisual(
    settings: SmartIslandSettings,
    layout: IslandLayout,
    ui: IslandInteractionState
) {
    // Deliberately NOT gated by isHiding: the dot must stay composed while it fades/scales out, or its exit
    // animation would be cut off when the pill comes back. The pill's own ~220 ms fade is the timing to match.
    val dotActive = settings.roundIdleDot && ui.isIdleDot
    AnimatedVisibility(
        visible = dotActive,
        enter = fadeIn(animationSpec = tween(DOT_ANIM_MS, easing = FastOutSlowInEasing)) +
            scaleIn(initialScale = 0.5f, animationSpec = tween(DOT_ANIM_MS, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(DOT_ANIM_MS, easing = FastOutSlowInEasing)) +
            scaleOut(targetScale = 0.5f, animationSpec = tween(DOT_ANIM_MS, easing = FastOutSlowInEasing))
    ) {
        Box(
            modifier = Modifier
                .width(settings.width.dp)
                .height(settings.height.dp)
                .graphicsLayer {
                    translationX = layout.collapsedMainOffset.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(settings.pillColor))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            )
        }
    }
}

private const val DOT_ANIM_MS = 220
