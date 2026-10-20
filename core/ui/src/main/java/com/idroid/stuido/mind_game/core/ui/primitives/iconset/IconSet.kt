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
    val settings: ImageVector,
    val undo: ImageVector,
    val notes: ImageVector,
    val erase: ImageVector,
    val hint: ImageVector,
    val timer: ImageVector,
    val error: ImageVector,
)

@Composable
fun rememberDefaultIconSet(): IconSet {
    return IconSet(
        back = ImageVector.vectorResource(R.drawable.ic_back_arror),
        settings = ImageVector.vectorResource(R.drawable.ic_settings),
        undo = ImageVector.vectorResource(R.drawable.ic_undo),
        notes = ImageVector.vectorResource(R.drawable.ic_notes),
        erase = ImageVector.vectorResource(R.drawable.ic_erase),
        hint = ImageVector.vectorResource(R.drawable.ic_hint),
        timer = ImageVector.vectorResource(R.drawable.ic_timer),
        error = ImageVector.vectorResource(R.drawable.ic_error),
    )
}

val LocalIconSet = staticCompositionLocalOf<IconSet> {
    error("No IconSet provided, Wrap your root composable in AppTheme.\"")
}