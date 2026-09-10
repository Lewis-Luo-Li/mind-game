package com.idroid.stuido.mind_game.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeScheme
import com.idroid.stuido.mind_game.core.ui.theme.schemeFor

/**
 * 设置页（Appearance / 主题选择）。
 *
 * 用户可在两处自定义：
 *  - Theme（外观亮度）: System / Light / Dark
 *  - Accent color（3 个自定义主题 + 默认紫调）: Lilac / Ocean / Forest / Sunset
 *
 * 所有选择即时写回 [themePrefs]，整个 App 会立刻换肤。
 */
@Composable
fun SettingsScreen(
    themePrefs: ThemePrefs,
    onBack: () -> Unit,
) {
    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("Back") }
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
            }
            Text(
                "Appearance & theme colors. Live preview on the Sudoku board.",
                style = MaterialTheme.typography.bodyMedium,
            )

            SectionCard("Theme (Light / Dark)") {
                FilterChipList(
                    labelsToValues = AppThemeMode.entries.map { it.label to it },
                    selected = themePrefs.mode,
                    onSelect = themePrefs::setMode,
                )
            }

            SectionCard("Accent color") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeScheme.entries.forEach { scheme ->
                        AccentRow(
                            label = scheme.label,
                            scheme = scheme,
                            dark = themePrefs.mode != AppThemeMode.LIGHT,
                            selected = scheme == themePrefs.scheme,
                            onClick = { themePrefs.setScheme(scheme) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

@Composable
private fun <T> FilterChipList(
    labelsToValues: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labelsToValues.forEach { (label, value) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

/** 一行「主题色预览圆点 + 名称 + 选中态」，让用户一眼看出颜色。 */
@Composable
private fun AccentRow(
    label: String,
    scheme: AppThemeScheme,
    dark: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val cs = schemeFor(scheme, dark)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(cs.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(cs.primaryContainer, CircleShape),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
        FilterChip(selected = selected, onClick = onClick, label = { Text(if (selected) "On" else "Off") })
    }
}
