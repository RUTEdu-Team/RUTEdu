package prz.rutedu.app.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*

/**
 * The three tabs available in the bottom navigation bar.
 *
 * Each entry carries the navigation [route] string that identifies the destination
 * inside the app's `NavHost`, and a [label] shown below the tab icon.
 *
 * @property route Navigation route string that matches a `composable(route)` call in `App.kt`.
 * @property label Display label shown beneath the icon (Polish).
 */
enum class NavTab(val route: String, val label: String) {
    /** Home screen - subject grid and mini-game launcher. */
    START("home", "START"),
    /** Learning tab - navigates to the last visited subject, or the subject list placeholder. */
    NAUKA("nauka", "NAUKA"),
    /** Exercises tab - navigates to the last opened lesson, or the exercises placeholder. */
    CWICZENIA("cwiczenia", "ĆWICZENIA")
}

/**
 * Bottom navigation bar with three tabs: START, NAUKA, ĆWICZENIA.
 *
 * The **active tab** is derived from the current navigation route rather than stored
 * as explicit state, so the bar stays in sync with back-stack navigation and deep links:
 * - Routes starting with `"lesson/"` -> ĆWICZENIA tab is highlighted.
 * - Routes starting with `"subject/"` or `"topic/"` -> NAUKA tab is highlighted.
 * - Everything else -> START tab is highlighted.
 *
 * The [activeColor] parameter changes the selected icon and label colour to match the
 * current subject's accent colour, giving visual continuity as the user moves through
 * different subjects.
 *
 * @param currentRoute  The current navigation route string from `navController.currentBackStackEntryAsState()`.
 *                      `null` defaults to the START tab.
 * @param onTabSelected Callback invoked with the tapped [NavTab]. The caller (`App.kt`) is
 *                      responsible for performing the navigation, including restoring the
 *                      last-visited subject or lesson for NAUKA and ĆWICZENIA respectively.
 * @param activeColor   Colour applied to the selected tab's icon and label.
 *                      Defaults to the app's global orange accent.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (NavTab) -> Unit,
    activeColor: Color = Color(0xFFF47B20)
) {
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Determine which tab is visually active based on the current route prefix.
    // This keeps the indicator in sync even when navigating via the system back button.
    val activeTab: NavTab = when {
        currentRoute == NavTab.CWICZENIA.route ||
            currentRoute?.startsWith("lesson/") == true -> NavTab.CWICZENIA
        currentRoute == NavTab.NAUKA.route ||
            currentRoute?.startsWith("subject/") == true ||
            currentRoute?.startsWith("topic/") == true -> NavTab.NAUKA
        else -> NavTab.START
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp // no shadow - border is handled by Scaffold's bottom bar
    ) {
        NavTab.entries.forEach { tab ->
            val isSelected = tab == activeTab
            val labelText = when (tab) {
                NavTab.START -> stringResource(Res.string.nav_start)
                NavTab.NAUKA -> stringResource(Res.string.nav_learn)
                NavTab.CWICZENIA -> stringResource(Res.string.nav_exercises)
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            NavTab.START     -> Icons.Default.Home
                            NavTab.NAUKA     -> Icons.AutoMirrored.Filled.MenuBook
                            NavTab.CWICZENIA -> Icons.AutoMirrored.Filled.Assignment
                        },
                        contentDescription = labelText
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = activeColor,
                    selectedTextColor   = activeColor,
                    indicatorColor      = Color.Transparent, // no pill/ripple background
                    unselectedIconColor = inactiveColor,
                    unselectedTextColor = inactiveColor
                )
            )
        }
    }
}
