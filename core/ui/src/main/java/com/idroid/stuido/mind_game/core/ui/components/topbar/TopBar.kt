package com.idroid.stuido.mind_game.core.ui.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.button.IconButton
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.core.ui.util.isHidden
import com.idroid.stuido.mind_game.core.ui.util.isVisible

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    uiContent: TopBarUiContent,
    uiConfig: TopBarUiConfig,
    uiStyle: TopBarUiStyle
) {
    if (uiConfig.visibility.isHidden()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(uiStyle.topbarBgColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        if (uiConfig.backButtonConfig.visibility.isVisible()) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                uiContent = uiContent.backButtonUiContent,
                uiConfig = uiConfig.backButtonConfig,
                uiStyle = uiStyle.backButtonStyle
            )
        }

        if (uiConfig.titleConfig.visibility.isVisible()) {
            // 标题按可见端按钮取较大的对称留白：文本始终居中于整条栏，且不压到按钮
            // （配合 titleConfig 单行省略；单侧隐藏时也不会偏移）。
            val titlePad = maxOf(
                if (uiConfig.backButtonConfig.visibility.isVisible()) {
                    uiStyle.backButtonStyle.surfaceSize
                } else {
                    0.dp
                },
                if (uiConfig.settingsButtonConfig.visibility.isVisible()) {
                    uiStyle.settingsButtonStyle.surfaceSize
                } else {
                    0.dp
                },
            ) + 4.dp
            BaseText(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = titlePad),
                text = AnnotatedString(uiContent.topBarTitle),
                style = Theme.typography.titleStyleSemiBold,
                config = uiConfig.titleConfig,
                color = uiStyle.titleTextColor
            )
        }

        if (uiConfig.settingsButtonConfig.visibility.isVisible()) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                uiContent = uiContent.settingsButtonUiContent,
                uiConfig = uiConfig.settingsButtonConfig,
                uiStyle = uiStyle.settingsButtonStyle
            )
        }
    }
}