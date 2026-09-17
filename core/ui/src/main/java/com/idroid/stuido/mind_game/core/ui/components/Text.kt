package com.idroid.stuido.mind_game.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.core.ui.util.isHidden

@Composable
fun BaseText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    style: TextStyle = Theme.typography.titleStyleSemiBold,
    config: TextConfig = TextConfig(),
    color: Color = Color.Unspecified
) {
    if (config.visibility.isHidden()) return

    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color,
        maxLines = config.maxLine,
        overflow = config.overflow
    )
}