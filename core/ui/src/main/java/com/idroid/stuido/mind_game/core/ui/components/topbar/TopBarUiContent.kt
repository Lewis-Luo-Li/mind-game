package com.idroid.stuido.mind_game.core.ui.components.topbar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class TopBarUiContent(
    val onBackClick: (() -> Unit)? = null,
    val onSettingsClick: (() -> Unit)? = null,
    val topBarTitle: String,
    val settingsButtonUiContent: IconButtonUiContent,
    val backButtonUiContent: IconButtonUiContent
)

@Immutable
data class TopBarUiConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val titleConfig: TextConfig = TextConfig(),
    val backButtonConfig: IconButtonConfig = IconButtonConfig(),
    val settingsButtonConfig: IconButtonConfig = IconButtonConfig(),
)

@Immutable
data class TopBarUiStyle(
    val topbarBgColor: Color,
    val titleTextColor: Color,
    val backButtonStyle: IconButtonStyle,
    val settingsButtonStyle: IconButtonStyle,

)