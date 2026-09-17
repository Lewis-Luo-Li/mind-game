package com.idroid.stuido.mind_game.mainpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.main.model.GameEntry

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    games: List<GameEntry>,
    onGameClick: (GameEntry) -> Unit,
    contentPadding: PaddingValues = PaddingValues(16.dp)
){
    val listState = rememberLazyListState()

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
