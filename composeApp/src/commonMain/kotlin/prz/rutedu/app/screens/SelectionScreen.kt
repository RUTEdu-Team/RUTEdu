package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.Screen
import prz.rutedu.app.components.MenuButton
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.back
import rutedu.composeapp.generated.resources.fragment_modes_add_sub
import rutedu.composeapp.generated.resources.fragment_modes_divisibility
import rutedu.composeapp.generated.resources.fragment_modes_instruction
import rutedu.composeapp.generated.resources.fragment_modes_mul_div
import rutedu.composeapp.generated.resources.fragment_modes_table
import rutedu.composeapp.generated.resources.fragment_modes_units
import rutedu.composeapp.generated.resources.ic_add_sub
import rutedu.composeapp.generated.resources.ic_dumbbell_solid
import rutedu.composeapp.generated.resources.ic_mult_div

/**
 * Game-mode selection screen shown after a player is chosen in [PlayerSelectionScreen].
 *
 * Lists all five solo arithmetic game modes as [MenuButton] entries. Each button navigates
 * to the corresponding [GameScreen] route.
 *
 * @param navController Navigation controller used to push a [Screen] game route.
 */
@Composable
fun SelectionScreen(navController: NavController) {
    val instructionText = stringResource(Res.string.fragment_modes_instruction)
    val addSubText = stringResource(Res.string.fragment_modes_add_sub)
    val mulDivText = stringResource(Res.string.fragment_modes_mul_div)
    val divisibilityText = stringResource(Res.string.fragment_modes_divisibility)
    val unitConversionText = stringResource(Res.string.fragment_modes_units)
    val multiplicationTableText = stringResource(Res.string.fragment_modes_table)
    val backText = stringResource(Res.string.back)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = instructionText,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))

        // 1. Addition & Subtraction
        MenuButton(iconRes = Res.drawable.ic_add_sub, text = addSubText) {
            navController.navigate(route = Screen.GameAddSubtract.route)
        }

        // 2. Multiplication & Division
        MenuButton(iconRes = Res.drawable.ic_mult_div, text = mulDivText) {
            navController.navigate(route = Screen.GameMultiplyDivide.route)
        }

        // 3. Divisibility
        MenuButton(iconRes = Res.drawable.ic_dumbbell_solid, text = divisibilityText) {
            navController.navigate(route = Screen.GameDivisibility.route)
        }

        // 4. Unit Conversion
        MenuButton(iconRes = Res.drawable.ic_dumbbell_solid, text = unitConversionText) {
            navController.navigate(route = Screen.GameUnitConversion.route)
        }

        // 5. Multiplication Table
        MenuButton(iconRes = Res.drawable.ic_dumbbell_solid, text = multiplicationTableText) {
            navController.navigate(route = Screen.GameMultiplicationTable.route)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(backText)
        }
    }
}