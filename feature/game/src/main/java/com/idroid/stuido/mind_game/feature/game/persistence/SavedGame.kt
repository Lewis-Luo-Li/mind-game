package com.idroid.stuido.mind_game.feature.game.persistence

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.RewardsSection
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells
import com.idroid.stuido.mind_game.feature.game.mvi.GameConfig
import com.idroid.stuido.mind_game.feature.game.mvi.GameProgress
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import com.idroid.stuido.mind_game.feature.game.mvi.Selection
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs

/**
 * 存档格式版本。**改动 [SavedGame] 字段语义时必须 +1**：版本不符的旧存档会被
 * [SavedGame.toRestore] 直接判废（宁可丢一局，也不要把错位的数字铺到棋盘上）。
 *
 * 新增字段只要带默认值即可向后兼容，无需升版本。
 */
const val SAVE_FORMAT_VERSION: Int = 1

/** 仅计时器在动时，最多隔这么久才落盘一次，避免每秒写盘。 */
const val TIMER_SAVE_INTERVAL_SECONDS: Int = 5

/**
 * 「一局未完成对局的存档」，与 [GameUiState] 解耦的扁平淡类型。
 *
 * 刻意不复用领域模型直接序列化：
 *  - 存档格式需要**独立演进**，domain 改字段名不该悄悄让玩家丢局；
 *  - 只存恢复必需的字段——撤销历史（最多 100 份快照）、conflicts 高亮、
 *    派生出的 `hintsTotal` 等一律不落盘。
 *
 * 所有字段带默认值，缺字段的旧存档仍可解码。
 */
@Serializable
data class SavedGame(
    val version: Int = SAVE_FORMAT_VERSION,
    /** 与宿主 `PlayRequest.id` 对应：恢复时复用它作为 ViewModel key，避免重复建局。 */
    val requestId: Long = 0L,
    /** 每日挑战所属期（ISO-8601）；非每日模式为 null。 */
    val puzzleDay: String? = null,

    // ---------------- 对局配置 ----------------
    val mode: String = GameMode.FREE.name,
    val spec: String = BoardSpec.CLASSIC.name,
    val difficulty: String = Difficulty.EASY.name,
    val seed: Long? = null,
    val maxMistakes: Int = 3,
    val maxHints: Int = 3,
    val level: Int? = null,
    val rewards: RewardsSection = RewardsSection(),

    // ---------------- 盘面 ----------------
    val size: Int = 0,
    val values: List<Int> = emptyList(),
    val solution: List<Int> = emptyList(),
    val givens: List<Int> = emptyList(),
    val notes: Map<Int, List<Int>> = emptyMap(),

    // ---------------- 进度 ----------------
    val status: String = GameStatus.IDLE.name,
    val mistakes: Int = 0,
    val timerSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val bonusHints: Int = 0,
    val bonusLives: Int = 0,
    val hintRewardsUsed: Int = 0,
    val lifeRewardsUsed: Int = 0,
    val gameInstance: Int = 0,

    // ---------------- 选中态 ----------------
    val selectedIndex: Int? = null,
    val inputMode: String = InputMode.NUMBER.name,
)

/** 把当前 UI State 摊平成可落盘的存档。 */
fun GameUiState.toSavedGame(requestId: Long, puzzleDay: String? = null): SavedGame = SavedGame(
    requestId = requestId,
    puzzleDay = puzzleDay,
    mode = config.mode.name,
    spec = config.spec.name,
    difficulty = config.difficulty.name,
    seed = config.seed,
    maxMistakes = config.maxMistakes,
    maxHints = config.maxHints,
    level = config.level,
    rewards = config.rewards,
    size = board.size,
    values = board.values,
    solution = board.solution,
    givens = board.givens.sorted(),
    // 空笔记集合不落盘：绝大多数格子没有笔记，省下大量无意义键。
    notes = board.notes
        .filterValues { it.isNotEmpty() }
        .mapValues { (_, digits) -> digits.sorted() },
    status = progress.status.name,
    mistakes = progress.mistakes,
    timerSeconds = progress.timerSeconds,
    hintsUsed = progress.hintsUsed,
    bonusHints = progress.bonusHints,
    bonusLives = progress.bonusLives,
    hintRewardsUsed = progress.hintRewardsUsed,
    lifeRewardsUsed = progress.lifeRewardsUsed,
    gameInstance = progress.gameInstance,
    selectedIndex = selection.index,
    inputMode = selection.inputMode.name,
)

/**
 * 还原为可直接铺进 [GameUiState] 的对局。
 *
 * 任何一处不可信都返回 null（调用方据此丢弃存档并回主菜单）：
 *  - 版本不符 / 枚举值解析失败；
 *  - 状态不是「进行中」或「暂停」——已结束的局没有恢复意义；
 *  - 盘面尺寸、取值、给定格自相矛盾；
 *  - 盘面其实已填满终盘（终局落盘前进程被杀留下的残档）。
 */
fun SavedGame.toRestore(): GameUiState? {
    if (version != SAVE_FORMAT_VERSION) return null

    val status = enumOrNull<GameStatus>(status) ?: return null
    if (status != GameStatus.RUNNING && status != GameStatus.PAUSED) return null

    val mode = enumOrNull<GameMode>(mode) ?: return null
    val spec = enumOrNull<BoardSpec>(spec) ?: return null
    val difficulty = enumOrNull<Difficulty>(difficulty) ?: return null
    val inputMode = enumOrNull<InputMode>(inputMode) ?: InputMode.NUMBER

    if (size <= 0) return null
    if (values.size != size * size || solution.size != size * size) return null
    if (values.any { it !in 0..size }) return null
    if (solution.any { it !in 1..size }) return null
    if (givens.any { it !in values.indices }) return null
    // 给定格永远等于终盘值；不等说明存档被改坏或来自不兼容的版本。
    if (givens.any { values[it] != solution[it] }) return null
    // 已经填满终盘 = 这局其实结束了（终局落盘前的残档），没有可续的进度。
    if (values.indices.all { values[it] == solution[it] }) return null

    val sanitizedNotes = notes
        .filterKeys { it in values.indices && values[it] == 0 && it !in givens }
        .mapValues { (_, digits) -> digits.filter { it in 1..size }.toSet() }
        .filterValues { it.isNotEmpty() }

    return GameUiState(
        config = GameConfig(
            mode = mode,
            spec = spec,
            difficulty = difficulty,
            seed = seed,
            maxMistakes = maxMistakes.coerceAtLeast(0),
            maxHints = maxHints.coerceAtLeast(0),
            rewards = rewards,
            level = level,
        ),
        board = BoardCells(
            size = size,
            values = values,
            givens = givens.toSet(),
            solution = solution,
            notes = sanitizedNotes,
        ),
        selection = Selection(
            index = selectedIndex?.takeIf { it in values.indices },
            conflicts = emptySet(),
            inputMode = inputMode,
        ),
        progress = GameProgress(
            status = status,
            mistakes = mistakes.coerceAtLeast(0),
            timerSeconds = timerSeconds.coerceAtLeast(0),
            isBusy = false,
            hintsUsed = hintsUsed.coerceAtLeast(0),
            outcome = null,
            bonusHints = bonusHints.coerceAtLeast(0),
            bonusLives = bonusLives.coerceAtLeast(0),
            hintRewardsUsed = hintRewardsUsed.coerceAtLeast(0),
            lifeRewardsUsed = lifeRewardsUsed.coerceAtLeast(0),
            gameInstance = gameInstance.coerceAtLeast(0),
        ),
        history = emptyList(),
    )
}

/**
 * 是否需要把 [next] 落盘。
 *
 * 计时器每秒都在变，没必要每秒写盘：只有**计时之外**的变化（落子/笔记/擦除/
 * 错误数/提示数…）才立即落盘；纯计时变化每累计 [TIMER_SAVE_INTERVAL_SECONDS] 秒补一次。
 * 这样即使进程被杀，最多丢几秒计时，绝不会丢玩家刚落的子。
 */
fun shouldPersist(
    previous: GameUiState?,
    next: GameUiState,
    timerSaveInterval: Int = TIMER_SAVE_INTERVAL_SECONDS,
): Boolean {
    val status = next.progress.status
    if (status != GameStatus.RUNNING && status != GameStatus.PAUSED) return false
    if (next.board.size <= 0) return false
    previous ?: return true

    val nonTimerChanged = next.board != previous.board ||
        next.selection != previous.selection ||
        next.progress.copy(timerSeconds = previous.progress.timerSeconds) != previous.progress
    if (nonTimerChanged) return true

    return abs(next.progress.timerSeconds - previous.progress.timerSeconds) >= timerSaveInterval
}

/** JSON 编解码。解析失败一律返回 null——存档不可信时按「没有存档」处理。 */
object GameSaveCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(saved: SavedGame): String = json.encodeToString(SavedGame.serializer(), saved)

    fun decode(raw: String?): SavedGame? {
        if (raw.isNullOrBlank()) return null
        return runCatching { json.decodeFromString(SavedGame.serializer(), raw) }.getOrNull()
    }
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
    runCatching { enumValueOf<T>(name) }.getOrNull()
