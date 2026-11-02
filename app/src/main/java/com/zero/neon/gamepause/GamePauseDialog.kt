package com.zero.neon.gamepause

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tictactoe.R
import com.zero.neon.common.theme.Blue
import com.zero.neon.common.theme.Pink

/**
 * نافذة الإيقاف المؤقت (متابعة / إعادة / خروج).
 * عُدّلت عن الأصل: الأصل كان يعتمد على Navigation ولا يملك زر متابعة.
 */
@Composable
fun GamePauseDialog(
    onResume: () -> Unit,
    onRestartGame: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        DialogCard(
            title = stringResource(id = R.string.neon_game_pause_dialog_title),
            actions = listOf(
                stringResource(id = R.string.neon_resume_game_button) to onResume,
                stringResource(id = R.string.neon_restart_game_button) to onRestartGame,
                stringResource(id = R.string.neon_exit_game_button) to onExit
            )
        )
    }
}

/** نافذة انتهاء اللعبة عندما تنفد طاقة المركبة (إضافة من 100+). */
@Composable
fun GameOverDialog(
    minerals: String,
    onRestartGame: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        DialogCard(
            title = stringResource(id = R.string.neon_game_over_title),
            subtitle = stringResource(id = R.string.neon_game_over_score, minerals),
            actions = listOf(
                stringResource(id = R.string.neon_restart_game_button) to onRestartGame,
                stringResource(id = R.string.neon_exit_game_button) to onExit
            )
        )
    }
}

@Composable
private fun DialogCard(
    title: String,
    actions: List<Pair<String, () -> Unit>>,
    subtitle: String? = null
) {
    Card(
        backgroundColor = Blue,
        border = BorderStroke(2.dp, Pink)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h5
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = subtitle, style = MaterialTheme.typography.h6)
            }
            Spacer(modifier = Modifier.height(16.dp))
            actions.forEach { (label, onClick) ->
                TextButton(onClick = onClick) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}
