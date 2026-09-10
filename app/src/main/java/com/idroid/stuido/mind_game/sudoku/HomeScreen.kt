package com.idroid.stuido.mind_game.sudoku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.R
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.feature.game.GameMode
import java.time.LocalDate

/**
 * 主菜单：选玩法（自由/每日）→ 规格 → 难度 → 开始。
 *
 * - 自由模式：随机生成、难度可选；
 * - 每日挑战：当日固定种子，难度自动取该规格可支持的最难一档（同日题目一致、可复现）。
 */
@Composable
internal fun HomeScreen(
    onOpenSettings: () -> Unit,
    onStart: (mode: GameMode, spec: BoardSpec, difficulty: Difficulty, seed: Long?) -> Unit,
) {
    val config = remember { DifficultyConfig.default() }

    var mode by rememberSaveable { mutableStateOf(GameMode.FREE) }
    var spec by rememberSaveable { mutableStateOf(BoardSpec.CLASSIC) }
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }

    val availableDiff = remember(config, spec) { config.availableDifficulties(spec) }

    // 换规格后若原所选难度不可再选，则自动校正
    if (availableDiff.isNotEmpty() && difficulty !in availableDiff) {
        difficulty = availableDiff.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium)

        SectionCard(stringResource(R.string.section_mode)) {
            SingleChoiceChips(
                labelsToValues = listOf(
                    stringResource(R.string.home_free) to GameMode.FREE,
                    stringResource(R.string.home_daily) to GameMode.DAILY,
                ),
                selected = mode,
                onSelect = { mode = it },
            )
        }

        SectionCard(stringResource(R.string.section_board_size)) {
            SingleChoiceChips(
                labelsToValues = listOf(
                    stringResource(R.string.board_x4) to BoardSpec.MINI,
                    stringResource(R.string.board_x6) to BoardSpec.MEDIUM,
                    stringResource(R.string.board_x9) to BoardSpec.CLASSIC,
                ),
                selected = spec,
                onSelect = { spec = it },
            )
        }

        if (mode == GameMode.FREE) {
            SectionCard(stringResource(R.string.section_difficulty)) {
                SingleChoiceChips(
                    labelsToValues = Difficulty.entries
                        .filter { it in availableDiff }
                        .map { it.name to it },
                    selected = difficulty,
                    onSelect = { difficulty = it },
                )
            }
        } else {
            val hardest = availableDiff.maxByOrNull { it.ordinal } ?: Difficulty.EASY
            Text(
                stringResource(R.string.home_daily_note, hardest),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = {
                val finalDifficulty = when (mode) {
                    GameMode.DAILY -> availableDiff.maxByOrNull { it.ordinal } ?: Difficulty.EASY
                    GameMode.FREE -> difficulty
                }
                val seed = if (mode == GameMode.DAILY) DailySeed.forToday() else null
                onStart(mode, spec, finalDifficulty, seed)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (mode == GameMode.DAILY) stringResource(R.string.button_start_daily)
                else stringResource(R.string.button_start_free),
            )
        }

        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_settings))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

/** 单选 Chip；顺序由传入列表决定。 */
@Composable
private fun <T> SingleChoiceChips(
    labelsToValues: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labelsToValues.forEach { (label, value) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

private object DailySeed {
    /** 当天自 1970 起的天数 —— 同设备同日期固定种子。 */
    fun forToday(): Long = LocalDate.now().toEpochDay()
}
