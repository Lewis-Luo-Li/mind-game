# Sudoku Universal Library Generation, Solving, and Validation Design (v2.0)

## 1. Core Principles: The Triple-A Strategy
This project strictly avoids static puzzle JSON files or pre-set databases. It follows these core rules:

*   **Generate:** All puzzles (4x4 / 6x6 / 9x9) are generated dynamically on the client using a pure Kotlin algorithm.
*   **Solve:** A unified backtracking solver is built-in for generating complete boards and validating unique solutions.
*   **Validate:** "Unique solution validation" is performed immediately after generation to ensure zero multi-solution or unsolvable puzzles.
*   **Configure:** Parameters like hole counts are dynamically delivered via remote JSON/Firebase to adjust difficulty without app updates.

## 2. Core Data Model & Specification Mapping
All configurations use the `SudokuBoardConfig` data class, with parameters controlling the variations:

```kotlin
data class SudokuBoardConfig(
    val size: Int,          // 4, 6, or 9
    val boxWidth: Int,      // 2, 3, or 3
    val boxHeight: Int,     // 2, 2, or 3
    val solution: IntArray, // Complete board (size * size)
    val puzzle: IntArray,   // Puzzle after digging (0 represents empty)
    val holes: Int          // Actual number of holes
)
```

| Spec | Grid Size | Box Size (W x H) | Number Range | Total Cells | Available Difficulty |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Mini** | 4 x 4 | 2 x 2 | 1 ~ 4 | 16 | Easy / Medium / Hard |
| **Medium** | 6 x 6 | 3 x 2 | 1 ~ 6 | 36 | Easy / Medium / Hard / Expert |
| **Classic** | 9 x 9 | 3 x 3 | 1 ~ 9 | 81 | Easy / Medium / Hard / Expert |

## 3. Puzzle Generator Algorithm
### 3.1 Three-Step Generation
1.  **Generate Complete Board:**
    *   Uses Backtracking + Fisher-Yates shuffle.
    *   Iterates through each empty cell, shuffles candidates 1..size, and attempts to fill while pruning based on row/column/box constraints.
2.  **Hole Digging:**
    *   Reads `holes` count from remote config based on spec and difficulty.
    *   Randomly selects coordinates and sets values to 0.
3.  **Unique Solution Validation & Retry:**
    *   Calls the solver to count solutions. If `count == 1`, accept the puzzle.
    *   If `count >= 2` or `count == 0`, discard and regenerate (max 50 retries).

### 3.2 Hole Count Mapping (Remotely Adjustable)
> [!NOTE]
> For 4x4, more than 7 holes often leads to multiple solutions; thus, "Expert" difficulty is disabled for this size.

| Difficulty \ Spec | 4x4 (Total 16) | 6x6 (Total 36) | 9x9 (Total 81) |
| :--- | :--- | :--- | :--- |
| **Easy** | 2 ~ 3 holes | 8 ~ 10 holes | 32 ~ 36 holes |
| **Medium** | 4 ~ 5 holes | 12 ~ 14 holes | 38 ~ 42 holes |
| **Hard** | 6 holes (Max) | 16 ~ 18 holes | 44 ~ 48 holes |
| **Expert** | N/A | 20 holes (Max) | 50 ~ 55 holes |

## 4. Solver & Uniqueness Validation
### 4.1 Unified Solver Interface
A universal backtracking solver that accepts `size`, `boxWidth`, and `boxHeight`.

```kotlin
interface SudokuSolver {
    fun solve(board: IntArray, config: SudokuBoardConfig): IntArray? // Returns one solution
    fun countSolutions(board: IntArray, config: SudokuBoardConfig, limit: Int = 2): Int
}
```

### 4.2 Validation Logic
*   Call `countSolutions(board, limit = 2)`.
*   **Pruning Optimization:** Stop recursion immediately once 2 solutions are found.
*   **Decision Rules:**
    *   Returns 1 → Valid puzzle.
    *   Returns 2 → Multiple solutions (discard & retry).
    *   Returns 0 → No solution (discard & retry).

### 4.3 Difficulty Quantification (Advanced)
To ensure "Hard" is actually hard, we can count recursive calls or branch nodes:
*   Branches < 100 → Easy
*   Branches 100 ~ 500 → Medium
*   Branches 500 ~ 2000 → Hard
*   Branches > 2000 → Expert

## 5. Non-Repetitive Solution
| Scenario | Strategy | Implementation |
| :--- | :--- | :--- |
| **Free Mode** | Mathematical Uniqueness | With ~6.67 × 10²¹ combinations for 9x9, collision probability is negligible. |
| **Daily Challenge** | Fixed Seed | Server sends `{"seed": 20260817}`. Mixed with size: `seed * 10 + size` to ensure unique but consistent puzzles globally. |

## 6. Remote Config Structure
```json
{
  "generator_config": {
    "4x4": {
      "easy_holes": 3,
      "medium_holes": 5,
      "hard_holes": 6
    },
    "6x6": {
      "easy_holes": 10,
      "medium_holes": 14,
      "hard_holes": 18,
      "expert_holes": 20
    },
    "9x9": {
      "easy_holes": 36,
      "medium_holes": 42,
      "hard_holes": 48,
      "expert_holes": 55
    },
    "max_retry_times": 50,
    "daily_seed": 20260817
  }
}
```

## 7. Testing & Quality Assurance
*   **Full Board Generation Test:** Generate 10,000 boards; 100% must satisfy rules.
*   **Uniqueness Stress Test:** 500 puzzles per difficulty; `countSolutions` must always return 1.
*   **Multi-Solution Detection:** Test with an empty 4x4 board; must trigger retry.
*   **Seed Consistency:** Fixed seed must produce identical puzzles across devices.

## 8. Package Structure Recommendations
Isolate logic from Android for potential KMP migration:
```text
domain/
└── logic/
    ├── model/
    │   └── SudokuBoardConfig.kt
    ├── generator/
    │   ├── SudokuGenerator.kt
    │   └── RandomProvider.kt
    ├── solver/
    │   ├── BacktrackingSolver.kt
    │   └── SolverStrategy.kt
    └── validator/
        └── PuzzleValidator.kt
```
