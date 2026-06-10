package prz.rutedu.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.Screen
import prz.rutedu.app.theme.isAppInDarkTheme

/** App-wide orange accent (matches the bottom-nav accent). */
private val AccentOrange = Color(0xFFF47B20)

/**
 * Home screen - the first screen the user sees after launch.
 *
 * Main menu styled consistently with the rest of the app (cards like [ConfigListScreen]):
 * - App identity header: logo box + "RUTEdu" + university subtitle.
 * - Three menu cards: ĆWICZENIA -> [Screen.Subjects], POJEDYNEK -> [Screen.PvPSelect],
 *   USTAWIENIA -> [Screen.ConfigList].
 * - Footer: PRZ and WEII emblems with translated institution names, linking to their websites.
 *
 * @param navController Navigation controller used to push the destination routes.
 * @param bottomPadding Extra bottom padding equal to the system navigation bar height.
 */
@Composable
fun MainScreen(
    navController: NavController,
    bottomPadding: Dp = 0.dp
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomPadding)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App identity header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.logo_rutedu),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "RUTEdu",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(Res.string.fragment_menu_university),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        MenuCard(
            iconRes = Res.drawable.ic_dumbbell_solid,
            title = stringResource(Res.string.menu_play),
            accent = AccentOrange,
            onClick = { navController.navigate(Screen.Subjects.route) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        MenuCard(
            iconRes = Res.drawable.ic_gamepad_solid,
            title = stringResource(Res.string.menu_pvp),
            accent = Color(0xFF4A80F0),
            onClick = { navController.navigate(Screen.PvPSelect.route) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        MenuCard(
            iconRes = Res.drawable.ic_settings_24dp,
            title = stringResource(Res.string.menu_settings),
            accent = MaterialTheme.colorScheme.secondary,
            onClick = { navController.navigate(Screen.ConfigList.route) }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Institution footer: two symmetric cards of equal height (emblem + name inside).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InstitutionFooter(
                emblem = Res.drawable.emblem_prz,
                name = stringResource(Res.string.fragment_menu_university),
                onClick = { uriHandler.openUri("https://prz.edu.pl/") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            InstitutionFooter(
                emblem = Res.drawable.emblem_weii,
                name = stringResource(Res.string.weii_description),
                onClick = { uriHandler.openUri("https://weii.prz.edu.pl/") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Full-width menu card styled like the rows in [ConfigListScreen]:
 * tinted icon box + bold title + chevron on a flat surface card.
 */
@Composable
private fun MenuCard(
    iconRes: DrawableResource,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Single institution block in the footer: emblem with the translated institution name below,
 * together inside one rounded card. Symmetric regardless of text length.
 *
 * Light theme: white card with the original navy emblem and navy text.
 * Dark theme: surface-colored card with the emblem tinted white and light text.
 *
 * Tapping anywhere opens the institution website.
 */
@Composable
private fun InstitutionFooter(
    emblem: DrawableResource,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = isAppInDarkTheme()
    val cardColor = if (darkTheme) MaterialTheme.colorScheme.surface else Color.White
    val textColor = if (darkTheme) MaterialTheme.colorScheme.onSurface else Color(0xFF1F365A)
    val emblemFilter = if (darkTheme) ColorFilter.tint(Color(0xFFE8ECF4)) else null

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(emblem),
            contentDescription = name,
            modifier = Modifier.size(52.dp),
            contentScale = ContentScale.Fit,
            colorFilter = emblemFilter
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}
