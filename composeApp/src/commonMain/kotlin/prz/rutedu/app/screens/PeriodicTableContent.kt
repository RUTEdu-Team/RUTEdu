package prz.rutedu.app.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.models.getElementNameRes
import prz.rutedu.app.models.ELEMENTS
import prz.rutedu.app.models.Element
import prz.rutedu.app.models.ElementCategory
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.elementByNumber
import prz.rutedu.app.theme.isAppInDarkTheme

/**
 * KMP-safe atomic mass formatter - see [ElementCardContent] for the full explanation.
 * Produces exactly 3 decimal places (e.g. `"12.011"`, `"107.868"`).
 */
private fun Float.to3dp(): String {
    val rounded = kotlin.math.round(this * 1000).toLong()
    val intPart = rounded / 1000
    val decPart = kotlin.math.abs(rounded % 1000)
    return "$intPart.${decPart.toString().padStart(3, '0')}"
}

/**
 * Verification state for the periodic table placement interaction.
 *
 * - [IDLE] - the student has not yet tapped "Sprawdź".
 * - [CORRECT] - all placements are correct; a green success indicator is shown briefly.
 * - [WRONG] - one or more placements are incorrect; incorrect slots flash red before resetting.
 */
enum class FindCheckState { IDLE, CORRECT, WRONG }

/** Width and height of each periodic-table cell in dp. */
private val CELL_SIZE = 44.dp
/** Gap between adjacent cells. */
private val CELL_PADDING = 1.dp
/** Standard IUPAC periodic table column count. */
private const val TABLE_COLS = 18
/**
 * Visual row count including the lanthanide/actinide separator gap:
 * rows 1-7 = periods 1–7, row 8 = visual gap, rows 9–10 = lanthanides + actinides.
 */
private const val TABLE_ROWS = 10

/**
 * Maps a chemical element category to its designated UI highlight color.
 *
 * @param cat The category classification of the element (e.g. noble gas, alkali metal).
 * @return Compose [Color] matching the category's hex value configuration.
 */
private fun elementColor(cat: ElementCategory): Color = Color(cat.colorHex)

private val COLOR_EMPTY_SLOT = Color(0xFFE0E0E0)
private val COLOR_EMPTY_BORDER = Color(0xFFBDBDBD)

/**
 * Question content for [Question.PeriodicTableQuiz] - the student places missing elements back
 * into their correct positions on a pinch-zoomable full periodic table.
 *
 * ## Layout
 * The table is rendered as an 18-column x 10-row grid of [ElementCell] composables:
 * - Rows 1–7 -> periods 1–7 of the standard IUPAC layout.
 * - Row 8 -> empty visual gap row separating f-block elements.
 * - Rows 9–10 -> lanthanides (La–Lu) and actinides (Ac–Lr).
 * Each cell is [CELL_SIZE] x [CELL_SIZE] dp with [CELL_PADDING] gaps, colored by [ElementCategory].
 *
 * ## Interaction
 * The student first taps an element chip in the bottom "tray" (selecting it), then taps an empty
 * slot in the table to place it. The tray only shows elements that are listed in
 * [Question.PeriodicTableQuiz.missingAtomicNumbers] and have not yet been placed.
 *
 * "Sprawdź" validates all slot contents: a `CORRECT` state triggers [onCorrect] after a 600 ms
 * delay; a `WRONG` state flashes red slots and resets placements after 1200 ms.
 *
 * ## Zoom/pan
 * `graphicsLayer` with `scaleX`/`scaleY`/`translationX`/`translationY` handles pinch-to-zoom
 * (scale range 0.5x–2.5x). Tap coordinates from `detectTapGestures` are transformed back to
 * table-local coordinates before computing which cell was hit. This is similar to the coordinate
 * inversion in [MapQuizContent].
 *
 * @param question      The question: which elements are removed from the table and must be placed.
 * @param accentColor   Subject accent color for selected tray chips and the check button.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect     Called after a short delay when all placements are correct.
 * @param onWrong       Called when the placement check fails (placements are reset automatically).
 */
@Composable
fun PeriodicTableContent(
    question: Question.PeriodicTableQuiz,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    // slotContents: slot's correct atomicNumber -> which element was placed there
    val slotContents = remember(question.id) { mutableStateMapOf<Int, Int>() }
    // checkResult: slot atomicNumber -> correct(true) / wrong(false), null = not checked yet
    var checkResult by remember(question.id) { mutableStateOf<Map<Int, Boolean>?>(null) }
    var selectedFromTray by remember(question.id) { mutableStateOf<Int?>(null) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    val allSlotsFilled = slotContents.size == question.missingAtomicNumbers.size
    // Elements still in the tray = not yet placed in any slot
    val placedElements = slotContents.values.toSet()
    val trayElements = question.missingAtomicNumbers.filter { it !in placedElements }

    // After Sprawdź: show result, then either advance or reset wrong slots
    LaunchedEffect(checkResult) {
        val result = checkResult ?: return@LaunchedEffect
        delay(1000)
        if (result.all { it.value }) {
            onCorrect()
        } else {
            // Return wrong-placed elements to tray
            result.filter { !it.value }.keys.forEach { slot -> slotContents.remove(slot) }
            checkResult = null
        }
    }

    if (showHint) {
        HintBottomSheet(
            hint = question.hint,
            accentColor = accentColor,
            onDismiss = { showHint = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(stringResource(Res.string.periodic_table_chemistry), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = question.questionText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Text(
            text = stringResource(Res.string.periodic_table_tray_instruction),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        // Zoomable periodic table
        ZoomablePeriodicTable(
            modifier = Modifier.fillMaxWidth().weight(1f),
            missingAtomicNumbers = question.missingAtomicNumbers,
            slotContents = slotContents,
            checkResult = checkResult,
            selectedFromTray = selectedFromTray,
            accentColor = accentColor,
            onSlotTapped = { slotAtomicNumber ->
                if (checkResult != null) return@ZoomablePeriodicTable  // locked during reveal
                val sel = selectedFromTray
                val currentOccupant = slotContents[slotAtomicNumber]
                if (sel != null) {
                    // Place selected element; if slot was occupied, its element returns to tray
                    slotContents[slotAtomicNumber] = sel
                    selectedFromTray = null
                } else if (currentOccupant != null) {
                    // Tap occupied slot with nothing selected -> pick up that element
                    slotContents.remove(slotAtomicNumber)
                    selectedFromTray = currentOccupant
                }
            }
        )

        // Element tray
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.missingAtomicNumbers.forEach { atomicNum ->
                        val el = elementByNumber[atomicNum] ?: return@forEach
                        val inTray = atomicNum in trayElements
                        val isSelected = selectedFromTray == atomicNum
                        TrayElementCard(
                            element = el,
                            isPlaced = !inTray,
                            isSelected = isSelected,
                            accentColor = accentColor,
                            onClick = {
                                if (checkResult != null) return@TrayElementCard
                                if (inTray) selectedFromTray = if (isSelected) null else atomicNum
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (question.hint.mainText.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showHint = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(Res.string.button_hint))
                        }
                    }
                    Button(
                        onClick = {
                            if (allSlotsFilled && checkResult == null) {
                                val cr = slotContents.mapValues { (slot, placed) -> slot == placed }
                                checkResult = cr
                                if (!cr.all { it.value }) onWrong()

                            }
                        },
                        enabled = allSlotsFilled && checkResult == null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(stringResource(Res.string.button_check), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Wrapper that adds pinch-to-zoom and pan to the full periodic table grid.
 *
 * Uses `graphicsLayer` for scale and translation so the transform is applied in a single render
 * pass without recomposing [TableContent]. Zoom is clamped to [0.3x, 4x].
 *
 * @param missingAtomicNumbers Elements removed from the table (shown as empty slots).
 * @param slotContents         Map of slot atomicNumber -> placed atomicNumber (for placement mode).
 * @param checkResult          Map of slot atomicNumber -> correct/wrong (null = pending).
 * @param selectedFromTray     Atomic number of the element selected in the tray, used to highlight
 *                             empty slots. `null` = nothing selected.
 * @param findSelected         In "find" mode, the atomic number the student last tapped.
 * @param findCheckState       Verification state for "find" mode (IDLE/CORRECT/WRONG).
 * @param onSlotTapped         Called with the slot's correct atomicNumber when tapped (placement mode).
 * @param onElementTapped      Called with the element's atomicNumber when tapped (find mode).
 */
@Composable
private fun ZoomablePeriodicTable(
    modifier: Modifier,
    missingAtomicNumbers: List<Int>,
    slotContents: Map<Int, Int> = emptyMap(),
    checkResult: Map<Int, Boolean>? = null,
    selectedFromTray: Int?,
    accentColor: Color,
    findSelected: Int? = null,
    findCheckState: FindCheckState = FindCheckState.IDLE,
    onSlotTapped: (Int) -> Unit,
    onElementTapped: (Int) -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val elementGrid: Map<Pair<Int,Int>, Element> = remember {
        ELEMENTS.associateBy { el -> Pair(el.tableRow, el.tableCol) }
    }

    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 4f)
                        offset += pan
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                TableContent(
                    elementGrid = elementGrid,
                    missingAtomicNumbers = missingAtomicNumbers,
                    slotContents = slotContents,
                    checkResult = checkResult,
                    selectedFromTray = selectedFromTray,
                    accentColor = accentColor,
                    scale = scale,
                    offset = offset,
                    findSelected = findSelected,
                    findCheckState = findCheckState,
                    onSlotTapped = onSlotTapped,
                    onElementTapped = onElementTapped
                )
            }
        }
    }
}

/**
 * The actual periodic table grid - an 18-column x 10-row absolute-position layout.
 *
 * Each [Element] is placed using `Modifier.offset(x, y)` computed from its [Element.tableRow] /
 * [Element.tableCol] coordinates. F-block elements (rows 8–9 in the data) are shifted one row
 * down visually (row 8 = gap) via `visualRow()`.
 *
 * Three cell types are rendered:
 * - [EmptySlotCell] - for missing elements that have not yet been filled.
 * - [FilledSlotCell] - for missing slots that have a placed element (pre- or post-check).
 * - [ElementCell] - for all other elements; can highlight `findSelected` in "find" mode.
 */
@Composable
private fun TableContent(
    elementGrid: Map<Pair<Int,Int>, Element>,
    missingAtomicNumbers: List<Int>,
    slotContents: Map<Int, Int> = emptyMap(),
    checkResult: Map<Int, Boolean>? = null,
    selectedFromTray: Int?,
    accentColor: Color,
    scale: Float,
    offset: Offset,
    findSelected: Int? = null,
    findCheckState: FindCheckState = FindCheckState.IDLE,
    onSlotTapped: (Int) -> Unit,
    onElementTapped: (Int) -> Unit = {}
) {
    val missingSet = missingAtomicNumbers.toSet()

    fun visualRow(tableRow: Int) = if (tableRow <= 7) tableRow else tableRow + 1

    val totalVisualRows = 11
    val totalWidth = (CELL_SIZE + CELL_PADDING * 2) * TABLE_COLS
    val totalHeight = (CELL_SIZE + CELL_PADDING * 2) * totalVisualRows

    Box(modifier = Modifier.size(width = totalWidth, height = totalHeight)) {
        ELEMENTS.forEach { el ->
            val vRow = visualRow(el.tableRow)
            val col = el.tableCol
            val isMissing = el.atomicNumber in missingSet
            val occupant = slotContents[el.atomicNumber]   // element placed in this slot
            val slotChecked = checkResult?.get(el.atomicNumber)  // null/true/false
            val isHighlightedSlot = selectedFromTray != null && isMissing && occupant == null
            val isFindSelected = findSelected == el.atomicNumber

            val x = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * (col - 1))
            val y = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * (vRow - 1))

            when {
                isMissing && occupant == null -> {
                    // Empty slot
                    EmptySlotCell(
                        x = x, y = y,
                        isHighlighted = isHighlightedSlot,
                        accentColor = accentColor,
                        onClick = { onSlotTapped(el.atomicNumber) }
                    )
                }
                isMissing && occupant != null -> {
                    // Slot with element placed - show Wordle color after check
                    val placedEl = elementByNumber[occupant]
                    if (placedEl != null) {
                        val slotColor = when (slotChecked) {
                            true  -> Color(0xFF4CAF50)   // correct - green
                            false -> Color(0xFFF44336)   // wrong - red
                            null  -> accentColor.copy(alpha = 0.25f)  // pending - light accent
                        }
                        FilledSlotCell(
                            x = x, y = y,
                            element = placedEl,
                            slotColor = slotColor,
                            checkState = slotChecked,
                            onClick = { onSlotTapped(el.atomicNumber) }
                        )
                    }
                }
                else -> {
                    // Normal element cell - tappable in find-mode
                    val findCellColor: Color? = when {
                        isFindSelected && findCheckState == FindCheckState.CORRECT -> Color(0xFF66BB6A)
                        isFindSelected && findCheckState == FindCheckState.WRONG   -> Color(0xFFE53935)
                        isFindSelected -> accentColor
                        else -> null
                    }
                    ElementCell(
                        x = x, y = y,
                        element = el,
                        isJustPlaced = false,
                        findOverrideColor = findCellColor,
                        onClick = { onElementTapped(el.atomicNumber) }
                    )
                }
            }
        }

        // Period labels (left side, col 0)
        for (period in 1..7) {
            val vRow = visualRow(period)
            val y = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * (vRow - 1))
            Text(
                text = "$period",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .offset(x = 2.dp, y = y + CELL_SIZE / 2 - 5.dp)
            )
        }

        // "57-71" and "89-103" placeholders in period 6,7 col 3 area for f-block ref
        // Already have La (57) at 6,3 and Ac (89) at 7,3

        // Gap row label
        Text(
            text = stringResource(Res.string.periodic_table_lanthanides),
            fontSize = 7.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.offset(
                x = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * 2),
                y = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * 8)
            )
        )
        Text(
            text = stringResource(Res.string.periodic_table_actinides),
            fontSize = 7.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.offset(
                x = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * 2),
                y = (CELL_PADDING + (CELL_SIZE + CELL_PADDING * 2) * 9)
            )
        )
    }
}

/**
 * A normal (non-missing) periodic-table cell.
 *
 * Background defaults to the element's [ElementCategory] color. In "find" mode, tapping a cell
 * calls [onClick] and [findOverrideColor] overrides the background/border to show selection or
 * correct/wrong feedback.
 */
@Composable
private fun ElementCell(
    x: Dp,
    y: Dp,
    element: Element,
    isJustPlaced: Boolean,
    findOverrideColor: Color? = null,
    onClick: () -> Unit = {}
) {
    val isDark = isAppInDarkTheme()
    val bgColor = when {
        isJustPlaced -> Color(0xFF66BB6A)
        findOverrideColor != null -> findOverrideColor.copy(alpha = 0.35f)
        else -> {
            val baseColor = elementColor(element.category)
            if (isDark) baseColor.copy(alpha = 0.25f) else baseColor
        }
    }
    val baseBgModifier = if (isDark) Modifier.background(MaterialTheme.colorScheme.surface) else Modifier

    val symbolColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1A1A1A)
    val numberColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF424242)
    val massColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else Color(0xFF616161)

    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(CELL_SIZE)
            .clip(RoundedCornerShape(4.dp))
            .then(baseBgModifier)
            .background(bgColor)
            .then(if (findOverrideColor != null) Modifier.border(2.dp, findOverrideColor, RoundedCornerShape(4.dp)) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${element.atomicNumber}",
                fontSize = 7.sp,
                color = numberColor,
                lineHeight = 7.sp
            )
            Text(
                text = element.symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = symbolColor,
                lineHeight = 14.sp
            )
            Text(
                text = element.atomicMass.to3dp(),
                fontSize = 6.sp,
                color = massColor,
                lineHeight = 6.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

/**
 * A blank slot for a missing element.
 *
 * Animates its border and background between neutral grey and the subject accent color when
 * [isHighlighted] (i.e. the student has selected a tray chip and this slot is available).
 * Shows a `?` placeholder until filled.
 */
@Composable
private fun EmptySlotCell(
    x: Dp,
    y: Dp,
    isHighlighted: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val defaultBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else COLOR_EMPTY_SLOT
    val defaultBorder = if (isDark) MaterialTheme.colorScheme.outline else COLOR_EMPTY_BORDER

    val borderColor by animateColorAsState(
        if (isHighlighted) accentColor else defaultBorder,
        animationSpec = tween(200)
    )
    val bgColor by animateColorAsState(
        if (isHighlighted) accentColor.copy(alpha = 0.15f) else defaultBg,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(CELL_SIZE)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor
        )
    }
}

/**
 * A slot that has been filled by the student with a tray element.
 *
 * Before "Sprawdź" is tapped ([checkState] = `null`): light accent background, clickable to
 * return the element to the tray.
 * After check: [checkState] is `true` (green) or `false` (red); clicking is disabled.
 * A `✓` / `✗` symbol appears below the symbol when the result is known.
 */
@Composable
private fun FilledSlotCell(
    x: Dp,
    y: Dp,
    element: Element,
    slotColor: Color,
    checkState: Boolean?,   // null=pending, true=correct, false=wrong
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val baseBgModifier = if (isDark) Modifier.background(MaterialTheme.colorScheme.surface) else Modifier
    val textColor = if (checkState != null) {
        Color.White
    } else {
        if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1A1A1A)
    }

    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(CELL_SIZE)
            .clip(RoundedCornerShape(4.dp))
            .then(baseBgModifier)
            .background(slotColor.copy(alpha = if (checkState != null) 0.8f else 0.25f))
            .border(2.dp, slotColor, RoundedCornerShape(4.dp))
            .clickable(enabled = checkState == null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = element.symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 14.sp
            )
            if (checkState != null) {
                Text(
                    text = if (checkState) "✓" else "✗",
                    fontSize = 10.sp,
                    color = Color.White,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

/**
 * Element chip shown in the bottom tray palette for [PeriodicTableContent].
 *
 * - **Unplaced, unselected:** category color background with symbol, name, and atomic mass.
 * - **Unplaced, selected:** light accent background with an accent border.
 * - **Placed:** shows a green `✓` icon; the chip is non-clickable and semi-transparent.
 */
@Composable
private fun TrayElementCard(
    element: Element,
    isPlaced: Boolean,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val bgColor = when {
        isPlaced -> if (isDark) Color(0xFF1B3C24) else Color(0xFFE8F5E9)
        isSelected -> accentColor.copy(alpha = 0.12f)
        else -> {
            val baseColor = elementColor(element.category)
            if (isDark) baseColor.copy(alpha = 0.25f) else baseColor
        }
    }
    val borderColor = when {
        isPlaced -> if (isDark) Color(0xFF2E7D32) else Color(0xFF66BB6A)
        isSelected -> accentColor
        else -> if (isDark) MaterialTheme.colorScheme.outline else Color.Transparent
    }
    val textAlpha = if (isPlaced) 0.4f else 1f

    val baseBgModifier = if (isDark) Modifier.background(MaterialTheme.colorScheme.surface) else Modifier
    val symbolColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1A1A1A)
    val nameColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF616161)
    val massColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else Color(0xFF757575)

    Box(
        modifier = Modifier
            .width(72.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(baseBgModifier)
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = !isPlaced, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaced) {
            Text("✓", fontSize = 24.sp, color = Color(0xFF66BB6A))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = element.symbol,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = symbolColor.copy(alpha = textAlpha)
                )
                Text(
                    text = stringResource(getElementNameRes(element.atomicNumber)),
                    fontSize = 9.sp,
                    color = nameColor.copy(alpha = textAlpha),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = element.atomicMass.to3dp(),
                    fontSize = 8.sp,
                    color = massColor.copy(alpha = textAlpha)
                )
            }
        }
    }
}

/**
 * Question content for [Question.PeriodicTableByShell] - displays a Bohr shell configuration
 * (e.g. `"2,8,4"`) and asks the student to tap the matching element on the full periodic table.
 *
 * Delegates to [PeriodicTableFindContent] with a large-text clue showing the shell string.
 *
 * @param question   Contains `shellConfig` (e.g. `"2,8,18,7"`) and `targetAtomicNumber`.
 * @param accentColor Subject accent color.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect  Called after the student taps the correct element.
 * @param onWrong    Called when an incorrect element is tapped.
 */
@Composable
fun PeriodicTableByShellContent(
    question: Question.PeriodicTableByShell,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    val isDark = isAppInDarkTheme()
    PeriodicTableFindContent(
        topLabel = stringResource(Res.string.periodic_table_chemistry),
        questionText = stringResource(Res.string.periodic_table_find_by_shell_prompt),
        clueContent = { clueColor ->
            Text(
                text = question.shellConfig,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Text(
                text = stringResource(Res.string.periodic_table_electrons_shells),
                fontSize = 12.sp,
                color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        },
        targetAtomicNumber = question.targetAtomicNumber,
        hint = question.hint,
        accentColor = accentColor,
        bottomPadding = bottomPadding,
        onCorrect = onCorrect,
        onWrong = onWrong
    )
}

/**
 * Question content for [Question.PeriodicTableByName] - displays a Polish element name
 * (e.g. `"Krzem"`) and asks the student to tap the matching element on the full periodic table.
 *
 * Delegates to [PeriodicTableFindContent] with a large-text clue showing the name.
 *
 * @param question   Contains `elementNamePL` (Polish name) and `targetAtomicNumber`.
 * @param accentColor Subject accent color.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect  Called after the student taps the correct element.
 * @param onWrong    Called when an incorrect element is tapped.
 */
@Composable
fun PeriodicTableByNameContent(
    question: Question.PeriodicTableByName,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    PeriodicTableFindContent(
        topLabel = stringResource(Res.string.periodic_table_chemistry),
        questionText = stringResource(Res.string.periodic_table_find_by_name_prompt),
        clueContent = { clueColor ->
            Text(
                text = stringResource(getElementNameRes(question.targetAtomicNumber)),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = clueColor,
                textAlign = TextAlign.Center
            )
        },
        targetAtomicNumber = question.targetAtomicNumber,
        hint = question.hint,
        accentColor = accentColor,
        bottomPadding = bottomPadding,
        onCorrect = onCorrect,
        onWrong = onWrong
    )
}

/**
 * Shared "find element on the table" screen used by both [PeriodicTableByShellContent] and
 * [PeriodicTableByNameContent].
 *
 * Renders:
 * 1. A `topLabel` pill badge (e.g. `"CHEMIA"`).
 * 2. A `questionText` subtitle.
 * 3. A clue card whose content is provided by the [clueContent] composable lambda (allows
 *    each variant to inject its own large-text clue).
 * 4. A full [ZoomablePeriodicTable] where the student taps an element.
 * 5. A "Podpowiedź" / "Sprawdź" button row.
 *
 * The clue card flashes green on `CORRECT` and red on `WRONG`. Feedback auto-dismisses:
 * - **CORRECT** -> 600 ms delay -> `onCorrect()`.
 * - **WRONG** -> 500 ms delay -> resets to `IDLE` (student can try again).
 *
 * @param topLabel           Small pill label above the question (e.g. `"CHEMIA"`).
 * @param questionText       Subtitle describing what the student should do.
 * @param clueContent        Composable content injected into the clue card.
 * @param targetAtomicNumber The correct element's atomic number; compared to the tapped element.
 * @param hint               Full hint for the [HintBottomSheet].
 * @param accentColor        Subject accent color.
 * @param bottomPadding      System navigation bar height padding.
 * @param onCorrect          Called after a successful tap and the 600 ms delay.
 * @param onWrong            Called immediately when the wrong element is tapped.
 */
@Composable
private fun PeriodicTableFindContent(
    topLabel: String,
    questionText: String,
    clueContent: @Composable ColumnScope.(clueColor: Color) -> Unit,
    targetAtomicNumber: Int,
    hint: Hint,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    // Keyed by targetAtomicNumber so state resets with each new question
    var selectedElement by remember(targetAtomicNumber) { mutableStateOf<Int?>(null) }
    var checkState by remember(targetAtomicNumber) { mutableStateOf(FindCheckState.IDLE) }
    var showHint by remember(targetAtomicNumber) { mutableStateOf(false) }

    LaunchedEffect(checkState) {
        when (checkState) {
            FindCheckState.CORRECT -> { delay(600); onCorrect() }
            FindCheckState.WRONG   -> { delay(500); checkState = FindCheckState.IDLE }
            FindCheckState.IDLE    -> {}
        }
    }

    if (showHint) {
        HintBottomSheet(hint = hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(topLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = questionText,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        // Clue card - flashes green/red based on check result
        val isDark = isAppInDarkTheme()
        val cardBg = when (checkState) {
            FindCheckState.CORRECT -> if (isDark) Color(0xFF1C3A27) else Color(0xFFE8F5E9)
            FindCheckState.WRONG   -> if (isDark) Color(0xFF422121) else Color(0xFFFFCDD2)
            FindCheckState.IDLE    -> MaterialTheme.colorScheme.surface
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                val clueColor = when (checkState) {
                    FindCheckState.CORRECT, FindCheckState.WRONG -> if (isDark) Color.White else Color(0xFF1A1A1A)
                    FindCheckState.IDLE -> MaterialTheme.colorScheme.onSurface
                }
                clueContent(clueColor)
            }
        }

        Spacer(Modifier.height(6.dp))

        // Zoomable table
        ZoomablePeriodicTable(
            modifier = Modifier.fillMaxWidth().weight(1f),
            missingAtomicNumbers = emptyList(),
            selectedFromTray = null,
            accentColor = accentColor,
            findSelected = selectedElement,
            findCheckState = checkState,
            onSlotTapped = {},
            onElementTapped = { atomicNumber ->
                if (checkState == FindCheckState.IDLE) {
                    selectedElement = atomicNumber
                }
            }
        )

        // Bottom bar - solid background so table doesn't show through
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hint.mainText.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showHint = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(Res.string.button_hint))
                    }
                }
                Button(
                    onClick = {
                        val sel = selectedElement
                        if (sel != null) {
                            checkState = if (sel == targetAtomicNumber) FindCheckState.CORRECT
                                         else FindCheckState.WRONG
                            if (sel != targetAtomicNumber) onWrong()

                        }
                    },
                    enabled = selectedElement != null && checkState == FindCheckState.IDLE,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(stringResource(Res.string.button_check), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
