100+ – Glossy 3D game controls and play-area frame (Phase 3)

Changes:
- Launcher artwork now focuses on the crowned 100+ emblem and excludes the Arabic tagline/banner.
- Shared game action buttons (BaseGameActivity.button and Ui.button/rowButton) use glossy raised gradients, bright highlights, white shadowed labels, elevation and press feedback.
- Direction-pad buttons use the same raised blue glossy treatment.
- Menu action button drawable is glossy and bright.
- Standard BaseGameActivity screens now place their game content in a rounded, raised cyan/blue frame; the top navigation bar remains outside that frame.
- Existing game logic, scoring, controls and assets are otherwise retained.

Validation performed:
- All Android resource XML files parsed successfully.
- ZIP archive integrity checked after packaging.
- A full Android Gradle build was not run in this environment.

Implementation note:
- The shared frame is applied to screens that use BaseGameActivity.setContentView(View).
- Custom activities that draw directly on a full-screen Canvas or use their own root layout are not forcibly wrapped, to avoid clipping or breaking their gameplay. They can be migrated in a later targeted pass.
