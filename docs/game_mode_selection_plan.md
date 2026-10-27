# Game Mode Selection Redesign — Implementation Plan

Status: decisions settled, ready for Phase 1 · Scope: Sudoku entry screen (`HomeScreen.kt`) + Daily/Ladder plumbing

## 1. Target UX

```
┌──────────────────────────────┐
│ Mind Game                  ⚙ │
│                              │
│ ┌──────────────────────────┐ │
│ │ ☀ Daily Challenge        │ │
│ │ Sun 26 Oct · Hard · 9×9  │ │
│ │ New puzzle in 03:12:45   │ │  ← countdown to next 5pm Sydney
│ │ [ Play ]  / ✓ Done 05:32 │ │
│ └──────────────────────────┘ │
│ ┌──────────────────────────┐ │
│ │ 🪜 Level 12              │ │
│ │ Hard · Round 3 · 7/20      │ │  ← position inside Easy→Expert cycle
│ │ [ Continue ]             │ │
│ └──────────────────────────┘ │
└──────────────────────────────┘
```

- Only two primary actions: **Daily Challenge** and **Level N**. Board-size and difficulty chips go away.
- Win in ladder → success panel shows **Next level** (starts N+1). Fail → **Retry level** (same puzzle). No "New game" in ladder/daily.
- Win in daily → panel shows "Come back in 3h 12m"; card on home shows ✓ + time.
- In-game top bar shows the context: "Daily · 26 Oct" or "Level 12 · Hard".

## 2. Rules

### Daily challenge (resets 17:00 Australia/Sydney)
- `puzzleDay` = the Sydney calendar date on which the current window opened:
  ```kotlin
  val t = clock.instant().atZone(ZoneId.of("Australia/Sydney"))
  val puzzleDay = if (t.toLocalTime() < resetTime) t.toLocalDate().minusDays(1) else t.toLocalDate()
  val nextResetAt = (if (t.toLocalTime() < resetTime) t.toLocalDate() else t.toLocalDate().plusDays(1))
      .atTime(resetTime).atZone(zone).toInstant()
  ```
  Compare **wall-clock** time, not `now − 17h`: subtracting hours runs on the instant timeline, so on DST-change days the rollover would drift to 18:00 (first Sun Oct) or 16:00 (first Sun Apr) — verified with java.time. The IANA zone keeps 17:00 correct in both AEDT and AEST, independent of the device's own timezone, so every player worldwide gets the same puzzle at the same instant (also the leaderboard `dayKey`).
- `seed = puzzleDay.toEpochDay() + daily.seed_salt` (today it is `LocalDate.now()` in device TZ).
- `nextResetAt` drives the countdown (recomputed each second while home is visible).
- A game started before the reset belongs to the puzzleDay it was started on (carry `puzzleDay` in `PlayRequest`), so finishing at 5:03pm still records yesterday's completion.
- Completion record per puzzleDay: `{solved, elapsedSeconds, mistakes, hintsUsed}`.

### Ladder ("Level N")
- A **round** = 20 levels = 5 × each tier (`levels_per_tier = 5`). `round(N) = (N−1)/20`, `slot = (N−1)%20`.
- **Round 1** (L1–20) is ordered: 5 Easy → 5 Medium → 5 Hard → 5 Expert (onboarding ramp).
- **Round 2+** is a deterministic shuffle of the same 20-slot bag, seeded by `(round, ladder.seed_salt)` → same order for every player; constraint: no more than 2 Expert in a row (re-shuffle with next derived seed until valid). Config: `shuffle_from_round: 2`, `max_consecutive_expert: 2`.
- Board: 9×9 Classic (only spec that has all four tiers; Mini has no Expert).
- `seed(N) = mix(LADDER_TAG xor mix(N)) + ladder.seed_salt` (SplitMix64) → level N is the same puzzle for everyone, Retry is the same puzzle.
- **Seed rules** (verified on the generator): the same seed at a different difficulty yields the *same solution grid* and the easier puzzle's holes are a subset of the harder one's → every level must have its own seed; ladder seeds live in their own namespace (tagged hash) so they can never equal a daily seed (`epochDay + salt`). Capacity: 64-bit seed space (~1.8×10¹⁹) ≪ 6.7×10²¹ valid grids → effectively unlimited; probe of 300 seeds/tier gave 300/300 distinct puzzles, 0 failures, avg gen 0.4 / 0.4 / 2 / 76 ms (Easy/Medium/Hard/Expert, JVM).
- Progress: `currentLevel` (next unplayed), advances **only on `GameOutcome.SOLVED`**. `REVEALED` (debug Solve) and `FAILED` never advance. Rewarded revive is allowed (it ends in SOLVED).

## 3. Config additions (`game_config.json`, parsed in `:core:logic` `config`)

```json
"daily":  { "enabled": true, "seed_salt": 0,
            "timezone": "Australia/Sydney", "reset_time": "17:00",
            "spec": "classic", "difficulty": "hard" },
"ladder": { "enabled": true, "seed_salt": 0, "spec": "classic",
            "cycle": ["easy", "medium", "hard", "expert"], "levels_per_tier": 5,
            "shuffle_from_round": 2, "max_consecutive_expert": 2,
            "holes_step_per_round": 0 }
```
Invalid values fall back to defaults (existing convention). `rules.overrides` gains `"mode": "ladder"` support for free (it already matches on `mode` string).

## 4. Code changes by module

| Module | Change |
|---|---|
| `:core:logic` | `DailySchedule(zone, resetTime, clock)` → `puzzleDay()`, `nextResetAt()`; `LadderPlan(config)` → `difficultyFor(level)`, `seedFor(level)`, `cyclePosition(level)`; config models + validation for `daily.*` / `ladder`. Pure JVM, `java.time.Clock` injected for tests. |
| `:feature:game` | `GameMode.LADDER`; `GameFactory` requires a seed for LADDER like DAILY; `GameConfig` carries a `ModeContext` (`Daily(puzzleDay)` / `Ladder(level)`) for the top-bar label; `GameStatusOverlays` success/fail buttons switch by mode (Next level / Retry level / Come back in …); new callback `onNextLevel`. |
| `:core:ui` | `ModeCard(title, subtitle, badge, statusText, actionLabel, onClick, enabled)` — generic (fits Crossword/Zooduku), flat, `BaseText`; `CycleProgressDots(total, filled)`. JVM tests + Compose UI tests + previews per repo convention. |
| `:app` | `PlayerProgressRepository` (SharedPreferences, same pattern as `ThemePrefs`, behind an interface with an in-memory fake): `ladderLevel`, `dailyResult(puzzleDay)`. `HomeScreen` rewritten to two `ModeCard`s + countdown. `MindGameApp.onGameFinished` records results (needs `PlayRequest.context`). Remove `DailySeed` and free-mode chips. |
| docs | README UI-flow Mermaid + `GameMode` section; `docs/game_config_json.md` new keys. |

## 5. PR breakdown (stacked on `develop`)

1. **Phase 1 – Logic & config**: `DailySchedule`, `LadderPlan`, config parsing/validation + unit tests (DST edges: 2026-10-04 17:00–17:59 must be the new day, 2026-04-05 16:00–16:59 must still be the old day; 16:59 vs 17:00; device in another TZ; level→tier mapping for 1 and 5 per tier; seed stability). No UI.
2. **Phase 2 – Plumbing & persistence**: `GameMode.LADDER`, `PlayRequest` context, `PlayerProgressRepository`, outcome → progress recording, overlay buttons per mode, top-bar label. Reducer/VM tests (REVEALED/FAILED don't advance; Next level starts N+1 with correct seed/difficulty).
3. **Phase 3 – Home redesign**: `ModeCard`, `CycleProgressDots`, new `HomeScreen`, countdown, completed state. Compose UI tests + previews + emulator screenshots (before 5pm / after win / ladder mid-cycle / small screen via `wm size`).
4. Docs/README sync can ride in phase 3.

Rough effort: ~1 Devin session for all three PRs.

## 6. Risks / notes
- **Daily puzzle changes on rollout day** (seed source switches from device-local date to Sydney puzzleDay). Acceptable pre-launch.
- **Clock tampering**: device clock can be moved to unlock tomorrow's daily. Fine offline; becomes server-time-authoritative when the leaderboard backend lands.
- **Rotation/process death** still drops the in-progress game (known `playRequest` issue); ladder progress is safe because it's written only on SOLVED.
- Ladder seed determinism + daily Sydney window are deliberately leaderboard-ready (same puzzle per level/day for everyone).

## 7. Decisions
| # | Question | Decision |
|---|---|---|
| 1 | Free mode | **Dropped entirely** (remove chips, `GameMode.FREE` entry point from home) |
| 2 | Daily difficulty | **Hard** (48 holes), fixed, fresh puzzle from the puzzleDay seed |
| 3 | Daily board size | **9×9** (ladder also 9×9) |
| 4 | Ladder levels per tier | **5** per tier; round 1 ordered, **shuffled from round 2**, max 2 Expert in a row |
| 5 | Later cycles | **Identical tiers**; `holes_step_per_round: 0` knob for a later ramp |
| 6 | Replay | **Allowed**, cleared levels / finished daily can be replayed but never re-counted |
