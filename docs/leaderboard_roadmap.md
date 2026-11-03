# Daily dashboard + top-3 hint rewards: product roadmap

Status: **proposal, needs decisions** (see [§8](#8-open-decisions)). Docs only, nothing here is implemented.
Related: `docs/game_config_json.md` (`rewards` section) and `docs/game_mode_selection_plan.md` (the Daily Challenge + Level ladder redesign; see [§0.1](#01-combined-with-the-game-mode-redesign)).

**TL;DR**
- A **Daily Dashboard** ranks everyone who solved today's Daily Challenge. The day runs from 5 pm to 5 pm Sydney time.
- When the window closes, the **top 3 eligible players get bonus hints**. The server grants them into a hint wallet.
- **Recommended backend: Firebase** (Anonymous Auth → link Google, Firestore, Cloud Functions, Cloud Scheduler with a Sydney time zone, App Check/Play Integrity, FCM).
- Google Play Games leaderboards **don't fit as the source of truth**. Their daily reset is fixed at UTC−7 midnight, which is 5 pm Sydney only during AEST. They also can't grant rewards.
- 5 phases. Phase 0 (local stats + Sydney-time daily) has value without any backend. Rewards come in Phase 3, after anti-cheat data from Phases 1–2.

---

## 0. Where we are today (verified in code)

| Area | Today | Gap for a leaderboard |
|---|---|---|
| Daily seed | `DailySeed.forToday()` = `LocalDate.now().toEpochDay()` + `sudoku.daily.seed_salt` (`HomeScreen.kt`) | Device-local midnight, so players in different time zones get different "days". Needs a shared Sydney 5 pm key (redesign in flight) |
| Daily puzzle | Player picks board size; difficulty = hardest available for that size | One daily per size means several boards per day. A leaderboard needs one canonical puzzle, or one board per size |
| Results | `GameOutcome` SOLVED / FAILED / REVEALED, `GameProgress` (mistakes, hintsUsed, bonusHints, bonusLives, elapsed) | Nothing is persisted after the game. No history, no streaks, no "first attempt" flag |
| Retry | `RetrySamePuzzle` replays the same seed | Must not count as a new ranked attempt |
| Hints/lives | Config budget (3/3) + rewarded ads (`extra_hint.max_per_game: 3`, `extra_life.max_per_game: 2`, `allow_in_daily: true`) | Ad bonuses are an advantage on a ranked board and have to be scored or excluded |
| Backend | None. Fully offline | Needs identity, storage, server validation, scheduled close |

### 0.1 Combined with the game-mode redesign
The game-mode plan (`docs/game_mode_selection_plan.md`) replaces the home screen with two modes: **Daily Challenge** (resets 17:00 Sydney) and a **Level N ladder** (Easy → Medium → Hard → Expert, repeating). This is how the two plans fit together:

| Topic | Game-mode plan | Effect on this roadmap |
|---|---|---|
| Daily window | `DailySchedule` in `:core:logic`; `puzzleDay` = the Sydney date the window **opens** | This doc uses the same key (§1.1). Its mode-plan PRs 1–2 **are** most of Phase 0 here |
| DST correctness | Defined as `(now in Sydney − 17h).toLocalDate()` | **Bug to avoid:** `ZonedDateTime.minusHours` works on the instant timeline, so on DST days the rollover lands at **18:00** (first Sun of Oct) and **16:00** (first Sun of Apr). Compare local time instead: `if (localTime < 17:00) date − 1 else date`. Verified with java.time |
| Daily board | Config `daily.spec` = classic 9×9; difficulty fixed or rotating (mode-plan decision 2) | Answers D1: one ranked board per day. With a rotating difficulty, times are comparable within a day but not across days (this matters only for weekly boards in Phase 4) |
| Seed | `puzzleDay.toEpochDay() + daily.seed_salt` (public, computed on the client) | Fine for Phase 0. Phase 1 switches ranked play to the server-published puzzle with a secret salt (§2.3 V3); local generation stays as the offline, unranked fallback |
| Results | `PlayerProgressRepository` (SharedPreferences): `ladderLevel`, `dailyResult(puzzleDay)` | Becomes the local cache for Phases 0–2. Streaks come from `dailyResult`. The move log still needs adding |
| Replay | Replays must not count (mode-plan decision 6) | Matches the first-attempt rule (§1.4) |
| Free mode | May be dropped or kept as "Practice" (mode-plan decision 1) | Prize hints are spent in **Ladder** (+ Practice if kept), not Daily |
| Ladder | Same puzzle per level for everyone, progress stored locally | New option: show the player's **ladder level** on the dashboard (D13). Not recommended for prizes: it is a lifetime race and progress is local (easy to edit) until it is server-validated |

---

## 1. Product definition

### 1.1 Daily window (aligned to the 5 pm Sydney reset)
- **dayKey** = `puzzleDay` from the game-mode plan: the Sydney calendar date on which the window *opens* (e.g. `2026-10-25` = Oct 25 17:00 → Oct 26 16:59:59 `Australia/Sydney`).
- The client and server compute it the same way: take the local time in `ZoneId.of("Australia/Sydney")`, then `if (time < 17:00) date − 1 else date`. Don't use `minusHours(17)`, which is off by an hour on DST days (§0.1). Days are 23 h / 25 h on transition days, which is fine.
- The seed is derived from `dayKey` (plus the salt), never from device-local midnight.
- The UI always shows the reset in **local time**, e.g. "Resets in 3h 12m (5:00 pm Sydney · 7:00 am London)".

| Moment (Sydney) | What happens |
|---|---|
| 17:00 | New daily opens. Yesterday's board becomes **read-only "final pending"** |
| 17:00–17:15 | **Grace window**: queued offline submissions for the previous dayKey are accepted if the attempt started before 17:00 and the server-side duration is plausible |
| 17:15 | Scheduled job **freezes** the board, runs final anti-cheat checks, writes `results/{dayKey}` and grants top-3 rewards |
| 17:15+ | Winners get a push notification and an in-app "Claim" card. Everyone sees the podium on the "Yesterday" tab |

### 1.2 What the dashboard shows

| Section | Content | Phase |
|---|---|---|
| Header | Countdown to close, participants so far, today's board (size/difficulty) | 2 |
| My card | Not played / In progress / Solved: time, mistakes, hints, **rank + percentile** ("#42 of 1,380 · top 3%") | 2 |
| Top list | Top 50: rank, avatar/initial, display name, time, mistakes, hints, streak flame. "You" row pinned if outside the top 50 | 2 |
| Yesterday | Podium (top 3 + rewards), your final rank, median time | 3 |
| Friends tab | Same table filtered to friends | 4 |
| Streaks | Current / best daily streak (consecutive dayKeys solved) | 0 (local) → 4 (shown on board) |

Global first. Friends needs a social graph, so it waits until Phase 4.

### 1.3 Scoring options

| Option | Formula (lower = better unless noted) | Pros | Cons |
|---|---|---|---|
| **A. Penalised time** *(recommended)* | `score = elapsedSec + 30·mistakes + 60·hints + 120·revives` | Easy to explain ("+30 s per mistake"), one number, rewards speed and accuracy | Penalty constants need tuning |
| B. Pure time, clean-only | Rank only solves with 0 hints / 0 revives by time | Simplest, fairest for experts | Excludes most casual players, so the board looks empty for them |
| C. Points | `1000 − mistakes·50 − hints·100 + timeBonus` (higher = better) | Feels "gamey", easy to extend with streak bonus | Harder to explain, harder to audit |

**Tie-breaks** (in order): fewer mistakes → fewer hints → earlier server-received submit time → lower `uid` hash (deterministic).

### 1.4 Eligibility

| Rule | Ranked on board | Prize-eligible (top 3) |
|---|---|---|
| Outcome = SOLVED (not FAILED, not REVEALED/debug Solve) | Required | Required |
| **First attempt** of the dayKey (Retry/Restart don't create a new ranked attempt) | Required | Required |
| Release build + Play Integrity pass | Required | Required |
| Ad-granted hint/life used | Allowed, penalised | Allowed, penalised (or excluded, see D5) |
| **Banked (prize) hints used** | Not usable in Daily *(recommended)* | Same. Otherwise winners keep winning with their prizes |
| Google-linked account (not anonymous only) | Not required | Required (reduces alt-account replay farming) |
| Min participants that day | — | Rewards only if ≥ 20 eligible entries (avoids "3 players, 3 winners") |

### 1.5 Rewards for top 3

| Option | 1st / 2nd / 3rd | Expiry | Notes |
|---|---|---|---|
| **A. Banked hints** *(recommended)* | 5 / 3 / 2 hints | 14 days after claim | Goes into a server wallet, spendable in Ladder (and Practice, if kept) |
| B. Next-daily bonus | +2 / +1 / +1 hints on the next Daily only | Next dayKey | No wallet needed, but it hands the leaders an advantage on the next board |
| C. Cosmetic + small hints | Badge/crown on name for 24 h + A | — | Bragging rights work for retention without devaluing rewarded ads |

What winners see:
1. A push notification: "You placed #1 in yesterday's Daily – 5 hints are waiting".
2. On next app open, a **Claim** card with the podium and an animated hint counter. Claiming writes to the wallet server-side and is idempotent.
3. A crown on their name for the next day (option C).

Unclaimed prizes expire after 7 days.

---

## 2. Backend options

### 2.1 Comparison

| | Google Play Games Services (PGS) leaderboards | **Firebase** *(recommended)* | Supabase | Custom server (Ktor + Postgres) |
|---|---|---|---|---|
| Daily reset | **Fixed by Google: UTC−7 midnight** (= 07:00 UTC). That is **17:00 Sydney in AEST (Apr–Oct) but 18:00 in AEDT (Oct–Apr)**, so it matches only half the year. Not configurable | Ours: Cloud Scheduler cron with `timeZone: Australia/Sydney` handles DST | Ours: `pg_cron` runs in UTC, so DST needs an hourly job that checks Sydney time | Ours |
| Score shape | One `long` per player per board. Tie-breaks would have to be packed into the number | Any document | SQL rows + `rank()` window functions | Anything |
| Grant rewards | **No server hooks/webhooks.** Reading the top 3 server-side means calling the Games REST API with player OAuth. Not designed for this | Cloud Function at close → wallet write + FCM | Edge Function / SQL function | Yes |
| Anti-cheat | Client-submitted scores; "tamper protection" hides outliers + min/max score limits | Callable function validates; **App Check with Play Integrity provider built in** | Must verify the Play Integrity token yourself in an Edge Function | Verify it yourself |
| Identity | PGS v2 auto sign-in on Android, Friends API | Anonymous Auth → link Google (Credential Manager) | Anonymous sign-ins + Google ID token | Build it / use a provider |
| UI | Stock Google leaderboard UI, or REST to build our own | Our Compose UI | Our Compose UI | Our Compose UI |
| Reuse `:core:logic` (Kotlin) on server | n/a | Java runtime on Cloud Run functions / Cloud Run job | Edge Functions are Deno/TS, so a separate JVM job is needed | Yes, native |
| Cost at 1k DAU | Free | ~$0 (needs **Blaze** plan for Functions/Scheduler; usage inside free quotas) | Free tier (pauses after 7 days of inactivity) | ~$10–30/mo min (Cloud Run/Fly + managed Postgres) |
| Cost at 100k DAU (rough) | Free | ~$20–60/mo (Firestore writes/reads + functions) | Pro $25/mo + usage | ~$50–150/mo + ops time |
| Ops burden | None | Low | Low–medium | High (auth, scaling, backups, security) |
| Fits with existing stack | Play only | AdMob ↔ Firebase linking, FCM, Remote Config (previously deferred) all in one console | Neutral | Neutral |

Cost figures are order-of-magnitude estimates. Check the current pricing pages before committing.

### 2.2 Recommendation
**Firebase as the source of truth.** Optionally **mirror** each verified score to a PGS leaderboard later for discoverability (Play Games profile, achievements), without using it for rewards.
- Region: `australia-southeast1` if most players are AU, otherwise `us-central1` (cheaper, more free-tier features). See D9.
- Separate Firebase projects: `mindgame-dev` (debug builds, App Check debug provider) and `mindgame-prod`.

### 2.3 Puzzle source and validation (key design choice)
The server has to know the daily solution to validate submissions. The generator is Kotlin in `:core:logic`.

| Option | How | Pros | Cons |
|---|---|---|---|
| V1 Port generator to TypeScript | Functions regenerate the puzzle from the seed | One runtime | Two generators drift; bugs mean false rejections |
| V2 JVM validation function | Java Cloud Run function depends on `:core:logic` and regenerates from the seed | Single source of code | Cold starts, Java function setup |
| **V3 Server-published puzzle** *(recommended)* | Scheduled JVM job (Cloud Run job or GitHub Actions cron) uses `:core:logic` to pre-generate the next N days into Firestore: `daily/{dayKey}` (givens) + server-only `daily_secret/{dayKey}` (solution). Client downloads at open; offline fallback generates locally but that game is **unranked** | Validation in TS is trivial (`grid == solution`). Secret server salt, so the puzzle can't be precomputed before release. Can re-roll a bad day | Online needed for a ranked attempt |

### 2.4 Data model sketch (Firestore)
```
daily/{dayKey}                 opensAt, closesAt, spec, difficulty, givens, status(open|final)
daily_secret/{dayKey}          solution                      (server-only)
daily/{dayKey}/attempts/{uid}  startedAt (server time)       (server-only, created by startDaily())
daily/{dayKey}/entries/{uid}   displayName, timeMs, mistakes, hints, revives, score, submittedAt, flags
results/{dayKey}               top3[], participants, medianMs (written at close)
users/{uid}                    displayName, linkedGoogle, streak, bestStreak, createdAt
wallets/{uid}                  hintBalance; ledger/{id}: type, amount, dayKey, expiresAt
```
Functions:
- `startDaily()`: callable, issues the single ranked attempt.
- `submitDaily(moveLog)`: callable, validates and writes the entry.
- `closeDaily`: scheduled 17:15 Sydney, freezes and rewards.
- `claimReward()`, `deleteAccount()`.

Security rules: clients **read** entries/results. They **never write** entries, results or wallets.

---

## 3. Identity, accounts, privacy

| Topic | Proposal |
|---|---|
| Sign-in | **Anonymous Firebase Auth** on first dashboard open (zero friction) → optional **"Link Google"** (Credential Manager) to keep progress across devices and become prize-eligible |
| Display names | Default generated name ("Swift Panda 4821"). Custom names: 3–16 chars, profanity filter (word list in a Function) + **Report** button + admin hide flag. No free-text bios/chat |
| Avatars | Initial/colour only (no uploads, so nothing to moderate) |
| GDPR / Australian Privacy Act 1988 (APPs) | Privacy policy update (what's collected, why, retention, region). Lawful basis = contract/legitimate interest for the leaderboard; leaderboard visibility **opt-in** ("Show me on public boards"). Entries older than 90 days aggregated/deleted |
| Data deletion | Play policy requires apps with account creation to offer **in-app account deletion + a web deletion link**. `deleteAccount()` removes user, entries, wallet. Also needs a simple web form/page |
| Kids / Families policy | Sudoku can attract children. If the target audience includes under-13s, **Families policy** applies: no public user-generated names for child users, child-directed ad settings, stricter consent. Recommendation: **target 13+**; or hide public boards behind an age gate. AU children's privacy code (OAIC) is in development; re-check before launch |
| Play Data safety form | New declarations: **User IDs** (Firebase uid), **Name** (display name, optional), **App activity → game progress/in-app actions**, **App info & performance** (if Crashlytics). Encrypted in transit: yes. Deletion request supported: yes. Shared with: none (Firebase = processor). AdMob's existing Device ID declaration stays |
| Ads consent | UMP consent (already a release TODO) should land before or with Phase 1 |

---

## 4. Anti-cheat

Threat: the daily puzzle is deterministic, so the solution can be computed by the client, by a solver app, or from a friend. Prizes are small (hints), so aim for **"expensive enough not to bother"**, not perfection.

| Layer | Measure | Phase |
|---|---|---|
| Rewards | **Only the server grants rewards.** The client never writes the wallet | 1 |
| Attestation | **Firebase App Check + Play Integrity**: reject non-Play, tampered or debug builds (also blocks the debug Solve button) | 1 |
| One attempt | `startDaily()` records server `startedAt`, once per uid per dayKey. Retry = unranked | 1 |
| Grid validation | Final grid must equal `daily_secret.solution` | 1 |
| Move-log replay | Client submits `[(cell, value, tMs, source=user|hint|ad)]`. Server replays it, recomputes mistakes/hints/time, and rejects mismatches | 1 |
| Time plausibility | `serverDuration = submit − startedAt ≥ claimed elapsed` (pause allowed); claimed time ≥ floor per empty cell (e.g. 1.5 s × holes); flag inhumanly uniform move intervals | 1 |
| Rate limits | 1 start + ≤ 3 submit calls per uid per day; per-IP/App Check limits on callables | 1 |
| Alt accounts | Prize requires a linked Google account; optionally Play Integrity **device recall** to cap prize accounts per device | 3 |
| Review | Top 3 auto-flagged if far below the median. Close job can hold the prize for manual review | 3 |
| Remaining risk | Someone memorises the grid on account A and replays it on B. Mitigated by the Google-link requirement, the per-device cap and the plausibility floor, not eliminated | — |

---

## 5. Reward delivery and hint economy

| | Local inventory | **Server-authoritative wallet** *(recommended for prizes)* |
|---|---|---|
| Trust | Editable on rooted devices | Server ledger, auditable |
| Offline | Works | Cached balance; spends sync when online (or require online to spend banked hints) |
| Cross-device | No | Yes (linked Google) |
| Effort | Low | Medium |

How the hint sources stack in one game:

| Source | Where it lives | Cap | Usable in Daily? |
|---|---|---|---|
| Base hints | `sudoku.rules` (3) | per game | Yes (penalised in score) |
| Rewarded-ad hint | `rewards.extra_hint` (+1, `max_per_game: 3`) | per game | Yes, penalised (or no, see D5) |
| **Banked prize hints** | `wallets/{uid}` | New `rewards.banked_hint.max_per_game` (e.g. 3) | No *(recommended)*: Ladder/Practice only |

- Order of use: base → banked → rewarded ad. The ad button appears only when base and banked are both exhausted, so ads still matter.
- Config additions (bundled `game_config.json`): `leaderboard { enabled, prize_hints: [5,3,2], prize_expiry_days, min_participants, penalties { mistake_s, hint_s, revive_s } }`. The server keeps its own copy and its copy wins.
- **Notifications (FCM)**: winner push at close + optional "Daily resets in 1 h" reminder. Needs the Android 13+ `POST_NOTIFICATIONS` runtime prompt: ask after the first daily solve, not at launch. Always show the in-app Claim card as well, because pushes are unreliable.

---

## 6. Phased roadmap

Effort is for one Android/Kotlin engineer, as a rough estimate.

| Phase | Scope | Effort | Depends on | Success metrics | Risks |
|---|---|---|---|---|---|
| **0. Foundations (offline)** | **Mostly delivered by game-mode plan PRs 1–2**: `DailySchedule`/`puzzleDay` in `:core:logic` (shared by the client and the server job), a single 9×9 daily board, `PlayerProgressRepository`. Extra work for the leaderboard: persistent local **game history + stats + streaks** (Room or DataStore); first-attempt flag; record move log in `GameProgress`; local "Stats" screen | +0.5–1 wk on top of the game-mode plan | Game-mode plan PRs 1–2 | Daily D1 retention, % players with ≥ 3-day streak, daily participation rate | DST edge cases (unit tests on transition dates); migration of existing daily seed |
| **1. Accounts + score submission** | Firebase projects (dev/prod), Anonymous Auth + Link Google, App Check/Play Integrity, V3 puzzle publisher job, `startDaily`/`submitDaily` with validation, privacy policy + Data safety + in-app/web deletion, UMP consent | 2–3 wk | Phase 0, D2–D4, D9 | ≥ 95% submissions validate; < 1% false rejections; p95 submit latency < 1 s | Play Integrity quota/setup; Families/age decision blocks launch |
| **2. Dashboard UI** | Dashboard screen (Today / Yesterday tabs), my card + rank/percentile, top 50, countdown in local time, display-name editor + report, opt-in visibility | 1.5–2 wk | Phase 1 | % daily solvers who open the board; D7 retention lift vs Phase 0 baseline | Empty board at low player counts (show percentile + "be first"); moderation load |
| **3. Top-3 rewards** | `closeDaily` scheduler 17:15 Sydney, results doc, wallet + ledger + expiry, Claim card, FCM winner push, prize eligibility rules, manual-review hold, banked hints in Ladder/Practice | 1.5–2 wk | Phase 2 + ~2 weeks of real submission data to tune penalties/thresholds | Daily participation lift; rewarded-ad views/user **not** dropping; flagged-winner rate < 5% | Cheaters taking prizes; hint economy cannibalising ads |
| **4. Social + seasonal** | Friends (invite code or PGS Friends), friends tab, streak shown on board + streak rewards, weekly/seasonal boards, leagues (groups of ~30 by skill, Duolingo-style) so non-experts can place, optional PGS mirror + achievements | 3–4 wk (incremental) | Phase 3 | Invites/user, D30 retention, % players who ever placed top 3 in a league | Scope creep; more moderation/privacy surface |

Total to "top-3 rewards live": **~6–8 engineer-weeks**. Phase 0 can ship on its own.

---

## 7. Risks summary
- **Small player base**: the top 3 is meaningless with few players. Mitigations: the min-participants rule, and leagues in Phase 4.
- **Experts dominate**: the same people win every day. Mitigations: leagues, a per-player prize cooldown (e.g. max 3 prizes/week), streak rewards for everyone.
- **Ad economy**: free prize hints could reduce rewarded-ad views. Mitigations: banked hints are not usable in Daily, and the cap is per game.
- **Compliance**: the account-deletion requirement, the Families policy and Data safety errors can block a Play release. Do them in Phase 1, not at the end.

---

## 8. Open decisions

| # | Decision | Options | Recommendation |
|---|---|---|---|
| D1 | Which board is ranked daily? | **Resolved by the game-mode plan**: one 9×9 daily board (difficulty = mode-plan decision 2) | — |
| D2 | Scoring | A penalised time / B clean-only time / C points | A (`+30 s` mistake, `+60 s` hint, `+120 s` revive) |
| D3 | Puzzle source | V1 TS port / V2 JVM function / V3 server-published | V3 |
| D4 | Backend | PGS / Firebase / Supabase / custom | Firebase (+ optional PGS mirror later) |
| D5 | Rewarded-ad hints/lives in Daily | allowed + penalised / disabled in Daily (`allow_in_daily: false`) | allowed + penalised |
| D6 | Prize amount and expiry | 5/3/2 hints, 14 days / other | 5/3/2, 14 days, + 24 h crown |
| D7 | Can prize hints be used in Daily? | yes / no (spend in Ladder/Practice only) | no |
| D8 | Prize eligibility | anonymous OK / Google-linked required | Google-linked required |
| D9 | Audience and region | 13+ vs includes kids; AU-first vs global | 13+; `australia-southeast1` if AU-first |
| D10 | Leaderboard visibility default | opt-in / opt-out | opt-in |
| D11 | Grace window and close time | 17:15 / 17:30 / none | 17:15 |
| D12 | Friends graph (Phase 4) | invite codes / PGS Friends / contacts | invite codes (no contacts permission) |
| D13 | Ladder on the dashboard? | (a) not shown, (b) show each player's level as profile info, (c) separate "highest level" board, (d) "levels cleared this week" board | (b) in Phase 2; (d) in Phase 4 once ladder results are server-validated. No prizes from the ladder |
| D14 | Daily difficulty vs the leaderboard (with mode-plan decision 2) | fixed (e.g. Hard) / rotate by weekday | Fixed Hard is simplest for comparable times. A weekday rotation works for daily boards but complicates weekly ones |
