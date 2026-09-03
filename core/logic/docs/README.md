# Mind-Game 数独核心逻辑库（core:logic）

> 一个纯 Kotlin（JVM）实现、与 Android 无关、可迁移至 KMP 的通用数独引擎。
> 覆盖 **4x4 / 6x6 / 9x9** 三种规格的统一**生成 / 求解 / 校验** 与 **难度配置**。

---

## 目录

1. [架构总览](#1-架构总览)
2. [核心原则：三位一体策略](#2-核心原则三位一体策略)
3. [包结构与职责](#3-包结构与职责)
4. [数据模型 SudokuBoardConfig](#4-数据模型-sudokuboardconfig)
5. [求解层 solver](#5-求解层-solver)
6. [生成层 generator](#6-生成层-generator)
7. [校验层 validator](#7-校验层-validator)
8. [配置层 config](#8-配置层-config)
9. [种子策略：自由模式与每日挑战](#9-种子策略自由模式与每日挑战)
10. [测试与可靠性](#10-测试与可靠性)
11. [快速开始](#11-快速开始)
12. [后续演进建议](#12-后续演进建议)

---

## 1. 架构总览

`core:logic` 是整个项目（`app` 应用壳 + `feature:game` 玩法层）背后的**纯逻辑引擎**。
它刻意**不与 Android 绑定**，只关心"棋盘数据 + 算法"，从而：

- 便于单元测试（纯 JVM 即可运行，无需模拟器 / Robolectric）；
- 为将来迁移 **KMP（Kotlin Multiplatform）** 预留空间（UI 层可换成 Compose / SwiftUI）；
- 通过 `DifficultyConfig` 实现"**远端动态调参**"，无需发版即可改变难度。

模块最终结构如下：

```text
core/logic/src/main/kotlin/com/idroid/stuido/mind_game/core/logic/
├── model/      SudokuBoardConfig.kt        # 通用棋盘数据类
├── solver/     SolverStrategy.kt           # 求解策略接口
│               BacktrackingSolver.kt       # 通用回溯求解器（核心算法）
│               SudokuSolver.kt             # 兼容旧命名的策略子接口
├── generator/  RandomProvider.kt           # 随机源（支持固定种子）
│               SudokuGenerator.kt          # 谜题生成器（三位一体主入口）
├── validator/  PuzzleValidationResult.kt  # 校验结果枚举
│               PuzzleValidator.kt          # 唯一解校验器
└── config/     Difficulty.kt               # 难度枚举
                BoardSpec.kt                # 规格枚举
                DifficultyConfig.kt         # 难度-挖空数量配置
```

---

## 2. 核心原则：三位一体策略

本库**不依赖任何静态 JSON 谜题库**，所有题目由算法在设备端实时动态生成。
其核心是"生成 → 求解 → 校验 → 配置"四位一体的闭环：

1. **Generate（生成）**：客户端以纯 Kotlin 算法动态生成完整谜题；
2. **Solve（求解）**：内置统一回溯求解器，用于生成终盘与校验唯一解；
3. **Validate（校验）**：生成后立即做"唯一解校验"，杜绝多解 / 无解题目；
4. **Configure（配置）**：挖空数量等参数由远端 JSON / Firebase 动态下发，**无需发版**即可调整难度。

---

## 3. 包结构与职责

| 包 | 文件 | 职责 |
|---|---|---|
| `model` | `SudokuBoardConfig` | 承载规格参数 + 完整终盘 + 谜题 + 挖空数 |
| `solver` | `SolverStrategy` / `BacktrackingSolver` / `SudokuSolver` | 求解与解数统计 |
| `generator` | `RandomProvider` / `SudokuGenerator` | 完整终盘生成 + 挖空 + 唯一解保持 |
| `validator` | `PuzzleValidator` / `PuzzleValidationResult` | 谜题合法性判定 |
| `config` | `Difficulty` / `BoardSpec` / `DifficultyConfig` | 规格与难度映射 |

---

## 4. 数据模型 SudokuBoardConfig

`SudokuBoardConfig` 是贯通全部算法的**统一数据契约**：

```kotlin
data class SudokuBoardConfig(
    val size: Int,          // 网格尺寸：4 / 6 / 9
    val boxWidth: Int,      // 宫格宽度：2 / 3 / 3
    val boxHeight: Int,     // 宫格高度：2 / 2 / 3
    val solution: IntArray, // 完整终盘（size * size）
    val puzzle: IntArray,   // 挖空后的谜题（0 代表空格）
    val holes: Int,         // 实际挖空数量
)
```

**棋盘统一用扁平一维 `IntArray` 表示**，索引规则为 `board[row * size + col]`，
与规格（4x4 / 6x6 / 9x9）无关。

| 规格 | 网格 | 宫格(宽×高) | 数字范围 | 总格数 |
|---|---|---|---|---|
| Mini | 4×4 | 2×2 | 1~4 | 16 |
| Medium | 6×6 | 3×2 | 1~6 | 36 |
| Classic | 9×9 | 3×3 | 1~9 | 81 |

---

## 5. 求解层 solver

### 5.1 策略接口 `SolverStrategy`

采用 **Strategy Pattern**，定义统一的求解契约，方便将来替换不同算法（如 DLX / Dancing Links）：

```kotlin
interface SolverStrategy {
    fun solve(board: IntArray, config: SudokuBoardConfig): IntArray?
    fun countSolutions(board: IntArray, config: SudokuBoardConfig, limit: Int = 2): Int
}
```

- `solve`：返回其中一个合法解，**不修改入参**；无解返回 `null`。
- `countSolutions`：统计解数，**找到第 `limit` 个解即提前终止递归**（剪枝优化）。
  默认 `limit = 2`，专用于唯一解校验。

### 5.2 实现 `BacktrackingSolver`

核心算法为经典**回溯（backtracking）+ 剪枝**：

```kotlin
class BacktrackingSolver(
    // 可注入候选值序列。默认升序 1..size；随机生成时注入 Fisher-Yates 洗牌序列。
    private val candidateProvider: (cell: Int, size: Int) -> Iterable<Int> =
        { _, size -> 1..size },
) : SolverStrategy
```

关键特性：

- **纯函数式输出**：内部对入参 `copyOf()`，绝不在原数组上直接改；
- **候选排序可定制**：通过构造参数 `candidateProvider` 注入自定义候选序列——
  - 固定升序 → 确定性测试；
  - Fisher-Yates 洗牌 → 随机终盘（供生成器使用）；
- **统一适配三种规格**：所有行列宫约束均从 `config` 读取 `size / boxWidth / boxHeight` 推导；
- **提前终止**：`countSolutions` 一旦累计达到 `limit` 即终止，避免无谓递归。

```kotlin
// 简易用法
val solver = BacktrackingSolver()
val solved = solver.solve(puzzle, config) // 解出一个
val count  = solver.countSolutions(puzzle, config, limit = 2) // 唯一解校验
```

### 5.3 `SudokuSolver`

为兼容旧有命名而保留的策略子接口，继承 `SolverStrategy`，无重复声明。

---

## 6. 生成层 generator

### 6.1 随机源 `RandomProvider`

封装 `Random`，对外提供两种模式所需的随机能力：

- **自由模式**：不传种子 → `Random.Default`，终盘不可预测；
- **每日挑战模式**：构造时传入固定 `seed` → 同一种子跨设备生成完全一致的谜题。

```kotlin
class RandomProvider(seed: Long? = null) {
    fun nextInt(until: Int): Int
    fun nextInt(from: Int, until: Int): Int
    fun <T> shuffle(list: MutableList<T>): MutableList<T>   // 原地 Fisher-Yates
}
```

### 6.2 谜题生成器 `SudokuGenerator`

采用**"三位一体"**流程，并针对可靠性做了关键优化（见 6.3）：

```kotlin
class SudokuGenerator(
    private val randomProvider: RandomProvider = RandomProvider(),
    private val maxRetry: Int = 50,
) {
    fun generate(
        size: Int, boxWidth: Int, boxHeight: Int,
        holes: Int, seed: Long? = null,
    ): SudokuBoardConfig
}
```

流程：

1. **生成完整终盘**：回溯 + Fisher-Yates，随机填充空位至填满；
2. **增量式挖空**（见 6.3）；
3. **唯一解保持**：挖空过程中实时校验解唯一性，保证终盘结果必然唯一解。

### 6.3 关键设计：增量式挖空（可靠性优化）

相比"一次性随机挖空 + 失败整体重来"的朴素方案（旧策略在高难度下可能耗尽重试次数），
本库改为**增量式挖空**：

```
对候选格逐个试探：
    1. 临时将该格置 0；
    2. 立即调用求解器统计解数（limit=2）；
    3. 若仍唯一解 -> 保留该空位；
       否则       -> 撤销，尝试下一格。
每挖满一个即保存，直到达到目标 holes 或安全格耗尽。
```

**优势**：

- 每次挖空都保证唯一解，**结果必然合法**；
- 通常在**一次完整终盘**上即可达成目标空位，几乎不依赖重试；
- `maxRetry` 仅用于"单张终盘可挖安全格不足"的极端回退情形。

> 🧪 实测：将 `maxRetry` 从 200 降到 8，高难度（9x9 48 洞 / 4x4 6 洞）仍可稳定生成，
> 证明该优化显著降低了对重试次数的依赖。

---

## 7. 校验层 validator

### 7.1 结果枚举 `PuzzleValidationResult`

```kotlin
enum class PuzzleValidationResult { VALID, MULTIPLE_SOLUTIONS, NO_SOLUTION }
```

### 7.2 校验器 `PuzzleValidator`

将对求解器的调用封装为清晰的判定 API，供上层（UI / DAO / 远端题源）复用：

```kotlin
class PuzzleValidator(
    private val solver: SolverStrategy = BacktrackingSolver(),
) {
    fun validate(board: IntArray, config: SudokuBoardConfig): PuzzleValidationResult
    fun isValid(board: IntArray, config: SudokuBoardConfig): Boolean
    fun validate(config: SudokuBoardConfig): PuzzleValidationResult   // 直接校验 puzzle
    fun isValid(config: SudokuBoardConfig): Boolean
}
```

判定规则（与实现计划 §4.2 一致）：

| `countSolutions` | 结果 | 处理 |
|---|---|---|
| `1` | `VALID` | 合法唯一解，通过 |
| `>=2` | `MULTIPLE_SOLUTIONS` | 丢弃重试 |
| `0` | `NO_SOLUTION` | 丢弃重试 |

---

## 8. 配置层 config

### 8.1 `Difficulty`（难度枚举）

```kotlin
enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }
```

> ⚠️ 4x4 挖空超过 7 个极易多解，因此 **EXPERT 对 4x4 不开放**。

### 8.2 `BoardSpec`（规格枚举）

携带规格参数并提供 `fromSize()` 反查：

```kotlin
enum class BoardSpec(val size: Int, val boxWidth: Int, val boxHeight: Int) {
    MINI(4, 2, 2),
    MEDIUM(6, 3, 2),
    CLASSIC(9, 3, 3);
    companion object { fun fromSize(size: Int): BoardSpec? }
}
```

### 8.3 `DifficultyConfig`（挖空数量配置）

对应远端 JSON 结构（§6），是一个纯 `data class`，便于从 Firebase 反序列化：

```kotlin
data class DifficultyConfig(
    val holes: Map<BoardSpec, Map<Difficulty, Int>>,
    val maxRetry: Int,
    val dailySeed: Long,
) {
    fun holeCount(spec, difficulty): Int?
    fun isAvailable(spec, difficulty): Boolean
    fun availableDifficulties(spec): Set<Difficulty>
    companion object { fun default(): DifficultyConfig }
}
```

默认映射（对应实现计划 §3.2 / §6）：

| 难度 \ 规格 | 4x4 | 6x6 | 9x9 |
|---|---|---|---|
| Easy | 3 | 10 | 36 |
| Medium | 5 | 14 | 42 |
| Hard | 6 | 18 | 48 |
| Expert | ❌ | 20 | 55 |

---

## 9. 种子策略：自由模式与每日挑战

| 场景 | 策略 | 实现 |
|---|---|---|
| **自由模式** | 数学唯一性 | 9x9 组合数极大，碰撞概率可忽略 |
| **每日挑战** | 固定种子 | `RandomProvider(seed)`，再按 `seed * 10 + size` 混合，保证同日三种规格题目均不同且跨设备一致 |

```kotlin
val provider = if (seed != null) RandomProvider(seed * 10 + size) else RandomProvider()
```

---

## 10. 测试与可靠性

测试位于 `core/logic/src/test/kotlin/...`，共 **5 个测试类、39 个用例**，纯 JVM 运行。

| 测试类 | 覆盖 |
|---|---|
| `BacktrackingSolverTest`（9） | 求解、解数统计、limit 剪枝、纯函数性、三规格合法终盘 |
| `SudokuGeneratorTest`（10） | 规格生成、唯一解、种子一致性、随机性、极端挖空重试 |
| `PuzzleReliabilityTest`（7） | §7 综合：1 万终盘冒泡、每难度 100 抽样、种子、多解/无解检测 |
| `PuzzleValidatorTest`（5） | VALID / MULTIPLE / NO_SOLUTION 判定 + 生成器集成 |
| `DifficultyConfigTest`（8） | 规格解析、默认映射、EXPERT 禁用、可用集、自定义覆盖 |

**可靠性结论**：

- 完整测试套件（含 1 万终盘冒泡 + 随机化唯一解压力）**约 7–10 秒**跑完；
- 连续多轮 `--rerun-tasks`（强制每次全新随机生成）均 **BUILD SUCCESSFUL**，零 flaky；
- 高难度 + 低 `maxRetry` 下仍稳定，验证增量挖空策略的可靠性。

---

## 11. 快速开始

```kotlin
import com.idroid.stuido.mind_game.core.logic.generator.SudokuGenerator
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.validator.PuzzleValidator

// 1. 通过难度配置拿到目标挖空数
val config = DifficultyConfig.default()
val holes = config.holeCount(com.idroid.stuido.mind_game.core.logic.config.BoardSpec.CLASSIC,
                             com.idroid.stuido.mind_game.core.logic.config.Difficulty.HARD)!!

// 2. 生成唯一解谜题（9x9 Hard）
val gen = SudokuGenerator(maxRetry = 50)
val board = gen.generate(9, 3, 3, holes)

// 3. 生产环境可再用校验器复核
val validator = PuzzleValidator()
check(validator.isValid(board)) { "生成的谜题必须为唯一解" }
```

---

## 12. 后续演进建议

1. **接入 `feature:game` UI**：把 `SudokuGenerator` / `PuzzleValidator` 注入 ViewModel；
2. **难度参数远端下发**：将 `DifficultyConfig` 反序列化自 Firebase `generator_config`，
   实现"改难度不再发版"；
3. **选填求解算法**：可再实现 `DLX`（Dancing Links）并注入 `SolverStrategy`，
   提升极难盘（9x9 EXPERT 55 洞）的求解性能；
4. **KMP 迁移**：本模块已与 Android 解耦，可直接迁至 `commonMain`，UI 端接入 Compose Multiplatform。

---

*本文档随 `core:logic` 源码持续演进；如与代码不一致，以源码为准。*
