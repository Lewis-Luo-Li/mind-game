# Game config JSON: generator, lives, hints, ads

Rule from the design discussion: **code ships in the APK, settings come from a download, puzzles are generated on the device.**
The JSON never holds puzzle numbers. It only holds the settings that control generation, game rules and ads, so they can be tuned without a Play Store release.

## 1. Today
- `DifficultyConfig.default()` (`:core:logic`) is hard-coded and created separately in `HomeScreen` and `GameFactory`.
- `DifficultyConfig.dailySeed` (20260817) is **never used**. Daily mode uses the device date: `LocalDate.now().toEpochDay()`, then `seed * 10 + size` inside the generator.
- Lives (`GameConfig.maxMistakes = 3`) are hard-coded in `:feature:game`. Hints have no limit.
- The JSON in `sudoku_design.md` §6 uses flat keys (`"4x4": {"easy_holes": 3}`) and only covers the generator.

## 2. Proposed schema (v1)

```json
{
  "schema_version": 1,
  "config_version": "2026-10-15.1",

  "sudoku": {
    "generator": {
      "max_retry": 50,
      "holes": {
        "mini":    { "easy": 3,  "medium": 5,  "hard": 6 },
        "medium":  { "easy": 10, "medium": 14, "hard": 18, "expert": 20 },
        "classic": { "easy": 36, "medium": 42, "hard": 48, "expert": 55 }
      }
    },

    "rules": {
      "default": { "lives": 3, "hints": 3 },
      "overrides": [
        { "mode": "daily", "lives": 3, "hints": 3 },
        { "spec": "classic", "difficulty": "expert", "hints": 5 }
      ]
    },

    "daily": {
      "enabled": true,
      "seed_salt": 0
    }
  },

  "rewards": {
    "extra_life": { "enabled": true, "amount": 1, "max_per_game": 2, "allow_in_daily": true },
    "extra_hint": { "enabled": true, "amount": 1, "max_per_game": 3, "allow_in_daily": true }
  },

  "ads": {
    "enabled": false,
    "interstitial": { "enabled": false, "min_games_between": 3, "min_seconds_between": 180 }
  },

  "features": {
    "onboarding_tour": true,
    "notes_auto_fill": false,
    "crossword": false,
    "zooduku": false
  }
}
```

Field notes:
- **Keys are the Kotlin enum names in lowercase** (`BoardSpec.MINI` → `"mini"`, `Difficulty.EXPERT` → `"expert"`). Leaving out a combination makes it unavailable. This matches today's `DifficultyConfig.holes` behaviour (4×4 has no expert).
- **`rules`:** `default`, then the most specific matching `override`. A match on `mode` + `spec` + `difficulty` beats a partial match, and later entries win ties. That gives `lives` and `hints` for a game. Example: Daily is fixed at 3/3 so every player has the same rules.
- **`rewards`:** what "watch an ad" grants once lives or hints run out. `max_per_game` stops endless revives. `allow_in_daily` keeps Daily fair if you ever add rankings.
- **`ads.enabled`:** a master switch, off until AdMob is integrated. With rewards on and ads off, a debug/fake ad grants the reward straight away (placeholder button).
- **`daily.seed_salt`:** mixed into the date-based seed (`epochDay`). Changing it remotely produces a fresh set of daily puzzles. It replaces the unused fixed `dailySeed`.
- **`features`:** feature flags. Ship code hidden behind a flag and switch it on remotely, for example for Crossword or a staged rollout.
- **`schema_version`:** bumped only on breaking changes. The app ignores a config whose `schema_version` is newer than it understands and keeps using the previous one. **`config_version`:** lets you track which settings a player had (logs/analytics).

## 3. Where it lives in code

| Layer | What |
|---|---|
| `:core:logic` (pure Kotlin) | `config/GameRemoteConfig.kt`: `@Serializable` data classes for the schema above. `GameConfigParser.parse(json): Result<GameRemoteConfig>` (kotlinx-serialization-json with `ignoreUnknownKeys = true`). `validate()`: holes `1 until size*size`, lives `1..9`, hints `0..9`; anything invalid → fall back to defaults for that section. Mapping `toDifficultyConfig()`, plus `rulesFor(mode, spec, difficulty): GameRules(lives, hints)` |
| `:app` | `GameConfigRepository`: (1) bundled `assets/game_config.json` = defaults that always work offline → (2) last downloaded copy cached in `filesDir` → (3) background fetch on startup, applied from the **next** game. Exposes `StateFlow<GameRemoteConfig>`. One instance (replaces the two `DifficultyConfig.default()` calls) |
| `:feature:game` | `GameEvent.NewGame(..., rules: GameRules, rewards: RewardRules)`. The ViewModel copies the rules into `GameConfig` at game start, so a mid-game config update never changes a running game. `RewardedAdGateway` interface (`suspend fun show(type): Boolean`), with a fake implementation in debug builds and an AdMob one in `:app` later |

**Remote source options:**
- **Firebase Remote Config (recommended):** one string parameter `game_config_v1` holding the JSON above. You get A/B testing and percentage rollout for free, and it fits with AdMob and Analytics later.
- **Static file** (GitHub raw / CDN): zero setup, but no targeting. Either way the `GameConfigRepository` interface stays the same.

## 4. Out-of-lives / out-of-hints flow with rewarded ads

```
mistake → lives == 0 → status FINISHED_PENDING, timer stopped, board locked
  ├─ "Watch ad: +1 life"   (shown if rewards.extra_life.enabled && revivesUsed < max_per_game)
  │     ad completed → lives += 1, revivesUsed++, status RUNNING
  │     ad failed/cancelled → stay on panel
  ├─ "Retry same puzzle"   → RetrySamePuzzle (lives, hints and rewards reset)
  ├─ "New game"
  └─ "Exit"

Hint tap with hintsLeft == 0
  └─ Hint button shows an ad badge → "Watch ad for +1 hint?"
        ad completed → hints += 1, hintRewardsUsed++, then apply the hint right away
        max_per_game reached → button disabled, "No hints left"
```

State additions to `GameProgress`: `bonusLives`, `bonusHints`, `livesRewardsUsed`, `hintRewardsUsed`. The status row shows `Errors 2/3` and `Hints 0/3`. The total includes any bonus. `outcome = FAILED` is final only after the player picks Retry, New game or Exit.

## 5. Tests
- Parser: valid JSON round-trip; unknown keys ignored; missing sections → defaults; invalid values (holes ≥ size², lives 0) → section defaults; `schema_version` too new → rejected.
- `rulesFor`: default vs `mode` / `spec` / `difficulty` override precedence.
- Mapping: `toDifficultyConfig()` equals today's `DifficultyConfig.default()` for the bundled JSON. This guards against changing difficulty by accident.
- ViewModel with a fake `RewardedAdGateway`: revive restores RUNNING and +1 life; `max_per_game` is respected; an ad failure keeps the panel; a +1 hint from an ad fills the cell.

## 6. Trade-offs and rollout

| Pros | Cons / risk | Mitigation |
|---|---|---|
| Tune difficulty, lives and hints without a release | A bad value (e.g. too many holes on 4×4) → generation fails after `max_retry` | Pre-publish check: run the generator against the candidate JSON (N puzzles per combination, all unique) |
| A/B test rewards and ad frequency (Firebase) | Daily puzzle differs between players on different config versions | Daily settings get `effective_from` (date) so everyone switches on the same day |
| Remote switches / kill switch for ads and new games | Changes are not instant (next game; Firebase fetch interval about 12h) | Accept; use a shorter fetch interval in debug only |
| One source of truth, testable pure-Kotlin parser | Old app versions read old schemas forever | Additive changes only; `schema_version` gate |
| Offline-safe (bundled copy + cache) | Harder bug reproduction | Log `config_version` with bug reports and analytics |
| | Firebase SDK, `google-services.json`, INTERNET permission, Data safety form | Defer to step 2 |
| | Cached JSON can be edited on rooted devices | Low stakes; AdMob server-side verification if rewards ever matter |

**Rollout:**
1. **Step 1 (P4):** schema + parser + validation, read from the **bundled** `assets/game_config.json` only. No network.
2. **Step 2 (later, before ads / A/B):** add the Firebase Remote Config download, the pre-publish generator check, and Daily `effective_from`.
