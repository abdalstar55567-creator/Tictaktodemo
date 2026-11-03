100+ BRAND + MUSIC UPDATE

Logo:
- Replaced the menu/splash logo and legacy launcher artwork with the approved glossy 100+ icon.
- Launcher foreground uses a 1024x1024 adaptive-icon asset with the emblem scaled into Android safe zone.
- Adaptive icon background remains deep blue.

Category music (all are original chiptune variations based on the selected Pixel Sprint direction):
- bg_action.wav: 160 BPM - action games
- bg_puzzle.wav: 118 BPM - puzzle games
- bg_focus.wav: 126 BPM - focus/memory games
- bg_quiz.wav: 145 BPM - quiz games
- bg_challenge.wav: 170 BPM - speed/challenge games
- bg_classic.wav: 135 BPM - classic/other games

GameMusic.kt already routes activities to these six resources; routing and game logic were preserved.
Build: gradle :app:assembleDebug
