package com.idroid.stuido.mind_game.core.ui.components.modecard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.util.isHidden

@Composable
fun ModeCard(
    modifier: Modifier = Modifier,
    uiContent: ModeCardUiContent,
    uiConfig: ModeCardConfig,
    uiStyle: ModeCardStyle,
    content: @Composable ColumnScope.() -> Unit = {},
    action: @Composable () -> Unit,
) {
    if (uiConfig.visibility.isHidden()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = uiStyle.shape,
        color = uiStyle.containerColor,
        contentColor = uiStyle.contentColor,
        border = uiStyle.border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(uiStyle.contentPadding),
            verticalArrangement = Arrangement.spacedBy(uiStyle.sectionSpacing),
        ) {
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = uiContent.icon,
                    contentDescription = uiContent.iconContentDescription,
                    tint = uiStyle.iconTint,
                    modifier = Modifier.size(uiStyle.iconSize),
                )
                Spacer(Modifier.width(uiStyle.headerSpacing))
                Column(modifier = Modifier.weight(1f)) {
                    BaseText(
                        text = AnnotatedString(uiContent.title),
                        color = uiStyle.contentColor,
                        style = uiStyle.titleStyle,
                        config = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
                    )
                    uiContent.subtitle?.let { subtitle ->
                        BaseText(
                            text = AnnotatedString(subtitle),
                            color = uiStyle.secondaryContentColor,
                            style = uiStyle.subtitleStyle,
                            config = TextConfig(maxLine = 2, overflow = TextOverflow.Ellipsis),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(uiStyle.sectionSpacing / 2),
                content = content,
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                action()
            }
        }
    }
}
