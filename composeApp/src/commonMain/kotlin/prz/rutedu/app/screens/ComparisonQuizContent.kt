package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme

@Composable
internal fun ComparisonQuizContent(
    question: Question.ComparisonQuiz,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var selectedSymbol by remember(question.id) { mutableStateOf<String?>(null) }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    if (showHint) {
        HintBottomSheet(hint = question.hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = question.prompt,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(48.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            ComparisonTerm(question.leftExpr)
            
            Spacer(Modifier.width(16.dp))
            
            SymbolSelector(
                selectedSymbol = selectedSymbol,
                onSelect = { selectedSymbol = it; isWrong = false },
                accentColor = accentColor,
                isWrong = isWrong
            )
            
            Spacer(Modifier.width(16.dp))
            
            ComparisonTerm(question.rightExpr)
        }

        Spacer(Modifier.height(64.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = {
                if (selectedSymbol == question.correctSymbol) onCorrect()
                else { isWrong = true; onWrong() }
            },
            checkEnabled = selectedSymbol != null
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ComparisonTerm(expr: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = expr,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SymbolSelector(
    selectedSymbol: String?,
    onSelect: (String) -> Unit,
    accentColor: Color,
    isWrong: Boolean
) {
    val symbols = listOf("<", "=", ">")
    val isDark = isAppInDarkTheme()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        symbols.forEach { symbol ->
            val isSelected = selectedSymbol == symbol
            val borderColor = when {
                isSelected && isWrong -> Color(0xFFE53935)
                isSelected -> accentColor
                else -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
            }
            val bgColor = if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onSelect(symbol) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
