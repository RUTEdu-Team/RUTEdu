package prz.rutedu.app.data

import prz.rutedu.app.models.ELEMENTS
import prz.rutedu.app.models.Element
import prz.rutedu.app.models.ElementCategory.ACTINIDE
import prz.rutedu.app.models.ElementCategory.ALKALINE_EARTH
import prz.rutedu.app.models.ElementCategory.ALKALI_METAL
import prz.rutedu.app.models.ElementCategory.HALOGEN
import prz.rutedu.app.models.ElementCategory.LANTHANIDE
import prz.rutedu.app.models.ElementCategory.METALLOID
import prz.rutedu.app.models.ElementCategory.NOBLE_GAS
import prz.rutedu.app.models.ElementCategory.POST_TRANSITION
import prz.rutedu.app.models.ElementCategory.REACTIVE_NONMETAL
import prz.rutedu.app.models.ElementCategory.TRANSITION_METAL
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.BalanceTerm
import prz.rutedu.app.models.Question.ElementCardQuiz
import prz.rutedu.app.models.Question.EquationBalance
import prz.rutedu.app.models.Question.PeriodicTableByName
import prz.rutedu.app.models.Question.PeriodicTableByShell
import prz.rutedu.app.models.Question.PeriodicTableQuiz
import prz.rutedu.app.models.Question.SelectFromList
import prz.rutedu.app.models.elementByNumber
import prz.rutedu.app.models.shellConfigByNumber
import kotlin.math.roundToInt
import kotlin.random.Random

private fun String.format(vararg args: Any): String {
    var result = this
    for (arg in args) {
        val indexS = result.indexOf("%s")
        val indexD = result.indexOf("%d")
        val index = when {
            indexS == -1 -> indexD
            indexD == -1 -> indexS
            else -> minOf(indexS, indexD)
        }
        if (index != -1) {
            result = result.substring(0, index) + arg.toString() + result.substring(index + 2)
        }
    }
    return result
}

/**
 * Procedurally generates chemistry quiz questions from a random seed.
 *
 * All user-visible text is resolved through [GeneratorStrings.chem], which loads flat
 * key-value JSON files from `files/chem/strings_<lang>.json`.  Adding a new language
 * requires only creating that file — no code changes needed.
 *
 * @see GeneratorStrings
 */
object ChemistryQuestionGenerator {

    private fun s(key: String) = GeneratorStrings.chem(key)

    /**
     * Generates a list of chemistry questions for the specified [lessonId].
     *
     * @param lessonId   The identifier of the chemistry lesson (e.g. `"chemia_1_1"`).
     * @param seed       The random seed to ensure deterministic question generation.
     * @param excludeIds The set of question IDs to exclude from the generated list.
     * @return List of generated chemistry [Question]s.
     */
    fun generateFor(lessonId: String, seed: Long, excludeIds: Set<Int> = emptySet()): List<Question> {
        val all = when (lessonId) {
            "chemia_1_1" -> chemia_1_1(seed)
            "chemia_1_2" -> chemia_1_2(seed)
            "chemia_1_3" -> chemia_1_3(seed)
            "chemia_1_4" -> chemia_1_4(seed)
            "chemia_2_1" -> chemia_2_1(seed)
            "chemia_2_2" -> chemia_2_2(seed)
            "chemia_3_1" -> chemia_3_1(seed)
            "chemia_3_2" -> chemia_3_2(seed)
            "chemia_3_3" -> chemia_3_3(seed)
            "chemia_3_4" -> chemia_3_4(seed)
            "chemia_4_1" -> chemia_4_1(seed)
            "chemia_4_2" -> chemia_4_2(seed)
            "chemia_5_1" -> chemia_5_1(seed)
            "chemia_5_2" -> chemia_5_2(seed)
            "chemia_6_1" -> chemia_6_1(seed)
            else -> emptyList()
        }
        return if (excludeIds.isEmpty()) all else all.filter { it.id !in excludeIds }
    }

    /**
     * Returns the total number of questions available for the specified [lessonId].
     *
     * @param lessonId The identifier of the chemistry lesson.
     * @return The count of generated questions.
     */
    fun totalFor(lessonId: String): Int = generateFor(lessonId, seed = 0L).size

    // -----------------------------------------------------------------------
    // 1-1  Atomic structure
    // -----------------------------------------------------------------------

    private fun chemia_1_1(seed: Long): List<Question> {
        val rng = Random(seed)
        return shellConfigByNumber.entries
            .filter { it.key in 1..54 }
            .shuffled(rng)
            .mapIndexed { i, (z, shell) ->
                val el = elementByNumber[z]!!
                PeriodicTableByShell(
                    id = 1100 + i,
                    shellConfig = shell,
                    targetAtomicNumber = z,
                    hint = Hint(
                        mainText = "${el.name} (${el.symbol}) — $z ${s("shell.layer").lowercase()}: $shell.",
                        boldPart = el.symbol,
                        steps = shellSteps(z, shell)
                    )
                )
            }
    }

    // -----------------------------------------------------------------------
    // 1-2  Periodic table by name
    // -----------------------------------------------------------------------

    private fun chemia_1_2(seed: Long): List<Question> {
        val rng = Random(seed)
        return ELEMENTS
            .filter { it.atomicNumber <= 86 && it.category !in listOf(LANTHANIDE, ACTINIDE) }
            .shuffled(rng)
            .mapIndexed { i, el ->
                PeriodicTableByName(
                    id = 1200 + i,
                    elementName = el.name,
                    targetAtomicNumber = el.atomicNumber,
                    hint = Hint(
                        mainText = "${el.name} (${el.symbol}) — gr. ${el.tableCol}, per. ${el.tableRow}.",
                        boldPart = el.symbol
                    )
                )
            }
    }

    // -----------------------------------------------------------------------
    // 1-3  Molecules and formulas
    // -----------------------------------------------------------------------

    private fun chemia_1_3(seed: Long) = moleculeQuestions(Random(seed))

    // -----------------------------------------------------------------------
    // 1-4  Electrons in atoms
    // -----------------------------------------------------------------------

    private fun chemia_1_4(seed: Long): List<Question> {
        val rng = Random(seed)
        return ELEMENTS
            .filter { it.atomicNumber in 1..36 }
            .flatMap { el -> listOf(electronQ(el, rng), protonQ(el, rng), massQ(el, rng), periodQ(el, rng)) }
            .shuffled(rng)
            .mapIndexed { i, q -> q.copy(id = 1400 + i) }
    }

    // -----------------------------------------------------------------------
    // 2-1  Periodic table sets
    // -----------------------------------------------------------------------

    private fun chemia_2_1(seed: Long): List<Question> {
        val rng = Random(seed)
        return periodicTableSets().shuffled(rng).mapIndexed { i, (title, nums, hint) ->
            PeriodicTableQuiz(id = 2100 + i, questionText = title, missingAtomicNumbers = nums, hint = hint)
        }
    }

    // -----------------------------------------------------------------------
    // 2-2  Element properties
    // -----------------------------------------------------------------------

    private fun chemia_2_2(seed: Long) = propertyQuestions(Random(seed))

    // -----------------------------------------------------------------------
    // 3-1  Acids
    // -----------------------------------------------------------------------

    /**
     * Represents a chemical acid definition.
     *
     * @property formula The chemical formula (e.g. "HCl").
     * @property nameKey The localization key for the acid's name.
     * @property typeKey The localization key for the acid type (e.g. binary or oxyacid).
     * @property hintKey The localization key for the help hint content.
     */
    private data class AcidEntry(
        val formula: String,
        val nameKey: String,
        val typeKey: String,
        val hintKey: String
    )

    private val acids = listOf(
        AcidEntry("HF",    "acid.HF.name",    "cat.acid.binary",  "acid.HF.hint"),
        AcidEntry("HCl",   "acid.HCl.name",   "cat.acid.binary",  "acid.HCl.hint"),
        AcidEntry("HBr",   "acid.HBr.name",   "cat.acid.binary",  "acid.HBr.hint"),
        AcidEntry("HI",    "acid.HI.name",    "cat.acid.binary",  "acid.HI.hint"),
        AcidEntry("H₂S",  "acid.H2S.name",   "cat.acid.binary",  "acid.H2S.hint"),
        AcidEntry("H₂SO₄", "acid.H2SO4.name", "cat.acid.oxyacid", "acid.H2SO4.hint"),
        AcidEntry("H₂SO₃", "acid.H2SO3.name", "cat.acid.oxyacid", "acid.H2SO3.hint"),
        AcidEntry("HNO₃",  "acid.HNO3.name",  "cat.acid.oxyacid", "acid.HNO3.hint"),
        AcidEntry("HNO₂",  "acid.HNO2.name",  "cat.acid.oxyacid", "acid.HNO2.hint"),
        AcidEntry("H₃PO₄", "acid.H3PO4.name", "cat.acid.oxyacid", "acid.H3PO4.hint"),
        AcidEntry("H₂CO₃", "acid.H2CO3.name", "cat.acid.oxyacid", "acid.H2CO3.hint"),
        AcidEntry("HClO₄", "acid.HClO4.name", "cat.acid.oxyacid", "acid.HClO4.hint"),
        AcidEntry("HClO₃", "acid.HClO3.name", "cat.acid.oxyacid", "acid.HClO3.hint"),
        AcidEntry("HClO₂", "acid.HClO2.name", "cat.acid.oxyacid", "acid.HClO2.hint"),
        AcidEntry("HClO",  "acid.HClO.name",  "cat.acid.oxyacid", "acid.HClO.hint"),
        AcidEntry("H₃BO₃", "acid.H3BO3.name", "cat.acid.oxyacid", "acid.H3BO3.hint")
    )

    private fun chemia_3_1(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<SelectFromList>()
        val typeOpts = listOf(s("cat.acid.binary"), s("cat.acid.oxyacid"))
        val typeHint = s("hint.acid_type")

        acids.forEach { acid ->
            val name = s(acid.nameKey)
            val type = s(acid.typeKey)
            val hint = s(acid.hintKey)
            val allNames    = acids.map { s(it.nameKey) }
            val allFormulas = acids.map { it.formula }

            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.acid.formula_to_name").format(acid.formula), o1,
                setOf(o1.indexOf(name)), hint = Hint(hint, boldPart = name))

            val wF = allFormulas.filter { it != acid.formula }.shuffled(rng).take(3)
            val o2 = (wF + acid.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.acid.name_to_formula").format(name), o2,
                setOf(o2.indexOf(acid.formula)), hint = Hint(hint, boldPart = acid.formula))

            qs += SelectFromList(0, s("prompt.acid.name_type").format(name, acid.formula), typeOpts,
                setOf(typeOpts.indexOf(type)), hint = Hint(typeHint, boldPart = type))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 3100 + i) }
    }

    // -----------------------------------------------------------------------
    // Equation-balance helper
    // -----------------------------------------------------------------------

    private fun eb(
        instr: String, sub: String? = null,
        r: List<BalanceTerm>, p: List<BalanceTerm>, h: Hint
    ) = EquationBalance(0, instr, sub ?: "", r, p, h)

    // -----------------------------------------------------------------------
    // 3-2  Acid reactions
    // -----------------------------------------------------------------------

    private val acidReactions = listOf(
        eb("instr.balance_acid_synth", null,
            listOf(BalanceTerm("H₂", null, 1), BalanceTerm("Cl₂", 1, null)),
            listOf(BalanceTerm("HCl", null, 2)),
            Hint("H₂ + Cl₂ → 2HCl.", boldPart = "2HCl")),
        eb("instr.balance_acid_synth", null,
            listOf(BalanceTerm("H₂", 1, null), BalanceTerm("F₂", null, 1)),
            listOf(BalanceTerm("HF", null, 2)),
            Hint("H₂ + F₂ → 2HF.", boldPart = "2HF")),
        eb("instr.balance_acid_synth", null,
            listOf(BalanceTerm("H₂", null, 1), BalanceTerm("Br₂", 1, null)),
            listOf(BalanceTerm("HBr", null, 2)),
            Hint("H₂ + Br₂ → 2HBr.", boldPart = "2HBr")),
        eb("instr.balance_acid_synth", null,
            listOf(BalanceTerm("H₂", 1, null), BalanceTerm("I₂", null, 1)),
            listOf(BalanceTerm("HI", null, 2)),
            Hint("H₂ + I₂ → 2HI.", boldPart = "2HI")),
        eb("instr.balance_acid_synth", null,
            listOf(BalanceTerm("H₂", null, 1), BalanceTerm("S", 1, null)),
            listOf(BalanceTerm("H₂S", null, 1)),
            Hint("H₂ + S → H₂S.", boldPart = "H₂S")),
        eb("instr.balance_acid_prep", null,
            listOf(BalanceTerm("SO₃", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("H₂SO₄", null, 1)),
            Hint("SO₃ + H₂O → H₂SO₄.", boldPart = "H₂SO₄")),
        eb("instr.balance_acid_prep", null,
            listOf(BalanceTerm("SO₂", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("H₂SO₃", null, 1)),
            Hint("SO₂ + H₂O → H₂SO₃.", boldPart = "H₂SO₃")),
        eb("instr.balance_acid_prep", null,
            listOf(BalanceTerm("CO₂", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("H₂CO₃", null, 1)),
            Hint("CO₂ + H₂O → H₂CO₃.", boldPart = "H₂CO₃")),
        eb("instr.balance_equation", null,
            listOf(BalanceTerm("N₂O₅", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("HNO₃", null, 2)),
            Hint("N₂O₅ + H₂O → 2HNO₃.", boldPart = "2HNO₃")),
        eb("instr.balance_equation", null,
            listOf(BalanceTerm("N₂O₃", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("HNO₂", null, 2)),
            Hint("N₂O₃ + H₂O → 2HNO₂.", boldPart = "2HNO₂")),
        eb("instr.balance_equation", null,
            listOf(BalanceTerm("P₂O₅", 1, null), BalanceTerm("H₂O", null, 3)),
            listOf(BalanceTerm("H₃PO₄", null, 2)),
            Hint("P₂O₅ + 3H₂O → 2H₃PO₄.", boldPart = "2H₃PO₄")),
        eb("instr.balance_equation", null,
            listOf(BalanceTerm("Cl₂O₇", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("HClO₄", null, 2)),
            Hint("Cl₂O₇ + H₂O → 2HClO₄.", boldPart = "2HClO₄")),
        eb("instr.balance_equation", null,
            listOf(BalanceTerm("Cl₂O", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("HClO", null, 2)),
            Hint("Cl₂O + H₂O → 2HClO.", boldPart = "2HClO")),
        eb("instr.balance_water_synth", null,
            listOf(BalanceTerm("H₂", null, 2), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("H₂O", null, 2)),
            Hint("2H₂ + O₂ → 2H₂O.", boldPart = "2H₂O"))
    )

    private val metalAcidReactions = listOf(
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Zn", 1, null), BalanceTerm("HCl", null, 2)),
            listOf(BalanceTerm("ZnCl₂", null, 1), BalanceTerm("H₂", 1, null)),
            Hint("Zn + 2HCl → ZnCl₂ + H₂.", boldPart = "2HCl")),
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Mg", 1, null), BalanceTerm("HCl", null, 2)),
            listOf(BalanceTerm("MgCl₂", null, 1), BalanceTerm("H₂", 1, null)),
            Hint("Mg + 2HCl → MgCl₂ + H₂.", boldPart = "2HCl")),
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Fe", 1, null), BalanceTerm("H₂SO₄", 1, null)),
            listOf(BalanceTerm("FeSO₄", null, 1), BalanceTerm("H₂", null, 1)),
            Hint("Fe + H₂SO₄ → FeSO₄ + H₂.", boldPart = "FeSO₄")),
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Ca", 1, null), BalanceTerm("HCl", null, 2)),
            listOf(BalanceTerm("CaCl₂", null, 1), BalanceTerm("H₂", 1, null)),
            Hint("Ca + 2HCl → CaCl₂ + H₂.", boldPart = "2HCl")),
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Zn", null, 1), BalanceTerm("H₂SO₄", 1, null)),
            listOf(BalanceTerm("ZnSO₄", null, 1), BalanceTerm("H₂", 1, null)),
            Hint("Zn + H₂SO₄ → ZnSO₄ + H₂.", boldPart = "ZnSO₄")),
        eb("instr.balance_metal_acid", null,
            listOf(BalanceTerm("Mg", null, 1), BalanceTerm("H₂SO₄", 1, null)),
            listOf(BalanceTerm("MgSO₄", null, 1), BalanceTerm("H₂", 1, null)),
            Hint("Mg + H₂SO₄ → MgSO₄ + H₂.", boldPart = "MgSO₄"))
    )

    private val neutralizationReactions = listOf(
        eb("instr.balance_neutralization", null,
            listOf(BalanceTerm("NaOH", null, 1), BalanceTerm("HCl", 1, null)),
            listOf(BalanceTerm("NaCl", 1, null), BalanceTerm("H₂O", null, 1)),
            Hint("NaOH + HCl → NaCl + H₂O.", boldPart = "NaCl + H₂O")),
        eb("instr.balance_neutralization", null,
            listOf(BalanceTerm("KOH", 1, null), BalanceTerm("HNO₃", null, 1)),
            listOf(BalanceTerm("KNO₃", null, 1), BalanceTerm("H₂O", 1, null)),
            Hint("KOH + HNO₃ → KNO₃ + H₂O.", boldPart = "KNO₃")),
        eb("instr.balance_neutralization", null,
            listOf(BalanceTerm("Ca(OH)₂", 1, null), BalanceTerm("HCl", null, 2)),
            listOf(BalanceTerm("CaCl₂", null, 1), BalanceTerm("H₂O", null, 2)),
            Hint("Ca(OH)₂ + 2HCl → CaCl₂ + 2H₂O.", boldPart = "2HCl")),
        eb("instr.balance_neutralization", null,
            listOf(BalanceTerm("NaOH", null, 2), BalanceTerm("H₂SO₄", 1, null)),
            listOf(BalanceTerm("Na₂SO₄", null, 1), BalanceTerm("H₂O", null, 2)),
            Hint("2NaOH + H₂SO₄ → Na₂SO₄ + 2H₂O.", boldPart = "2NaOH")),
        eb("instr.balance_neutralization", "subinstr.baso4",
            listOf(BalanceTerm("Ba(OH)₂", 1, null), BalanceTerm("H₂SO₄", 1, null)),
            listOf(BalanceTerm("BaSO₄", null, 1), BalanceTerm("H₂O", null, 2)),
            Hint("Ba(OH)₂ + H₂SO₄ → BaSO₄↓ + 2H₂O.", boldPart = "BaSO₄")),
        eb("instr.balance_neutralization", null,
            listOf(BalanceTerm("NaOH", null, 3), BalanceTerm("H₃PO₄", 1, null)),
            listOf(BalanceTerm("Na₃PO₄", null, 1), BalanceTerm("H₂O", null, 3)),
            Hint("3NaOH + H₃PO₄ → Na₃PO₄ + 3H₂O.", boldPart = "3NaOH"))
    )

    private val combustionReactions = listOf(
        eb("instr.balance_combustion", null,
            listOf(BalanceTerm("C", null, 1), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("CO₂", null, 1)),
            Hint("C + O₂ → CO₂.", boldPart = "CO₂")),
        eb("instr.balance_combustion", null,
            listOf(BalanceTerm("S", 1, null), BalanceTerm("O₂", null, 1)),
            listOf(BalanceTerm("SO₂", null, 1)),
            Hint("S + O₂ → SO₂.", boldPart = "SO₂")),
        eb("instr.balance_combustion", null,
            listOf(BalanceTerm("Mg", null, 2), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("MgO", null, 2)),
            Hint("2Mg + O₂ → 2MgO.", boldPart = "2MgO")),
        eb("instr.balance_combustion", null,
            listOf(BalanceTerm("Ca", null, 2), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("CaO", null, 2)),
            Hint("2Ca + O₂ → 2CaO.", boldPart = "2CaO")),
        eb("instr.balance_methane", "subinstr.methane",
            listOf(BalanceTerm("CH₄", 1, null), BalanceTerm("O₂", null, 2)),
            listOf(BalanceTerm("CO₂", 1, null), BalanceTerm("H₂O", null, 2)),
            Hint("CH₄ + 2O₂ → CO₂ + 2H₂O.", boldPart = "2O₂")),
        eb("instr.balance_ethane", null,
            listOf(BalanceTerm("C₂H₆", null, 2), BalanceTerm("O₂", null, 7)),
            listOf(BalanceTerm("CO₂", null, 4), BalanceTerm("H₂O", null, 6)),
            Hint("2C₂H₆ + 7O₂ → 4CO₂ + 6H₂O.", boldPart = "7O₂"))
    )

    private val decompositionReactions = listOf(
        eb("instr.balance_decomposition", "subinstr.peroxide",
            listOf(BalanceTerm("H₂O₂", null, 2)),
            listOf(BalanceTerm("H₂O", null, 2), BalanceTerm("O₂", 1, null)),
            Hint("2H₂O₂ → 2H₂O + O₂.", boldPart = "2H₂O₂")),
        eb("instr.balance_decomposition", "subinstr.mercury_oxide",
            listOf(BalanceTerm("HgO", null, 2)),
            listOf(BalanceTerm("Hg", null, 2), BalanceTerm("O₂", 1, null)),
            Hint("2HgO → 2Hg + O₂.", boldPart = "2HgO")),
        eb("instr.balance_limestone", "subinstr.limestone",
            listOf(BalanceTerm("CaCO₃", null, 1)),
            listOf(BalanceTerm("CaO", null, 1), BalanceTerm("CO₂", 1, null)),
            Hint("CaCO₃ → CaO + CO₂.", boldPart = "CaO")),
        eb("instr.balance_electrolysis", null,
            listOf(BalanceTerm("H₂O", null, 2)),
            listOf(BalanceTerm("H₂", null, 2), BalanceTerm("O₂", 1, null)),
            Hint("2H₂O → 2H₂ + O₂.", boldPart = "2H₂"))
    )

    private fun chemia_3_2(seed: Long): List<Question> {
        val rng = Random(seed)
        val all = acidReactions + metalAcidReactions + neutralizationReactions + combustionReactions + decompositionReactions
        return all.shuffled(rng).mapIndexed { i, q ->
            q.copy(id = 3200 + i, instruction = s(q.instruction),
                subInstruction = s(q.subInstruction))
        }
    }

    // -----------------------------------------------------------------------
    // 4-1  pH scale
    // -----------------------------------------------------------------------

    private val phRealWorldKeys = listOf(
        Triple("pH.lemon_juice",    2, "cat.pH.acidic"),
        Triple("pH.vinegar",        3, "cat.pH.acidic"),
        Triple("pH.coffee",         5, "cat.pH.acidic"),
        Triple("pH.milk",           6, "cat.pH.acidic"),
        Triple("pH.distilled_water",7, "cat.pH.neutral"),
        Triple("pH.blood",          7, "cat.pH.neutral"),
        Triple("pH.sea_water",      8, "cat.pH.basic"),
        Triple("pH.baking_soda",    9, "cat.pH.basic"),
        Triple("pH.soap",          10, "cat.pH.basic"),
        Triple("pH.lime_milk",     12, "cat.pH.basic")
    )

    private fun chemia_4_1(seed: Long): List<Question> {
        val rng     = Random(seed)
        val qs      = mutableListOf<SelectFromList>()
        val catA    = s("cat.pH.acidic"); val catN = s("cat.pH.neutral"); val catB = s("cat.pH.basic")
        val opts    = listOf(catA, catN, catB)
        val phSteps = listOf(s("step.pH.acidic"), s("step.pH.neutral"), s("step.pH.basic"))

        (0..14).forEach { ph ->
            val type = when { ph < 7 -> catA; ph == 7 -> catN; else -> catB }
            qs += SelectFromList(0, s("prompt.pH.classify").format(ph), opts, setOf(opts.indexOf(type)),
                hint = Hint(when {
                    ph < 7  -> s("hint.pH_scale_acid").format(ph)
                    ph == 7 -> s("hint.pH_scale_neutral")
                    else    -> s("hint.pH_scale_basic").format(ph)
                }, boldPart = type, steps = phSteps))
        }
        phRealWorldKeys.forEach { (dk, _, tk) ->
            val desc = s(dk); val type = s(tk)
            qs += SelectFromList(0, s("prompt.pH.real_world").format(desc), opts, setOf(opts.indexOf(type)),
                hint = Hint("$desc → $type.", boldPart = type, steps = phSteps))
        }
        val acidPHs = (1..6).toList().shuffled(rng); val basePHs = (8..13).toList()
        acidPHs.forEach { ph ->
            val w = (listOf(7) + basePHs.shuffled(rng).take(2)).map { it.toString() }
            val o = (w + ph.toString()).shuffled(rng)
            qs += SelectFromList(0, s("prompt.pH.pick_acidic"), o, setOf(o.indexOf(ph.toString())),
                hint = Hint(s("hint.pH_pick_acidic"), boldPart = "pH < 7", steps = phSteps))
        }
        basePHs.shuffled(rng).take(6).forEach { ph ->
            val w = (listOf(7) + acidPHs.take(2)).map { it.toString() }
            val o = (w + ph.toString()).shuffled(rng)
            qs += SelectFromList(0, s("prompt.pH.pick_basic"), o, setOf(o.indexOf(ph.toString())),
                hint = Hint(s("hint.pH_pick_basic"), boldPart = "pH > 7", steps = phSteps))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 4100 + i) }
    }

    // -----------------------------------------------------------------------
    // 4-2  Dissociation
    // -----------------------------------------------------------------------

    /**
     * Represents a chemical compound definition for electrolytic dissociation exercises.
     *
     * @property formula The chemical formula (e.g. "NaCl").
     * @property nameKey The localization key for the compound name.
     * @property ionsKey The localization key for the dissociation equation/ions.
     * @property typeKey The localization key for the compound type (e.g. acid, base, or salt).
     */
    private data class DissocEntry(
        val formula: String,
        val nameKey: String,
        val ionsKey: String,
        val typeKey: String
    )

    private val dissocData = listOf(
        DissocEntry("HCl",     "dissoc.HCl.name",   "dissoc.HCl.ions",   "cat.compound.acid"),
        DissocEntry("HBr",     "dissoc.HBr.name",   "dissoc.HBr.ions",   "cat.compound.acid"),
        DissocEntry("HI",      "dissoc.HI.name",    "dissoc.HI.ions",    "cat.compound.acid"),
        DissocEntry("HNO₃",    "dissoc.HNO3.name",  "dissoc.HNO3.ions",  "cat.compound.acid"),
        DissocEntry("H₂SO₄",   "dissoc.H2SO4.name", "dissoc.H2SO4.ions", "cat.compound.acid"),
        DissocEntry("HClO₄",   "dissoc.HClO4.name", "dissoc.HClO4.ions", "cat.compound.acid"),
        DissocEntry("NaOH",    "dissoc.NaOH.name",  "dissoc.NaOH.ions",  "cat.compound.base"),
        DissocEntry("KOH",     "dissoc.KOH.name",   "dissoc.KOH.ions",   "cat.compound.base"),
        DissocEntry("Ca(OH)₂", "dissoc.CaOH2.name", "dissoc.CaOH2.ions", "cat.compound.base"),
        DissocEntry("Ba(OH)₂", "dissoc.BaOH2.name", "dissoc.BaOH2.ions", "cat.compound.base"),
        DissocEntry("NaCl",    "dissoc.NaCl.name",  "dissoc.NaCl.ions",  "cat.compound.salt"),
        DissocEntry("KCl",     "dissoc.KCl.name",   "dissoc.KCl.ions",   "cat.compound.salt"),
        DissocEntry("CaCl₂",   "dissoc.CaCl2.name", "dissoc.CaCl2.ions", "cat.compound.salt"),
        DissocEntry("K₂SO₄",   "dissoc.K2SO4.name", "dissoc.K2SO4.ions", "cat.compound.salt"),
        DissocEntry("Na₂CO₃",  "dissoc.Na2CO3.name","dissoc.Na2CO3.ions","cat.compound.salt")
    )

    private fun chemia_4_2(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<SelectFromList>()
        val typeOpts = listOf(s("cat.compound.acid"), s("cat.compound.base"), s("cat.compound.salt"))
        val typeHints = mapOf(
            s("cat.compound.acid") to s("hint.dissoc_acid"),
            s("cat.compound.base") to s("hint.dissoc_base"),
            s("cat.compound.salt") to s("hint.dissoc_salt")
        )
        val allIons     = dissocData.map { s(it.ionsKey) }
        val allFormulas = dissocData.map { it.formula }

        dissocData.forEach { e ->
            val name = s(e.nameKey); val ions = s(e.ionsKey); val type = s(e.typeKey)
            val wI = allIons.filter { it != ions }.shuffled(rng).take(3)
            val o1 = (wI + ions).shuffled(rng)
            qs += SelectFromList(0, s("prompt.dissoc.ions").format(e.formula, name), o1, setOf(o1.indexOf(ions)),
                hint = Hint("${e.formula} → $ions.", boldPart = ions))
            val wF = allFormulas.filter { it != e.formula }.shuffled(rng).take(3)
            val o2 = (wF + e.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.dissoc.from_ions").format(ions), o2, setOf(o2.indexOf(e.formula)),
                hint = Hint("$ions — ${e.formula}.", boldPart = e.formula))
            qs += SelectFromList(0, s("prompt.dissoc.type").format(e.formula, name), typeOpts, setOf(typeOpts.indexOf(type)),
                hint = Hint(typeHints[type] ?: type, boldPart = type))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 4200 + i) }
    }

    // -----------------------------------------------------------------------
    // 5-1  Hydrocarbons
    // -----------------------------------------------------------------------

    /**
     * Represents a hydrocarbon definition.
     *
     * @property formula The chemical formula of the hydrocarbon.
     * @property nameKey The localization key for the hydrocarbon's name.
     * @property cCount The number of carbon atoms in the molecule.
     * @property typeKey The localization key for the type of hydrocarbon (e.g. alkane, alkene, alkyne).
     */
    private data class Hydrocarbon(
        val formula: String,
        val nameKey: String,
        val cCount: Int,
        val typeKey: String
    )

    private val hydrocarbons = listOf(
        Hydrocarbon("CH₄",    "hc.CH4.name",   1, "cat.hc.alkane"),
        Hydrocarbon("C₂H₆",  "hc.C2H6.name",  2, "cat.hc.alkane"),
        Hydrocarbon("C₃H₈",  "hc.C3H8.name",  3, "cat.hc.alkane"),
        Hydrocarbon("C₄H₁₀","hc.C4H10.name", 4, "cat.hc.alkane"),
        Hydrocarbon("C₅H₁₂","hc.C5H12.name", 5, "cat.hc.alkane"),
        Hydrocarbon("C₆H₁₄","hc.C6H14.name", 6, "cat.hc.alkane"),
        Hydrocarbon("C₂H₄",  "hc.C2H4.name",  2, "cat.hc.alkene"),
        Hydrocarbon("C₃H₆",  "hc.C3H6.name",  3, "cat.hc.alkene"),
        Hydrocarbon("C₄H₈",  "hc.C4H8.name",  4, "cat.hc.alkene"),
        Hydrocarbon("C₅H₁₀","hc.C5H10.name", 5, "cat.hc.alkene"),
        Hydrocarbon("C₂H₂",  "hc.C2H2.name",  2, "cat.hc.alkyne"),
        Hydrocarbon("C₃H₄",  "hc.C3H4.name",  3, "cat.hc.alkyne"),
        Hydrocarbon("C₄H₆",  "hc.C4H6.name",  4, "cat.hc.alkyne")
    )

    private fun chemia_5_1(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<SelectFromList>()
        val typeOpts = listOf(s("cat.hc.alkane"), s("cat.hc.alkene"), s("cat.hc.alkyne"))
        val hcHints  = mapOf(
            s("cat.hc.alkane") to s("hint.hc_alkane"),
            s("cat.hc.alkene") to s("hint.hc_alkene"),
            s("cat.hc.alkyne") to s("hint.hc_alkyne")
        )
        val allNames    = hydrocarbons.map { s(it.nameKey) }
        val allFormulas = hydrocarbons.map { it.formula }

        hydrocarbons.forEach { hc ->
            val name = s(hc.nameKey); val type = s(hc.typeKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.hc.formula_to_name").format(hc.formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("${hc.formula} — $name.", boldPart = name))
            val wF = allFormulas.filter { it != hc.formula }.shuffled(rng).take(3)
            val o2 = (wF + hc.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.hc.name_to_formula").format(name), o2, setOf(o2.indexOf(hc.formula)),
                hint = Hint("$name — ${hc.formula}.", boldPart = hc.formula))
            qs += SelectFromList(0, s("prompt.hc.type").format(name, hc.formula), typeOpts, setOf(typeOpts.indexOf(type)),
                hint = Hint(hcHints[type] ?: type, boldPart = type))
            val wC = (1..8).filter { it != hc.cCount }.shuffled(rng).take(3).map { it.toString() }
            val o4 = (wC + hc.cCount.toString()).shuffled(rng)
            qs += SelectFromList(0, s("prompt.hc.carbon_count").format(name, hc.formula), o4, setOf(o4.indexOf(hc.cCount.toString())),
                hint = Hint("${hc.formula}: ${hc.cCount} C.", boldPart = "${hc.cCount}"))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 5100 + i) }
    }

    // -----------------------------------------------------------------------
    // 5-2  Organic compounds
    // -----------------------------------------------------------------------

    /**
     * Represents an organic compound definition.
     *
     * @property formula The chemical formula of the organic compound.
     * @property nameKey The localization key for the compound's name.
     * @property group The functional group representation (e.g. "-OH").
     * @property groupNameKey The localization key for the functional group name.
     */
    private data class OrgCompound(
        val formula: String,
        val nameKey: String,
        val group: String,
        val groupNameKey: String
    )

    private val orgCompounds = listOf(
        OrgCompound("CH₃OH",       "org.CH3OH.name",      "-OH",   "cat.org.alcohol"),
        OrgCompound("C₂H₅OH",      "org.C2H5OH.name",     "-OH",   "cat.org.alcohol"),
        OrgCompound("C₃H₇OH",      "org.C3H7OH.name",     "-OH",   "cat.org.alcohol"),
        OrgCompound("C₄H₉OH",      "org.C4H9OH.name",     "-OH",   "cat.org.alcohol"),
        OrgCompound("HCOOH",         "org.HCOOH.name",      "-COOH", "cat.org.carboxylic"),
        OrgCompound("CH₃COOH",      "org.CH3COOH.name",    "-COOH", "cat.org.carboxylic"),
        OrgCompound("C₂H₅COOH",    "org.C2H5COOH.name",   "-COOH", "cat.org.carboxylic"),
        OrgCompound("C₃H₇COOH",    "org.C3H7COOH.name",   "-COOH", "cat.org.carboxylic"),
        OrgCompound("CH₃NH₂",      "org.CH3NH2.name",     "-NH₂",  "cat.org.amine"),
        OrgCompound("C₂H₅NH₂",     "org.C2H5NH2.name",    "-NH₂",  "cat.org.amine"),
        OrgCompound("HCOOCH₃",      "org.HCOOCH3.name",    "ester", "cat.org.ester"),
        OrgCompound("CH₃COOC₂H₅", "org.CH3COOC2H5.name", "ester", "cat.org.ester")
    )

    private fun chemia_5_2(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<SelectFromList>()
        val typeOpts = listOf(s("cat.org.alcohol"), s("cat.org.carboxylic"), s("cat.org.amine"), s("cat.org.ester"))
        val groupOpts = listOf("-OH", "-COOH", "-NH₂", "ester")
        val orgHints = mapOf(
            s("cat.org.alcohol")    to s("hint.org_alcohol"),
            s("cat.org.carboxylic") to s("hint.org_carboxylic"),
            s("cat.org.amine")      to s("hint.org_amine"),
            s("cat.org.ester")      to s("hint.org_ester")
        )
        val allNames = orgCompounds.map { s(it.nameKey) }

        orgCompounds.forEach { oc ->
            val name = s(oc.nameKey); val gn = s(oc.groupNameKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.org.formula_to_name").format(oc.formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("${oc.formula} — $name.", boldPart = name))
            qs += SelectFromList(0, s("prompt.org.type").format(name, oc.formula), typeOpts, setOf(typeOpts.indexOf(gn)),
                hint = Hint(orgHints[gn] ?: gn, boldPart = gn))
            qs += SelectFromList(0, s("prompt.org.functional_group").format(name, oc.formula), groupOpts, setOf(groupOpts.indexOf(oc.group)),
                hint = Hint("${oc.group} → $gn.", boldPart = oc.group))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 5200 + i) }
    }

    // -----------------------------------------------------------------------
    // 3-3  Bases / hydroxides
    // -----------------------------------------------------------------------

    /**
     * Represents a chemical base or hydroxide definition.
     *
     * @property formula The chemical formula (e.g. "NaOH").
     * @property nameKey The localization key for the base's name.
     * @property soluble Indicates whether the base is water-soluble.
     */
    private data class BaseEntry(val formula: String, val nameKey: String, val soluble: Boolean)

    private val hydroxides = listOf(
        BaseEntry("NaOH",     "base.NaOH.name",  true),
        BaseEntry("KOH",      "base.KOH.name",   true),
        BaseEntry("LiOH",     "base.LiOH.name",  true),
        BaseEntry("Ca(OH)₂", "base.CaOH2.name", true),
        BaseEntry("Ba(OH)₂", "base.BaOH2.name", true),
        BaseEntry("Mg(OH)₂", "base.MgOH2.name", false),
        BaseEntry("Fe(OH)₂", "base.FeOH2.name", false),
        BaseEntry("Fe(OH)₃", "base.FeOH3.name", false),
        BaseEntry("Cu(OH)₂", "base.CuOH2.name", false),
        BaseEntry("Al(OH)₃", "base.AlOH3.name", false),
        BaseEntry("Zn(OH)₂", "base.ZnOH2.name", false),
        BaseEntry("Mn(OH)₂", "base.MnOH2.name", false),
        BaseEntry("Ni(OH)₂", "base.NiOH2.name", false)
    )

    private val hydroxideFormingReactions = listOf(
        eb("instr.balance_base_prep", null,
            listOf(BalanceTerm("Na₂O", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("NaOH", null, 2)),
            Hint("Na₂O + H₂O → 2NaOH.", boldPart = "2NaOH")),
        eb("instr.balance_lime_slaking", "subinstr.lime",
            listOf(BalanceTerm("CaO", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("Ca(OH)₂", null, 1)),
            Hint("CaO + H₂O → Ca(OH)₂.", boldPart = "Ca(OH)₂")),
        eb("instr.balance_base_prep", null,
            listOf(BalanceTerm("K₂O", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("KOH", null, 2)),
            Hint("K₂O + H₂O → 2KOH.", boldPart = "2KOH")),
        eb("instr.balance_base_prep", null,
            listOf(BalanceTerm("BaO", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("Ba(OH)₂", null, 1)),
            Hint("BaO + H₂O → Ba(OH)₂.", boldPart = "Ba(OH)₂")),
        eb("instr.balance_base_prep", null,
            listOf(BalanceTerm("Li₂O", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("LiOH", null, 2)),
            Hint("Li₂O + H₂O → 2LiOH.", boldPart = "2LiOH"))
    )

    private fun chemia_3_3(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<Question>()
        val catS     = s("cat.base.soluble"); val catI = s("cat.base.insoluble")
        val solubOpts = listOf(catS, catI)
        val solubHint = s("hint.base_solubility"); val solubSec = s("section.base_solubility")
        val solubItems = listOf(s("item.base.sol1"), s("item.base.sol2"), s("item.base.insol"))
        val allFormulas = hydroxides.map { it.formula }
        val allNames    = hydroxides.map { s(it.nameKey) }

        hydroxides.forEach { b ->
            val name = s(b.nameKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.base.formula_to_name").format(b.formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("${b.formula} — $name.", boldPart = name))
            val wF = allFormulas.filter { it != b.formula }.shuffled(rng).take(3)
            val o2 = (wF + b.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.base.name_to_formula").format(name), o2, setOf(o2.indexOf(b.formula)),
                hint = Hint("$name — ${b.formula}.", boldPart = b.formula))
            val sType = if (b.soluble) catS else catI
            qs += SelectFromList(0, s("prompt.base.name_solubility").format(name, b.formula), solubOpts, setOf(solubOpts.indexOf(sType)),
                hint = Hint(solubHint, boldPart = sType, sectionTitle = solubSec, items = solubItems))
        }
        qs.addAll(hydroxideFormingReactions.shuffled(rng).map { q ->
            q.copy(instruction = s(q.instruction), subInstruction = s(q.subInstruction))
        })
        return qs.shuffled(rng).mapIndexed { i, q ->
            when (q) {
                is SelectFromList  -> q.copy(id = 3300 + i)
                is EquationBalance -> q.copy(id = 3300 + i)
                else -> q
            }
        }
    }

    // -----------------------------------------------------------------------
    // 3-4  Salts
    // -----------------------------------------------------------------------

    /**
     * Represents a chemical salt definition.
     *
     * @property formula The chemical formula of the salt (e.g. "NaCl").
     * @property nameKey The localization key for the salt's name.
     * @property acidNameKey The localization key for the parent acid's name.
     */
    private data class SaltEntry(val formula: String, val nameKey: String, val acidNameKey: String)

    private val salts = listOf(
        SaltEntry("NaCl",       "salt.NaCl.name",    "salt.NaCl.acid"),
        SaltEntry("KCl",        "salt.KCl.name",     "salt.KCl.acid"),
        SaltEntry("CaCl₂",  "salt.CaCl2.name",   "salt.CaCl2.acid"),
        SaltEntry("MgCl₂",  "salt.MgCl2.name",   "salt.MgCl2.acid"),
        SaltEntry("FeCl₂",  "salt.FeCl2.name",   "salt.FeCl2.acid"),
        SaltEntry("FeCl₃",  "salt.FeCl3.name",   "salt.FeCl3.acid"),
        SaltEntry("AlCl₃",  "salt.AlCl3.name",   "salt.AlCl3.acid"),
        SaltEntry("ZnCl₂",  "salt.ZnCl2.name",   "salt.ZnCl2.acid"),
        SaltEntry("Na₂SO₄", "salt.Na2SO4.name",  "salt.Na2SO4.acid"),
        SaltEntry("CaSO₄",  "salt.CaSO4.name",   "salt.CaSO4.acid"),
        SaltEntry("MgSO₄",  "salt.MgSO4.name",   "salt.MgSO4.acid"),
        SaltEntry("ZnSO₄",  "salt.ZnSO4.name",   "salt.ZnSO4.acid"),
        SaltEntry("FeSO₄",  "salt.FeSO4.name",   "salt.FeSO4.acid"),
        SaltEntry("BaSO₄",  "salt.BaSO4.name",   "salt.BaSO4.acid"),
        SaltEntry("K₂SO₄",  "salt.K2SO4.name",   "salt.K2SO4.acid"),
        SaltEntry("Na₂CO₃", "salt.Na2CO3.name",  "salt.Na2CO3.acid"),
        SaltEntry("CaCO₃",  "salt.CaCO3.name",   "salt.CaCO3.acid"),
        SaltEntry("MgCO₃",  "salt.MgCO3.name",   "salt.MgCO3.acid"),
        SaltEntry("NaNO₃",  "salt.NaNO3.name",   "salt.NaNO3.acid"),
        SaltEntry("Ca(NO₃)₂", "salt.CaNO32.name",  "salt.CaNO32.acid"),
        SaltEntry("KNO₃",   "salt.KNO3.name",    "salt.KNO3.acid"),
        SaltEntry("Na₃PO₄", "salt.Na3PO4.name",  "salt.Na3PO4.acid"),
        SaltEntry("Ca₃(PO₄)₂","salt.Ca3PO42.name","salt.Ca3PO42.acid")
    )

    private fun chemia_3_4(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<SelectFromList>()
        val allFormulas = salts.map { it.formula }
        val allNames    = salts.map { s(it.nameKey) }
        val allAcids    = salts.map { s(it.acidNameKey) }.distinct()
        val saltSec   = s("section.salt_names")
        val saltItems = listOf(s("item.salt.chlorides"), s("item.salt.sulfates"),
            s("item.salt.nitrates"), s("item.salt.carbonates"), s("item.salt.phosphates"))

        salts.forEach { salt ->
            val name = s(salt.nameKey); val acid = s(salt.acidNameKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.salt.formula_to_name").format(salt.formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("${salt.formula} — $name.", boldPart = name, sectionTitle = saltSec, items = saltItems))
            val wF = allFormulas.filter { it != salt.formula }.shuffled(rng).take(3)
            val o2 = (wF + salt.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.salt.name_to_formula").format(name), o2, setOf(o2.indexOf(salt.formula)),
                hint = Hint("$name — ${salt.formula}.", boldPart = salt.formula))
            val wA = allAcids.filter { it != acid }.shuffled(rng).take(3)
            val o3 = (wA + acid).shuffled(rng)
            qs += SelectFromList(0, s("prompt.salt.parent_acid").format(name, salt.formula), o3, setOf(o3.indexOf(acid)),
                hint = Hint("${salt.formula} — $acid.", boldPart = acid))
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 3400 + i) }
    }

    // -----------------------------------------------------------------------
    // 6-1  Oxides
    // -----------------------------------------------------------------------

    /**
     * Represents a chemical oxide definition.
     *
     * @property formula The chemical formula of the oxide (e.g. "CO₂").
     * @property nameKey The localization key for the oxide's name.
     * @property typeKey The localization key for the oxide's classification type (e.g. basic, acidic, neutral, or amphoteric).
     */
    private data class OxideEntry(val formula: String, val nameKey: String, val typeKey: String)

    private val oxides = listOf(
        OxideEntry("Na₂O",  "oxide.Na2O.name",  "cat.oxide.basic"),
        OxideEntry("K₂O",   "oxide.K2O.name",   "cat.oxide.basic"),
        OxideEntry("CaO",   "oxide.CaO.name",   "cat.oxide.basic"),
        OxideEntry("MgO",   "oxide.MgO.name",   "cat.oxide.basic"),
        OxideEntry("BaO",   "oxide.BaO.name",   "cat.oxide.basic"),
        OxideEntry("FeO",   "oxide.FeO.name",   "cat.oxide.basic"),
        OxideEntry("Fe₂O₃", "oxide.Fe2O3.name", "cat.oxide.basic"),
        OxideEntry("CuO",   "oxide.CuO.name",   "cat.oxide.basic"),
        OxideEntry("Li₂O",  "oxide.Li2O.name",  "cat.oxide.basic"),
        OxideEntry("CO₂",   "oxide.CO2.name",   "cat.oxide.acidic"),
        OxideEntry("SO₂",   "oxide.SO2.name",   "cat.oxide.acidic"),
        OxideEntry("SO₃",   "oxide.SO3.name",   "cat.oxide.acidic"),
        OxideEntry("N₂O₅",  "oxide.N2O5.name",  "cat.oxide.acidic"),
        OxideEntry("P₂O₅",  "oxide.P2O5.name",  "cat.oxide.acidic"),
        OxideEntry("SiO₂",  "oxide.SiO2.name",  "cat.oxide.acidic"),
        OxideEntry("CO",    "oxide.CO.name",    "cat.oxide.neutral"),
        OxideEntry("NO",    "oxide.NO.name",    "cat.oxide.neutral"),
        OxideEntry("ZnO",   "oxide.ZnO.name",   "cat.oxide.amphoteric"),
        OxideEntry("Al₂O₃", "oxide.Al2O3.name", "cat.oxide.amphoteric")
    )

    private val oxideFormingReactions = listOf(
        eb("instr.balance_oxide", null,
            listOf(BalanceTerm("Mg", null, 2), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("MgO", null, 2)), Hint("2Mg + O₂ → 2MgO.", boldPart = "2MgO")),
        eb("instr.balance_oxide", null,
            listOf(BalanceTerm("Ca", null, 2), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("CaO", null, 2)), Hint("2Ca + O₂ → 2CaO.", boldPart = "2CaO")),
        eb("instr.balance_oxide", null,
            listOf(BalanceTerm("Na", null, 4), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("Na₂O", null, 2)), Hint("4Na + O₂ → 2Na₂O.", boldPart = "2Na₂O")),
        eb("instr.balance_oxide_acidic", null,
            listOf(BalanceTerm("C", null, 1), BalanceTerm("O₂", 1, null)),
            listOf(BalanceTerm("CO₂", null, 1)), Hint("C + O₂ → CO₂.", boldPart = "CO₂")),
        eb("instr.balance_oxide_acidic", null,
            listOf(BalanceTerm("S", 1, null), BalanceTerm("O₂", null, 1)),
            listOf(BalanceTerm("SO₂", null, 1)), Hint("S + O₂ → SO₂.", boldPart = "SO₂")),
        eb("instr.balance_oxide_acidic_water", null,
            listOf(BalanceTerm("SO₃", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("H₂SO₄", null, 1)), Hint("SO₃ + H₂O → H₂SO₄.", boldPart = "H₂SO₄")),
        eb("instr.balance_oxide_basic_water", "subinstr.lime",
            listOf(BalanceTerm("CaO", 1, null), BalanceTerm("H₂O", null, 1)),
            listOf(BalanceTerm("Ca(OH)₂", null, 1)), Hint("CaO + H₂O → Ca(OH)₂.", boldPart = "Ca(OH)₂")),
        eb("instr.balance_oxide_acidic_water", null,
            listOf(BalanceTerm("CO₂", null, 1), BalanceTerm("H₂O", 1, null)),
            listOf(BalanceTerm("H₂CO₃", null, 1)), Hint("CO₂ + H₂O → H₂CO₃.", boldPart = "H₂CO₃"))
    )

    private fun chemia_6_1(seed: Long): List<Question> {
        val rng      = Random(seed)
        val qs       = mutableListOf<Question>()
        val typeOpts = listOf(s("cat.oxide.basic"), s("cat.oxide.acidic"), s("cat.oxide.amphoteric"), s("cat.oxide.neutral"))
        val typeHint = s("hint.oxide_types"); val typeSec = s("section.oxide_types")
        val typeItems = listOf(s("item.oxide.basic"), s("item.oxide.acidic"), s("item.oxide.amphoteric"), s("item.oxide.neutral"))
        val allFormulas = oxides.map { it.formula }; val allNames = oxides.map { s(it.nameKey) }

        oxides.forEach { ox ->
            val name = s(ox.nameKey); val type = s(ox.typeKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            qs += SelectFromList(0, s("prompt.oxide.formula_to_name").format(ox.formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("${ox.formula} — $name.", boldPart = name))
            val wF = allFormulas.filter { it != ox.formula }.shuffled(rng).take(3)
            val o2 = (wF + ox.formula).shuffled(rng)
            qs += SelectFromList(0, s("prompt.oxide.name_to_formula").format(name), o2, setOf(o2.indexOf(ox.formula)),
                hint = Hint("$name — ${ox.formula}.", boldPart = ox.formula))
            qs += SelectFromList(0, s("prompt.oxide.type").format(name, ox.formula), typeOpts, setOf(typeOpts.indexOf(type)),
                hint = Hint(typeHint, boldPart = type, sectionTitle = typeSec, items = typeItems))
        }
        qs.addAll(oxideFormingReactions.shuffled(rng).map { q ->
            q.copy(instruction = s(q.instruction), subInstruction = s(q.subInstruction))
        })
        return qs.shuffled(rng).mapIndexed { i, q ->
            when (q) {
                is SelectFromList  -> q.copy(id = 6100 + i)
                is EquationBalance -> q.copy(id = 6100 + i)
                else -> q
            }
        }
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    private fun electronQ(el: Element, rng: Random): ElementCardQuiz {
        val z = el.atomicNumber; val opts = buildOptions(z.toString(), distractors(z, 1, 120, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = s("prompt.elem.electrons").format(el.name),
            atomicNumber = z,
            options = opts,
            correctIndex = opts.indexOf(z.toString()),
            hint = Hint(s("hint.elem_electrons").format(z), steps = listOf("Z = $z", s("step.electrons_eq_Z").format(z)))
        )
    }

    private fun protonQ(el: Element, rng: Random): ElementCardQuiz {
        val z = el.atomicNumber; val opts = buildOptions(z.toString(), distractors(z, 1, 120, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = s("prompt.elem.protons").format(el.name),
            atomicNumber = z,
            options = opts,
            correctIndex = opts.indexOf(z.toString()),
            hint = Hint(s("hint.elem_protons").format(z), steps = listOf("Z = $z", s("step.protons_eq_Z").format(z)))
        )
    }

    private fun massQ(el: Element, rng: Random): ElementCardQuiz {
        val m = el.atomicMass.roundToInt(); val opts = buildOptions(m.toString(), distractors(m, 1, 300, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = s("prompt.elem.mass").format(el.symbol),
            atomicNumber = el.atomicNumber,
            options = opts,
            correctIndex = opts.indexOf(m.toString()),
            hint = Hint(s("hint.elem_mass").format(el.symbol, m), steps = listOf(s("hint.read_from_card")))
        )
    }

    private fun periodQ(el: Element, rng: Random): ElementCardQuiz {
        val p = el.tableRow.coerceIn(1, 7)
        val dists = (1..7).filter { it != p }.shuffled(rng).take(3).map { it.toString() }
        val opts = buildOptions(p.toString(), dists, rng)
        return ElementCardQuiz(
            id = 0,
            prompt = s("prompt.elem.period").format(el.symbol),
            atomicNumber = el.atomicNumber,
            options = opts,
            correctIndex = opts.indexOf(p.toString()),
            hint = Hint(s("hint.elem_period").format(el.name, p), steps = listOf(s("step.count_period")))
        )
    }

    private fun distractors(correct: Int, min: Int, max: Int, rng: Random): List<String> {
        val step = (correct / 5).coerceAtLeast(1)
        return (1..step * 10).flatMap { d -> listOf(correct - d, correct + d) }
            .filter { it != correct && it in min..max }.distinct().shuffled(rng).take(3).map { it.toString() }
    }

    private fun buildOptions(correct: String, wrongs: List<String>, rng: Random): List<String> =
        (wrongs.take(3) + correct).shuffled(rng)

    private fun periodicTableSets(): List<Triple<String, List<Int>, Hint>> = listOf(
        Triple(s("prompt.pt.noble_gases"),     listOf(2,10,18,36,54),   Hint("Noble gases — group 18.", items=listOf("He—gr.18,p.1","Ne—gr.18,p.2","Ar—gr.18,p.3","Kr—gr.18,p.4","Xe—gr.18,p.5"))),
        Triple(s("prompt.pt.halogens"),        listOf(9,17,35,53,85),   Hint("Halogens — group 17.",    items=listOf("F—gr.17,p.2","Cl—gr.17,p.3","Br—gr.17,p.4","I—gr.17,p.5","At—gr.17,p.6"))),
        Triple(s("prompt.pt.alkali"),          listOf(3,11,19,37,55),   Hint("Alkali metals — group 1.",items=listOf("Li—gr.1,p.2","Na—gr.1,p.3","K—gr.1,p.4","Rb—gr.1,p.5","Cs—gr.1,p.6"))),
        Triple(s("prompt.pt.alkaline_earth"),  listOf(4,12,20,38,56),   Hint("Alkaline earth metals.",      items=listOf("Be—gr.2,p.2","Mg—gr.2,p.3","Ca—gr.2,p.4","Sr—gr.2,p.5","Ba—gr.2,p.6"))),
        Triple(s("prompt.pt.mixed1"),          listOf(6,7,8,11,17),     Hint("Look at group and period.",   items=listOf("C—gr.14,p.2","N—gr.15,p.2","O—gr.16,p.2","Na—gr.1,p.3","Cl—gr.17,p.3"))),
        Triple(s("prompt.pt.noble_metals"),    listOf(26,28,29,47,79),  Hint("Transition metals 3–12.", items=listOf("Fe—gr.8","Ni—gr.10","Cu—gr.11","Ag—gr.11,p.5","Au—gr.11,p.6"))),
        Triple(s("prompt.pt.halogen_alkali"),  listOf(3,9,19,35,53),    Hint("Alkali: gr.1, halogens: gr.17.",items=listOf("Li—gr.1,p.2","F—gr.17,p.2","K—gr.1,p.4","Br—gr.17,p.4","I—gr.17,p.5"))),
        Triple(s("prompt.pt.period3"),         listOf(12,13,14,15,16),  Hint("All in row 3.",               items=listOf("Mg—gr.2","Al—gr.13","Si—gr.14","P—gr.15","S—gr.16"))),
        Triple(s("prompt.pt.period2"),         listOf(3,5,6,7,8),       Hint("All in row 2.",               items=listOf("Li—gr.1","B—gr.13","C—gr.14","N—gr.15","O—gr.16"))),
        Triple(s("prompt.pt.transition4"),     listOf(22,24,25,26,28),  Hint("Ti,Cr,Mn,Fe,Ni — period 4.",items=listOf("Ti—gr.4","Cr—gr.6","Mn—gr.7","Fe—gr.8","Ni—gr.10"))),
        Triple(s("prompt.pt.reactive_nonmetals"),listOf(6,7,8,15,16),  Hint("C,N,O,P,S — reactive nonmetals.",items=listOf("C—gr.14,p.2","N—gr.15,p.2","O—gr.16,p.2","P—gr.15,p.3","S—gr.16,p.3"))),
        Triple(s("prompt.pt.s_block"),         listOf(1,2,11,12,19),    Hint("s-block: groups 1 and 2.",    items=listOf("H—gr.1,p.1","He—gr.18,p.1","Na—gr.1,p.3","Mg—gr.2,p.3","K—gr.1,p.4"))),
        Triple(s("prompt.pt.period3_nonmetals"),listOf(14,15,16,17,18), Hint("Right side of row 3.",        items=listOf("Si—gr.14","P—gr.15","S—gr.16","Cl—gr.17","Ar—gr.18"))),
        Triple(s("prompt.pt.period4_p_block"), listOf(31,32,33,34,35),  Hint("Right side of row 4.",        items=listOf("Ga—gr.13","Ge—gr.14","As—gr.15","Se—gr.16","Br—gr.17"))),
        Triple(s("prompt.pt.period4_5_ends"),  listOf(19,36,37,54,55),  Hint("Group 1 and group 18.",       items=listOf("K—gr.1,p.4","Kr—gr.18,p.4","Rb—gr.1,p.5","Xe—gr.18,p.5","Cs—gr.1,p.6")))
    )

    private val molecules: List<Pair<String, String>> = listOf(
        "H₂O" to "mol.H2O.name",   "CO₂" to "mol.CO2.name",  "NaCl" to "mol.NaCl.name",
        "NH₃" to "mol.NH3.name",   "CH₄" to "mol.CH4.name",  "NaOH" to "mol.NaOH.name",
        "Ca(OH)₂" to "mol.CaOH2.name", "CaO" to "mol.CaO.name",
        "Fe₂O₃" to "mol.Fe2O3.name", "SO₂" to "mol.SO2.name", "SO₃" to "mol.SO3.name",
        "N₂" to "mol.N2.name", "O₂" to "mol.O2.name", "H₂" to "mol.H2.name",
        "CaCO₃" to "mol.CaCO3.name", "Al₂O₃" to "mol.Al2O3.name", "Na₂O" to "mol.Na2O.name",
        "CO" to "mol.CO.name", "HF" to "mol.HF.name", "HBr" to "mol.HBr.name"
    )

    private fun moleculeQuestions(rng: Random): List<Question> {
        val allNames    = molecules.map { s(it.second) }
        val allFormulas = molecules.map { it.first }
        val qs = molecules.flatMap { (formula, nameKey) ->
            val name = s(nameKey)
            val wN = allNames.filter { it != name }.shuffled(rng).take(3)
            val o1 = (wN + name).shuffled(rng)
            val q1 = SelectFromList(0, s("prompt.mol.formula_to_name").format(formula), o1, setOf(o1.indexOf(name)),
                hint = Hint("$formula — $name.", boldPart = name))
            val wF = allFormulas.filter { it != formula }.shuffled(rng).take(3)
            val o2 = (wF + formula).shuffled(rng)
            val q2 = SelectFromList(0, s("prompt.mol.name_to_formula").format(name), o2, setOf(o2.indexOf(formula)),
                hint = Hint("$name — $formula.", boldPart = formula))
            listOf(q1, q2)
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 1300 + i) }
    }

    private val metalCats    = setOf(ALKALI_METAL, ALKALINE_EARTH, TRANSITION_METAL, POST_TRANSITION)
    private val nonMetalCats = setOf(REACTIVE_NONMETAL, HALOGEN)

    private fun propertyQuestions(rng: Random): List<Question> {
        val qs  = mutableListOf<SelectFromList>()
        val cM  = s("cat.elem.metal"); val cNM = s("cat.elem.nonmetal")
        val cML = s("cat.elem.metalloid"); val cNG = s("cat.elem.noble_gas")
        val opts = listOf(cM, cNM, cML, cNG)

        ELEMENTS.filter { it.atomicNumber in 1..54 }.forEach { el ->
            val type = when (el.category) {
                NOBLE_GAS       -> cNG
                in metalCats    -> cM
                in nonMetalCats -> cNM
                METALLOID       -> cML
                else -> return@forEach
            }
            qs += SelectFromList(0, s("prompt.elem.type_of").format(el.name, el.symbol), opts, setOf(opts.indexOf(type)),
                hint = Hint("${el.name} — $type.", boldPart = type))
        }
        listOf(
            Triple(s("cat.elem.is_metal"),     metalCats,    nonMetalCats + setOf(METALLOID, NOBLE_GAS)),
            Triple(s("cat.elem.is_nonmetal"),  nonMetalCats, metalCats    + setOf(METALLOID, NOBLE_GAS)),
            Triple(s("cat.elem.is_noble_gas"), setOf(NOBLE_GAS), metalCats + nonMetalCats + setOf(METALLOID))
        ).forEach { (label, cCats, wCats) ->
            val cPool = ELEMENTS.filter { it.category in cCats && it.atomicNumber in 1..54 }
            val wPool = ELEMENTS.filter { it.category in wCats && it.atomicNumber in 1..54 }
            if (cPool.isNotEmpty() && wPool.size >= 3) {
                cPool.shuffled(rng).forEach { c ->
                    val o = (wPool.shuffled(rng).take(3).map { it.name } + c.name).shuffled(rng)
                    qs += SelectFromList(0, s("prompt.elem.which_is").format(label), o, setOf(o.indexOf(c.name)),
                        hint = Hint("${c.name} — $label.", boldPart = c.name))
                }
            }
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 2200 + i) }
    }

    private fun shellSteps(z: Int, config: String): List<String> {
        val shellNames = listOf("K", "L", "M", "N", "O", "P")
        return config.split(",").mapIndexed { i, n ->
            "${s("shell.layer")} ${shellNames.getOrElse(i) { "?" }}: $n"
        } + "${s("shell.total")} $z → ${elementByNumber[z]?.name} (${elementByNumber[z]?.symbol})"
    }
}
