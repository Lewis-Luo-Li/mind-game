package com.idroid.stuido.mind_game.feature.game.mvi

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode

/**
 * UI Event（Intent）：由 View / 系统向 ViewModel 派发的**单向意图**。
 *
 * MVI 约定：所有用户操作、生命周期、计时 tick 均统一编码为 [GameEvent]，
 * 经 `GameViewModel.dispatch(...)` 进入，绝不反向携带数据到 UI。
 */
sealed interface GameEvent {

    // ---------- 初始化 / 启动 ----------

    /**
     * 根据参数开启新局。可选择 [mode]=[GameMode.DAILY] 时传入 [seed]（今日种子）。
     */
    data class NewGame(
        val mode: GameMode,
        val spec: BoardSpec,
        val difficulty: Difficulty = Difficulty.EASY,
        val seed: Long? = null,
    ) : GameEvent

    /** 重新开始当前对局（同参数再开一局）。 */
    data object Restart : GameEvent

    /** 返回上一级 / 退出当前对局。 */
    data object Exit : GameEvent

    // ---------- 选中 / 输入 ----------

    /** 选中指定格子（改变 selection；再次点击可取消选择）。 */
    data class SelectCell(val index: Int) : GameEvent

    /** 在当前选中格**输入实际值** [value]。 */
    data class InputNumber(val index: Int, val value: Int) : GameEvent

    /** 在当前选中格切换/添加/移除一个**笔记**候选 [value]。 */
    data class ToggleNote(val index: Int, val value: Int) : GameEvent

    /** 清除当前选中格（仅限允许改动的非给定格）的内容与笔记。 */
    data object Erase : GameEvent

    // ---------- 墨迹模式 ----------

    /** 在 [InputMode.NUMBER] 与 [InputMode.NOTE] 之间切换。 */
    data object ToggleNoteMode : GameEvent

    // ---------- 操作工具 ----------

    /** 使用一次提示：填入正确值到当前格，不增加错误。 */
    data object RequestHint : GameEvent

    /** 请求核对当前已填数值是否与终盘一致（同步提示冲突）。 */
    data object RequestCheck : GameEvent

    /** 自动求解并铺满（演示用途）：成功后进入 FINISHED。 */
    data object RequestSolve : GameEvent

    // ---------- 计时 / 暂停 ----------

    /** 每秒触发的节拍，驱动计时器增加。仅当 progress.status == RUNNING 时生效。 */
    data class Tick(val elapsedSeconds: Int) : GameEvent

    /** 暂停 / 继续切换。 */
    data object TogglePause : GameEvent
}
