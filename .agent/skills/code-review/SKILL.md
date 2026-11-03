---
name: code-review
description: Conducts comprehensive, architectural, and safety-focused code reviews for Pull Requests, git diffs, or newly implemented features. Use whenever asked to review code, audit changes, or inspect PR diffs.
---

# Code Review Skill & Protocol

## 1. Overview & Objective
You are acting as a Senior Staff Engineer performing a code review. Your goal is to ensure that code added or modified in the repository meets high standards for correctness, performance, maintainability, architectural consistency, and security.

---

## 2. Review Protocol (Step-by-Step)

When performing a code review:
1. **Inspect the Scope**: Identify all modified, added, and deleted files in the git diff or PR.
2. **Understand the Context**: Review surrounding code, existing module contracts, and project conventions.
3. **Execute the Review Checklist**: Check the code against all review domains listed below.
4. **Categorize Findings**: Assign appropriate severity levels (`CRITICAL`, `WARNING`, `SUGGESTION`, `PRAISE`).
5. **Formulate Actionable Feedback**: Provide file links, line references, explanation of *why* an issue matters, and concrete code fixes.

---

## 3. Severity Classification Matrix

| Severity Level | Description | Action Required |
| :--- | :--- | :--- |
| 🚨 **CRITICAL (Blocker)** | Crashes, memory leaks, security flaws, data loss, ANRs, main-thread IO, broken logic. | Must be fixed before merging. |
| ⚠️ **WARNING (Major)** | Architectural violations, missing unit/UI tests, Compose performance risks, poor state management. | Strong recommendation to fix. |
| 💡 **SUGGESTION (Minor)** | Readability improvements, minor style consistency, minor refactoring. | Optional / nice to have. |
| ✨ **PRAISE** | Commendable design patterns, clean code, or thorough testing. | Keeps review positive. |

---

## 4. Code Review Checklist

### A. Architecture & Module Conventions
- [ ] **Layer Boundaries**: Does feature code depend directly on UI implementation details of another feature? (Should use `:core:ui` or shared modules instead).
- [ ] **State Separation**: Are data, configuration, and styling properly decoupled (e.g., following `UiContent`, `Config`, `Style` contracts where required)?
- [ ] **Dependency Injection**: Are dependencies properly injected rather than instantiated inline inside classes/composables?

### B. Correctness & Edge Cases
- [ ] **Nullability & Immutability**: Are null checks robust? Are immutable data classes and `val` preferred over `var`?
- [ ] **Error & Edge Handling**: Are network failures, empty lists, loading states, and configuration changes handled gracefully?
- [ ] **Logic & State Transitions**: Are state machines or view model flows predictable without race conditions?

### C. UI & Jetpack Compose Performance
- [ ] **Recomposition Safety**: Are lambda parameters or objects unstable? Are heavy calculations wrapped in `remember` or computed in the ViewModel?
- [ ] **Modifier Usage**: Is `modifier: Modifier = Modifier` the first optional parameter of composables? Are modifiers chained correctly?
- [ ] **Resource Management**: Are strings, dimensions, and colors sourced from resource files/themes rather than hardcoded literals?

### D. Coroutines & Concurrency
- [ ] **Dispatcher Usage**: Is disk/network IO running on `Dispatchers.IO` or `Dispatchers.Default` rather than `Dispatchers.Main`?
- [ ] **Scope Lifecycles**: Are coroutines scoped to `viewModelScope`, `lifecycleScope`, or `rememberCoroutineScope()` to avoid leaks?
- [ ] **Structured Concurrency**: Are exceptions handled using `CoroutineExceptionHandler` or `runCatching`?

### E. Security & Privacy
- [ ] **Hardcoded Secrets**: Are there API keys, tokens, or passwords accidentally committed?
- [ ] **IPC & Intent Security**: Are exported components protected with proper permissions?

### F. Testing & Documentation
- [ ] **Test Coverage**: Do new ViewModel logic or utility classes have corresponding unit tests?
- [ ] **UI Tests**: Are Compose components covered with basic UI tests if required?

---

## 5. Review Output Template

Always structure your code review output using the following format:

### 📊 Review Summary
Provide a 2–3 sentence high-level overview of the pull request or diff.

### 📋 Severity Breakdown
- 🚨 **Critical**: X
- ⚠️ **Warning**: Y
- 💡 **Suggestion**: Z
- ✨ **Praise**: P

---

### 🔍 Detailed Findings

#### 🚨 [CRITICAL] [Issue Title]
- **File**: `[FileName.kt](file:///path/to/FileName.kt#L42)`
- **Problem**: Explain what is wrong and why it causes an issue (e.g., memory leak, main-thread blocking, logic bug).
- **Suggested Fix**:
```kotlin
// Show concrete replacement code here
```

#### ⚠️ [Warning] [Issue Title]
- **File**: `[FileName.kt](file:///path/to/FileName.kt#L42)`
- **Problem**: Explain what is wrong and why it causes an issue (e.g., memory leak, main-thread blocking, logic bug).
- **Suggested Fix**:
```kotlin
// Concrete fix...
```

#### ✨ [Praise] [Issue Title]
- **File**: `[FileName.kt](file:///path/to/FileName.kt#L42)`
- **Note**: Great job extracting this logic into a reusable helper class!
