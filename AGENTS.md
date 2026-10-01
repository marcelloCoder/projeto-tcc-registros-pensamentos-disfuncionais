# AGENTS.md

## Project Overview
- **Project:** primeiroprojeto - an Android app for recording dysfunctional thoughts with the situation, automatic thought, emotion, date, and time.
- **Target user:** people who suffer from functional disorders
- **My skill level:** beginner
- **Stack:** Kotlin, Android Views/XML, Gradle, Android SDK

## MVP Decisions
- **Authentication:** users can create an account for this app
- **Storage:** local storage only, on-device for now
- **Required fields:** date, time, situation, automatic thinking, emotional
- **Post-login flow:** create, view, edit, delete, search, and filter thought records

## Layout Reference
- **Reference image:** `C:\Users\Marcello23\Pictures\layout.png`
- **Visual direction:** clean light-theme mobile UI with rounded cards, soft shadows, lots of white space, and subtle gray backgrounds
- **Top area:** simple header with a back button or page title and small utility actions
- **Content style:** card-based sections with large rounded corners and calm spacing between blocks
- **Interactive elements:** pill-shaped chips/tabs, rounded text inputs, and clear focus on one primary task per screen
- **Navigation pattern:** bottom navigation bar for moving between the main areas after login
- **How to adapt it for this app:** after login, the user should land on a thought-record screen that visually follows this style and lets them capture date, time, situation, automatic thinking, and emotional state
- **Tone:** calm, supportive, readable, and not visually crowded

## Commands
- **Install:** `.\gradlew.bat build`
- **Dev:** run from Android Studio on an emulator or device
- **Build:** `.\gradlew.bat assembleDebug`
- **Test:** `.\gradlew.bat test`
- **Lint:** `.\gradlew.bat lint`

## Do
- Read existing code before modifying anything
- Match existing patterns, naming, and style
- Handle errors gracefully — no silent failures
- Keep changes small and scoped to what was asked
- Run dev/build after changes to verify nothing broke
- Ask clarifying questions before guessing

## Don't
- Install new dependencies without asking
- Delete or overwrite files without confirming
- Hardcode secrets, API keys, or credentials
- Rewrite working code unless explicitly asked
- Push, deploy, or force-push without permission
- Make changes outside the scope of the request

## When Stuck
- If a task is large, break it into steps and confirm the plan first
- If you can't fix an error in 2 attempts, stop and explain the issue

## Testing
- Run existing tests after any change
- Add at least one test for new features
- Never skip or delete tests to make things pass

## Git
- Small, focused commits with descriptive messages
- Never force push

## Response Style
- always respond with clear & concise messages
- use plain English when explaining to the User
- avoid long sentences, complex words, or long paragraphs
