package com.idroid.stuido.mind_game.main.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.R
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBar
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiConfig
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiStyle
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.main.model.GameEntry

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    games: List<GameEntry>,
    onGameClick: (GameEntry) -> Unit,
    contentPadding: PaddingValues = PaddingValues(16.dp)
){
    val listState = rememberLazyListState()
    val topBarUiContent = TopBarUiContent(
        onBackClick = {},
        onSettingsClick = {},
        topBarTitle = stringResource(R.string.home_title),
        backButtonUiContent = IconButtonUiContent(
            icon = Theme.icons.back,
            contentDescription = "Back",
            onClick = {},
        ),
        settingsButtonUiContent = IconButtonUiContent(
            icon = Theme.icons.settings,
            contentDescription = "Settings",
            onClick = {},
        ),
    )
    val iconButtonStyle = IconButtonStyle(
        surfaceColor = MaterialTheme.colorScheme.surface,
        iconTintColor = MaterialTheme.colorScheme.onSurface,
        surfaceSize = 48.dp,
        iconSize = 24.dp
    )
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopBar(
            uiContent = topBarUiContent,
            uiConfig = TopBarUiConfig(),
            uiStyle = TopBarUiStyle(
                topbarBgColor = MaterialTheme.colorScheme.background,
                titleTextColor = MaterialTheme.colorScheme.onBackground,
                backButtonStyle = iconButtonStyle,
                settingsButtonStyle = iconButtonStyle
            )
        )
        // 系统栏内边距由上层（MindGameApp 的 safeDrawing 容器）统一处理，这里只做视觉间距。
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = games,
                key = { it.id }
            ) { entry ->
                GameCard(entry = entry, onClick = onGameClick)
            }
        }
    }

}
