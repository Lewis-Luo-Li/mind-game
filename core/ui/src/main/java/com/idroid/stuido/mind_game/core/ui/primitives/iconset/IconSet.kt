package com.idroid.stuido.mind_game.core.ui.primitives.iconset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.idroid.stuido.mind_game.core.ui.R

@Immutable
data class IconSet(
    val back: ImageVector,
    val settings: ImageVector
)

@Composable
fun rememberDefaultIconSet(): IconSet {
    return IconSet(
        back = ImageVector.vectorResource(R.drawable.ic_back_arror),
        settings = ImageVector.vectorResource(R.drawable.ic_settings)
    )
}

val LocalIconSet = staticCompositionLocalOf<IconSet> {
    error("No IconSet provided, Wrap your root composable in AppTheme.\"")
}