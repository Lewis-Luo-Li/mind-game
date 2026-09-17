package com.idroid.stuido.mind_game.core.ui.components.text

import androidx.compose.ui.text.style.TextOverflow
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

data class TextConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val maxLine: Int = Int.MAX_VALUE,
    val overflow: TextOverflow = TextOverflow.Clip,
)
