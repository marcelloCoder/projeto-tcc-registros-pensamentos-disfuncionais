# Mind Check

Project codename: `primeiroprojeto`

Mind Check is an Android app for recording dysfunctional thoughts. Each entry is intended to capture the situation, automatic thought, emotion, and the specific date and time.

## Target Audience
- People who suffer from functional disorders

## Current Stack
- Kotlin
- Android Views with XML layouts
- Gradle
- Android SDK

## Commands
- **Install / sync:** `.\gradlew.bat build`
- **Run locally:** open the project in Android Studio and run it on an emulator or Android device
- **Build debug APK:** `.\gradlew.bat assembleDebug`
- **Run unit tests:** `.\gradlew.bat test`
- **Run lint:** `.\gradlew.bat lint`

## Current Status
- The repository currently contains a basic single-screen Android app scaffold.
- The thought-recording feature still needs to be implemented.

## MVP Decisions
- Authentication: users can create an account for this app
- Storage: local storage only, on-device for now
- Post-login flow: create, view, edit, delete, search, and filter thought records

## Layout Reference
- Reference image: `C:\Users\Marcello23\Pictures\layout.png`
- The post-login experience should follow a clean, modern, light mobile design.
- The UI direction from the image uses rounded cards, soft shadows, subtle gray backgrounds, and generous spacing.
- The top of the screen should stay simple, with a compact header and light utility actions.
- Main content should be organized into clear card sections instead of dense text blocks.
- Inputs should feel soft and approachable, with rounded fields and pill-style controls where useful.
- The overall navigation pattern can include a bottom navigation bar for main sections.

## Intended Post-Login Screen
The main screen after login should let the user record a dysfunctional thought in a calm and structured way. The layout should visually follow the reference image while focusing on these fields:
- Date
- Time
- Situation
- Automatic Thinking
- Emotional

The screen should feel supportive and easy to use, especially for people who may already be under stress.

## Planned App Data
Each thought record should include:
- Date
- Time
- Situation
- Automatic Thinking
- Emotional

## Common Dysfunctional Thought Patterns
More than 20 commonly cited dysfunctional thought patterns, also called cognitive distortions. Some sources treat a few of these as overlapping names or close variants:
- All-or-nothing thinking
- Black-and-white thinking
- Overgeneralization
- Mental filter
- Discounting the positive
- Minimizing the positive
- Jumping to conclusions
- Mind reading
- Fortune telling
- Catastrophizing
- Magnification
- Minimization
- Emotional reasoning
- "Should" statements
- "Must" statements
- Labeling
- Mislabeling
- Personalization
- Blaming
- Control fallacies
- Fallacy of fairness
- Change fallacy
- Always being right

## Next Steps
- Design the entry form for a thought record
- Save entries locally on the device
- Add a screen to review past entries
- Add authentication with account creation
- Support create, view, edit, delete, search, and filter flows
- Validate required fields and handle errors clearly
