package prz.rutedu.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A 4x3 numeric keypad used for entering integer answers in quiz questions.
 *
 * The layout mirrors a standard calculator:
 * ```
 * [ 1 ] [ 2 ] [ 3 ]
 * [ 4 ] [ 5 ] [ 6 ]
 * [ 7 ] [ 8 ] [ 9 ]
 * [ C ] [ 0 ] [ ⌫ ]
 * ```
 *
 * The three special keys are:
 * - **C** - clears the entire current input ([onClear]).
 * - **⌫** - removes the last entered digit ([onBackspace]).
 * - **0–9** - appends a digit string to the current input ([onDigit]).
 *
 * The keypad does **not** manage its own input state - it merely fires callbacks.
 * The caller is responsible for maintaining the displayed value and enforcing any
 * length or range constraints before forwarding input to question logic.
 *
 * @param onDigit     Called with the digit string `"0"`-`"9"` when a number key is tapped.
 * @param onClear     Called when the **C** key is tapped. Should reset the input to empty.
 * @param onBackspace Called when the **⌫** key is tapped. Should remove the last character
 *                    from the input, or do nothing if the input is already empty.
 */
@Composable
fun NumberKeypad(
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                when (key) {
                                    "C" -> onClear()
                                    "⌫" -> onBackspace()
                                    else -> onDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = key, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
