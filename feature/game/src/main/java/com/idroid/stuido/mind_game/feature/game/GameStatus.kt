package com.idroid.stuido.mind_game.feature.game

/**
 * 一局游戏的进行状态。
 *
 *  - [IDLE]     尚未开始（仅初始占位）。
 *  - [RUNNING]  正在游玩且计时中。
 *  - [PAUSED]   暂停（计时停止，棋盘被覆盖/隐藏）。
 *  - [FINISHED] 已完成（成功解出，或错误用尽而失败）。
 */
enum class GameStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED,
}
