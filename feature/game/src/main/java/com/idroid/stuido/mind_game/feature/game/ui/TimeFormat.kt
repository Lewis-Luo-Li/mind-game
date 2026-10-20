package com.idroid.stuido.mind_game.feature.game.ui

import java.util.Locale

/**
 * 对局计时文本（对齐 docs/sudoku_ui.jpeg 状态行的 "04:18"）：
 * 不足一小时显示 `MM:SS`，达到一小时后显示 `H:MM:SS`；负值按 0 处理。
 */
fun formatTimer(totalSeconds: Int): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", m, s)
    }
}
