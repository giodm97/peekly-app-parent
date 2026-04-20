---
repo: giodm97/peekly-app-parent
title: "Add Nunito font (800 weight) to replace FontWeight.ExtraBold approximation"
labels: enhancement
status: pending
---

## Overview
The app currently approximates Nunito 800 using `FontWeight.ExtraBold` on the system font.
Add the real Nunito font via Google Fonts to match the warm design prototype.

## Changes required

### 1. `app/build.gradle.kts`
Add the Google Fonts dependency inside the `dependencies` block:
```
implementation("androidx.compose.ui:ui-text-google-fonts:<version>")
```
Use the same Compose BOM version already present in the file.

### 2. `app/src/main/java/com/peekly/parent/ui/theme/Type.kt`
- Import `androidx.compose.ui.text.googlefonts.GoogleFont`
- Import `androidx.compose.ui.text.googlefonts.Font`
- Import `com.peekly.parent.R`
- Define a `GoogleFont.Provider` using `res/font/` (certificate hash for Google Fonts)
- Define a `nunitoFamily` FontFamily with weights 400, 600, 700, 800
- Update the existing `Typography` object so `displayLarge`, `headlineLarge`, `headlineMedium`, `titleLarge`, `titleMedium`, `bodyLarge`, `bodyMedium`, `labelLarge` all use `nunitoFamily`

### 3. `app/src/main/res/values/strings.xml` (if needed)
No changes expected.

## Acceptance criteria
- [ ] App compiles without errors
- [ ] `nunitoFamily` is used in the `Typography` object in `Type.kt`
- [ ] No hardcoded `FontWeight.ExtraBold` fallback references remain in `Type.kt`
- [ ] The Google Fonts dependency is present in `app/build.gradle.kts`
