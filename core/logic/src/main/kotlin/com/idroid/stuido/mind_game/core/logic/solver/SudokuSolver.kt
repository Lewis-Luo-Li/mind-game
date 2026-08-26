package com.idroid.stuido.mind_game.core.logic.solver

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig

/**
 * 统一求解器接口，接受 size / boxWidth / boxHeight 作为环境参数。
 *
 * 继承自 [SolverStrategy]，对外保持原有命名的同时纳入统一的策略契约，
 * 便于通过依赖注入在运行时切换不同求解实现。
 *
 * 接口本身无需重复声明 [SolverStrategy] 中的方法，直接继承即可。
 */
interface SudokuSolver : SolverStrategy
