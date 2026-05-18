package com.example.myapplication.data

import com.example.myapplication.models.*
import com.example.myapplication.models.ElementCategory.*
import com.example.myapplication.models.Question.*
import kotlin.math.roundToInt
import kotlin.random.Random

object ChemistryQuestionGenerator {

    fun generateFor(lessonId: String): List<Question> {
        val seed = Random.nextLong()
        return when (lessonId) {
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
    }

    // ── lesson generators ─────────────────────────────────────────────────────

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
                        mainText = "${el.namePL} (${el.symbol}) ma $z elektronów: $shell.",
                        boldPart = el.symbol,
                        steps = shellSteps(z, shell)
                    )
                )
            }
    }

    private fun chemia_1_2(seed: Long): List<Question> {
        val rng = Random(seed)
        return ELEMENTS
            .filter { it.atomicNumber <= 86 && it.category !in listOf(LANTHANIDE, ACTINIDE) }
            .shuffled(rng)
            .mapIndexed { i, el ->
                PeriodicTableByName(
                    id = 1200 + i,
                    elementNamePL = el.namePL,
                    targetAtomicNumber = el.atomicNumber,
                    hint = Hint(
                        mainText = "${el.namePL} (${el.symbol}) — gr. ${el.tableCol}, okres ${el.tableRow}.",
                        boldPart = el.symbol
                    )
                )
            }
    }

    private fun chemia_1_3(seed: Long): List<Question> {
        val rng = Random(seed)
        return moleculeQuestions(rng)
    }

    private fun chemia_1_4(seed: Long): List<Question> {
        val rng = Random(seed)
        return ELEMENTS
            .filter { it.atomicNumber in 1..36 }
            .flatMap { el ->
                listOf(electronQ(el, rng), protonQ(el, rng), massQ(el, rng), periodQ(el, rng))
            }
            .shuffled(rng)
            .mapIndexed { i, q -> q.copy(id = 1400 + i) }
    }

    private fun chemia_2_1(seed: Long): List<Question> {
        val rng = Random(seed)
        return periodicTableSets().shuffled(rng).mapIndexed { i, (title, nums, hint) ->
            PeriodicTableQuiz(id = 2100 + i, questionText = title, missingAtomicNumbers = nums, hint = hint)
        }
    }

    private fun chemia_2_2(seed: Long): List<Question> {
        val rng = Random(seed)
        return propertyQuestions(rng)
    }

    // ── chemia_3_1  Wzory kwasów ─────────────────────────────────────────────

    private data class AcidEntry(
        val formula: String,
        val namePL: String,
        val type: String,           // "beztlenowy" / "tlenowy"
        val hint: String
    )

    private val acids = listOf(
        AcidEntry("HF",      "kwas fluorowodorowy",   "beztlenowy", "HF – fluorowodorowy, trawiący szkło."),
        AcidEntry("HCl",     "kwas chlorowodorowy",   "beztlenowy", "HCl – chlorowodorowy (solny), składnik soku żołądkowego."),
        AcidEntry("HBr",     "kwas bromowodorowy",    "beztlenowy", "HBr – bromowodorowy, podobny do HCl."),
        AcidEntry("HI",      "kwas jodowodorowy",     "beztlenowy", "HI – jodowodorowy, silny kwas beztlenowy."),
        AcidEntry("H₂S",    "kwas siarkowodorowy",   "beztlenowy", "H₂S – siarkowodorowy, znany ze smrodu zgniłych jaj."),
        AcidEntry("H₂SO₄",  "kwas siarkowy(VI)",      "tlenowy",    "H₂SO₄ – siarkowy(VI), siarka na +6. Najważniejszy kwas przemysłowy."),
        AcidEntry("H₂SO₃",  "kwas siarkowy(IV)",      "tlenowy",    "H₂SO₃ – siarkowy(IV), siarka na +4. Przyczyna kwaśnych deszczy."),
        AcidEntry("HNO₃",   "kwas azotowy(V)",        "tlenowy",    "HNO₃ – azotowy(V), azot na +5. Używany do nawozów i materiałów wybuchowych."),
        AcidEntry("HNO₂",   "kwas azotowy(III)",      "tlenowy",    "HNO₂ – azotowy(III), azot na +3. Słabszy kwas niż HNO₃."),
        AcidEntry("H₃PO₄",  "kwas fosforowy(V)",      "tlenowy",    "H₃PO₄ – fosforowy(V). Składnik nawozów i napojów cola."),
        AcidEntry("H₂CO₃",  "kwas węglowy",           "tlenowy",    "H₂CO₃ – węglowy. Powstaje gdy CO₂ rozpuszcza się w wodzie."),
        AcidEntry("HClO₄",  "kwas chlorowy(VII)",     "tlenowy",    "HClO₄ – chlorowy(VII), chlor na +7. Jeden z najmocniejszych kwasów."),
        AcidEntry("HClO₃",  "kwas chlorowy(V)",       "tlenowy",    "HClO₃ – chlorowy(V), chlor na +5."),
        AcidEntry("HClO₂",  "kwas chlorowy(III)",     "tlenowy",    "HClO₂ – chlorowy(III), chlor na +3."),
        AcidEntry("HClO",   "kwas chlorowy(I)",       "tlenowy",    "HClO – chlorowy(I), chlor na +1. Słaby kwas, właściwości odkażające."),
        AcidEntry("H₃BO₃",  "kwas borowy",            "tlenowy",    "H₃BO₃ – borowy. Stosowany w okulistyce jako środek dezynfekcyjny."),
    )

    private fun chemia_3_1(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<SelectFromList>()
        val allFormulas = acids.map { it.formula }
        val allNames    = acids.map { it.namePL }
        val typeOpts    = listOf("beztlenowy", "tlenowy")
        val typeHint    = "Kwasy beztlenowe nie zawierają tlenu (HX, H₂X). Kwasy tlenowe zawierają tlen (np. HNO₃, H₂SO₄)."

        acids.forEach { acid ->
            // formula → name
            val wNames = allNames.filter { it != acid.namePL }.shuffled(rng).take(3)
            val opts1 = (wNames + acid.namePL).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Kwas o wzorze ${acid.formula} to:",
                options = opts1,
                correctIndices = setOf(opts1.indexOf(acid.namePL)),
                hint = Hint(acid.hint, boldPart = acid.namePL)
            )

            // name → formula
            val wForms = allFormulas.filter { it != acid.formula }.shuffled(rng).take(3)
            val opts2 = (wForms + acid.formula).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Wzór ${acid.namePL} to:",
                options = opts2,
                correctIndices = setOf(opts2.indexOf(acid.formula)),
                hint = Hint(acid.hint, boldPart = acid.formula)
            )

            // type: tlenowy / beztlenowy
            qs += SelectFromList(
                id = 0,
                prompt = "${acid.namePL} (${acid.formula}) jest kwasem:",
                options = typeOpts,
                correctIndices = setOf(typeOpts.indexOf(acid.type)),
                hint = Hint(typeHint, boldPart = acid.type)
            )
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 3100 + i) }
    }

    // ── chemia_3_2  Reakcje otrzymywania kwasów ───────────────────────────────

    private val acidReactions: List<EquationBalance> = listOf(
        // ── Direct synthesis (H₂ + non-metal) ────────────────────────────────
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy kwasu",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("Cl₂", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("H₂ + Cl₂ → 2HCl. Po lewej: 2H i 2Cl → po prawej 2 cząsteczki HCl.", boldPart = "2HCl",
                steps = listOf("1×H₂ = 2H, 1×Cl₂ = 2Cl", "2H + 2Cl → 2×HCl"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy kwasu",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = 1), BalanceTerm("F₂", fixedCoefficient = null, correctCoefficient = 1)),
            products = listOf(BalanceTerm("HF", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("H₂ + F₂ → 2HF. Analogicznie jak synteza HCl.", boldPart = "2HF",
                steps = listOf("1×H₂ = 2H, 1×F₂ = 2F", "2H + 2F → 2×HF"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy kwasu",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("Br₂", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("HBr", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("H₂ + Br₂ → 2HBr.", boldPart = "2HBr",
                steps = listOf("1×H₂ = 2H, 1×Br₂ = 2Br", "2H + 2Br → 2×HBr"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy kwasu",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = 1), BalanceTerm("I₂", fixedCoefficient = null, correctCoefficient = 1)),
            products = listOf(BalanceTerm("HI", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("H₂ + I₂ → 2HI.", boldPart = "2HI",
                steps = listOf("1×H₂ = 2H, 1×I₂ = 2I", "2H + 2I → 2×HI"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy kwasu",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("S", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("H₂S", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("H₂ + S → H₂S. Wszystkie współczynniki = 1.", boldPart = "H₂S")
        ),
        // ── Oxide + water ─────────────────────────────────────────────────────
        EquationBalance(
            id = 0,
            instruction = "Uzupełnij reakcję otrzymywania kwasu",
            reactants = listOf(BalanceTerm("SO₃", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("H₂SO₄", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("SO₃ + H₂O → H₂SO₄. Tlenek kwasowy + woda → kwas.", boldPart = "H₂SO₄")
        ),
        EquationBalance(
            id = 0,
            instruction = "Uzupełnij reakcję otrzymywania kwasu",
            reactants = listOf(BalanceTerm("SO₂", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products = listOf(BalanceTerm("H₂SO₃", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("SO₂ + H₂O → H₂SO₃. Kwas siarkowy(IV) — przyczyna kwaśnych deszczy.", boldPart = "H₂SO₃")
        ),
        EquationBalance(
            id = 0,
            instruction = "Uzupełnij reakcję otrzymywania kwasu",
            reactants = listOf(BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("H₂CO₃", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("CO₂ + H₂O → H₂CO₃. Tak powstaje kwas węglowy w napojach gazowanych.", boldPart = "H₂CO₃")
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj równanie reakcji",
            reactants = listOf(BalanceTerm("N₂O₅", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("HNO₃", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("N₂O₅ + H₂O → 2HNO₃. Dwa atomy N → 2 cząsteczki HNO₃.", boldPart = "2HNO₃",
                steps = listOf("N₂O₅ ma 2N → potrzeba 2×HNO₃", "2×HNO₃ ma 2H → 1×H₂O"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj równanie reakcji",
            reactants = listOf(BalanceTerm("N₂O₃", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products = listOf(BalanceTerm("HNO₂", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("N₂O₃ + H₂O → 2HNO₂. Kwas azotowy(III).", boldPart = "2HNO₂",
                steps = listOf("N₂O₃ ma 2N → potrzeba 2×HNO₂", "2×HNO₂ ma 2H → 1×H₂O"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj równanie reakcji",
            reactants = listOf(BalanceTerm("P₂O₅", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 3)),
            products = listOf(BalanceTerm("H₃PO₄", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("P₂O₅ + 3H₂O → 2H₃PO₄.", boldPart = "2H₃PO₄",
                steps = listOf("2P → 2×H₃PO₄", "2×H₃PO₄ ma 6H → 3×H₂O", "Sprawdź O: 5+3=8 = 2×4 ✓"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj równanie reakcji",
            reactants = listOf(BalanceTerm("Cl₂O₇", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("HClO₄", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Cl₂O₇ + H₂O → 2HClO₄. Dwa atomy Cl → 2 cząsteczki kwasu.", boldPart = "2HClO₄",
                steps = listOf("Cl₂O₇ ma 2Cl → 2×HClO₄", "2×HClO₄ ma 2H → 1×H₂O"))
        ),
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj równanie reakcji",
            reactants = listOf(BalanceTerm("Cl₂O", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products = listOf(BalanceTerm("HClO", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Cl₂O + H₂O → 2HClO. Kwas chlorowy(I).", boldPart = "2HClO",
                steps = listOf("Cl₂O ma 2Cl → 2×HClO", "2×HClO ma 2H → 1×H₂O"))
        ),
        // ── Synthesis of water (context reaction) ────────────────────────────
        EquationBalance(
            id = 0,
            instruction = "Zbilansuj reakcję syntezy wody",
            reactants = listOf(BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            products = listOf(BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2H₂ + O₂ → 2H₂O. Klasyczna reakcja syntezy wody.", boldPart = "2H₂O",
                steps = listOf("2×H₂O = 4H i 2O", "4H → 2×H₂", "2O → 1×O₂"))
        ),
    )

    private val metalAcidReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Zn", fixedCoefficient = 1), BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("ZnCl₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = 1)),
            hint = Hint("Zn + 2HCl → ZnCl₂ + H₂. Metal + kwas → sól + wodór.", boldPart = "2HCl",
                steps = listOf("Zn²⁺ + 2Cl⁻ → ZnCl₂", "2H z HCl → H₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Mg", fixedCoefficient = 1), BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("MgCl₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = 1)),
            hint = Hint("Mg + 2HCl → MgCl₂ + H₂.", boldPart = "2HCl",
                steps = listOf("Mg²⁺ + 2Cl⁻ → MgCl₂", "2H → H₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Fe", fixedCoefficient = 1), BalanceTerm("H₂SO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("FeSO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("Fe + H₂SO₄ → FeSO₄ + H₂. Wszystkie współczynniki = 1.", boldPart = "FeSO₄",
                steps = listOf("Fe²⁺ + SO₄²⁻ → FeSO₄", "2H z H₂SO₄ → H₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Ca", fixedCoefficient = 1), BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("CaCl₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = 1)),
            hint = Hint("Ca + 2HCl → CaCl₂ + H₂.", boldPart = "2HCl",
                steps = listOf("Ca²⁺ + 2Cl⁻ → CaCl₂", "2H → H₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Zn", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂SO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("ZnSO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = 1)),
            hint = Hint("Zn + H₂SO₄ → ZnSO₄ + H₂. Wszystkie współczynniki = 1.", boldPart = "ZnSO₄")
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję metalu z kwasem",
            reactants = listOf(BalanceTerm("Mg", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂SO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("MgSO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂", fixedCoefficient = 1)),
            hint = Hint("Mg + H₂SO₄ → MgSO₄ + H₂. Wszystkie współczynniki = 1.", boldPart = "MgSO₄")
        ),
    )

    private val neutralizationReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            reactants = listOf(BalanceTerm("NaOH", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("HCl", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("NaCl", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("NaOH + HCl → NaCl + H₂O. Zasada + kwas → sól + woda.", boldPart = "NaCl + H₂O",
                steps = listOf("Na⁺ + Cl⁻ → NaCl", "OH⁻ + H⁺ → H₂O"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            reactants = listOf(BalanceTerm("KOH", fixedCoefficient = 1), BalanceTerm("HNO₃", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("KNO₃", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            hint = Hint("KOH + HNO₃ → KNO₃ + H₂O.", boldPart = "KNO₃",
                steps = listOf("K⁺ + NO₃⁻ → KNO₃", "OH⁻ + H⁺ → H₂O"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            reactants = listOf(BalanceTerm("Ca(OH)₂", fixedCoefficient = 1), BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("CaCl₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Ca(OH)₂ + 2HCl → CaCl₂ + 2H₂O.", boldPart = "2HCl",
                steps = listOf("Ca(OH)₂ ma 2 grupy OH⁻ → potrzeba 2 HCl", "2Cl⁻ + Ca²⁺ → CaCl₂", "2OH⁻ + 2H⁺ → 2H₂O"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            reactants = listOf(BalanceTerm("NaOH", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("H₂SO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("Na₂SO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2NaOH + H₂SO₄ → Na₂SO₄ + 2H₂O.", boldPart = "2NaOH",
                steps = listOf("H₂SO₄ ma 2H⁺ → potrzeba 2 NaOH", "2Na⁺ + SO₄²⁻ → Na₂SO₄", "2OH⁻ + 2H⁺ → 2H₂O"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            subInstruction = "Siarczan baru wytrąca się jako biały osad",
            reactants = listOf(BalanceTerm("Ba(OH)₂", fixedCoefficient = 1), BalanceTerm("H₂SO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("BaSO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Ba(OH)₂ + H₂SO₄ → BaSO₄↓ + 2H₂O. BaSO₄ to biały osad — reakcja służy do wykrywania siarczanów.", boldPart = "BaSO₄",
                steps = listOf("Ba²⁺ + SO₄²⁻ → BaSO₄↓", "2OH⁻ + 2H⁺ → 2H₂O"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję zobojętniania",
            reactants = listOf(BalanceTerm("NaOH", fixedCoefficient = null, correctCoefficient = 3), BalanceTerm("H₃PO₄", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("Na₃PO₄", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 3)),
            hint = Hint("3NaOH + H₃PO₄ → Na₃PO₄ + 3H₂O.", boldPart = "3NaOH",
                steps = listOf("H₃PO₄ ma 3H⁺ → potrzeba 3 NaOH", "3Na⁺ + PO₄³⁻ → Na₃PO₄", "3OH⁻ + 3H⁺ → 3H₂O"))
        ),
    )

    private val combustionReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję spalania",
            reactants = listOf(BalanceTerm("C", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("C + O₂ → CO₂. Spalanie węgla. Wszystkie współczynniki = 1.", boldPart = "CO₂")
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję spalania",
            reactants = listOf(BalanceTerm("S", fixedCoefficient = 1), BalanceTerm("O₂", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("SO₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("S + O₂ → SO₂. Spalanie siarki. Wszystkie współczynniki = 1.", boldPart = "SO₂")
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję spalania",
            reactants = listOf(BalanceTerm("Mg", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("MgO", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2Mg + O₂ → 2MgO. O₂ = 2 atomy O → 2 cząsteczki MgO.", boldPart = "2MgO",
                steps = listOf("1×O₂ = 2 atomy O", "2O → 2×MgO", "2×MgO wymaga 2×Mg"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję spalania",
            reactants = listOf(BalanceTerm("Ca", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("CaO", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2Ca + O₂ → 2CaO. Analogicznie jak spalanie magnezu.", boldPart = "2CaO",
                steps = listOf("O₂ = 2O → 2×CaO → 2×Ca"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj spalanie metanu",
            subInstruction = "Metan — główny składnik gazu ziemnego",
            reactants = listOf(BalanceTerm("CH₄", fixedCoefficient = 1), BalanceTerm("O₂", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("CO₂", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("CH₄ + 2O₂ → CO₂ + 2H₂O.", boldPart = "2O₂",
                steps = listOf("CH₄ ma 4H → 2×H₂O (2O)", "CO₂ potrzebuje 2O", "Razem 4O → 2×O₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj spalanie etanu",
            reactants = listOf(BalanceTerm("C₂H₆", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = null, correctCoefficient = 7)),
            products  = listOf(BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 4), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 6)),
            hint = Hint("2C₂H₆ + 7O₂ → 4CO₂ + 6H₂O.", boldPart = "7O₂",
                steps = listOf("2×C₂H₆ ma 4C → 4CO₂", "2×C₂H₆ ma 12H → 6H₂O", "4CO₂ + 6H₂O = 8O+6O = 14O → 7O₂"))
        ),
    )

    private val decompositionReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję rozkładu",
            subInstruction = "Rozkład wody utlenionej (katalizator: MnO₂)",
            reactants = listOf(BalanceTerm("H₂O₂", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            hint = Hint("2H₂O₂ → 2H₂O + O₂. Katalizator MnO₂ przyspiesza rozkład.", boldPart = "2H₂O₂",
                steps = listOf("2×H₂O₂ = 4H + 4O", "4H → 2×H₂O (2O)", "Pozostałe 2O → O₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję rozkładu",
            subInstruction = "Historyczna reakcja Lavoisiera — rozkład tlenku rtęci(II)",
            reactants = listOf(BalanceTerm("HgO", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("Hg", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            hint = Hint("2HgO → 2Hg + O₂.", boldPart = "2HgO",
                steps = listOf("2×HgO = 2Hg + 2O", "2O → O₂"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję prażenia wapienia",
            subInstruction = "Ważna reakcja przemysłowa — produkcja wapna palonego",
            reactants = listOf(BalanceTerm("CaCO₃", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("CaO", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("CO₂", fixedCoefficient = 1)),
            hint = Hint("CaCO₃ → CaO + CO₂. Wapno palone (CaO) stosowane w budownictwie.", boldPart = "CaO")
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję elektrolizy wody",
            reactants = listOf(BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)),
            products  = listOf(BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            hint = Hint("2H₂O → 2H₂ + O₂. Elektroliza — odwrotność syntezy wody.", boldPart = "2H₂",
                steps = listOf("2×H₂O = 4H + 2O", "4H → 2×H₂", "2O → O₂"))
        ),
    )

    private fun chemia_3_2(seed: Long): List<Question> {
        val rng = Random(seed)
        val all = acidReactions + metalAcidReactions + neutralizationReactions + combustionReactions + decompositionReactions
        return all.shuffled(rng).mapIndexed { i, q -> q.copy(id = 3200 + i) }
    }

    // ── chemia_4_1  Skala pH ──────────────────────────────────────────────────

    private val phRealWorld = listOf(
        Triple("Sok cytrynowy (pH ≈ 2)", 2, "kwasowy"),
        Triple("Ocet (pH ≈ 3)", 3, "kwasowy"),
        Triple("Kawa (pH ≈ 5)", 5, "kwasowy"),
        Triple("Mleko (pH ≈ 6)", 6, "kwasowy"),
        Triple("Woda destylowana (pH = 7)", 7, "obojętny"),
        Triple("Krew ludzka (pH ≈ 7)", 7, "obojętny"),
        Triple("Woda morska (pH ≈ 8)", 8, "zasadowy"),
        Triple("Soda oczyszczona w wodzie (pH ≈ 9)", 9, "zasadowy"),
        Triple("Mydło (pH ≈ 10)", 10, "zasadowy"),
        Triple("Mleko wapienne (pH ≈ 12)", 12, "zasadowy"),
    )

    private fun chemia_4_1(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<SelectFromList>()
        val phSteps = listOf("pH < 7 → odczyn kwasowy", "pH = 7 → odczyn obojętny", "pH > 7 → odczyn zasadowy")

        // 15 questions: classify each pH 0–14
        (0..14).forEach { ph ->
            val type = when {
                ph < 7  -> "kwasowy"
                ph == 7 -> "obojętny"
                else    -> "zasadowy"
            }
            val opts = listOf("kwasowy", "obojętny", "zasadowy")
            qs += SelectFromList(
                id = 0,
                prompt = "Roztwór o pH = $ph ma odczyn:",
                options = opts,
                correctIndices = setOf(opts.indexOf(type)),
                hint = Hint(
                    mainText = when {
                        ph < 7  -> "pH $ph jest mniejsze od 7 — odczyn kwasowy."
                        ph == 7 -> "pH = 7 — odczyn obojętny."
                        else    -> "pH $ph jest większe od 7 — odczyn zasadowy."
                    },
                    boldPart = type,
                    steps = phSteps
                )
            )
        }

        // 10 real-world examples
        phRealWorld.forEach { (desc, _, type) ->
            val opts = listOf("kwasowy", "obojętny", "zasadowy")
            qs += SelectFromList(
                id = 0,
                prompt = "$desc ma odczyn:",
                options = opts,
                correctIndices = setOf(opts.indexOf(type)),
                hint = Hint(mainText = "$desc → $type.", boldPart = type, steps = phSteps)
            )
        }

        // 6 "pick acidic pH" questions — each with unique set of options
        val acidPHs = (1..6).toList().shuffled(rng)
        val basePHs = (8..13).toList()
        acidPHs.forEach { ph ->
            val wrongs = (listOf(7) + basePHs.shuffled(rng).take(2)).map { it.toString() }
            val opts = (wrongs + ph.toString()).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Które z poniższych pH odpowiada odczynowi kwasowemu?",
                options = opts,
                correctIndices = setOf(opts.indexOf(ph.toString())),
                hint = Hint("Odczyn kwasowy → pH < 7.", boldPart = "pH < 7", steps = phSteps)
            )
        }

        // 6 "pick basic pH" questions
        basePHs.shuffled(rng).take(6).forEach { ph ->
            val wrongs = (listOf(7) + acidPHs.take(2)).map { it.toString() }
            val opts = (wrongs + ph.toString()).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Które z poniższych pH odpowiada odczynowi zasadowemu?",
                options = opts,
                correctIndices = setOf(opts.indexOf(ph.toString())),
                hint = Hint("Odczyn zasadowy → pH > 7.", boldPart = "pH > 7", steps = phSteps)
            )
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 4100 + i) }
    }

    // ── chemia_4_2  Dysocjacja elektrolityczna ────────────────────────────────

    private data class DissocEntry(
        val formula: String,
        val namePL: String,
        val ions: String,
        val type: String  // kwas / zasada / sól
    )

    private val dissocData = listOf(
        DissocEntry("HCl",       "kwas solny",               "H⁺ i Cl⁻",        "kwas"),
        DissocEntry("HBr",       "kwas bromowodorowy",       "H⁺ i Br⁻",        "kwas"),
        DissocEntry("HI",        "kwas jodowodorowy",        "H⁺ i I⁻",         "kwas"),
        DissocEntry("HNO₃",     "kwas azotowy(V)",          "H⁺ i NO₃⁻",       "kwas"),
        DissocEntry("H₂SO₄",    "kwas siarkowy(VI)",        "2H⁺ i SO₄²⁻",     "kwas"),
        DissocEntry("HClO₄",    "kwas chlorowy(VII)",       "H⁺ i ClO₄⁻",      "kwas"),
        DissocEntry("NaOH",      "wodorotlenek sodu",        "Na⁺ i OH⁻",        "zasada"),
        DissocEntry("KOH",       "wodorotlenek potasu",      "K⁺ i OH⁻",         "zasada"),
        DissocEntry("Ca(OH)₂",  "wodorotlenek wapnia",     "Ca²⁺ i 2OH⁻",      "zasada"),
        DissocEntry("Ba(OH)₂",  "wodorotlenek baru",       "Ba²⁺ i 2OH⁻",      "zasada"),
        DissocEntry("NaCl",      "chlorek sodu",             "Na⁺ i Cl⁻",        "sól"),
        DissocEntry("KCl",       "chlorek potasu",           "K⁺ i Cl⁻",         "sól"),
        DissocEntry("CaCl₂",    "chlorek wapnia",           "Ca²⁺ i 2Cl⁻",      "sól"),
        DissocEntry("K₂SO₄",    "siarczan(VI) potasu",     "2K⁺ i SO₄²⁻",      "sól"),
        DissocEntry("Na₂CO₃",   "węglan sodu",              "2Na⁺ i CO₃²⁻",     "sól"),
    )

    private fun chemia_4_2(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<SelectFromList>()
        val allIons     = dissocData.map { it.ions }
        val allFormulas = dissocData.map { it.formula }
        val typeOpts    = listOf("kwas", "zasada", "sól")
        val typeHints   = mapOf(
            "kwas"   to "Kwasy dysocjują oddając jon H⁺.",
            "zasada" to "Zasady dysocjują oddając jon OH⁻.",
            "sól"    to "Sole to produkty reakcji kwasu z zasadą — dysocjują na kationy metalu i aniony reszty kwasowej."
        )

        dissocData.forEach { entry ->
            val wrongIons = allIons.filter { it != entry.ions }.shuffled(rng).take(3)
            val opts1 = (wrongIons + entry.ions).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Podczas dysocjacji ${entry.formula} (${entry.namePL}) powstają jony:",
                options = opts1,
                correctIndices = setOf(opts1.indexOf(entry.ions)),
                hint = Hint("${entry.formula} → ${entry.ions}.", boldPart = entry.ions)
            )

            val wrongForms = allFormulas.filter { it != entry.formula }.shuffled(rng).take(3)
            val opts2 = (wrongForms + entry.formula).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Który związek chemiczny dysocjuje dając jony ${entry.ions}?",
                options = opts2,
                correctIndices = setOf(opts2.indexOf(entry.formula)),
                hint = Hint("${entry.ions} to jony z ${entry.formula}.", boldPart = entry.formula)
            )

            qs += SelectFromList(
                id = 0,
                prompt = "${entry.formula} (${entry.namePL}) jest:",
                options = typeOpts,
                correctIndices = setOf(typeOpts.indexOf(entry.type)),
                hint = Hint(typeHints[entry.type]!!, boldPart = entry.type)
            )
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 4200 + i) }
    }

    // ── chemia_5_1  Węglowodory ───────────────────────────────────────────────

    private data class Hydrocarbon(
        val formula: String,
        val namePL: String,
        val cCount: Int,
        val type: String  // alkan / alken / alkyn
    )

    private val hydrocarbons = listOf(
        Hydrocarbon("CH₄",    "metan",   1, "alkan"),
        Hydrocarbon("C₂H₆",  "etan",    2, "alkan"),
        Hydrocarbon("C₃H₈",  "propan",  3, "alkan"),
        Hydrocarbon("C₄H₁₀", "butan",   4, "alkan"),
        Hydrocarbon("C₅H₁₂", "pentan",  5, "alkan"),
        Hydrocarbon("C₆H₁₄", "heksan",  6, "alkan"),
        Hydrocarbon("C₂H₄",  "eten",    2, "alken"),
        Hydrocarbon("C₃H₆",  "propen",  3, "alken"),
        Hydrocarbon("C₄H₈",  "buten",   4, "alken"),
        Hydrocarbon("C₅H₁₀", "penten",  5, "alken"),
        Hydrocarbon("C₂H₂",  "etyn",    2, "alkyn"),
        Hydrocarbon("C₃H₄",  "propyn",  3, "alkyn"),
        Hydrocarbon("C₄H₆",  "butyn",   4, "alkyn"),
    )

    private val hcTypeHints = mapOf(
        "alkan" to "Alkany — CₙH₂ₙ₊₂. Tylko wiązania pojedyncze C−C.",
        "alken" to "Alkeny — CₙH₂ₙ. Zawierają jedno wiązanie podwójne C=C.",
        "alkyn" to "Alkiny — CₙH₂ₙ₋₂. Zawierają jedno wiązanie potrójne C≡C."
    )

    private fun chemia_5_1(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<SelectFromList>()
        val allNames    = hydrocarbons.map { it.namePL }
        val allFormulas = hydrocarbons.map { it.formula }
        val typeOpts    = listOf("alkan", "alken", "alkyn")

        hydrocarbons.forEach { hc ->
            val wNames = allNames.filter { it != hc.namePL }.shuffled(rng).take(3)
            val opts1 = (wNames + hc.namePL).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Jak nazywa się związek o wzorze ${hc.formula}?",
                options = opts1,
                correctIndices = setOf(opts1.indexOf(hc.namePL)),
                hint = Hint("${hc.formula} to ${hc.namePL}.", boldPart = hc.namePL)
            )

            val wForms = allFormulas.filter { it != hc.formula }.shuffled(rng).take(3)
            val opts2 = (wForms + hc.formula).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Jaki jest wzór sumaryczny ${hc.namePL}?",
                options = opts2,
                correctIndices = setOf(opts2.indexOf(hc.formula)),
                hint = Hint("Wzór ${hc.namePL} to ${hc.formula}.", boldPart = hc.formula)
            )

            qs += SelectFromList(
                id = 0,
                prompt = "${hc.namePL} (${hc.formula}) należy do szeregu:",
                options = typeOpts,
                correctIndices = setOf(typeOpts.indexOf(hc.type)),
                hint = Hint(hcTypeHints[hc.type]!!, boldPart = hc.type)
            )

            val wC = (1..8).filter { it != hc.cCount }.shuffled(rng).take(3).map { it.toString() }
            val opts4 = (wC + hc.cCount.toString()).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Ile atomów węgla zawiera ${hc.namePL} (${hc.formula})?",
                options = opts4,
                correctIndices = setOf(opts4.indexOf(hc.cCount.toString())),
                hint = Hint("${hc.formula}: ${hc.cCount} atomy C.", boldPart = "${hc.cCount}")
            )
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 5100 + i) }
    }

    // ── chemia_5_2  Pochodne węglowodorów ────────────────────────────────────

    private data class OrgCompound(
        val formula: String,
        val namePL: String,
        val group: String,     // -OH / -COOH / -NH₂ / ester
        val groupName: String  // alkohol / kwas karboksylowy / amina / ester
    )

    private val orgCompounds = listOf(
        OrgCompound("CH₃OH",         "metanol",           "-OH",   "alkohol"),
        OrgCompound("C₂H₅OH",        "etanol",            "-OH",   "alkohol"),
        OrgCompound("C₃H₇OH",        "propanol",          "-OH",   "alkohol"),
        OrgCompound("C₄H₉OH",        "butanol",           "-OH",   "alkohol"),
        OrgCompound("HCOOH",          "kwas metanowy",     "-COOH", "kwas karboksylowy"),
        OrgCompound("CH₃COOH",       "kwas etanowy",      "-COOH", "kwas karboksylowy"),
        OrgCompound("C₂H₅COOH",     "kwas propanowy",    "-COOH", "kwas karboksylowy"),
        OrgCompound("C₃H₇COOH",     "kwas butanowy",     "-COOH", "kwas karboksylowy"),
        OrgCompound("CH₃NH₂",        "metyloamina",       "-NH₂",  "amina"),
        OrgCompound("C₂H₅NH₂",      "etyloamina",        "-NH₂",  "amina"),
        OrgCompound("HCOOCH₃",       "mrówczan metylu",   "ester", "ester"),
        OrgCompound("CH₃COOC₂H₅",  "octan etylu",       "ester", "ester"),
    )

    private val orgGroupHints = mapOf(
        "alkohol"           to "Alkohole zawierają grupę hydroksylową -OH.",
        "kwas karboksylowy" to "Kwasy karboksylowe zawierają grupę karboksylową -COOH.",
        "amina"             to "Aminy zawierają grupę aminową -NH₂.",
        "ester"             to "Estry powstają z reakcji kwasu karboksylowego z alkoholem."
    )

    private fun chemia_5_2(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<SelectFromList>()
        val allNames   = orgCompounds.map { it.namePL }
        val typeOpts   = listOf("alkohol", "kwas karboksylowy", "amina", "ester")
        val groupOpts  = listOf("-OH", "-COOH", "-NH₂", "ester")

        orgCompounds.forEach { oc ->
            val wNames = allNames.filter { it != oc.namePL }.shuffled(rng).take(3)
            val opts1 = (wNames + oc.namePL).shuffled(rng)
            qs += SelectFromList(
                id = 0,
                prompt = "Jak nazywa się związek o wzorze ${oc.formula}?",
                options = opts1,
                correctIndices = setOf(opts1.indexOf(oc.namePL)),
                hint = Hint("${oc.formula} to ${oc.namePL}.", boldPart = oc.namePL)
            )

            qs += SelectFromList(
                id = 0,
                prompt = "${oc.namePL} (${oc.formula}) należy do grupy:",
                options = typeOpts,
                correctIndices = setOf(typeOpts.indexOf(oc.groupName)),
                hint = Hint(
                    orgGroupHints[oc.groupName]!!,
                    boldPart = oc.groupName,
                    items = listOf(
                        "Alkohole — gr. -OH",
                        "Kwasy karboksylowe — gr. -COOH",
                        "Aminy — gr. -NH₂",
                        "Estry — z kwasu + alkoholu"
                    )
                )
            )

            qs += SelectFromList(
                id = 0,
                prompt = "Jaką grupę funkcyjną zawiera ${oc.namePL} (${oc.formula})?",
                options = groupOpts,
                correctIndices = setOf(groupOpts.indexOf(oc.group)),
                hint = Hint("Grupa ${oc.group} → ${oc.groupName}.", boldPart = oc.group)
            )
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 5200 + i) }
    }

    // ── chemia_3_3  Wodorotlenki ─────────────────────────────────────────────

    private data class BaseEntry(val formula: String, val namePL: String, val soluble: Boolean)

    private val hydroxides = listOf(
        BaseEntry("NaOH",      "wodorotlenek sodu",         true),
        BaseEntry("KOH",       "wodorotlenek potasu",       true),
        BaseEntry("LiOH",      "wodorotlenek litu",         true),
        BaseEntry("Ca(OH)₂",  "wodorotlenek wapnia",       true),
        BaseEntry("Ba(OH)₂",  "wodorotlenek baru",         true),
        BaseEntry("Mg(OH)₂",  "wodorotlenek magnezu",      false),
        BaseEntry("Fe(OH)₂",  "wodorotlenek żelaza(II)",   false),
        BaseEntry("Fe(OH)₃",  "wodorotlenek żelaza(III)",  false),
        BaseEntry("Cu(OH)₂",  "wodorotlenek miedzi(II)",   false),
        BaseEntry("Al(OH)₃",  "wodorotlenek glinu",        false),
        BaseEntry("Zn(OH)₂",  "wodorotlenek cynku",        false),
        BaseEntry("Mn(OH)₂",  "wodorotlenek manganu(II)",  false),
        BaseEntry("Ni(OH)₂",  "wodorotlenek niklu(II)",    false),
    )

    private val hydroxideFormingReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję otrzymywania zasady",
            subInstruction = "Tlenek zasadowy + woda → zasada",
            reactants = listOf(BalanceTerm("Na₂O", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("NaOH", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Na₂O + H₂O → 2NaOH. 2 atomy Na → 2 cząsteczki NaOH.", boldPart = "2NaOH",
                steps = listOf("Na₂O ma 2Na → 2×NaOH", "H₂O daje grupę OH⁻ dla każdego Na"))
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję gaszenia wapna",
            subInstruction = "Wapno palone + woda → wapno gaszone",
            reactants = listOf(BalanceTerm("CaO", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("Ca(OH)₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("CaO + H₂O → Ca(OH)₂. Wszystkie współczynniki = 1.", boldPart = "Ca(OH)₂")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję otrzymywania zasady",
            reactants = listOf(BalanceTerm("K₂O", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("KOH", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("K₂O + H₂O → 2KOH. 2 atomy K → 2 cząsteczki KOH.", boldPart = "2KOH")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję otrzymywania zasady",
            reactants = listOf(BalanceTerm("BaO", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("Ba(OH)₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("BaO + H₂O → Ba(OH)₂. Wszystkie współczynniki = 1.", boldPart = "Ba(OH)₂")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję otrzymywania zasady",
            reactants = listOf(BalanceTerm("Li₂O", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("LiOH", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("Li₂O + H₂O → 2LiOH. 2 atomy Li → 2 cząsteczki LiOH.", boldPart = "2LiOH")
        ),
    )

    private fun chemia_3_3(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs = mutableListOf<Question>()
        val allFormulas = hydroxides.map { it.formula }
        val allNames    = hydroxides.map { it.namePL }
        val solubHint   = "Rozpuszczalne zasady (mocne): NaOH, KOH, LiOH, Ca(OH)₂, Ba(OH)₂. Pozostałe są trudno rozpuszczalne."
        val solubItems  = listOf("NaOH, KOH, LiOH — gr. 1", "Ca(OH)₂, Ba(OH)₂ — gr. 2", "Inne wodorotlenki — trudno rozpuszczalne")

        hydroxides.forEach { base ->
            val wNames = allNames.filter { it != base.namePL }.shuffled(rng).take(3)
            val opts1  = (wNames + base.namePL).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Wodorotlenek o wzorze ${base.formula} to:",
                options = opts1, correctIndices = setOf(opts1.indexOf(base.namePL)),
                hint = Hint("${base.formula} to ${base.namePL}.", boldPart = base.namePL))

            val wForms = allFormulas.filter { it != base.formula }.shuffled(rng).take(3)
            val opts2  = (wForms + base.formula).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Wzór ${base.namePL} to:",
                options = opts2, correctIndices = setOf(opts2.indexOf(base.formula)),
                hint = Hint("Wzór ${base.namePL} to ${base.formula}.", boldPart = base.formula))

            val sType = if (base.soluble) "rozpuszczalny" else "trudno rozpuszczalny"
            val sOpts = listOf("rozpuszczalny", "trudno rozpuszczalny")
            qs += SelectFromList(id = 0,
                prompt = "${base.namePL} (${base.formula}) jest:",
                options = sOpts, correctIndices = setOf(sOpts.indexOf(sType)),
                hint = Hint(solubHint, boldPart = sType, sectionTitle = "Rozpuszczalność zasad", items = solubItems))
        }

        qs.addAll(hydroxideFormingReactions.shuffled(rng))
        return qs.shuffled(rng).mapIndexed { i, q ->
            when (q) {
                is SelectFromList -> q.copy(id = 3300 + i)
                is EquationBalance -> q.copy(id = 3300 + i)
                else -> q
            }
        }
    }

    // ── chemia_3_4  Sole ─────────────────────────────────────────────────────

    private data class SaltEntry(val formula: String, val namePL: String, val acidName: String)

    private val salts = listOf(
        SaltEntry("NaCl",          "chlorek sodu",              "kwas chlorowodorowy"),
        SaltEntry("KCl",           "chlorek potasu",             "kwas chlorowodorowy"),
        SaltEntry("CaCl₂",         "chlorek wapnia",             "kwas chlorowodorowy"),
        SaltEntry("MgCl₂",         "chlorek magnezu",            "kwas chlorowodorowy"),
        SaltEntry("FeCl₂",         "chlorek żelaza(II)",         "kwas chlorowodorowy"),
        SaltEntry("FeCl₃",         "chlorek żelaza(III)",        "kwas chlorowodorowy"),
        SaltEntry("AlCl₃",         "chlorek glinu",              "kwas chlorowodorowy"),
        SaltEntry("ZnCl₂",         "chlorek cynku",              "kwas chlorowodorowy"),
        SaltEntry("Na₂SO₄",        "siarczan(VI) sodu",          "kwas siarkowy(VI)"),
        SaltEntry("CaSO₄",         "siarczan(VI) wapnia",        "kwas siarkowy(VI)"),
        SaltEntry("MgSO₄",         "siarczan(VI) magnezu",       "kwas siarkowy(VI)"),
        SaltEntry("ZnSO₄",         "siarczan(VI) cynku",         "kwas siarkowy(VI)"),
        SaltEntry("FeSO₄",         "siarczan(VI) żelaza(II)",    "kwas siarkowy(VI)"),
        SaltEntry("BaSO₄",         "siarczan(VI) baru",          "kwas siarkowy(VI)"),
        SaltEntry("K₂SO₄",         "siarczan(VI) potasu",        "kwas siarkowy(VI)"),
        SaltEntry("Na₂CO₃",        "węglan sodu",                "kwas węglowy"),
        SaltEntry("CaCO₃",         "węglan wapnia",              "kwas węglowy"),
        SaltEntry("MgCO₃",         "węglan magnezu",             "kwas węglowy"),
        SaltEntry("NaNO₃",         "azotan(V) sodu",             "kwas azotowy(V)"),
        SaltEntry("Ca(NO₃)₂",      "azotan(V) wapnia",           "kwas azotowy(V)"),
        SaltEntry("KNO₃",          "azotan(V) potasu",           "kwas azotowy(V)"),
        SaltEntry("Na₃PO₄",        "fosforan(V) sodu",           "kwas fosforowy(V)"),
        SaltEntry("Ca₃(PO₄)₂",    "fosforan(V) wapnia",         "kwas fosforowy(V)"),
    )

    private fun chemia_3_4(seed: Long): List<Question> {
        val rng  = Random(seed)
        val qs   = mutableListOf<SelectFromList>()
        val allFormulas = salts.map { it.formula }
        val allNames    = salts.map { it.namePL }
        val allAcids    = salts.map { it.acidName }.distinct()
        val saltItems   = listOf(
            "Chlorki — od HCl (kwas chlorowodorowy)",
            "Siarczany(VI) — od H₂SO₄",
            "Azotany(V) — od HNO₃",
            "Węglany — od H₂CO₃",
            "Fosforany(V) — od H₃PO₄"
        )

        salts.forEach { salt ->
            val wNames = allNames.filter { it != salt.namePL }.shuffled(rng).take(3)
            val opts1  = (wNames + salt.namePL).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Sól o wzorze ${salt.formula} to:",
                options = opts1, correctIndices = setOf(opts1.indexOf(salt.namePL)),
                hint = Hint("${salt.formula} to ${salt.namePL}.", boldPart = salt.namePL,
                    sectionTitle = "Nazewnictwo soli", items = saltItems))

            val wForms = allFormulas.filter { it != salt.formula }.shuffled(rng).take(3)
            val opts2  = (wForms + salt.formula).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Wzór ${salt.namePL} to:",
                options = opts2, correctIndices = setOf(opts2.indexOf(salt.formula)),
                hint = Hint("Wzór ${salt.namePL} to ${salt.formula}.", boldPart = salt.formula))

            val wAcids = allAcids.filter { it != salt.acidName }.shuffled(rng).take(3)
            val opts3  = (wAcids + salt.acidName).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "${salt.namePL} (${salt.formula}) pochodzi od:",
                options = opts3, correctIndices = setOf(opts3.indexOf(salt.acidName)),
                hint = Hint("${salt.formula} pochodzi od ${salt.acidName}.", boldPart = salt.acidName))
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 3400 + i) }
    }

    // ── chemia_6_1  Tlenki ───────────────────────────────────────────────────

    private data class OxideEntry(val formula: String, val namePL: String, val type: String)

    private val oxides = listOf(
        OxideEntry("Na₂O",   "tlenek sodu",           "zasadowy"),
        OxideEntry("K₂O",    "tlenek potasu",          "zasadowy"),
        OxideEntry("CaO",    "tlenek wapnia",          "zasadowy"),
        OxideEntry("MgO",    "tlenek magnezu",         "zasadowy"),
        OxideEntry("BaO",    "tlenek baru",            "zasadowy"),
        OxideEntry("FeO",    "tlenek żelaza(II)",      "zasadowy"),
        OxideEntry("Fe₂O₃",  "tlenek żelaza(III)",     "zasadowy"),
        OxideEntry("CuO",    "tlenek miedzi(II)",      "zasadowy"),
        OxideEntry("Li₂O",   "tlenek litu",            "zasadowy"),
        OxideEntry("CO₂",    "dwutlenek węgla",        "kwasowy"),
        OxideEntry("SO₂",    "dwutlenek siarki",       "kwasowy"),
        OxideEntry("SO₃",    "trójtlenek siarki",      "kwasowy"),
        OxideEntry("N₂O₅",   "tlenek azotu(V)",        "kwasowy"),
        OxideEntry("P₂O₅",   "tlenek fosforu(V)",      "kwasowy"),
        OxideEntry("SiO₂",   "dwutlenek krzemu",       "kwasowy"),
        OxideEntry("CO",     "tlenek węgla(II)",       "obojętny"),
        OxideEntry("NO",     "tlenek azotu(II)",       "obojętny"),
        OxideEntry("ZnO",    "tlenek cynku",           "amfoteryczny"),
        OxideEntry("Al₂O₃",  "tlenek glinu",           "amfoteryczny"),
    )

    private val oxideFormingReactions: List<EquationBalance> = listOf(
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję tworzenia tlenku",
            reactants = listOf(BalanceTerm("Mg", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("MgO", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2Mg + O₂ → 2MgO. O₂ = 2 atomy O → 2×MgO.", boldPart = "2MgO",
                steps = listOf("O₂ dostarcza 2O", "2O → 2×MgO", "2×MgO wymaga 2×Mg"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję tworzenia tlenku",
            reactants = listOf(BalanceTerm("Ca", fixedCoefficient = null, correctCoefficient = 2), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("CaO", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("2Ca + O₂ → 2CaO.", boldPart = "2CaO",
                steps = listOf("O₂ = 2O → 2×CaO → 2×Ca"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję tworzenia tlenku",
            reactants = listOf(BalanceTerm("Na", fixedCoefficient = null, correctCoefficient = 4), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("Na₂O", fixedCoefficient = null, correctCoefficient = 2)),
            hint = Hint("4Na + O₂ → 2Na₂O.", boldPart = "2Na₂O",
                steps = listOf("O₂ = 2O → 2×Na₂O", "2×Na₂O ma 4Na → 4×Na"))
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję tworzenia tlenku kwasowego",
            reactants = listOf(BalanceTerm("C", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("O₂", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("C + O₂ → CO₂. Tlenek kwasowy — reaguje z zasadami i z wodą.", boldPart = "CO₂")
        ),
        EquationBalance(
            id = 0, instruction = "Zbilansuj reakcję tworzenia tlenku kwasowego",
            reactants = listOf(BalanceTerm("S", fixedCoefficient = 1), BalanceTerm("O₂", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("SO₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("S + O₂ → SO₂. Dwutlenek siarki — przyczyna kwaśnych deszczy.", boldPart = "SO₂")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję tlenku kwasowego z wodą",
            reactants = listOf(BalanceTerm("SO₃", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("H₂SO₄", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("SO₃ + H₂O → H₂SO₄. Tlenek kwasowy + woda → kwas.", boldPart = "H₂SO₄")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję tlenku zasadowego z wodą",
            subInstruction = "Wapno palone + woda → wapno gaszone",
            reactants = listOf(BalanceTerm("CaO", fixedCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)),
            products  = listOf(BalanceTerm("Ca(OH)₂", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("CaO + H₂O → Ca(OH)₂. Tlenek zasadowy + woda → zasada.", boldPart = "Ca(OH)₂")
        ),
        EquationBalance(
            id = 0, instruction = "Uzupełnij reakcję tlenku kwasowego z wodą",
            reactants = listOf(BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1), BalanceTerm("H₂O", fixedCoefficient = 1)),
            products  = listOf(BalanceTerm("H₂CO₃", fixedCoefficient = null, correctCoefficient = 1)),
            hint = Hint("CO₂ + H₂O → H₂CO₃. Tak powstaje kwas węglowy w napojach gazowanych.", boldPart = "H₂CO₃")
        ),
    )

    private fun chemia_6_1(seed: Long): List<Question> {
        val rng = Random(seed)
        val qs  = mutableListOf<Question>()
        val allFormulas = oxides.map { it.formula }
        val allNames    = oxides.map { it.namePL }
        val typeOpts    = listOf("zasadowy", "kwasowy", "amfoteryczny", "obojętny")
        val typeHint    = "Tlenki zasadowe = tlenki metali; tlenki kwasowe = tlenki niemetali. Amfoteryczne reagują zarówno z kwasami jak i zasadami."
        val typeItems   = listOf(
            "zasadowy: tlenek metalu → reaguje z kwasami (→ sól + H₂O)",
            "kwasowy: tlenek niemetalu → reaguje z zasadami (→ sól + H₂O) i z H₂O (→ kwas)",
            "amfoteryczny: reaguje z kwasami i z zasadami (ZnO, Al₂O₃)",
            "obojętny: nie reaguje z kwasami ani zasadami (CO, NO)"
        )

        oxides.forEach { oxide ->
            val wNames = allNames.filter { it != oxide.namePL }.shuffled(rng).take(3)
            val opts1  = (wNames + oxide.namePL).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Tlenek o wzorze ${oxide.formula} to:",
                options = opts1, correctIndices = setOf(opts1.indexOf(oxide.namePL)),
                hint = Hint("${oxide.formula} to ${oxide.namePL}.", boldPart = oxide.namePL))

            val wForms = allFormulas.filter { it != oxide.formula }.shuffled(rng).take(3)
            val opts2  = (wForms + oxide.formula).shuffled(rng)
            qs += SelectFromList(id = 0,
                prompt = "Wzór ${oxide.namePL} to:",
                options = opts2, correctIndices = setOf(opts2.indexOf(oxide.formula)),
                hint = Hint("Wzór ${oxide.namePL} to ${oxide.formula}.", boldPart = oxide.formula))

            qs += SelectFromList(id = 0,
                prompt = "${oxide.namePL} (${oxide.formula}) jest tlenkiem:",
                options = typeOpts, correctIndices = setOf(typeOpts.indexOf(oxide.type)),
                hint = Hint(typeHint, boldPart = oxide.type, sectionTitle = "Typy tlenków", items = typeItems))
        }

        qs.addAll(oxideFormingReactions.shuffled(rng))
        return qs.shuffled(rng).mapIndexed { i, q ->
            when (q) {
                is SelectFromList  -> q.copy(id = 6100 + i)
                is EquationBalance -> q.copy(id = 6100 + i)
                else -> q
            }
        }
    }

    // ── ElementCardQuiz builders ──────────────────────────────────────────────

    private fun electronQ(el: Element, rng: Random): ElementCardQuiz {
        val z = el.atomicNumber
        val opts = buildOptions(z.toString(), distractors(z, 1, 120, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = "Ile elektronów posiada atom ${el.namePL}?",
            atomicNumber = z,
            options = opts,
            correctIndex = opts.indexOf(z.toString()),
            hint = Hint(
                "Elektrony = liczba atomowa Z = $z.",
                steps = listOf("Z = $z", "Elektronów = $z")
            )
        )
    }

    private fun protonQ(el: Element, rng: Random): ElementCardQuiz {
        val z = el.atomicNumber
        val opts = buildOptions(z.toString(), distractors(z, 1, 120, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = "Ile protonów posiada atom ${el.namePL}?",
            atomicNumber = z,
            options = opts,
            correctIndex = opts.indexOf(z.toString()),
            hint = Hint(
                "Protonów = liczba atomowa Z = $z.",
                steps = listOf("Z = $z", "Protonów = $z")
            )
        )
    }

    private fun massQ(el: Element, rng: Random): ElementCardQuiz {
        val m = el.atomicMass.roundToInt()
        val opts = buildOptions(m.toString(), distractors(m, 1, 300, rng), rng)
        return ElementCardQuiz(
            id = 0,
            prompt = "Przybliżona masa atomowa ${el.symbol} wynosi:",
            atomicNumber = el.atomicNumber,
            options = opts,
            correctIndex = opts.indexOf(m.toString()),
            hint = Hint(
                "Masa atomowa ${el.symbol} ≈ $m u.",
                steps = listOf("Odczytaj masę atomową z karty pierwiastka")
            )
        )
    }

    private fun periodQ(el: Element, rng: Random): ElementCardQuiz {
        val p = el.tableRow.coerceIn(1, 7)
        val dists = (1..7).filter { it != p }.shuffled(rng).take(3).map { it.toString() }
        val opts = buildOptions(p.toString(), dists, rng)
        return ElementCardQuiz(
            id = 0,
            prompt = "W którym okresie leży pierwiastek ${el.symbol}?",
            atomicNumber = el.atomicNumber,
            options = opts,
            correctIndex = opts.indexOf(p.toString()),
            hint = Hint(
                "${el.namePL} leży w $p. okresie układu.",
                steps = listOf("Policz wiersz od góry tabeli — to numer okresu")
            )
        )
    }

    // ── distractor & option helpers ───────────────────────────────────────────

    private fun distractors(correct: Int, min: Int, max: Int, rng: Random): List<String> {
        val step = (correct / 5).coerceAtLeast(1)
        return (1..step * 10)
            .flatMap { d -> listOf(correct - d, correct + d) }
            .filter { it != correct && it in min..max }
            .distinct()
            .shuffled(rng)
            .take(3)
            .map { it.toString() }
    }

    private fun buildOptions(correct: String, wrongs: List<String>, rng: Random): List<String> =
        (wrongs.take(3) + correct).shuffled(rng)

    // ── periodic table sets ───────────────────────────────────────────────────

    private fun periodicTableSets(): List<Triple<String, List<Int>, Hint>> = listOf(
        Triple(
            "Umieść brakujące gazy szlachetne w układzie",
            listOf(2, 10, 18, 36, 54),
            Hint("Gazy szlachetne — gr. 18 (ostatnia kolumna).",
                items = listOf("He — gr.18, p.1", "Ne — gr.18, p.2", "Ar — gr.18, p.3", "Kr — gr.18, p.4", "Xe — gr.18, p.5"))
        ),
        Triple(
            "Uzupełnij halogeny w układzie",
            listOf(9, 17, 35, 53, 85),
            Hint("Halogeny — gr. 17.",
                items = listOf("F — gr.17, p.2", "Cl — gr.17, p.3", "Br — gr.17, p.4", "I — gr.17, p.5", "At — gr.17, p.6"))
        ),
        Triple(
            "Uzupełnij litowce w układzie",
            listOf(3, 11, 19, 37, 55),
            Hint("Litowce — gr. 1.",
                items = listOf("Li — gr.1, p.2", "Na — gr.1, p.3", "K — gr.1, p.4", "Rb — gr.1, p.5", "Cs — gr.1, p.6"))
        ),
        Triple(
            "Uzupełnij berylowce w układzie",
            listOf(4, 12, 20, 38, 56),
            Hint("Berylowce — gr. 2.",
                items = listOf("Be — gr.2, p.2", "Mg — gr.2, p.3", "Ca — gr.2, p.4", "Sr — gr.2, p.5", "Ba — gr.2, p.6"))
        ),
        Triple(
            "Umieść brakujące pierwiastki w układzie",
            listOf(6, 7, 8, 11, 17),
            Hint("Patrz na numer grupy i okresu.",
                items = listOf("C — gr.14, p.2", "N — gr.15, p.2", "O — gr.16, p.2", "Na — gr.1, p.3", "Cl — gr.17, p.3"))
        ),
        Triple(
            "Umieść brakujące metale szlachetne w układzie",
            listOf(26, 28, 29, 47, 79),
            Hint("Metale przejściowe — gr. 3–12.",
                items = listOf("Fe — gr.8", "Ni — gr.10", "Cu — gr.11", "Ag — gr.11, p.5", "Au — gr.11, p.6"))
        ),
        Triple(
            "Uzupełnij halogeny i litowce w układzie",
            listOf(3, 9, 19, 35, 53),
            Hint("Litowce → gr. 1, halogeny → gr. 17.",
                items = listOf("Li — gr.1, p.2", "F — gr.17, p.2", "K — gr.1, p.4", "Br — gr.17, p.4", "I — gr.17, p.5"))
        ),
        Triple(
            "Znajdź miejsca pierwiastków 3. okresu",
            listOf(12, 13, 14, 15, 16),
            Hint("Wszystkie w 3. wierszu układu.",
                items = listOf("Mg — gr.2", "Al — gr.13", "Si — gr.14", "P — gr.15", "S — gr.16"))
        ),
        Triple(
            "Znajdź miejsca pierwiastków 2. okresu",
            listOf(3, 5, 6, 7, 8),
            Hint("Wszystkie w 2. wierszu układu.",
                items = listOf("Li — gr.1", "B — gr.13", "C — gr.14", "N — gr.15", "O — gr.16"))
        ),
        Triple(
            "Znajdź miejsca metali przejściowych okresu 4",
            listOf(22, 24, 25, 26, 28),
            Hint("Ti, Cr, Mn, Fe, Ni — środkowy blok, okres 4.",
                items = listOf("Ti — gr.4", "Cr — gr.6", "Mn — gr.7", "Fe — gr.8", "Ni — gr.10"))
        ),
        Triple(
            "Uzupełnij niemetale reaktywne w układzie",
            listOf(6, 7, 8, 15, 16),
            Hint("C, N, O, P, S — niemetale reaktywne.",
                items = listOf("C — gr.14, p.2", "N — gr.15, p.2", "O — gr.16, p.2", "P — gr.15, p.3", "S — gr.16, p.3"))
        ),
        Triple(
            "Uzupełnij pierwiastki bloku s w układzie",
            listOf(1, 2, 11, 12, 19),
            Hint("Blok s — gr. 1 i 2 (oraz H i He).",
                items = listOf("H — gr.1, p.1", "He — gr.18, p.1", "Na — gr.1, p.3", "Mg — gr.2, p.3", "K — gr.1, p.4"))
        ),
        Triple(
            "Znajdź miejsca niemetali 3. okresu",
            listOf(14, 15, 16, 17, 18),
            Hint("Prawa strona 3. wiersza — niemetale.",
                items = listOf("Si — gr.14", "P — gr.15", "S — gr.16", "Cl — gr.17", "Ar — gr.18"))
        ),
        Triple(
            "Znajdź miejsca pierwiastków 4. okresu — blok p",
            listOf(31, 32, 33, 34, 35),
            Hint("Prawa strona 4. wiersza — po metalach przejściowych.",
                items = listOf("Ga — gr.13", "Ge — gr.14", "As — gr.15", "Se — gr.16", "Br — gr.17"))
        ),
        Triple(
            "Uzupełnij koniec i początek okresu 4 i 5",
            listOf(19, 36, 37, 54, 55),
            Hint("Gr. 1 (litowce) i gr. 18 (gazy szlachetne).",
                items = listOf("K — gr.1, p.4", "Kr — gr.18, p.4", "Rb — gr.1, p.5", "Xe — gr.18, p.5", "Cs — gr.1, p.6"))
        ),
    )

    // ── molecule questions ────────────────────────────────────────────────────

    private val molecules: List<Pair<String, String>> = listOf(
        "H₂O"      to "woda",
        "CO₂"      to "dwutlenek węgla",
        "NaCl"     to "chlorek sodu",
        "NH₃"      to "amoniak",
        "CH₄"      to "metan",
        "NaOH"     to "wodorotlenek sodu",
        "Ca(OH)₂"  to "wodorotlenek wapnia",
        "CaO"      to "tlenek wapnia",
        "Fe₂O₃"    to "tlenek żelaza(III)",
        "SO₂"      to "dwutlenek siarki",
        "SO₃"      to "trójtlenek siarki",
        "N₂"       to "azot cząsteczkowy",
        "O₂"       to "tlen cząsteczkowy",
        "H₂"       to "wodór cząsteczkowy",
        "CaCO₃"    to "węglan wapnia",
        "Al₂O₃"    to "tlenek glinu",
        "Na₂O"     to "tlenek sodu",
        "CO"       to "tlenek węgla(II)",
        "HF"       to "kwas fluorowodorowy",
        "HBr"      to "kwas bromowodorowy"
    )

    private fun moleculeQuestions(rng: Random): List<Question> {
        val allNames = molecules.map { it.second }
        val allFormulas = molecules.map { it.first }
        val qs = molecules.flatMap { (formula, name) ->
            val wrongNames = allNames.filter { it != name }.shuffled(rng).take(3)
            val opts1 = (wrongNames + name).shuffled(rng)
            val q1 = SelectFromList(
                id = 0,
                prompt = "Jak nazywa się związek o wzorze $formula?",
                options = opts1,
                correctIndices = setOf(opts1.indexOf(name)),
                hint = Hint("$formula to $name.", boldPart = name)
            )

            val wrongForms = allFormulas.filter { it != formula }.shuffled(rng).take(3)
            val opts2 = (wrongForms + formula).shuffled(rng)
            val q2 = SelectFromList(
                id = 0,
                prompt = "Jaki jest wzór chemiczny: $name?",
                options = opts2,
                correctIndices = setOf(opts2.indexOf(formula)),
                hint = Hint("Wzór $name to $formula.", boldPart = formula)
            )

            listOf(q1, q2)
        }
        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 1300 + i) }
    }

    // ── property questions ────────────────────────────────────────────────────

    private val metalCats = setOf(ALKALI_METAL, ALKALINE_EARTH, TRANSITION_METAL, POST_TRANSITION)
    private val nonMetalCats = setOf(REACTIVE_NONMETAL, HALOGEN)

    private fun propertyQuestions(rng: Random): List<Question> {
        val qs = mutableListOf<SelectFromList>()

        // One question per element: "Do jakiej grupy zaliczamy X?"
        ELEMENTS.filter { it.atomicNumber in 1..54 }.forEach { el ->
            val type = when (el.category) {
                NOBLE_GAS        -> "gaz szlachetny"
                in metalCats     -> "metal"
                in nonMetalCats  -> "niemetal"
                METALLOID        -> "metaloid"
                else -> return@forEach
            }
            val opts = listOf("metal", "niemetal", "metaloid", "gaz szlachetny")
            qs += SelectFromList(
                id = 0,
                prompt = "Do jakiej grupy zaliczamy ${el.namePL} (${el.symbol})?",
                options = opts,
                correctIndices = setOf(opts.indexOf(type)),
                hint = Hint("${el.namePL} to $type.", boldPart = type)
            )
        }

        // "Which element is a metal/non-metal/noble-gas?" — each correct element used at most once
        listOf(
            Triple("metalem",          metalCats,    nonMetalCats + setOf(METALLOID, NOBLE_GAS)),
            Triple("niemetalem",        nonMetalCats, metalCats    + setOf(METALLOID, NOBLE_GAS)),
            Triple("gazem szlachetnym", setOf(NOBLE_GAS),  metalCats + nonMetalCats + setOf(METALLOID))
        ).forEach { (label, correctCats, wrongCats) ->
            val correctPool = ELEMENTS.filter { it.category in correctCats && it.atomicNumber in 1..54 }
            val wrongPool   = ELEMENTS.filter { it.category in wrongCats   && it.atomicNumber in 1..54 }
            if (correctPool.isNotEmpty() && wrongPool.size >= 3) {
                correctPool.shuffled(rng).forEach { c ->
                    val ws   = wrongPool.shuffled(rng).take(3)
                    val opts = (ws.map { it.namePL } + c.namePL).shuffled(rng)
                    qs += SelectFromList(
                        id = 0,
                        prompt = "Który z tych pierwiastków jest $label?",
                        options = opts,
                        correctIndices = setOf(opts.indexOf(c.namePL)),
                        hint = Hint("${c.namePL} to $label.", boldPart = c.namePL)
                    )
                }
            }
        }

        return qs.shuffled(rng).mapIndexed { i, q -> q.copy(id = 2200 + i) }
    }

    // ── shared utils ──────────────────────────────────────────────────────────

    private fun shellSteps(z: Int, config: String): List<String> {
        val shellNames = listOf("K", "L", "M", "N", "O", "P")
        return config.split(",").mapIndexed { i, n ->
            "Powłoka ${shellNames.getOrElse(i) { "?" }}: $n"
        } + "Razem: $z → ${elementByNumber[z]?.namePL} (${elementByNumber[z]?.symbol})"
    }
}
