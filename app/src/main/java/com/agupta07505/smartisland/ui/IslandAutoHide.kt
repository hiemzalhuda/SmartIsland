/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.model.IslandNotification

/**
 * Fork hiemz round idle dot: once the island has sat idle (no cards, collapsed, widget island closed) for
 * [IDLE_DOT_TIMEOUT_MS] with the setting on, the pill shrinks to a round dot ([IslandInteractionState.isIdleDot]).
 * Any card, expansion, widget island or touch restarts the wait; the dot itself is drawn by
 * [HiddenPillTouchTarget], and tapping it opens the widget island.
 */
@Composable
internal fun IdleDotEffects(
    expanded: Boolean,
    notifications: List<IslandNotification>,
    settings: SmartIslandSettings,
    ui: IslandInteractionState
) {
    LaunchedEffect(
        expanded, settings.roundIdleDot, ui.widgetIslandOpen,
        notifications.isEmpty(), ui.userInteractionTimestamp
    ) {
        if (expanded || ui.widgetIslandOpen || !settings.roundIdleDot || notifications.isNotEmpty()) {
            ui.isIdleDot = false
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(IDLE_DOT_TIMEOUT_MS)
        ui.isIdleDot = true
    }
}

private const val IDLE_DOT_TIMEOUT_MS = 10_000L

/**
 * Auto-hide of the collapsed pill: any change of cards or selection counts as a touch, and when the pill stays
 * collapsed for the configured time with auto-hide on, [IslandInteractionState.isAutoHidden] turns on.
 */
@Composable
internal fun AutoHideEffects(
    expanded: Boolean,
    notifications: List<IslandNotification>,
    selectedIndex: Int,
    settings: SmartIslandSettings,
    ui: IslandInteractionState
) {
    LaunchedEffect(notifications.map { it.key }, selectedIndex) {
        ui.isAutoHidden = false
        ui.touch()
    }

    LaunchedEffect(expanded, settings.autoHidePill, settings.autoHideTimeoutSeconds, ui.userInteractionTimestamp, ui.widgetIslandOpen) {
        // An open widget island has its own close timer: the pill underneath stays until the island is gone.
        if (expanded || ui.widgetIslandOpen || !settings.autoHidePill) {
            ui.isAutoHidden = false
            return@LaunchedEffect
        }
        val timeoutMs = (settings.autoHideTimeoutSeconds.coerceAtLeast(1) * 1000L)
        kotlinx.coroutines.delay(timeoutMs)
        ui.isAutoHidden = true
    }
}
