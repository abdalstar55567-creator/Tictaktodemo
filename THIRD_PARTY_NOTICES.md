# Third-party game attribution

## Tangler

- Original title: **Tangler**
- Original author: **Andrzej Novosiolov**
- Source repository: https://github.com/andrzej-nov/Tangler
- Original project license: **Creative Commons Attribution 4.0 International (CC BY 4.0)**
- Original project description: a casual tile puzzle inspired by Tantrix, in which the player places randomly generated tiles and forms continuous color loops.

This 100+ integration includes the original Tangler Kotlin game code, LibGDX Android launcher, and required game assets. The game is presented inside 100+ as **"تشابك الألوان (Tangler)"**.

Changes made for this integration:
- Embedded the original core game sources and assets in the 100+ Android application.
- Added an Android activity entry point and registered the game in the 100+ game catalogue.
- Adapted the Android dependency configuration and native-library packaging to the host application.

The original work remains attributed to Andrzej Novosiolov. The full original license text is preserved in `TANGLER-LICENSE.txt`.

## Neon

- Original title: **Neon** (STG / shoot-'em-up)
- Original author: **Mario Dujić**
- Source repository: https://github.com/mariodujic/Neon
- Original project license: **MIT** (full text in `NEON-LICENSE.txt`, Copyright (c) 2022 Mario Dujić)

Presented inside 100+ as **"نيون: معارك الفضاء"**.

Changes made for this integration:
- Moved the sources into the host app (package `com.zero.neon` kept) and resources prefixed with `neon_`.
- Replaced Navigation Compose with a simple splash/game switch (`NeonApp.kt`) and added a pause menu (resume/restart/exit).
- Removed the online ExoPlayer music (streamed from the internet); the host app's offline music is used instead. No network permission is needed.
- Replaced the GIF explosion (Coil) with a drawn animation and removed Accompanist.
- Added a game-over dialog when the ship's HP reaches zero (the original never ended).
- Translated all player-facing text to Arabic.
