# Sudoku completion plan: win / lose / hints / notes + mockup UI

UI reference: `docs/sudoku_ui.jpeg`. Scope: `:feature:game`, plus small changes in `:app` (settings and passing settings into the game) and `:core:ui` (top bar).

## 0. What exists today (verified in code)

| Area | Current behaviour | Gap |
|---|---|---|
| Win | `inputNumber` / `requestHint` → `InputValidator.isComplete` → `finishAsSuccess()` → `SOLVED` + `FinishPanel` | `GameOutcome` now distinguishes solved, failed, and revealed games; `RequestSolve` is not a win |
| Lose | Every wrong digit → `registerMistake()`. At configured lives → `FAILED` + `FinishPanel` | Lives and hints come from bundled `game_config.json` rules. Retry same puzzle reuses the stored seed; Restart starts a new puzzle with the same parameters |
| Hints | `requestHint()` fills the selected unresolved cell or first unresolved cell, and records `HintFilled` | Hints have a configured budget; exhausted requests show a message and do not change state |
| Notes | `ToggleNoteMode`, `ToggleNote`, `NotesGrid` in `CellView`. Placing a value clears that cell's notes | Placing a value doesn't clean up neighbouring notes. No highlight for notes matching the selected digit. No undo |
| Effects | `GameViewModel.effect` (Channel) | `GameScreenRoute` collects messages and hint feedback as snackbars; haptics and cell flashes remain future work |

## 1. State and rules (`:feature:game`)

```kotlin
data class GameConfig(..., val maxMistakes: Int = 3, val maxHints: Int = 3)
enum class GameOutcome { SOLVED, FAILED, REVEALED }
data class GameProgress(..., val hintsUsed: Int = 0, val outcome: GameOutcome? = null)
GameUiState.hintsLeft // maxHints - hintsUsed, clamped to zero
GameUiState.livesLeft // maxMistakes - mistakes, clamped to zero
GameEvent.NewGame(mode, spec, difficulty, seed, maxMistakes = 3, maxHints = 3)
GameEvent.RetrySamePuzzle   // reset to the stored GameSeed; lives, hints and timer reset
GameEvent.Restart           // new puzzle with the same parameters ("New game")
GameEvent.Undo              // new (see §4)
GameEffect.HintFilled(index, value)
```

- `GameReducer.finish(state, outcome)` replaces `finish` / `fail`. `FinishPanel` reads `outcome` instead of guessing.
- `GameViewModel` keeps `lastSeed: GameSeed` so `RetrySamePuzzle` needs no regeneration.
- Ticker already stops outside `RUNNING`, so no change there.

### 1.1 Success (requirement 1)
- Last correct digit, or a hint that completes the board → `outcome = SOLVED`, `FINISHED`, `PuzzleCompleted` effect.
- **Success panel**: "Solved!", time `mm:ss`, mistakes `n/max`, hints used `n/max`. Buttons: **New game** (`Restart`) and **Exit**.
- `RequestSolve` → `outcome = REVEALED` and a "Solution shown" panel, not a success *(decision D1)*.

### 1.2 Out of lives → retry (requirement 2)
- Lives = `maxMistakes - mistakes`. Default 3, shown in the status row as `Errors: 1/3`, as in the mockup.
- When lives reach 0 → `outcome = FAILED`, board locked, timer stopped, `GameOver` effect.
- **Retry panel**: "Out of lives", time, mistakes and hints. Buttons: **Retry same puzzle** (`RetrySamePuzzle`), **New game**, **Exit** *(decision D2)*.
- **Configurable**: lives and hints come from the game config JSON (`docs/game_config_json.md`, `rules` section). Path: `GameConfigRepository` → `PlayRequest.rules` → `GameScreenRoute` → `NewGame(maxMistakes = …, maxHints = …)` *(decision D3)*. "Watch an ad for +1 life / +1 hint" is phase P6.

### 1.3 Hint counter (requirement 3)
`requestHint()` rules:
1. `hintsLeft == 0` → `ShowMessage("No hints left")`, no state change. Button shows disabled `0`.
2. Use the selected cell if it is unresolved and not given; otherwise pick the first unresolved non-given cell in row-major order.
3. If no unresolved cell exists, return without spending a hint.
4. Fill `solution[index]`, `hintsUsed++`, clear that cell's notes, and send `HintFilled(index, value)`. If the board is now complete → success.
- Hints never cost a life.
- Shown in two places, both counting down: `Hints: 2/3` in the status row, and a badge on the Hint button.

### 1.4 Show effects
`GameScreenRoute`: `LaunchedEffect(vm) { vm.effect.collect { … } }` → `SnackbarHostState` on the `Scaffold` for `ShowMessage`, `NewGameFailed`, and `HintFilled`. Terminal effects are represented by the outcome panel. This also covers the empty screen shown today when generation fails.

## 2. UI to match `sudoku_ui.jpeg`

| Mockup element | Implementation |
|---|---|
| Top bar 56dp: ← · "Classic Sudoku · Expert" · ⚙ | Use the `:core:ui` `TopBar` / `IconButton` (added in #5). Title from `BoardSpec` display name (Mini / Medium / Classic) + difficulty display name, plus "· Daily" when in Daily mode. ⚙ opens a settings sheet (lives, theme), or is hidden until settings are reachable from the game |
| Status row 44dp: ⏱ 04:18 · ⚠ Errors 1/3 · 💡 Hints 3/3 | New `GameStatusBar` composable. Tapping the timer pauses |
| Board | Given = bold / onSurface. User digit = primary. Wrong digit = error red. Selected = primary outline. **Crosshair**: whole row + column at ~5–8% primary. **Same number**: 20% primary. Box lines thick, cell lines thin. Board fills the free space (`weight(1f)` + `aspectRatio(1f)`), no `verticalScroll` |
| Toolbar 56dp: Undo · Notes (On badge) · Erase · Hint | Round icon buttons with label. "On" chip on Notes in note mode. Hint shows remaining count badge |
| Number pad 64dp | 1..size keys with a **remaining-count** badge (`size - placedCorrectly(digit)`). Key disabled at 0 |
| Overlays | `FinishPanel` split into Success / Failed / Revealed. `PauseScrim` unchanged |

Current extra buttons *(decision D4)*: **Pause** moves to tapping the timer (with a pause icon). **Check** is removed, since mistakes are already flagged instantly. **Solve** is debug-only.

## 3. Notes: recommended feature set (requirement 4)

Ranked by value. Items 1–4 are recommended for this round; 5–6 are optional.

1. **Auto-clean peer notes**: placing a correct digit (typed or hinted) removes that digit from notes in the same row, column and box. This is the most-requested QoL feature in Sudoku apps.
2. **Note-mode toggle as in the mockup**: Notes button with an "On" chip. In note mode, digit keys toggle notes and the keypad shows a note-style look (outlined keys). **Long-press a digit** = toggle that note without switching modes.
3. **Highlight matching notes**: when the selected cell's digit is N, show N in other cells' notes in primary/bold. This helps with spotting singles.
4. **Undo covers notes**: every board change (value, note, erase, hint) pushes a snapshot. Undo restores `values` + `notes` only and never refunds lives or hints.
5. *(Optional, setting off by default)* **Auto candidates**: a "Fill notes" action or setting that fills every empty cell with legal candidates (`InputValidator.isLegalPlacement`). Useful for Expert 9×9, but it makes the game easier, so it stays opt-in.
6. *(Optional)* Notes are ignored by win detection and never cost a life (already true today). Keep it that way and add tests.

## 4. Undo (in the mockup toolbar)
- `GameViewModel` keeps `ArrayDeque<BoardSnapshot>(values, notes)` capped at ~100 entries. Push before every `InputNumber` / `ToggleNote` / `Erase` / hint.
- `Undo` pops one snapshot and restores it. Mistakes and hints used stay as they are. Undo is disabled when the stack is empty, or when the game is finished or paused.

## 4.5 Where UI components live: `:core:ui` vs `:feature:game`

**Rule:** a component goes in `:core:ui` if it is game-agnostic and takes only plain parameters (text, numbers, icons, lambdas). It must never import `GameUiState` / `GameEvent`. Anything Sudoku-specific stays in `:feature:game` and is composed from `:core:ui` parts. `:feature:game` already depends on `:core:ui`.

**Follow the existing `:core:ui` convention** (as in `IconButton` / `TopBar`):
- `XxxUiContent`: data and callbacks.
- `XxxUiConfig`: visibility and behaviour flags (`VisibilityConfig`).
- `XxxUiStyle`: colors, sizes, shapes.
- Icons come from `IconSet` / `LocalIconSet` through `Theme.icons`. Text goes through `BaseText` + `TextConfig`. Every component gets a `XxxPreview.kt` with dark and light previews.

### New in `:core:ui`
| Component | Package | Used for (mockup) |
|---|---|---|
| `IconSet` additions: `undo`, `notes` (pencil), `erase`, `hint` (bulb), `timer`, `error`, `pause` | `primitives/iconset` + `res/drawable/ic_*.xml` | Toolbar, status row |
| `Badge` (count) | `components/badge` | Remaining digits on the number pad, hints left on the Hint button. Hidden at 0 or shown in a disabled tint (config flag) |
| `Badge` text variant ("On") | `components/badge` | Notes toggle state |
| `LabeledCircleButton` = existing `IconButton` + label + optional `Badge` slot + `enabled` / `selected` | `components/button` | Undo · Notes · Erase · Hint |
| `StatusChip` (icon + text, e.g. `⏱ 04:18`, `Errors 1/3`, `Hints 2/3`) | `components/status` | Status row (each cell), tappable variant for pausing on the timer |
| `KeyButton` (big digit + corner `Badge` + enabled / selected / note-style) | `components/button` | Number pad keys (game-agnostic: any label) |
| `ResultDialog` (title, subtitle, stat rows, primary / secondary / tertiary actions) | `components/dialog` | Success / Out of lives / Solution revealed panels |
| Tokens: `Dimens` (`topBar = 56.dp`, `statusRow = 44.dp`, `toolbar = 56.dp`, `keypad = 64.dp`, ...) + `GameColors` (`selected`, `crosshair`, `sameValue = primary @20%`, `conflict`, `given`, `userValue`) exposed through `Theme.dimens` / `Theme.gameColors` | `primitives` | Consistent sizes and highlight colors across games |

### Stays in `:feature:game` (Sudoku-specific, built from the above)
- `GameBoard`, `CellView`, `NotesGrid`: grid geometry, box borders, crosshair / same-value logic.
- `GameStatusBar`: maps `GameUiState` to 3 × `StatusChip`.
- `GameToolBar`: maps state to 4 × `LabeledCircleButton` (undo enabled, notes "On", hints badge).
- `NumberPad`: computes remaining counts per digit, then renders 1..size × `KeyButton`.
- `GameTopBar`: wraps `:core:ui` `TopBar` with the "Classic Sudoku · Expert" title.
- Finish panels: map `outcome` to `ResultDialog` content.

These move into phase P3. The `:core:ui` pieces land first, in the same PR or a preceding one, each with previews.

## 4.6 First-run guided tour (coach marks)
- Runs the first time a game screen opens. A spotlight overlay highlights one element at a time and shows a tip bubble with **Next / Skip** and a step count ("2 of 6").
- Steps, in order:
  1. Timer: "Tap to pause / resume".
  2. Errors: "You have 3 lives; a wrong digit costs one".
  3. Notes: "Toggle pencil marks; long-press a digit for a quick note".
  4. Hint: "Fills the selected cell; the count goes down".
  5. Undo / Erase.
  6. Number keys: "Badge = how many of this digit are left".
- The timer is paused while the tour is showing.
- `:core:ui`: `CoachMarkOverlay` + `Modifier.coachMarkTarget(id)`. Bounds come from `onGloballyPositioned`. Content / config / style follow the usual pattern. The component knows nothing about Sudoku.
- `:feature:game`: the `SudokuTour` step list (ids + string resources) and the anchors on the timer, toolbar and keypad.
- `:app`: `OnboardingPrefs.hasSeenGameTour` (SharedPreferences). Settings gets "Show tips again".

## 5. Delivery phases (each one PR into `develop`)

| Phase | Content | Tests (`:feature:game:testDebugUnitTest`) |
|---|---|---|
| P1 Rules | §1 state (`maxHints`, `hintsUsed`, `outcome`), configurable lives, hint rules, `RetrySamePuzzle`, collected effects + Snackbar, success / retry / revealed panels | Reducer: outcome transitions, hint decrement / no-op on correct cell / blocked at 0, lives at 0 → `FAILED`, retry restores givens, lives and hints. VM: `HintFilled` / `GameOver` / `PuzzleCompleted` effects (`kotlinx-coroutines-test`) |
| P2 Notes + Undo | §3 items 1–4, §4 | Peer-note cleanup, undo restores values and notes but not counters, cap |
| P3 Mockup UI | §4.5 `:core:ui` components (+ previews) first, then §2: top bar, status row, board highlights, toolbar, keypad badges, fixed bottom layout, title display names | `BoardGeometry` crosshair helpers, remaining-count calculation. `@Preview` per 4×4 / 6×6 / 9×9 compared to the mockup. Emulator screenshots |
| P4 Config | `GameRemoteConfig` + parser in `:core:logic`, bundled `assets/game_config.json`, `GameConfigRepository` in `:app`, `GameRules` passed through `PlayRequest` (see `game_config_json.md`). Settings: optional auto-candidates. Also fix `playRequest` using `remember` (game lost on rotation) | Parser / validation / `rulesFor` precedence / bundled JSON == today's defaults |
| P5 Onboarding | §4.6 `CoachMarkOverlay` (`:core:ui`), `SudokuTour`, `OnboardingPrefs`, "Show tips again" | Prefs round-trip; Compose UI test: the tour shows once and Skip dismisses it |

| P6 Rewards | Revive and extra-hint flow with `RewardedAdGateway` (fake in debug), `rewards` limits; AdMob integration last | ViewModel with a fake gateway: revive, limits, ad failure |

Every phase: `./gradlew test assembleDebug` green, plus a short emulator run of the changed flow.

## 6. Decisions
- **D1 (decided):** Solve exists only in debug builds (`BuildConfig.DEBUG`). It asks for confirmation, then shows a neutral "Solution revealed" panel (`outcome = REVEALED`; not a win, and never counted in future stats or streaks) with Retry same puzzle / New game / Exit. No "Give up" in release for now.
- **D2 (decided):** The failed panel offers **Retry same puzzle** (`RetrySamePuzzle`) + New game + Exit.
- **D3 (revised):** Lives and hints come from the **game config JSON** (`docs/game_config_json.md`), default 3/3, with no player setting. A player-chosen lives count would make the "watch an ad for +1 life / +1 hint" reward pointless. Awaiting confirmation.
- **D4 (decided):** Toolbar = Undo · Notes · Erase · Hint. Pause = tap the timer. Check removed. Solve debug-only.
