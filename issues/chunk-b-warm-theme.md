---
repo: giodm97/peekly-app-parent
title: "[Chunk B] Warm theme — ActivityScreen, SettingsScreen, Onboarding/Pairing/AddChild restyle"
labels: enhancement
status: pending
---

## Overview
Complete the warm cream/orange redesign started in Chunk A. All five screens need to adopt the
`WarmBg / WarmPrimary / WarmInk` palette from `Color.kt` and match the HTML prototype style.

---

## Screens

### 1. ActivityScreen (replaces `DigestDetailScreen`)
- AI summary card (orange gradient, Groq digest text)
- App list with category filter chips (social media / gaming / video / other)
- Age-restriction badges (🔴 13+ / 🔴 18+) per app row
- Route: `digest/{childId}/{childName}` (already wired in NavGraph)

### 2. SettingsScreen (new)
- Per-child controls section (select child from list)
- Bedtime toggle with time picker
- Alert thresholds (screen time limit slider)
- Route: Settings tab in `MainScreen` (currently placeholder)

### 3. OnboardingScreen restyle
- Warm cream background (`WarmBg`)
- Orange CTA button (`WarmPrimary`)
- Progress dots styled in `WarmPrimary` / `WarmHairline`
- Keep existing 3-step flow (welcome → permissions → done)

### 4. PairingCodeScreen restyle
- Warm surface card with pairing code in large `WarmInk` text
- Orange confirm button
- Soft `WarmSurfaceAlt` background

### 5. AddChildScreen restyle
- Warm form fields (outlined, `WarmHairline` border)
- Orange submit button
- Avatar picker with `WarmPrimarySoft` selection highlight

---

## Acceptance criteria
- [ ] All five screens compile with zero use of legacy dark-navy color values
- [ ] `ActivityScreen` renders AI digest + filtered app list
- [ ] `SettingsScreen` is wired to the Settings tab in `MainScreen`
- [ ] Onboarding / Pairing / AddChild visually match the warm prototype
- [ ] No regressions on existing navigation flows (onboarding → main, add child → pairing → main)

---

## Notes
- Legacy color aliases in `Color.kt` can be removed once Chunk B is merged
- `DigestDetailScreen.kt` can be deleted after `ActivityScreen` takes over its route
- Font: `FontWeight.ExtraBold` approximates Nunito 800 until the Nunito dependency is added
