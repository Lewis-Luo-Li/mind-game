package com.idroid.stuido.mind_game.feature.game.engine

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.generator.SudokuGenerator
import com.idroid.stuido.mind_game.feature.game.GameMode

/**
 * 对局工厂：把 `core:logic` 的生成能力封装成 UI 层友好的"开局素材工厂"。
 *
 * - 自由模式：依据 (spec, difficulty) 取挖空数**随机**生成；
 * - 每日挑战：依据 (spec, difficulty, seed) 以**固定种子**生成，保证同日题目一致、可复现；
 *
 * 构造参数均可注入，便于在单测中替换为确定性实现。
 * ⚠️ 内部会调用 [SudokuGenerator]（CPU 密集、回溯+校验），务必在**非主线程**执行。
 */
class GameFactory(
    private val generator: SudokuGenerator = SudokuGenerator(),
    private val difficultyConfig: DifficultyConfig = DifficultyConfig.default(),
) {

    /**
     * 生成一盘指定玩法的初始素材。
     *
     * @param mode   游戏玩法（自由 or 每日）。
     * @param spec   棋盘规格（4x4 / 6x6 / 9x9）。
     * @param difficulty 期望难度；当该 (spec, difficulty) 不可用（如 4x4 EXPERT）时抛异常。
     * @param seed   每日挑战的固定种子；自由模式下可为 null（使用随机源）。
     * @throws IllegalArgumentException 当该规格不支持所请求难度时。
     * @throws IllegalStateException    当生成器在重试后仍无法产出目标挖空数时。
     */
    fun newGame(
        mode: GameMode,
        spec: BoardSpec,
        difficulty: Difficulty,
        seed: Long? = null,
    ): GameSeed {
        val holes = difficultyConfig.holeCount(spec, difficulty)
            ?: throw IllegalArgumentException("$spec does not support difficulty $difficulty")

        val effectiveSeed = when (mode) {
            GameMode.DAILY -> seed ?: throw IllegalArgumentException("Daily challenge requires today's seed")
            GameMode.FREE -> seed // 自由模式可带或不带种子（带则一致性，不带则随机）
        }

        val boardConfig = generator.generate(
            size = spec.size,
            boxWidth = spec.boxWidth,
            boxHeight = spec.boxHeight,
            holes = holes,
            seed = effectiveSeed,
        )
        return GameSeed.from(boardConfig)
    }
}
