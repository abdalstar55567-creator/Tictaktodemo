package com.zero.neon.game.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R

@Composable
fun SettingsButton(modifier: Modifier = Modifier, onSettings: () -> Unit) {

    val buttonPaddingEnd = dimensionResource(id = R.dimen.neon_button_padding)
    val buttonPaddingTop = buttonPaddingEnd * 2
    val buttonSize = 60.dp

    Image(
        painter = painterResource(id = R.drawable.neon_settings_button),
        contentDescription = stringResource(id = R.string.neon_game_settings_button),
        modifier = modifier
            .padding(top = buttonPaddingTop, end = buttonPaddingEnd)
            .size(buttonSize)
            .clickable { onSettings() }
    )
}