package com.idroid.stuido.mind_game.core.logic.mode

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.config.LadderSection
import com.idroid.stuido.mind_game.core.logic.config.maxHoles
import com.idroid.stuido.mind_game.core.logic.generator.SplitMix64

/**
 * 天梯（Level N，从 1 开始）的关卡规划：关卡 → 难度 / 种子 / 挖空数。纯函数、确定性，
 * 所有玩家的第 N 关完全相同（为排行榜预留）。
 *
 * - 一轮 = cycle 中每档难度各 levelsPerTier 关（默认 4 × 5 = 20 关）。
 * - 早于 shuffleFromRound 的轮次按 Easy → Expert 顺序；之后每轮内确定性打乱，
 *   Expert 连续不超过 maxConsecutiveExpert，且打乱轮不以 Expert 开头/结尾，
 *   因此 Expert 连段不会跨轮拼接（第 1 轮以 5 个 Expert 结尾）。见 [roundOrder]。
 * - 每关种子为带标签的 64 位散列：同种子不同难度会得到同一终盘，故每关独立种子；
 *   与每日种子（epochDay + salt，小整数）分属不同命名空间。
 */
class LadderPlan(private val ladder: LadderSection) {

    val spec: BoardSpec = ladder.boardSpec()

    private val orderedRound: List<Difficulty> =
        ladder.cycleDifficulties().flatMap { tier -> List(ladder.levelsPerTier) { tier } }

    val roundSize: Int = orderedRound.size

    /** 第几轮（从 1 开始）。 */
    fun roundOf(level: Int): Int {
        requireLevel(level)
        return (level - 1) / roundSize + 1
    }

    /** 轮内位置（从 1 开始，1..roundSize）。 */
    fun positionInRound(level: Int): Int {
        requireLevel(level)
        return (level - 1) % roundSize + 1
    }

    fun difficultyFor(level: Int): Difficulty =
        roundOrder(roundOf(level))[positionInRound(level) - 1]

    fun seedFor(level: Int): Long {
        requireLevel(level)
        return SplitMix64.mix(LEVEL_SEED_TAG xor SplitMix64.mix(level.toLong())) + ladder.seedSalt
    }

    /** 该关挖空数：基础挖空 + 每轮递增量，封顶于唯一解最少提示数；规格/难度不可用时为 null。 */
    fun holesFor(level: Int, difficultyConfig: DifficultyConfig): Int? {
        val base = difficultyConfig.holeCount(spec, difficultyFor(level)) ?: return null
        val extra = ladder.holesStepPerRound.toLong() * (roundOf(level) - 1)
        return (base + extra).coerceAtMost(maxHoles(spec).toLong()).toInt()
    }

    /**
     * 第 [round] 轮的难度序列（长度 [roundSize]）。打乱轮：非 Expert 关随机排列，
     * 再把 Expert 随机分进相邻非 Expert 之间的空隙（每个空隙至多 maxConsecutiveExpert 个），
     * 因而首尾一定不是 Expert。约束无法满足时（如 Expert 太多）该轮保持顺序。
     */
    fun roundOrder(round: Int): List<Difficulty> {
        require(round >= 1) { "round must be >= 1" }
        if (round < ladder.shuffleFromRound || !shuffleFeasible) return orderedRound

        val random = SplitMix64(SplitMix64.mix(SHUFFLE_SEED_TAG xor SplitMix64.mix(round.toLong())) + ladder.seedSalt)
        val others = orderedRound.filter { it != Difficulty.EXPERT }.shuffled(random)
        val expertsPerGap = IntArray(others.size - 1)
        repeat(expertCount) {
            val open = expertsPerGap.indices.filter { expertsPerGap[it] < ladder.maxConsecutiveExpert }
            expertsPerGap[open[random.nextInt(open.size)]]++
        }
        return buildList {
            others.forEachIndexed { index, difficulty ->
                add(difficulty)
                if (index < expertsPerGap.size) repeat(expertsPerGap[index]) { add(Difficulty.EXPERT) }
            }
        }
    }

    private val expertCount = orderedRound.count { it == Difficulty.EXPERT }

    private val shuffleFeasible: Boolean = run {
        val gaps = roundSize - expertCount - 1
        gaps >= 1 && expertCount <= gaps.toLong() * ladder.maxConsecutiveExpert
    }

    private fun requireLevel(level: Int) = require(level >= 1) { "level must be >= 1" }

    private companion object {
        const val LEVEL_SEED_TAG = 0x4C41444445524C56L // "LADDERLV"
        const val SHUFFLE_SEED_TAG = 0x4C41444445525348L // "LADDERSH"
    }
}

private fun <T> List<T>.shuffled(random: SplitMix64): List<T> {
    val list = toMutableList()
    for (i in list.lastIndex downTo 1) {
        val j = random.nextInt(i + 1)
        val tmp = list[i]
        list[i] = list[j]
        list[j] = tmp
    }
    return list
}
