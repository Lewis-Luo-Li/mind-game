---
name: core-ui-guidelines
description: Enforces architecture, reuse policies, and strict state/config/style separation for the core:ui Jetpack Compose module. Use this whenever creating or modifying UI components.
---

# `core:ui` Module Guidelines

## 1. Architecture & Purpose
The `core:ui` module is the centralized UI component and theme library for the entire application. It contains all design system primitives (colors, sizing, typography) and custom self-defined components (e.g., IconButton, BaseText, Keypad). This module is the single source of truth for UI elements and is shared across all feature and app modules.

## 2. Component Usage & Contribution Policy
Before building a new UI element in any feature module, you must check `core:ui` first:
* **Reuse:** If an existing component fulfills the requirement, reference it directly.
* **Create:** If no existing component can fulfill the purpose, you must create a new component within the `core:ui` module following the strict structural rules below.

## 3. Component Creation Rules
Every new component created in `core:ui` must strictly adhere to the following contract:

1. **Naming:** The function must be annotated with `@Composable` and use PascalCase matching the component name.
2. **Modifier:** `modifier: Modifier` must always be the first parameter in the Composable function signature.
3. **Base Component Reuse:** When building new components, always reuse existing `core:ui` base components (such as `BaseText`, `Button`, `IconButton`, `LabeledCircleButton`, etc.) instead of using corresponding Compose SDK views (e.g., standard Compose `Text` or `Button`). If no suitable base component exists, create a new one or use low-level Compose primitives.
4. **Component State & Configuration Classes:** The component's data, behavior, and styling must be separated into three distinct data classes:
    * **UiContent (`<ComponentName>UiContent`):** Contains the information and data to display (e.g., text strings, image resources).
    * **UiConfig (`<ComponentName>Config`):** Controls the behavior and functional state of the component (e.g., `isVisible`, enabled state, `onClick` callbacks).
    * **UiStyle (`<ComponentName>Style`):** Contains the styling parameters (e.g., background colors, text styles, padding).

**Example Structure:**
```kotlin
data class CustomButtonUiContent(val text: String)
data class CustomButtonConfig(val isVisible: Boolean = true, val onClick: () -> Unit)
data class CustomButtonStyle(val backgroundColor: Color)

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    content: CustomButtonUiContent,
    config: CustomButtonConfig,
    style: CustomButtonStyle
) {
    if (!config.isVisible) return
    // Implementation...
}
```
## 4. Preview
Creating Compose `@Preview` functions for any new component is mandatory. Previews must demonstrate the component rendering correctly using mock UiContent, UiConfig, and UiStyle data.

## 5. Testing Requirements
To ensure design system reliability, every new component must include:

* Unit Tests: To verify any internal logic, state transformations, or helper functions associated with the component's state classes.
* Android UI Tests: Compose tests (using `composeTestRule`) are mandatory to verify that the component renders correctly and responds accurately across different states.