package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.GameRules
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.persistence.SavedGame
import com.idroid.stuido.mind_game.feature.game.persistence.toRestore
import java.time.LocalDate

/**
 * 一次"开始游玩"的完整请求，由主菜单产出、交由 GameRoute 开局。
 *
 * 用 [encode] 存成字符串放进 `rememberSaveable`：旋转屏幕与进程重建后仍是同一局
 * （[id] 不变 → 同一个 ViewModel key），不会因为组合重建而重新发牌。
 */
data class PlayRequest(
    val id: Long,                 // 每次开始唯一，用来强制新建 GameViewModel（状态隔离）
    val mode: GameMode,
    val spec: BoardSpec,
    val difficulty: Difficulty,
    val seed: Long?,
    val rules: GameRules,
    /** 每日挑战所属期（开局时确定；跨过换题时刻再解出仍记入这一期）。 */
    val puzzleDay: LocalDate? = null,
    /** 天梯关卡号。 */
    val level: Int? = null,
    /** 覆盖该规格/难度的挖空数（天梯 holes_step_per_round 逐轮加难）。 */
    val holes: Int? = null,
)

/** 字段分隔符。只用于内部编解码，不出现在任何玩家可见文本里。 */
private const val PLAY_REQUEST_SEP = "|"

/** 编码为可 `rememberSaveable` 的稳定字符串。 */
internal fun PlayRequest.encode(): String = listOf(
    id.toString(),
    mode.name,
    spec.name,
    difficulty.name,
    seed?.toString() ?: "",
    rules.lives.toString(),
    rules.hints.toString(),
    puzzleDay?.toString() ?: "",
    level?.toString() ?: "",
    holes?.toString() ?: "",
).joinToString(PLAY_REQUEST_SEP)

/** [encode] 的逆过程；空串或损坏内容返回 null（按「不在对局中」处理）。 */
internal fun decodePlayRequest(raw: String): PlayRequest? {
    if (raw.isEmpty()) return null
    val fields = raw.split(PLAY_REQUEST_SEP)
    if (fields.size < 7) return null
    return runCatching {
        PlayRequest(
            id = fields[0].toLong(),
            mode = GameMode.valueOf(fields[1]),
            spec = BoardSpec.valueOf(fields[2]),
            difficulty = Difficulty.valueOf(fields[3]),
            seed = fields[4].toLongOrNull(),
            rules = GameRules(lives = fields[5].toInt(), hints = fields[6].toInt()),
            puzzleDay = fields.getOrNull(7)?.takeIf { it.isNotEmpty() }?.let(LocalDate::parse),
            level = fields.getOrNull(8)?.toIntOrNull(),
            holes = fields.getOrNull(9)?.toIntOrNull(),
        )
    }.getOrNull()
}

/**
 * 由存档还原开局请求。[requestId] 沿用存档里的值——恢复对局必须复用同一个
 * ViewModel key，否则会另起一局把存档再铺一次。
 */
internal fun SavedGame.toResumePlayRequest(): PlayRequest? {
    val config = toRestore()?.config ?: return null
    return PlayRequest(
        id = requestId,
        mode = config.mode,
        spec = config.spec,
        difficulty = config.difficulty,
        seed = config.seed,
        // 恢复不再需要生成，lives/hints 是 PlayRequest 里唯一被消费的规则字段。
        rules = GameRules(lives = config.maxMistakes, hints = config.maxHints),
        puzzleDay = puzzleDay?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        level = config.level,
    )
}
