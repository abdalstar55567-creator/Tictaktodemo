

## كيف تحصل على ملف APK

1. أنشئ مستودعاً جديداً على GitHub.
2. ارفع كل محتويات هذا المجلد إلى المستودع (على الفرع main).
3. افتح تبويب **Actions** وانتظر انتهاء تشغيل "Build APK" (دقيقتان تقريباً).
4. افتح التشغيل الناجح ونزّل الملف **tictactoe-apk** من قسم Artifacts، ثم فك ضغطه لتجد `app-debug.apk`.
5. انقل الملف إلى هاتفك وثبّته (فعّل "التثبيت من مصادر غير معروفة" إذا طُلب).

يمكنك أيضاً تشغيل البناء يدوياً من Actions ثم "Run workflow".

# 🎮 Project Guide for AI Coding Assistants

## 1. Project Overview

This repository contains an Android application named **100+ (100 Games and More)**.

The application is a collection of simple, fun, lightweight mini-games designed for Android devices.

The project is developed using:
- **Programming Language:** Kotlin
- **Platform:** Android
- **UI:** Native Android views and XML layouts where appropriate
- **Build System:** Gradle
- **IDE:** Android Studio or GitHub Codespaces
- **Minimum SDK:** Android 21

The application currently includes multiple games, such as:
- Tic-Tac-Toe (XO)
- Memory Game
- Number Guessing
- Rock Paper Scissors
- Math Challenge
- Whack-a-Mole
- Tap Speed
- Reaction Time
- Number Puzzle
- Dice Rolling
- Color Matching
- Snake
- Simon
- Connect Four
- Hangman
- 2048
- Word Scramble
- Trivia
- Higher or Lower
- Find the Odd One Out
- Number Ordering
- Lucky Box
- Number Memory

The list above is illustrative. Always inspect the actual repository before making changes.

The main objective is to continuously expand the collection with new, simple, entertaining, and reliable games.

---

## 2. Mandatory Rules for AI Assistants

Before making any changes, follow these rules:

1. Inspect the repository structure and read the existing Kotlin source files.
2. Understand how the current game menu, game activities, and shared UI components work.
3. Follow the existing architecture and coding style.
4. Write all new application logic in **Kotlin**.
5. Do not rewrite the entire application to add a single game.
6. Do not remove, rename, or break existing games.
7. Do not modify unrelated files.
8. Reuse existing UI components, colors, buttons, layouts, and helper functions whenever possible.
9. Do not introduce additional libraries unless they are genuinely necessary.
10. Prefer offline games that do not require internet access, accounts, external APIs, or paid services.
11. Keep the interface simple, attractive, responsive, and easy to use on small Android screens.
12. Use Arabic for game titles, instructions, buttons, and player-facing messages unless the project explicitly requires another language.
13. Handle invalid input and unexpected game states safely.
14. Avoid crashes, memory leaks, and unnecessary background tasks.
15. Never claim that the application builds successfully unless the build has actually been tested.

**Important:** Inspect the actual repository before assuming that a file, class, resource, or function exists. Do not invent class names or replace working code based on assumptions.

---

## 3. Existing Project Architecture

The following files are particularly important. Verify their current contents before editing them.

### A. Games.kt

Expected location:

`app/src/main/java/com/example/tictactoe/Games.kt`

This file contains the central list of available games.

Each game is represented by a `GameInfo` entry containing:

- An emoji or icon.
- The game's display name.
- The Activity class responsible for opening the game.

When adding a new game, register it in this list so it appears in the game's menu.

Follow the existing `GameInfo` structure exactly.

Example:

```kotlin
GameInfo(
    "🪙",
    "وجه أم كتابة؟",
    CoinFlipActivity::class.java
)

Use the actual structure and formatting found in the repository.

Do not add duplicate entries.

B. BaseGameActivity.kt

Expected location:

app/src/main/java/com/example/tictactoe/BaseGameActivity.kt

This file provides shared functionality for game screens.

It may include:

A common game screen structure.

The game title.

Back navigation.

Confirmation dialogs.

UI helper functions.

Common buttons and layouts.

Shared styling and colors.


Before creating a new game, inspect this class carefully.

Where appropriate, new games should inherit from BaseGameActivity rather than implementing an entirely separate screen architecture.

Reuse its existing helper functions instead of duplicating functionality.

Do not modify this file unless the new feature genuinely requires a shared improvement.

Any change to this base class must be checked for compatibility with all existing games.

C. AndroidManifest.xml

Expected location:

app/src/main/AndroidManifest.xml

New Android Activity classes may need to be declared in the manifest.

If the new game uses a separate Activity, register it using the same conventions as existing game Activities.

For example:

<activity
    android:name=".CoinFlipActivity"
    android:exported="false"
    android:screenOrientation="portrait" />

Adapt the declaration to the actual project configuration.

Do not create duplicate Activity declarations.

Do not change the launcher Activity when adding a normal game.

D. Game-specific Kotlin files

Expected directory:

app/src/main/java/com/example/tictactoe/

Each game should normally have its own Kotlin Activity or follow another established game structure already used by the project.

Examples of existing game classes include:

MainActivity.kt

MemoryActivity.kt

GuessNumberActivity.kt

RpsActivity.kt


Inspect the actual directory to identify the correct naming conventions and existing implementations.

E. Layout and resource files

Expected directories:

app/src/main/res/layout/

app/src/main/res/drawable/

app/src/main/res/values/

Some games build their interfaces programmatically in Kotlin, while others may use XML layouts.

Follow the approach used by similar existing games.

If a game requires a new XML layout, create it in the appropriate directory and reference it correctly from Kotlin.

If existing buttons, backgrounds, colors, or styles can be reused, prefer reusing them.

Do not introduce unnecessary resources.


---

4. Step-by-Step Procedure for Adding a New Game

Whenever I request a new game, follow this workflow.

Step 1: Choose and design the game

Identify a simple game that is different from the existing games.

Before implementing it:

Check whether a similar game already exists.

Define the rules.

Define the player's objective.

Decide how the player wins or loses.

Decide how scoring works, if applicable.

Identify all possible game states.

Keep the controls intuitive.


Avoid adding a duplicate game under a different name.

Step 2: Inspect the existing code

Read the following files or their current equivalents:

Games.kt

BaseGameActivity.kt

At least two existing game Activity files.

AndroidManifest.xml

Any layout or resource files needed by the new game.


Identify:

How games are launched.

How the menu displays game names and icons.

How shared UI components are used.

How buttons respond to user actions.

How game state is managed.

How restart and back navigation work.

How colors, text sizes, and spacing are handled.


Do not start coding until the relevant architecture is understood.

Step 3: Implement the game in Kotlin

Create a new Kotlin file in the existing package:

app/src/main/java/com/example/tictactoe/

For example:

CoinFlipActivity.kt

Use a descriptive class name that does not conflict with existing classes.

Where appropriate, inherit from:

BaseGameActivity

Implement the complete playable game, not just a visual mockup.

The game should include:

A clear title.

Brief instructions.

The main game area.

Interactive controls.

Correct game logic.

Feedback after each action.

A clear win, loss, or completion state when applicable.

A restart or new-game option.

Appropriate handling of invalid actions.


Use Kotlin for all game logic.

Do not implement the game's core logic in Java, JavaScript, Python, or another language.

XML may be used for Android layouts when appropriate.

Step 4: Register the game in Games.kt

Add the new game to the central game list.

Include:

A suitable emoji or icon.

A short Arabic title.

The correct Activity class.


Place the entry logically among the other games.

Verify that the entry appears only once.

Step 5: Register the Activity in AndroidManifest.xml

If the new game uses a separate Activity, add its declaration to the manifest if required.

Follow the existing conventions for:

Activity names.

Export settings.

Screen orientation.


Do not change the application's launcher configuration.

Step 6: Add resources only when needed

If the game needs a new layout, drawable, string, or other resource:

1. Create the resource in the correct directory.


2. Use valid Android resource naming conventions.


3. Reference it correctly from Kotlin.


4. Check that resource identifiers are unique.


5. Reuse existing project resources whenever practical.



Do not create unnecessary files.

Step 7: Verify navigation and compatibility

Check that:

The new game appears in the menu.

Selecting it opens the correct Activity.

The game can be played from start to finish.

Restarting the game works correctly.

Back navigation works correctly.

The game does not interfere with other games.

The app can return to the game menu normally.

No duplicate Activity registrations or menu entries were introduced.


Step 8: Build and test

Use the existing Gradle configuration.

From the repository root, run:

./gradlew assembleDebug

On Windows, use:

.\gradlew.bat assembleDebug

If the build fails:

1. Read the complete error message.


2. Identify the relevant source file or resource.


3. Fix the underlying issue.


4. Run the build again.


5. Do not declare the task complete until the available checks have passed.



If an Android emulator or device is available, test the new game there as well.

If execution or testing is unavailable, clearly state which checks were not performed.

Step 9: Summarize the changes

After implementing the game, provide a concise report containing:

The name of the new game.

Its rules and features.

Every file created.

Every existing file modified.

The build and tests performed.

Any known limitations.


Do not claim that a file was modified or a test passed unless it actually happened.


---

5. File Modification Policy

For a typical new game, the expected changes are:

File or directory	Action	Purpose

app/src/main/java/com/example/tictactoe/NewGameActivity.kt	Create	Implement the new game in Kotlin
app/src/main/java/com/example/tictactoe/Games.kt	Modify	Add the game to the menu
app/src/main/AndroidManifest.xml	Modify if needed	Register the new Activity
app/src/main/res/layout/	Modify only if needed	Add an XML layout
app/src/main/res/drawable/	Modify only if needed	Add visual resources
app/src/main/res/values/	Modify only if needed	Add strings, colors, or styles


This table is a guide, not permission to modify every listed file.

Make the smallest set of changes required for a complete implementation.

Never replace the entire Games.kt, BaseGameActivity.kt, or AndroidManifest.xml merely to add one game.

Preserve all existing games and unrelated functionality.


---

6. Code Quality Requirements

Every new game must follow these requirements:

Use readable, maintainable Kotlin.

Give classes, functions, and variables meaningful names.

Avoid unnecessary code duplication.

Keep game logic separate from unrelated application functionality.

Prevent invalid moves and repeated actions after a game ends.

Reset all relevant state when restarting.

Handle Activity lifecycle events appropriately.

Avoid long-running work on the main thread.

Avoid unnecessary network access and permissions.

Ensure that buttons and text remain readable on different screen sizes.

Use Arabic text that is clear and correctly displayed.

Follow Android development best practices.

Preserve compatibility with the project's configured minimum SDK.


Do not add advertisements, analytics, permissions, tracking, or external services unless explicitly requested.


---

7. Adding Multiple Games

If I request several games at once:

1. Inspect the existing repository first.


2. Propose a list of distinct games.


3. Check for duplicates.


4. Implement each game as an independent, maintainable component.


5. Register every game correctly.


6. Verify all Activity declarations.


7. Build the application after the changes.


8. Report every created and modified file.



Prefer several complete, working games over many unfinished games.

Do not modify all existing games unnecessarily.


---

8. Working with GitHub Codespaces

This project may be developed using GitHub Codespaces.

When working in Codespaces:

1. Inspect the current branch and working tree.


2. Preserve unrelated user changes.


3. Make focused modifications.


4. Review the changes before committing.


5. Run the available Gradle build and tests.


6. Summarize the changes and test results.


7. Commit and push only when explicitly authorized.



Do not discard existing changes, force-push, or rewrite Git history without explicit permission.

Do not expose credentials, API keys, or secrets in source files.


---

9. Final Instruction

Whenever I ask you to add a game to 100+ (100 Games and More), act as an experienced Android Kotlin developer.

Your priorities are:

1. Preserve the existing application.


2. Understand the actual source code before editing.


3. Add a complete, enjoyable, and reliable game.


4. Integrate it correctly into the existing menu and navigation.


5. Minimize unrelated changes.


6. Build and test whenever possible.


7. Explain exactly what was changed and what remains unverified.



The goal is to grow 100+ into a large collection of simple, polished, lightweight Android games while keeping the codebase stable and maintainable.
