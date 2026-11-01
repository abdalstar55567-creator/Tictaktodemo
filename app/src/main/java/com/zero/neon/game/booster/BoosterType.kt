package com.zero.neon.game.booster

import androidx.annotation.DrawableRes
import com.example.tictactoe.R

enum class BoosterType(@DrawableRes val drawableId: Int) {
    ULTIMATE_WEAPON_BOOSTER(R.drawable.neon_booster_ultimate_weapon),
    SHIELD_BOOSTER(R.drawable.neon_booster_shield),
    HEALTH_BOOSTER(R.drawable.neon_booster_health),
    LASER_BOOSTER(R.drawable.neon_booster_red_lasers),
    TRIPLE_LASER_BOOSTER(R.drawable.neon_booster_triple_laser)
}