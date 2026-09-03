# feature:game — MVI + Jetpack Compose 实现计划

> 目标：把已完成的 `core:logic` 纯逻辑引擎接入玩法层 `feature:game`，
> 采用 **MVI（Model-View-Intent）** 架构，UI 以 **Jetpack Compose** 构建，
> 完整实现自由模式与每日挑战两种玩法。

---

## 目录

1. [当前现状盘点](#1-当前现状盘点)
2. [架构设计总览](#2-架构设计总览)
3. [依赖与构建配置](#3-依赖与构建配置)
4. [核心概念：UI State / UI Event / UI Effect](#4-核心概念ui-state--ui-event--ui-effect)
5. [文件清单与包结构](#5-文件清单与包结构)
6. [UI State 设计](#6-ui-state-设计)
7. [UI Event 设计（Intent）](#7-ui-event-设计intent)
8. [UI Effect 设计](#8-ui-effect-设计)
9. [GameViewModel 逻辑编排](#9-gameviewmodel-逻辑编排)
10. [Compose 界面结构](#10-compose-界面结构)
11. [关键交互流程](#11-关键交互流程)
12. [与 core:logic 的接线](#12-与-corelogic-的接线)
13. [导航与 App 壳接线](#13-导航与-app-壳接线)
14. [测试计划](#14-测试计划)
15. [分阶段实施步骤](#15-分阶段实施步骤)

---

## 1. 当前现状盘点

### 已完成（core:logic，可复用 API）
| 类 | 位置 | 用途 |
|---|---|---|
| `SudokuGenerator.generate(size,boxWidth,boxHeight,holes,seed?)` | `generator` | 生成唯一解谜题 |
| `DifficultyConfig.default()`、`holeCount/isAvailable/availableDifficulties` | `config` | 难度→挖空数映射 |
| `BoardSpec`（MINI/MEDIUM/CLASSIC）、`Difficulty`（EASY/MEDIUM/HARD/EXPERT） | `config` | 规格与难度枚举 |
| `PuzzleValidator.isValid(config)` | `validator` | 复核谜题唯一解 |
| `SudokuBoardConfig`（solution/puzzle/holes...） | `model` | 棋盘数据模型 |

### feature:game 模块现状
- `feature/game/build.gradle.kts` 已配置 **Android Library + Compose**，
  且已 `implementation(project(":core:logic"))`、`implementation(project(":core:ui"))`；
- 已含 `lifecycle-runtime-ktx` 与 `lifecycle-viewmodel-compose`；
- 目前**尚无任何源码文件**（仅 manifest + gradle + .gitignore）。

### 待补齐
- kotlinx-coroutines / StateFlow（viewmodel 提供 state/effect）
- 导航（可选：`app` 壳中单屏即可）
- MVI 三件套源码骨架

---

## 2. 架构设计总览

采用**单向数据流（UDF）**的 MVI：

```text
   View(Compose)                      ViewModel(状态持有者 / Reducer)
        │  dispatch(event)                       │
        ▼                                       ▼
     UI Event ──────────────────►  intent 处理 + 业务（调用 core:logic）
        ▲                                       │  产出
        │                    ┌──────────────────┼──────────────────┐
        │                    ▼                   ▼                  ▼
  渲染(State)            更新 UI State       emit UI Effect     （一次性导航/吐司）
        ▲                    ▲
        └────── StateFlow ◄──┘
```

- **UI State**：可观察的当前状态（`StateFlow<GameUiState>`），Compose 收集渲染；
- **UI Event（Intent）**：用户/系统发起的**单向意图**，`ViewModel.dispatch(event)`；
- **UI Effect**：**一次性**副作用（如提示、完成动画、错误吐司），经 `Channel/SharedFlow` 消费。

> 原则：
> - State 唯一来源在 ViewModel，Compose 不做本地 mutable 状态决策；
> - Event 只进不出；对 State、Effect 分频道下发；
> - 业务逻辑尽量保持"纯函数 + 可注入依赖"，便于单测。

---

## 3. 依赖与构建配置

在 `feature/game/build.gradle.kts` 增补：

```kotlin
dependencies {
    // 已有
    implementation(project(":core:logic"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 新增建议
    implementation(libs.androidx.lifecycle.viewmodel.ktx)   // viewModelScope
    implementation(libs.kotlinx.coroutines.android)          // Dispatchers/Flow
}
```

> `kotlinx-coroutines-android` 通常由 lifecycle-runtime 传递带入，但显式声明更稳妥。
> 建议在 `libs.versions.toml` 显式登记，避免隐式版本。

---

## 4. 核心概念：UI State / UI Event / UI Effect

以一个游戏屏（同时承载自由模式 + 每日挑战）为例，三种类型严格区分：

### 4.1 UI State（游戏截图状态，可重建）
```kotlin
data class GameUiState(
    val mode: GameMode = GameMode.FREE,          // FREE / DAILY
    val spec: BoardSpec = BoardSpec.CLASSIC,     // 4x4 / 6x6 / 9x9
    val difficulty: Difficulty = Difficulty.EASY, // 仅自由模式有意义
    val board: List<Int> = emptyList(),          // 当前盘值（扁平）
    val solution: List<Int> = emptyList(),       // 终盘（用于提示/判错）
    val givens: Set<Int> = emptySet(),           // 原始给定格的索引（不可改）
    val selectedCell: Int? = null,               // 当前选中格
    val notes: Map<Int, Set<Int>> = emptyMap(),  // 各格的铅笔笔记（候选）
    val mistakes: Int = 0,                       // 已用错误次数
    val maxMistakes: Int = 3,                    // 允许错误上限
    val timerSeconds: Int = 0,
    val status: GameStatus = GameStatus.IDLE,    // IDLE/RUNNING/PAUSED/FINISHED
    val isSolving: Boolean = false,              // 求解中（转圈）
)
```

### 4.2 UI Event（由 View / 生命周期派发的单向意图）
```kotlin
sealed interface GameEvent {
    // 启动类
    data class StartGame(mode, spec, difficulty, seed?) : GameEvent
    object RestartGame : GameEvent
    object BackToMenu : GameEvent
    // 棋盘操作
    data class SelectCell(val index: Int) : GameEvent
    data class InputDigit(val index: Int, val value: Int) : GameEvent
    data class InputNote(val index: Int, val value: Int) : GameEvent  // 铅笔笔记
    data class ClearCell(val index: Int) : GameEvent
    // 工具
    object ToggleNoteMode : GameEvent
    object RequestHint       : GameEvent   // 提示
    object RequestErase      : GameEvent
    object RequestCheck      : GameEvent   // 判错
    object RequestSolve      : GameEvent   // 自动解完（演示）
    // 计时 / 状态
    object TogglePause : GameEvent
    data class Tick(val seconds: Long) : GameEvent
}
```

### 4.3 UI Effect（一次性副作用）
```kotlin
sealed interface GameEffect {
    data class ShowMessage(val msg: String) : GameEffect   // 错误/完成/超出错误上限
    object GameCompleted : GameEffect                       // 触发完成动画/导航
    object PuzzleInvalid  : GameEffect                       // 生成本身异常
    data class HighlightHint(val index: Int) : GameEffect   // 提示聚焦闪烁
}
```

---

## 5. 文件清单与包结构

```text
feature/game/src/main/java/com/idroid/stuido/mind_game/feature/game/
├── GameMode.kt                     // FREE / DAILY 枚举
├── GameStatus.kt                   // IDLE/RUNNING/PAUSED/FINISHED
├── mvi/
│   ├── GameUiState.kt              // 见 §6
│   ├── GameEvent.kt                // 见 §7
│   ├── GameEffect.kt               // 见 §8
│   └── GameViewModel.kt            // 状态持有者 + Reducer（见 §9）
├── reducers/
│   └── GameReducer.kt              // 纯函数 state→state（便于单测）
├── engine/                          // 封装 core:logic 的用例/交互对象（见 §12）
│   ├── GameFactory.kt              // 由 mode/spec/difficulty/seed 产出初始 puzzle
│   ├── InputValidator.kt           // 判错、唯一解复核
│   ├── HintProvider.kt             // 用 solution 给出提示
│   └── TimerUseCase.kt             // 计时（或放 ViewModel）
└── ui/
    ├── GameScreen.kt               // 顶层可组合：收集 state、派发 event、收集 effect
    ├── GameBoard.kt                // 渲染 size×size 网格（cell + notes）
    ├── CellView.kt                 // 单格（值/笔记/给定/选中/冲突高亮）
    ├── NumberPad.kt / NotePad.kt   // 输入栏（数字选择 + 笔记模式切换）
    ├── ToolBar.kt                  // 提示/橡皮/判错/求解/暂停
    ├── TopBar.kt                   // 难度/规格/计时/错误计数显示
    └── components/                 // 通用（如完成弹窗、暂停覆盖层）
```

---

## 6. UI State 设计（细化）

为支撑"自由模式 + 每日挑战"两种进入方式与完整对局，`GameUiState` 分解为几个内聚片段并以不可变数据持有：

```kotlin
data class GameUiState(
    val config: GameConfig,          // mode/spec/difficulty/seed
    val board: BoardCells,           // 当前进展（值 + 笔记 + 给定标记）
    val selection: Selection,        // 当前选中格、笔记模式
    val progress: GameProgress,      // 已填/正确/错误次数、状态、计时
    val inputMode: InputMode,        // INPUT / NOTE
)

data class GameConfig(val mode, spec, difficulty, seed: Long?, maxMistakes)
data class BoardCells(val size: Int, val values: List<Int>, val givens: Set<Int>, val notes: Map<Int, Set<Int>>)
data class Selection(val index: Int?, val conflicts: Set<Int>)
data class GameProgress(val status, mistakes, timer: Duration, isBusy: Boolean)
```

> 选择不可变 `List`/`Map`/`data class` 而非可变数组，便于 Compose 组合键 / 稳定跳过与单元断言。

---

## 7. UI Event 设计（Intent）— 输入归一

ViewModel 只暴露两个入口保持 MVI 纯净：

```kotlin
class GameViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effect = Channel<GameEffect>(Channel.BUFFERED)
    val effect: Flow<GameEffect> = _effect.receiveAsFlow()

    fun dispatch(event: GameEvent) { /* 见 §9 */ }
}
```

所有 UI/系统输入统一走 `dispatch`，避免散落回调污染。

---

## 8. UI Effect 设计 — 一次性副作用

- **Message/Snackbar**：错误计数、完成、生成失败。
- **Highlight hint**：定位提示格，短暂聚焦。
- **Navigation**：每日挑战完成后跳结果页（本轮：单屏内完成态即可）。

ViewModel 通过 `GameEffect` 并发给 View 消费；View 用 `LaunchedEffect` + `eventFlow.collect`。

---

## 9. GameViewModel 逻辑编排

采用"**交互器 + 纯 Reducer**"拆分：

```
GameEvent ──► [interactor 处理副作用/耗时] ──新 UiState──► reduce──► _uiState
                      │                                     不用 state? ─► `_effect`（Channel）
                      ▼
                 core:logic 引擎调用（suspend 化或 CPU 密集在默认调度器）
```

`GameViewModel` 内部大致骨架：

```kotlin
class GameViewModel(
    private val gameFactory: GameFactory,     // 包装 SudokuGenerator
    private val inputValidator: InputValidator, // 包装 PuzzleValidator + 判错
    private val hintProvider: HintProvider,     // 从 solution 给提示
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    init { handleArgsAndStart() }   // 从 SavedStateHandle 读取 mode/spec/difficulty/seed 启动

    fun dispatch(event: GameEvent) {
        when (event) {
            is GameEvent.StartGame  -> startGame(event)
            is GameEvent.SelectCell -> _uiState.update { selectCell(it, event.index) }
            is GameEvent.InputDigit -> inputDigit(event)
            is GameEvent.RequestHint -> requestHint()
            // ...
            GameEvent.RestartGame  -> { startGame(config); GameEffect? }
        }
    }

    // 例：输入一个数字（含冲突与判错）
    private fun inputDigit(event: GameEvent.InputDigit) {
        val s = _uiState.value
        _uiState.update { GameReducer.applyDigit(it, event.index, event.value) } // 纯更新
        // 冲突/错误判定可能需要 terminal 求解 => 交由 interactor 判断
        when (val result = inputValidator.checkForMistake(s, event.index, event.value)) {
            is MistakeResult.Mistake -> incrementMistake(result)
            is MistakeResult.Solved   -> finishGame()
        }
    }
}
```

> 说明：交互器可在 `viewModelScope.launch { withContext(Dispatchers.Default) { ... } }`
> 中跑 `gameFactory`（涉及重回溯生成）；`_uiState.update{}` 全部在主调度。

---

## 10. Compose 界面结构

顶层 `GameScreen` 作为**唯一可组合入口**，对接 MVI：

```kotlin
@Composable
fun GameScreen(
    viewModel: GameViewModel,               // 或 VM 由上层注入
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.effect.collect { handleEffect(it, onExit) } }

    Scaffold(
        topBar = { GameTopBar(state, ...) },
        bottomBar = { NumberPad(...) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            GameBoard(
                state = state,
                modifier = Modifier.weight(1f),
                onCellClick = { viewModel.dispatch(GameEvent.SelectCell(it)) },
            )
            ToolBar(onEvent = viewModel::dispatch)
        }
    }
}
```

### Compose 读取 state 的关键点
- `GameBoard` 依据 `state.spec.size & boxWidth/boxHeight` 计算布局（4x4/6x6/9x9）。
- `CellView` 高亮三种态：**给定**、**选中/同值/同行列宫**、**冲突（错误红）**。
- 输入条：数字 `1..size`（依规格变化），且提供 **NOTE(铅笔) 模式切换**。

---

## 11. 关键交互流程

### 玩法A：自由模式进入
```
1. 用户点选 规格(4/6/9) + 难度
2. dispatch(GameEvent.StartGame(mode=FREE, spec, difficulty))
3. ViewModel → gameFactory.buildFree(spec, difficulty) → core SudokuGenerator.generate(...)
4. 产出 BoardConfig → 拆成 GameUiState（values/givens/solution）
5. state → RUNNING → 开始计时
```

### 玩法B：每日挑战进入
```
1. dispatch(StartGame(mode=DAILY, spec=difficulty?, seed=todaySeed))
2. seed 由 App 壳提供（复用 内存中 date→seed，日粒度；参考 app 现有 DateUtils 精神）
3. gameFactory.buildDaily(spec, seed) → generate(..., seed=seed)
4. 同一设备/同日 seed 相同 → 题目一致（跨设备需 Firestore，见演进）
```

### 玩法C：提示 / 判错 / 求解
- **提示**：从 `solution[index]` 取正解填入该格（记错误不应增加）。
- **判错**：填入值与 solution 不一致 → `mistakes++`；达上限 → FINISHED(失败)。
- **求解(演示)**：直接把盘面整体替换成 solution → FINISHED(成功)。

### 玩法D：单元格冲突可视化
用户输入数字后，若与同行/列/宫已有给定冲突，计算 `selection.conflicts` 高亮红。

---

## 12. 与 core:logic 的接线

`engine` 层封装 `core:logic`，让 MVI 层无感知其细节：

```kotlin
class GameFactory(
    private val generator: SudokuGenerator = SudokuGenerator(),
    private val difficultyConfig: DifficultyConfig = DifficultyConfig.default(),
) {
    fun buildFree(spec: BoardSpec, difficulty: Difficulty): GameSeed {
        val holes = difficultyConfig.holeCount(spec, difficulty)
            ?: throw IllegalArgumentException("$spec 不支持 $difficulty")
        val bc = generator.generate(spec.size, spec.boxWidth, spec.boxHeight, holes)
        return toGameSeed(spec, bc)
    }
    fun buildDaily(spec: BoardSpec, seed: Long): GameSeed {
        val difficulty = hardestAvailable(spec)             // 每日通常取可支持的最难
        val holes = difficultyConfig.holeCount(spec, hardestAvailable(spec))!!
        val bc = generator.generate(spec.size, spec.boxWidth, spec.boxHeight, holes, seed = seed)
        return toGameSeed(spec, bc)
    }
}
```

冲突/胜负判定可快速利用 `solution` 数组比对，**无需**每次调用求解器（更优）；
仅当需"唯一解复核/外部校验"时才用 `PuzzleValidator`。

---

## 13. 导航与 App 壳接线

`sudoku_ui.jpeg`/SVG 显示游戏主棋盘。本轮聚焦单屏玩法，导航可最简化：

- `app` 的 `MainActivity` 直接装载 `feature:game` 的 `GameScreen`（或 `SetupScreen`）；
- 为不阻塞，可将"退出"设计为返回上一处；`onExit` 由 app 注入（默认 finishing）；
- 未来如需多屏（主菜单→设置选规格→对局页），引入 `feature:game` 内部 `NavHost`
  或抽取 `core:ui` 的共同导航骨架。

---

## 14. 测试计划

| 层面 | 测试对象 | 说明 |
|---|---|---|
| 纯 Reducer单测 | `GameReducerTest` | 不依赖 coroutine/VM，覆盖 select/applyDigit/clear/note/timer 纯变换 |
| VM 集成 | `GameViewModelTest` | 注入 fake `GameFactory`/`HintProvider`，用 `StandardTestDispatcher` 断言 state/effect |
| engine 单测 | `GameFactoryTest` | 对 4/6/9 free/daily 产出唯一解谜题（复用 `PuzzleValidator`） |
| Compose UI | `GameScreenTest` | `createComposeRule` + 注入 VM：事件派发、渲染、snackbar |
| 端到端冒烟 | 手测 | 完整对局流程 |

> 因 feature 是 Android Library，Reducer / State 可放 `main` 并新增 `test` 依赖
> `org.jetbrains.kotlinx:kotlinx-coroutines-test`。

---

## 15. 分阶段实施步骤

### Phase 0 — 骨架与依赖（不含游戏性）
1. 在 `libs.versions.toml` 登记 coroutines / viewmodel 版本。
2. `feature/game/build.gradle.kts` 补依赖。
3. 建立 `mvi/` `ui/` 空包 + 最小可编译 `GameScreen`（静态棋盘）验证链跑通。

### Phase 1 — MVI 数据层落地
4. 定义 `GameMode`、`GameStatus`、`GameUiState`、`GameEvent`、`GameEffect`。
5. 实现 `engine/` 三件（Factory/Hint/InputValidation）+ README 记录 `core:logic` 契约。

### Phase 2 — GameViewModel + Reducer
6. 实现 `GameReducer`（纯函数）与 `GameViewModel`（dispatch + reduce + effect）。
7. 写 `reducer/VM` 单测，保证核心状态变迁正确。

### Phase 3 — Compose 界面
8. `GameScreen + GameBoard + CellView + NumberPad + ToolBar + TopBar`。
9. 对 4x4/6x6/9x9 自适应、主题沿用 `MindgameTheme`。

### Phase 4 — App 壳接线 & 打磨
10. MainActivity 装载 `GameScreen`；处理退出/重开/暂停。
11. 完成动画与 Snackbar、错误上限、计时显示；接入 `savedstate` 保存状态。

### Phase 5 — 测试收尾
12. UI 测试 + 端到端冒烟 + 文档 `feature:game` README。

---

## 风险与技术备注
- **CPU 密集生成**置于 `Dispatchers.Default` 避免卡 UI；
- **9x9 大规格**网格绘制注意性能：尽量用稳定 `key`、避免整屏重组；
- **状态恢复**优先用 `SavedStateHandle`（旋转/进程重建）；
- **每日挑战一致性**需跨设备时间基准一致：后续接 Firebase `GeneratorConfig.DailySeed`
  或时钟对齐（本轮先用固定 `seed_from(date)`）。

---

*本计划为 `feature:game` 阶段一（单屏玩法）范围；多屏 / 在线排行榜 / 每日同步属后续迭代。*
