package com.idroid.stuido.mind_game.feature.game

/**
 * 玩法模式。
 *
 *  - [FREE] 自由模式：玩家自选规格与难度，服务器/本地随机生成，题目不可预测。
 *  - [DAILY] 每日挑战：使用"今日固定种子"生成，同一天内题目一致且可跨设备复现。
 */
enum class GameMode {
    FREE,
    DAILY,
}
