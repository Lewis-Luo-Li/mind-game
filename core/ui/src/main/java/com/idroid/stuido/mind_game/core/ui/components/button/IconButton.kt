package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.isHidden

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    uiContent: IconButtonUiContent,
    uiConfig: IconButtonConfig,
    uiStyle: IconButtonStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    Surface(
        modifier = modifier.size(uiStyle.surfaceSize),
        shape = uiStyle.shape,
        color = uiStyle.surfaceColor,
        shadowElevation = 3.dp,
        onClick = uiContent.onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = uiContent.icon,
                contentDescription = uiContent.contentDescription,
                tint = uiStyle.iconTintColor,
                modifier = Modifier.size(uiStyle.iconSize)
            )
        }
    }
}