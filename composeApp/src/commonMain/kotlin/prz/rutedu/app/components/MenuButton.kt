package prz.rutedu.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Full-width horizontal button used on the main menu / home screen for primary navigation actions
 * (e.g. "Graj solo", "Tryb PvP").
 *
 * Layout: `[spacer] [icon box] [spacer] [label text]`
 *
 * The button uses a fixed cyan/teal hue (`HSL 195.82°, 100%, 50%`) regardless of the active
 * `MaterialTheme`, giving a consistent brand look across all subjects:
 * - **Background:** the hue at 70% alpha so the underlying scaffold color shows faintly through.
 * - **Icon box:** the same hue at full opacity, creating a slightly darker square relative to the
 *   semi-transparent button background.
 * - **Text:** `MaterialTheme.colorScheme.onPrimary` (white in the default theme) for contrast.
 *
 * The icon is loaded from a drawable resource (PNG/SVG via Compose resources) rather than a
 * vector `ImageVector`, because the menu icons are raster assets in the resource bundle.
 *
 * @param iconRes         Drawable resource ID for the 36 dp image displayed inside the icon box.
 * @param text            Label rendered to the right of the icon box at 24 sp bold.
 * @param onClick         Called when the user taps anywhere on the button row.
 * @param backgroundColor Base hue for the button. The row background uses it at 70% alpha,
 *                        the icon box at full opacity. Defaults to the legacy cyan.
 */
@Composable
fun MenuButton(
    iconRes: DrawableResource,
    text: String,
    backgroundColor: Color = Color.hsl(195.82f, 1f, 0.5f),
    onClick: () -> Unit
) {
    val buttonBg = backgroundColor.copy(0.70f)
    val iconBoxBg = backgroundColor
    val textColor = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(buttonBg)
            .clickable { onClick() }
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(12.dp))
        // Left icon box
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = text,
                modifier = Modifier
                    .size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}