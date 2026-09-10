package com.idroid.stuido.mind_game.core.ui.theme

/**
 * 用户可选的「主题风格」（外观亮度）。
 */
enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

/**
 * 用户可选的「主题色 / 品牌色板」。
 *
 * 在「外观」设置页里，用户既能切换亮度（SYSTEM/LIGHT/DARK），
 * 也能在下面几套强调色之间切换——真正的「3 个自定义主题」。
 */
enum class AppThemeScheme(val label: String) {
    LILAC("Lilac"),     // 默认紫调（现有品牌色）
    OCEAN("Ocean"),     // 自定义 1：宁静蓝
    FOREST("Forest"),   // 自定义 2：松林绿
    SUNSET("Sunset"),   // 自定义 3：落日橙
}
