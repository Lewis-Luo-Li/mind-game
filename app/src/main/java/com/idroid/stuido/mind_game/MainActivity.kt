package com.idroid.stuido.mind_game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.idroid.stuido.mind_game.config.GameConfigRepository
import com.idroid.stuido.mind_game.sudoku.MindgameRoot
import com.idroid.stuido.mind_game.sudoku.MindGameApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 广告总开关：仅 ads.enabled=true 时初始化 SDK（GameRemoteConfig 默认关闭）。
        if (GameConfigRepository.fromAssets(this).config.ads.enabled) {
            MobileAds.initialize(this) {}
        }
        setContent {
            // 由 MindgameThemeApp 凭 ThemePrefs 应用用户选择的外观/色板。
            MindgameRoot { themePrefs ->
                MindGameApp(themePrefs = themePrefs)
            }
        }
    }
}
