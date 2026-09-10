# GameScreen UI 与设计稿（sudoku_ui.jpeg）差异对照

> 目标：将 `feature:game` 的 `GameScreen` 逐步对齐到 `docs/sudoku_ui.jpeg` 的 9×9 目标 UI。
> 本文为「随时可复用的 backlog/对照表」，按优先级排序，每条注明出处（文件/结构）与建议改法。

---

## 0. 方法说明

设计稿是一张位图（`docs/sudoku_ui.jpeg`, 2816×1536），我通过 **OCR 提取其文字标注** 来对照，
而不是逐像素目测。因此**文案、布局层级、关键元素**可信度较高；而配色/图标/视觉密度等无法从文字还原的部分，
文中标为「需人工比照渲染」或纳入取舍，未强推。

图中可信的 9×9 结构（自上而下）：
```text
Top Navigation Bar (56dp)    :  Back · 标题 "Classic Sudoku · Expert" · 计时 04:18
Game Status Information(44dp):  Errors: 1/3 · Hints: 3/3
┌──────── 9×9 主棋盘 ────────┐
│ Given Number (Bold/Medium)  │
│ User Number                 │
│ Selected → 整行+整列十字高亮  │
│ Same Number 高亮 @20% 主题色  │
│ Notes（格内角标小字）          │
│ Major/Minor Border (宫线/细线)│
Auxiliary Toolbar (56dp)     :  Undo · Erase · Hint
Number Keyboard (64dp)       :  1…9 + 每个数字的 Remaining Count badge
```

---

## A. 顶部信息区：结构与高度

### A1 [P1] 缺少独立的 “Game-Status 信息条”（Errors/Hints 数值区）
- 设计稿：TopBar(56dp) 之下另有一行 `Errors: 1/3 · Hints: 3/3`（44dp）。
- 现状：`GameTopBar`（`feature/game/.../ui/TopBar.kt`）把 title + 计时 + `⚠ mistake/max` 挤在同一行的标题
  下方小字里；**没有 Hints 计数**（VM 不追踪已用提示数）。
- 改法：抽出独立的 `GameProgress 信息行`（errors、hints、可选计时），高度对齐 44dp；
  需要 `GameViewModel` 把 hint 计数并入 state。
- 涉及文件：`TopBar.kt`、`GameUiState.kt`、`GameViewModel.kt`、`GameReducer.kt`、`GameScreen.kt`

### A2 [P1] 顶部无 “Back” 图标 / 返回
- 设计稿：左上 Back。
- 现状：`GameTopBar` 只有 `TextButton("Exit")`（无图标返回箭头）。
- 改法：加返回箭头图标或 Back 按钮（置于 56dp top bar）。当前宿主退出通过 `onExit` 完成，可复用同一回调。
- 涉及文件：`TopBar.kt`

### A3 [P2] 规格标题改为「可读难度词 + 规格展示名」
- 现状：`describeTitle`(`GameScreen.kt`) 渲染成 `"9×9 Sudoku · EASY"`（裸 size + 枚举名全大写）。
- 设计稿：`"Classic Sudoku · Expert"` 这类可读词（spec 展示名 + difficulty 展示名）。
- 改法：`spec` 加展示名（Mini/Medium/Classic，资源化），难度加展示名映射（EASY→Easy…）；
  `describeTitle` 用这些展示名拼接。已存在 `label_daily` 供 Daily 后缀。
- 涉及文件：`GameScreen.kt`、`strings.xml`（feature 模块）

---

## B. 棋盘渲染语义

### B1 [P1] 选中格 “整行 + 整列十字高亮”
- 设计稿：`Selected Triggers … vertical + horizontal crosshair`（淡化整行、整列成十字）。
- 现状：`GameBoard`（`feature/game/.../ui/GameBoard.kt`）对选中只用
  `selectedPeers`（同行/列/宫整体交圈淡显，无“十字”分离感）。
- 改法：拆成两层——同行/列用 20% 主题色淡显（十字），同宫另用更浅/不同底色；`CellView`/`CellVisual`
  需增加区分字段同列、同宫。
- 涉及文件：`GameBoard.kt`、`CellView.kt`

### B2 [P1] 与选中格“同值”的高亮用 20% 主题色
- 设计稿：与选中格同数值的格用主题色 20% 透明度高亮。
- 现状：`GameBoard` 里 `sameValueAsSelection` 并入 `isSoft`（同 peers 一条），透明度未单独用 20%。
- 改法：把“同值格”独立成 20% 主题色层，区别于行/列/宫的 soft。
- 涉及文件：`GameBoard.kt`、`CellView.kt`

### B3 [确认] Notes（格内小字候选）——基本符合
- 现状：`CellView` 已支持渲染 `board.notes[i]` 小字候选组。
- 仅需在 9×9 上人工确认字号/间距是否合适。
- 涉及文件：`CellView.kt`

---

## C. 底部工具 / 键盘

### C1 [P0·结构性] 缺少 `Undo`（撤销）
- 设计稿 Auxiliary Toolbar：**Undo · Erase · Hint**。
- 现状 `ToolBar.kt`：**Notes · Erase · Hint · Check · Solve · Pause**——**无 Undo**，
  `GameEvent` / `GameReducer` 无上一步撤销栈（只有 Erase）。
- 改法（结构性）：新增 `Undo` 事件 + reducer 快照/撤销栈（记录每次变动，见函数式做法：
  在 reducer 内每步 push 前态或在 VM 维护 undo list），并重排 Auxiliary toolbar 成 Undo/Erase/Hint 一组。
- 涉及文件：`GameEvent.kt`、`GameReducer.kt`、`GameViewModel.kt`、`ToolBar.kt`、`GameScreen.kt`

### C2 [P1] 数字键盘缺 “剩余数 badge / 已满禁用”
- 设计稿：1..size 每个数字下显示“该数字剩余未填数”，填满则 `Disabled/Completed`。
- 现状：`NumberPad`（`NumberPad.kt`）只画 1..size 圆形数字，点亮 `selectedGiven`；
  **无**每数字剩余计数，也**无**填满禁用态。
- 改法：由 state（board.values + solution 或目标）汇出每数字剩余数；渲染于键盘对应圆钮下方 badge；
  该数字 count==0（即已全部填对/在若干 given）→ 禁用并淡化。
- 涉及文件：`NumberPad.kt`、`GameUiState.kt`、`GameScreen.kt`

### C3 [P1·校准] 工具/键盘高度模型对齐
- 设计稿：Aux toolbar 56dp、Keyboard 64dp。
- 现状：`DigitButton` 圆钮高 46dp；`GameToolBar` 按钮 `heightIn(min=34.dp)`。
- 改法：如要像素对齐可用稿子这三段高度（56/44/64）统一布局定高（可选，非必须，看是否追求强对齐）。
- 涉及文件：`NumberPad.kt`、`ToolBar.kt`

---

## D. 布局层级小项

### D1 [P1] 9×9 底部控件固定、避免整体纵向滚动挤坏
- 现状：`GameScreen` 整体 `Column` 用 `verticalScroll`，棋盘 `aspectRatio(1f)` 撑到接近整宽，
  在窄/高屏/大规格下底部工具栏/键盘易被滚出或互相挤压。
- 设计稿：Board + Toolbar + Keyboard 同屏满铺、底部锚定感。
- 改法：9×9 用固定尺寸棋盘；把工具栏/NumberPad 放到 `Scaffold` bottom bar（不随内容滚动），
  或为 over 尺寸时固定底部、仅中部棋盘区域可滚/缩放。
- 涉及文件：`GameScreen.kt`

---

## E. 设计稿没体现、但我们已有——需产品取舍
我们实现了 **Notes 按钮、Check/判错、Solve 演示、Pause**；稿子精简底栏只有 Undo/Erase/Hint。
执行时需决定：Notes/Check/Solve 是否降级到二级或移到别处，属交互取舍，先由你拍板。

---

## 建议落地顺序（供以后直接调用）
1. **P0** — Undo 撤销栈 + Hints/剩余数状态追踪（涉及 reducer/VM/state 结构性改动）。
2. **P1** — 拆出 Game-Status 信息行；Board 十字 + 同值 20% 两层高亮改为视觉层。
3. **P1** — 9×9 底部控件固定化（把键盘/工具栏 bottom anchor）。
4. **P2** — 标题难度展示名；TopBar 加 Back 图标样式。

> 验收：功能改动后用 `feature:game` 的 4×4/6×6/9×9 **@Preview** 逐规格人工比照设计稿；
> 结构改动需保证 `:feature:game:testDebugUnitTest`、`:core:logic:test` 与 `assembleDebug` 全绿。

---

## 参与文件速查
| 区域 | 文件 |
|---|---|
| 顶栏/状态 | `ui/TopBar.kt` |
| 棋盘/高亮/宫线 | `ui/GameBoard.kt`、`ui/CellView.kt`、`ui/BoardGeometry.kt` |
| 底部工具/键盘 | `ui/ToolBar.kt`、`ui/NumberPad.kt` |
| 整屏组装/Preview | `ui/GameScreen.kt` |
| MVI | `mvi/GameUiState.kt`、`mvi/GameEvent.kt`、`mvi/GameViewModel.kt`、`reducers/GameReducer.kt` |
| 资源 | feature:game `res/values/strings.xml` |
