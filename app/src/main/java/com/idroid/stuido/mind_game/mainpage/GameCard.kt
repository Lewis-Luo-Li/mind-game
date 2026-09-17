package com.idroid.stuido.mind_game.mainpage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.idroid.stuido.mind_game.core.ui.components.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.main.model.GameEntry

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    entry: GameEntry,
    onClick: (GameEntry) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        onClick = { onClick(entry) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1) 顶部图片
            Image(
                painter = painterResource(entry.iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
            // 2) 标题
            BaseText(
                text = AnnotatedString(stringResource(entry.titleRes)),
                style = Theme.typography.bodyStyleSemiBold,
            )
            // 3) 描述
            BaseText(
                text = AnnotatedString(stringResource(entry.descriptionRes)),
                style = Theme.typography.bodyStyleNormal,
            )
        }
    }

}