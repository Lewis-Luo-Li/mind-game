# 主菜单（Main Screen）实现计划

> 目标：新增一个主菜单屏，用 **LazyColumn** 列出可玩游戏（首个为 Sudoku，其后为
> Crossword、Zooduku）。每个列表项包含三个 UI 组件：
> **顶部图片（从 drawable 资源加载矢量图）→ 标题文本 → 简短描述**。
> 点击任一项导航到对应的游戏页。

---

## 目录

1. [现状盘点与约束](#1-现状盘点与约束)
2. [推荐的文件结构](#2-推荐的文件结构)
3. [数据模型：GameCatalog](#3-数据模型gamecatalog)
4. [UIModel 与资源解析：`@Immutable` 与 Context](#4-uimodel-与资源解析immutable-与-context)
5. [矢量图资源：新增 drawable](#5-矢量图资源新增-drawable)
6. [文案资源：strings.xml](#6-文案资源stringsxml)
7. [列表项组件：GameCard](#7-列表项组件gamecard)
8. [列表屏：MainScreen（LazyColumn）](#8-列表屏mainscreenlazycolumn)
9. [导航接线：SudokuApp](#9-导航接线sudokuapp)
10. [导航方案选型](#10-导航方案选型)
11. [关于「从 drawable 加载 SVG」](#11-关于从-drawable-加载-svg)
12. [建议的构建顺序](#12-建议的构建顺序)
13. [风险与技术备注](#13-风险与技术备注)

---

## 1. 现状盘点与约束

### 模块结构
| 模块 | 职责 |
|---|---|
| `app` | 承载 `MainActivity` + 页内导航宿主 `SudokuApp`、主菜单屏 `mainpage/MainScreen.kt`、`GameCard.kt`，以及 Sudoku 开局入口 `sudoku/HomeScreen.kt` |
| `feature:game` | 玩法层库模块（`GameScreenRoute` 等） |
| `core:logic` | 纯逻辑领域类型（`BoardSpec` / `Difficulty` 等） |
| `core:ui` | 全局主题与通用组件（`MindgameTheme` / `Theme` / `BaseText` 等） |

### 关键约束
1. **尚无图片加载库**：项目未引入 Coil。`app/src/main/res/drawable/` 中的图标是
   Android **VectorDrawable XML / PNG**，**不是** `.svg` 文件。
   Android 可原生用 `painterResource()` 渲染 —— 因此「从 drawable
   加载矢量图」应使用 `Image(painter = painterResource(entry.iconRes), …)`，
   **无需新增任何依赖**。
2. **导航为纯 Compose 状态机**：`SudokuApp` 通过一个页内导航栈在
   「主菜单 / 游戏详情 / 设置 / 游玩屏」间切换，**未引入 Nav 库**（见第 9、10 节）。
3. **文案/排版统一走 `core:ui`**：文本使用 `core:ui` 的 `BaseText` +
   `Theme.typography`（`MindGameTypography`）；资源引用用 `@StringRes` / `@DrawableRes`。

### 现有相关文件（已在代码中落地）
- `app/.../main/model/GameEntry.kt`：`GameEntry` 数据模型 + `GameId` 枚举 + `gameCatalog`。
- `app/.../mainpage/MainScreen.kt`：`LazyColumn` 游戏列表（无状态）。
- `app/.../mainpage/GameCard.kt`：单个列表项（图片 / 标题 / 描述）。
- `app/.../sudoku/SudokuApp.kt`：导航宿主（`Dest` 返回栈 + `BackHandler`）。
- `app/.../sudoku/ComingSoonScreen.kt`：Courtsword / Zooduku 占位屏。
- `app/.../sudoku/HomeScreen.kt`：Sudoku 的开局入口（模式/规格/难度选择）。
- `app/.../sudoku/PlayRequest.kt`：一次「开始游玩」请求的数据类。

---

## 2. 推荐的文件结构

```text
app/src/main/res/drawable/
  ic_sudoku.xml / ic_deepseek_g_sudoku.png   (已存在 – 图标)
  ic_crossword.xml                            (新增 – 矢量图)
  ic_zooduku.xml                              (新增 – 矢量图)

app/src/main/res/values/
  strings.xml            (各游戏的标题/描述、通用按钮文案)

app/src/main/java/com/idroid/stuido/mind_game/
  main/model/GameEntry.kt     (数据模型：GameEntry + GameId + gameCatalog)
  mainpage/MainScreen.kt      (LazyColumn 游戏列表 – 无状态)
  mainpage/GameCard.kt        (单个列表项：图片 / 标题 / 描述)
  sudoku/SudokuApp.kt         (导航宿主：Dest 返回栈 + BackHandler)
  sudoku/ComingSoonScreen.kt  (Crossword / Zooduku 占位屏)
```

> 结构说明：主菜单相关代码位于独立的 `main` 子包（`main/model` 数据层 +
> `mainpage` UI 层），与 `sudoku` 玩法包解耦，便于后续把「游戏目录」抽成独立模块。

---

## 3. 数据模型：GameCatalog

一个简单、可测试的描述符（UI Model）。作为 UI 关注点，置于 `app` 的
`main/model` 包下。

```kotlin
package com.idroid.stuido.mind_game.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.idroid.stuido.mind_game.R

/** 一个可玩游戏的静态描述，用于主菜单列表。 */
@Immutable
data class GameEntry(
    val id: GameId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
)

enum class GameId { SUDOKU, CROSSWORD, ZOODUKU }

val gameCatalog: List<GameEntry> = listOf(
    GameEntry(GameId.SUDOKU,    R.drawable.ic_deepseek_g_sudoku, R.string.game_sudoku_title, R.string.game_sudoku_desc),
    GameEntry(GameId.CROSSWORD, R.drawable.ic_crossword,        R.string.game_crossword_title, R.string.game_crossword_desc),
    GameEntry(GameId.ZOODUKU,   R.drawable.ic_zooduku,          R.string.game_zooduku_title,   R.string.game_zooduku_desc),
)
```

> - `@DrawableRes` / `@StringRes` 保证资源引用类型安全；`@param:` 目标注解
>   让注解落在构造参数上（Kotlin 2.x 推荐写法）。
> - `GameId` 枚举为列表项提供稳定 key，且便于导航分支。
> - **未实现游戏**：`game_crossword_*` / `game_zooduku_*` 文案与两个 drawable
>   需先补齐才能把上表的全部条目编译通过；如仅先跑通 Sudoku，可暂时只保留首条。

---

## 4. UIModel 与资源解析：`@Immutable` 与 Context

### 4.1 `GameEntry` 是 UI Model 而非「UI Content」

- **UI Model（状态/数据模型）**：描述*数据身份与形状*，与平台解耦。
  `GameEntry` 属于此类 —— 它是一个「目录条目（catalog entry）」。
- **UI Content（展示内容）**：*已经解析好的、可直接呈现的*视图
  （已解析的字符串、已加载的图片、格式化后的数值）。

`GameEntry` 同时带有 `@StringRes` / `@DrawableRes` **资源 ID**（而非已解析值），
因此它是 **UI Model**：真正的「内容」在 `GameCard` 中由 `stringResource()` /
`painterResource()` 现场物化。

| 关注点 | 归属 |
|---|---|
| 「Sudoku 存在，图标是 `R.drawable.x`，标题是 `R.string.y`」 | `GameEntry`（UI **Model**） |
| 已解析文本「Sudoku」、已加载的图标画笔 | `GameCard` 内部（UI **Content**） |

> 不要把已解析字符串塞进 `GameEntry`：保留 ID 才能保证可测试、i18n 安全，
> 且构造时无需 `Context`。

### 4.2 为什么 ViewModel 不需要 `Context`

这是 MVI 中一个常见误解：**资源 ID 是编译期常量 `Int`，不是 Context 绑定对象**。
因此 ViewModel 持有 `GameEntry`（内含 `Int` ID）**完全不需要 `Context`**：

```
ViewModel (MVI)                        Composable (View)
─────────────────                      ─────────────────
GameEntry(                             stringResource(entry.titleRes)  -> "Sudoku"
  id,                                  painterResource(entry.iconRes)  -> ImageVector
  titleRes:  @StringRes Int   ───────►
  descRes:   @StringRes Int
  iconRes:   @DrawableRes Int
)
    ↑ 纯 Int，无 Context                    ↑ 解析在此发生
```

**UIModel 携带*引用*（稳定 `Int` ID）；View *解析*这些引用。**
VM 决定「显示什么」，View 决定「长什么样」。

三种做法的取舍：

| 做法 | 说明 | 评价 |
|---|---|---|
| **A. ViewModel 持有资源 ID**（本项目采用） | VM 暴露 `Int` ID，Composable 用 `stringResource`/`painterResource` 解析 | ✅ VM 无 Context、易单测、i18n 安全 |
| B. ViewModel 持有已解析 `String` | VM 调 `context.getString(...)`，UIModel 携带 `String` | ❌ 需注入 `Context`、locale 切换失效、难测 |
| C. VM 持有语义键，View 映射 | VM 携带 `GameId`/key，UI 层再查 `R.string` | ✅ VM 彻底无 Android 依赖；代价是多一层映射 |

> 结论：**采用 A**。与现有代码一致（`HomeScreen` 也直接在 Composable 里
> 调用 `stringResource(...)`）。
> **注意多模块**：本项目 `R` 为 app 模块的 `com.idroid.stuido.mind_game.R`
> （`android.nonTransitiveRClass=true`）。若日后把 `GameEntry` 下沉到库模块，
> 库模块无法引用 app 的 `R`，届时需改用做法 C（VM 发语义键、app 层解析）。

### 4.3 `@Immutable` 还是 `@Stable`？

- **`@Immutable`**：承诺实例**深度不可变**且相等性在生命周期内稳定，编译器无条件信任。
- **`@Stable`**：更弱的契约 —— 只承诺「相等的实例产生相等的读取」，
  用于编译器无法推断、但你能*保证*行为稳定的类型。

`GameEntry` 所有字段都是 `Int` / 枚举，**天然深度不可变**，因此用 **`@Immutable`**
（而非作为兜底的 `@Stable`）。

> 说明：在 Compose Compiler（Kotlin 2.2.x）下，这种仅含基本类型/枚举的
> `data class` 本就会被*推断*为稳定，注解在功能上不改变行为；加它主要为
> **表达意图**与**鲁棒性**——若日后新增 `List<...>` 字段，推断可能翻转为不稳定。
> 注意：此时若字段是普通 `List`，`@Immutable` 就成了「谎言」（接口可变），
> 需改用 `kotlinx.collections.immutable.ImmutableList` 或 `@Stable` 包装类。
> 参照 `core:ui` 的 `MindGameTypography` 也使用 `@Immutable`。

---

## 5. 矢量图资源：新增 drawable

以现有 `ic_sudoku.xml` 为模板（24dp viewport 约定）创建两个占位矢量图：

- `app/src/main/res/drawable/ic_crossword.xml`
- `app/src/main/res/drawable/ic_zooduku.xml`

> 若暂无最终美术资源，可先生成占位 VectorDrawable；后续替换即可，不影响代码。

---

## 6. 文案资源：strings.xml

```xml
    <!-- 游戏列表 -->
    <string name="games_title">Games</string>
    <string name="game_sudoku_title">Sudoku</string>
    <string name="game_sudoku_desc">Fill the grid so every row, column and box holds 1–9.</string>
    <string name="game_crossword_title">Crossword</string>
    <string name="game_crossword_desc">Solve clues and fill the crossword grid.</string>
    <string name="game_zooduku_title">Zooduku</string>
    <string name="game_zooduku_desc">A picture-based logic puzzle for younger players.</string>

    <!-- 通用 -->
    <string name="action_back">Back</string>
    <string name="coming_soon_message">This game is coming soon. Stay tuned!</string>
```

---

## 7. 列表项组件：GameCard

可复用列表项，包含所要求的三段式组件：**图片（顶）→ 标题 → 描述**，
包裹在可点击的 `Card` 中。文本统一走 `core:ui` 的 `BaseText` + `Theme.typography`。

```kotlin
package com.idroid.stuido.mind_game.mainpage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.main.model.GameEntry

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    entry: GameEntry,
    onClick: (GameEntry) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(entry) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 1) 顶部图片（VectorDrawable -> painterResource 零依赖渲染）
            Image(
                painter = painterResource(entry.iconRes),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            // 2) 标题
            BaseText(
                text = AnnotatedString(stringResource(entry.titleRes)),
                style = Theme.typography.bodyStyleSemiBold,
            )
            // 3) 描述
            BaseText(
                text = AnnotatedString(stringResource(entry.descriptionRes)),
                style = Theme.typography.bodyStyleNormal,
            )
        }
    }
}
```

> - `Card(onClick = …)` 需要 Material3 1.2+，当前 BOM（`2024.09.00`）已满足。
> - `BaseText` 来自 `core:ui`，`Theme.typography` 即 `MindGameTypography`；
>   若偏好 Material3 默认样式，也可直接用 `MaterialTheme.typography`。

---

## 8. 列表屏：MainScreen（LazyColumn）

列表屏**无状态**（接收 `games` 与 `onGameClick`），便于预览与测试。
位于 `app` 的 `mainpage` 包。

```kotlin
package com.idroid.stuido.mind_game.mainpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.main.model.GameEntry

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    games: List<GameEntry>,
    onGameClick: (GameEntry) -> Unit,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = games,
            key = { it.id },               // 稳定 key -> 更优的 diff
        ) { entry ->
            GameCard(entry = entry, onClick = onGameClick)
        }
    }
}
```

要点：
- `key = { it.id }` 为每行提供稳定身份（复用 `GameId` 枚举）。
- 保持界面**无状态**（注入 `games` + `onGameClick`），把导航留在宿主，便于预览与测试。
  也可选择直接传 `gameCatalog`，但注入参数更易测试。
- 如需列表顶部标题，可在 `LazyColumn` 内用 `item { … }` 追加（当前实现省略，标题交由外层）。

---

## 9. 导航接线：SudokuApp

### 9.1 设计：`sealed Dest` 返回栈 + `BackHandler`

`SudokuApp` 用 **返回栈列表（`List<Dest>`）** 而非单个当前目标：

- `Dest` 用 `sealed interface`（可携带数据，且每个子类型 ≈ 未来的一条 Nav route）；
- `navigate(to)` ≈ 压栈、`pop()` ≈ 出栈 —— 语义与 `NavController` 一一对应；
- 用 `rememberSaveable` + `listSaver` 保存为稳定字符串，**配置变更/进程重建不丢失**；
- 用 `BackHandler` 响应系统返回键，行为与 Nav 默认返回一致。

```kotlin
package com.idroid.stuido.mind_game.sudoku

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.listSaver
// … 其他 import …

/** 页内导航目标（非对局态）。 */
private sealed interface Dest {
    data object Games : Dest
    data object Settings : Dest
    data class GameDetail(val game: GameId) : Dest
}

private fun Dest.encode(): String = when (this) {
    Dest.Games -> "games"
    Dest.Settings -> "settings"
    is Dest.GameDetail -> "game:${game.name}"
}

private fun decodeDest(value: String): Dest = when {
    value == "settings" -> Dest.Settings
    value.startsWith("game:") -> Dest.GameDetail(GameId.valueOf(value.removePrefix("game:")))
    else -> Dest.Games
}

@Composable
fun SudokuApp(themePrefs: ThemePrefs) {
    var playRequest by remember { mutableStateOf<PlayRequest?>(null) }
    var backStack by rememberSaveable(
        stateSaver = listSaver(
            save = { stack -> stack.map { it.encode() } },
            restore = { encoded -> encoded.map(::decodeDest) },
        ),
    ) { mutableStateOf(listOf<Dest>(Dest.Games)) }

    fun navigate(to: Dest) { backStack = backStack + to }
    fun pop() { backStack = backStack.dropLast(1) }

    // 对局屏：覆盖式“页面”，由 playRequest 驱动；退出即回到下方导航栈。
    val req = playRequest
    if (req != null) {
        GameScreenRoute(
            onExit = { playRequest = null },
            mode = req.mode, spec = req.spec,
            difficulty = req.difficulty, seed = req.seed,
            sessionKey = "sudoku-game-${req.id}",
        )
        return
    }

    // 系统返回键 = 出栈（栈底不响应，交还系统）。
    BackHandler(enabled = backStack.size > 1) { pop() }

    when (val dest = backStack.last()) {
        Dest.Games -> MainScreen(
            games = gameCatalog,
            onGameClick = { entry -> navigate(Dest.GameDetail(entry.id)) },
        )
        Dest.Settings -> SettingsScreen(themePrefs = themePrefs, onBack = ::pop)
        is Dest.GameDetail -> GameHost(
            game = dest.game,
            onBack = ::pop,
            onOpenSettings = { navigate(Dest.Settings) },
            onStart = { mode, spec, difficulty, seed ->
                playRequest = PlayRequest(nextSessionId(), mode, spec, difficulty, seed)
            },
        )
    }
}
```

### 9.2 `GameHost` 分发

`GameHost` 按 `GameId` 分发：`SUDOKU` 渲染既有 `HomeScreen`（模式/规格/难度选择 →
`onStart`）；`CROSSWORD` / `ZOODUKU` 渲染带返回按钮的 `ComingSoonScreen` 占位屏。

```kotlin
@Composable
private fun GameHost(
    game: GameId,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStart: (GameMode, BoardSpec, Difficulty, Long?) -> Unit,
) {
    when (game) {
        GameId.SUDOKU -> HomeScreen(onOpenSettings = onOpenSettings, onStart = onStart)
        GameId.CROSSWORD, GameId.ZOODUKU ->
            ComingSoonScreen(title = game.name, onBack = onBack)
    }
}
```

> **重要行为变更**：改造后 `SudokuApp` 首先落到新的 `MainScreen`，
> `HomeScreen` 变为「Sudoku 详情」屏（由点击 Sudoku 卡片进入）。
> `MainActivity` 本身无需改动（它只是调用 `SudokuApp`）。

---

## 10. 导航方案选型

**结论：当前使用手写状态机（`Dest` 返回栈），但按「可平滑迁移」的方式组织。**

| 方案 | 优点 | 缺点 |
|---|---|---|
| **A. 扩展现有状态机**（当前采用） | 与现状一致、零新依赖、可传非 URL 状态（如 `PlayRequest`） | 手动管理返回栈 |
| B. 引入 `androidx.navigation-compose` | 类型安全路由、深链、返回栈、导航测试 | 新依赖 + 需重构现有流程 |

### 为什么现在不用 Nav 库
1. **图很浅**：3-4 屏 + 一个嵌套流程，Nav 的优势（多 route / 嵌套 graph）用不上；
2. **有非 URL 状态**：`PlayRequest`（mode/spec/difficulty/seed）用 Nav 参数反而更麻烦
   （需 `Parcelable`/`@Serializable` + `navArgument`），当前按引用传 `data class` 更简单；
3. **无深链需求**；
4. **`rememberSaveable` 已覆盖配置变更/进程重建**，无需 Nav 的 SavedStateHandle。

### 何时值得切换
- 需要**深链/外部入口**（通知、小组件打开某游戏）；
- **每款游戏各自成子流程**（Crossword / Zooduku 也有 setup→game→result）；
- 需要**类型安全路由**（`navigation-compose` 2.8+ 的 `@Serializable` route）；
- **route 数量增多**（10+），单 `when` 难以维护；
- 需要**导航测试**（`TestNavHostController`）。

> 现有 `Dest` 每个子类型 ≈ 一条 route，`navigate/pop` ≈ `NavController.navigate/popBackStack`，
> 因此迁移是**机械替换**，而非重写。

---

## 11. 关于「从 drawable 加载 SVG」

当前 `drawable/` 存放的是 **VectorDrawable XML / PNG**。两条受支持路径：

1. **VectorDrawable XML + `painterResource()`**（推荐，开箱即用，零依赖）——
   即第 7 节所用方式。
2. **真正的 `.svg` 文件放 `res/raw/`**——Android 无法原生渲染。需引入
   [Coil 3 + `coil-svg`](https://coil-kt.github.io/coil/svg/) 并使用
   `AsyncImage(model = R.raw.ic_sudoku, …)`。仅当设计直接交付 `.svg` 且不愿转换时选用。
   更建议用 Android Studio「New → Vector Asset」把 `.svg` 转为 VectorDrawable。

---

## 12. 建议的构建顺序

1. 新增 `main/model/GameEntry.kt`（`GameEntry` + `GameId` + `gameCatalog`）+ strings。
2. 新增矢量图资源（`ic_crossword.xml` / `ic_zooduku.xml`）。
3. 新增 `mainpage/GameCard.kt`（含 Preview 验证三个组件）。
4. 新增/重写 `mainpage/MainScreen.kt`（`LazyColumn`）。
5. 改造 `sudoku/SudokuApp.kt`（`Dest` 返回栈 + `BackHandler`），把 `HomeScreen`
   下沉为 Sudoku 详情屏，主菜单成为首屏。
6. 新增 `sudoku/ComingSoonScreen.kt` 占位屏，并在 `GameHost` 分发。
7. 运行验证：点击每张卡片可导航、返回回到列表、设置仍可达、系统返回键行为正确。

---

## 13. 风险与技术备注

- **行为变更**：主菜单将成为首屏，`HomeScreen` 的角色由「首屏」变为「Sudoku 详情」，
  需同步调整 `SudokuApp` 的分支与（原 HomeScreen 上的）设置入口。
- **LazyColumn 性能**：列表项尽量用稳定 `key`，避免整屏重组；
  图片统一尺寸、使用 `painterResource` 缓存矢量图。
- **占位游戏**：Crossword / Zooduku 尚无玩法，需在 `GameHost` 明确占位，避免误导航；
  对应 strings/drawable 补齐前，`gameCatalog` 只能安全保留已实现条目。
- **资源可访问性**：为装饰性图片设 `contentDescription = null`；
  若图片承载信息，应提供有意义的 contentDescription。
- **多模块边界**：`GameEntry` 依赖 app 的 `R`，暂不能下沉到库模块；
  若需下沉，改用「VM 发语义键、app 层解析资源」的做法（见第 4 节做法 C）。
- **未来演进**：屏数/深链增多时，按第 10 节的迁移路径切换到 `navigation-compose`。

---

*本计划范围：新增单屏主菜单（LazyColumn 游戏列表）+ 导航接线（`Dest` 返回栈）；
Crossword / Zooduku 的实际玩法属后续迭代。*
