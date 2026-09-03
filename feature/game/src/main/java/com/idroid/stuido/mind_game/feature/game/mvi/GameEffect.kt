package com.idroid.stuido.mind_game.feature.game.mvi

/**
 * UI Effect：由 ViewModel 触发的**一次性副作用**，经单向 Channel/SharedFlow
 * 推送给 View 进行一次性消费（导航、Toast、Snackbar、Haptic 等）。
 *
 * 与 [GameUiState]（长时间持有、可重建）区分开：Effect 发出即被消费、不保存到 State。
 */
sealed interface GameEffect {

    /** 生成新谜题失败，无法开局（携带原因）。 */
    data class NewGameFailed(val reason: String) : GameEffect

    /** 请求以算法自动填入"某个正确值"，UI 可据此聚焦/闪烁提示。 */
    data class HintFilled(val index: Int, val value: Int) : GameEffect

    /** 请求显示一段轻量提示（Snackbar / Toast）。 */
    data class ShowMessage(val message: String) : GameEffect

    /** 谜题成功完成（调用方据此播放动画 / 弹窗 / 进入结算）。 */
    data object PuzzleCompleted : GameEffect

    /** 误触错误达上限而失败结束。 */
    data object GameOver : GameEffect
}
