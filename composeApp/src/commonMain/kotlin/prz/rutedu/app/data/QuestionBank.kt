package prz.rutedu.app.data

import prz.rutedu.app.data.QuestionBank.banks
import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.Pt
import prz.rutedu.app.math.TriangleBuilder
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.MathOperator
import prz.rutedu.app.models.MathOperator.ADD
import prz.rutedu.app.models.MathOperator.DIVIDE
import prz.rutedu.app.models.MathOperator.MULTIPLY
import prz.rutedu.app.models.MathOperator.POWER
import prz.rutedu.app.models.MathOperator.SUBTRACT
import prz.rutedu.app.models.MathOperator.ROOT
import prz.rutedu.app.models.MathOperator.LOG
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.BalanceTerm
import prz.rutedu.app.models.Question.EquationBalance
import prz.rutedu.app.models.Question.FindAnswer
import prz.rutedu.app.models.Question.FindOperator
import prz.rutedu.app.models.Question.GraphSelectFromList
import prz.rutedu.app.models.Question.GraphTypeAnswer
import prz.rutedu.app.models.Question.MapQuiz
import prz.rutedu.app.models.Question.SelectFromList
import prz.rutedu.app.models.Question.Factorization
import prz.rutedu.app.models.Question.LinearEquation
import prz.rutedu.app.models.Question.SystemOfEquations
import prz.rutedu.app.models.Equation

/**
 * Central registry of all static (hardcoded) quiz questions.
 *
 * Questions for non-chemistry lessons are stored as private `List<Question>` fields and
 * registered in the [banks] map at the bottom of this file. Chemistry lessons are routed
 * to [ChemistryQuestionGenerator] which creates questions dynamically from a random seed.
 *
 * ## How to add questions for a new lesson
 *
 * 1. Declare a new private `val` (e.g. `private val mat_6_1: List<Question> = listOf(...)`).
 * 2. Populate it with [Question] instances. Each question's `id` must be unique within
 *    the list (sequential integers from 0 work well).
 * 3. Register it in [banks]: `"mat_6_1" to mat_6_1`.
 * 4. Make sure the lesson exists in [SubjectRepository] with the same id.
 *
 * For chemistry lessons, implement a generator function in [ChemistryQuestionGenerator]
 * instead of adding a static list here.
 */
object QuestionBank {

    /**
     * Returns the ordered list of questions for the given lesson.
     *
     * Routing:
     * - Lesson IDs starting with `"chemia_"` are delegated to [ChemistryQuestionGenerator],
     *   which shuffles and filters the pool using [seed] and [excludeIds].
     * - All other IDs look up the static list in [banks]. The [seed] and [excludeIds]
     *   parameters are **ignored** for static banks.
     *
     * @param lessonId   The lesson identifier (e.g. `"mat_1_1"`, `"chemia_3_1"`).
     * @param seed       Random seed for chemistry lesson generation. Ignored for static banks.
     * @param excludeIds Set of question IDs to omit (previously answered chemistry questions).
     *                   Ignored for static banks.
     * @return Ordered list of questions to present, or an empty list if [lessonId] is not registered.
     */
    fun questionsFor(lessonId: String, seed: Long = 0L, excludeIds: Set<Int> = emptySet()): List<Question> =
        when {
            lessonId.startsWith("chemia_") ->
                ChemistryQuestionGenerator.generateFor(lessonId, seed, excludeIds)
            lessonId.startsWith("algebra_") ->
                AlgebraQuestionGenerator.generateFor(lessonId, seed, excludeIds)
            lessonId.startsWith("mat_1_") || lessonId.startsWith("mat_2_") || 
            lessonId.startsWith("mat_3_") || lessonId.startsWith("mat_4_") || 
            lessonId.startsWith("mat_5_") || lessonId.startsWith("mat_6_") ||
            lessonId.startsWith("mat_7_") || lessonId.startsWith("mat_8_") ||
            lessonId.startsWith("mat_9_") || lessonId.startsWith("mat_10_") ||
            lessonId.startsWith("mat_11_") || lessonId.startsWith("mat_12_") ->
                MathQuestionGenerator.questionsFor(lessonId, seed, excludeIds)
            lessonId.startsWith("chemia_3_1") || lessonId.startsWith("chemia_3_2") ->
                banks[lessonId] ?: emptyList()
            else ->
                banks[lessonId] ?: emptyList()
        }

    /**
     * Static questions for Lesson 3-1: "Wzory kwasów" (Acid Formulas).
     *
     * Generates simple linear equations to solve for variable x using elementary arithmetic.
     */
    private val genericMath: List<Question> = listOf(
        FindAnswer(0, 5, 3, ADD,
            Hint("Dodaj obie liczby.", steps = listOf("5 + 3 = 8"))),
        FindAnswer(1, 9, 2, SUBTRACT,
            Hint("Odejmij.", steps = listOf("9 − 2 = 7"))),
        FindOperator(2, 4, 3, 12, MULTIPLY,
            Hint("4 × 3 = 12.", steps = listOf("4 × 3 = 12 ✓"))),
        FindAnswer(3, 8, 4, DIVIDE,
            Hint("8 ÷ 4 = 2.", steps = listOf("4 × 2 = 8", "Więc 8 ÷ 4 = 2"))),
        FindAnswer(4, 6, 7, ADD,
            Hint("6 + 7 = 13.", steps = listOf("6 + 7 = 13"))),
        FindOperator(5, 10, 5, 5, DIVIDE,
            Hint("10 ÷ 5 = 2.", steps = listOf("10 ÷ 5 = 2 ✓", "5 × 2 = 10 ✓"))),
        FindAnswer(6, 15, 8, SUBTRACT,
            Hint("15 − 8 = 7.", steps = listOf("15 − 8 = 7"))),
        FindAnswer(7, 3, 9, MULTIPLY,
            Hint("3 × 9 = 27.", steps = listOf("3 × 9 = 27"))),
        FindOperator(8, 11, 4, 15, ADD,
            Hint("11 + 4 = 15.", steps = listOf("11 + 4 = 15 ✓"))),
        FindAnswer(9, 20, 4, DIVIDE,
            Hint("20 ÷ 4 = 5.", steps = listOf("4 × 5 = 20", "Więc 20 ÷ 4 = 5")))
    )

    /**
     * Static questions for Lesson 1-1: "Lądy i oceany świata" (Lands & Oceans).
     *
     * Presents interactive world map quizzes where students locate various European countries.
     */
    private val geo_1_1: List<Question> = listOf(
        MapQuiz(0, "Poland", "Gdzie leży Polska?", MapRegion.EUROPE,
            hint = Hint("Polska leży w środkowej Europie, nad Morzem Bałtyckim.",
                steps = listOf("Centrum-wschodnia Europa", "Na południe od Morza Bałtyckiego"))),
        MapQuiz(1, "Germany", "Gdzie leżą Niemcy?", MapRegion.EUROPE,
            hint = Hint("Niemcy leżą w środkowej Europie Zachodniej.",
                steps = listOf("Na zachód od Polski", "Największy kraj Europy Zachodniej"))),
        MapQuiz(2, "France", "Gdzie leży Francja?", MapRegion.EUROPE,
            hint = Hint("Francja leży w zachodniej Europie.",
                steps = listOf("Na zachód od Niemiec", "Od La Manche po Morze Śródziemne"))),
        MapQuiz(3, "Italy", "Gdzie leżą Włochy?", MapRegion.EUROPE,
            hint = Hint("Włochy to półwysep w kształcie buta.",
                steps = listOf("Południe Europy", "Półwysep Apeniński wchodzi do Morza Śródziemnego"))),
        MapQuiz(4, "Spain", "Gdzie leży Hiszpania?", MapRegion.EUROPE,
            hint = Hint("Hiszpania leży na Półwyspie Iberyjskim.",
                steps = listOf("Skrajny południe-zachód Europy", "Sąsiaduje z Francją i Portugalią"))),
        MapQuiz(5, "Ukraine", "Gdzie leży Ukraina?", MapRegion.EUROPE,
            hint = Hint("Ukraina to największy kraj w całości leżący w Europie.",
                steps = listOf("Wschodnia Europa", "Na południe od Białorusi, na wschód od Polski"))),
        MapQuiz(6, "Sweden", "Gdzie leży Szwecja?", MapRegion.EUROPE,
            hint = Hint("Szwecja leży na Półwyspie Skandynawskim.",
                steps = listOf("Północna Europa — Skandynawia", "Wschodnia część Półwyspu Skandynawskiego"))),
        MapQuiz(7, "Norway", "Gdzie leży Norwegia?", MapRegion.EUROPE,
            hint = Hint("Norwegia leży wzdłuż zachodniego wybrzeża Skandynawii.",
                steps = listOf("Zachodnia część Półwyspu Skandynawskiego", "Długa linia brzegowa nad Atlantykiem"))),
        MapQuiz(8, "Romania", "Gdzie leży Rumunia?", MapRegion.EUROPE,
            hint = Hint("Rumunia leży na Bałkanach.",
                steps = listOf("Południowo-wschodnia Europa", "Sąsiaduje z Ukrainą, Bułgarią i Serbią"))),
        MapQuiz(9, "Greece", "Gdzie leży Grecja?", MapRegion.EUROPE,
            hint = Hint("Grecja leży na południu Bałkanów.",
                steps = listOf("Południe Europy", "Dużo wysp na Morzu Egejskim")))
    )

    /**
     * Static questions for Lesson 4-1: "Sąsiedzi Polski" (Poland's Neighbors).
     *
     * Quizzes students on pinpointing the 7 neighboring countries of Poland on a regional map.
     */
    private val geo_4_1: List<Question> = listOf(
        MapQuiz(0, "Germany", "Zaznacz Niemcy — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Niemcy graniczą z Polską na zachodzie.",
                steps = listOf("Szukaj na zachód od Polski", "Nad Morzem Bałtyckim i Północnym"))),
        MapQuiz(1, "Czechia", "Zaznacz Czechy — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Czechy graniczą z Polską na południe-zachodzie.",
                steps = listOf("Szukaj na południe od Polski", "Bez dostępu do morza — w środku Europy"))),
        MapQuiz(2, "Slovakia", "Zaznacz Słowację — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Słowacja graniczy z Polską na południu.",
                steps = listOf("Na południe od Polski, na wschód od Czech", "Mały kraj w środkowej Europie"))),
        MapQuiz(3, "Ukraine", "Zaznacz Ukrainę — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Ukraina graniczy z Polską na wschodzie.",
                steps = listOf("Na wschód od Polski", "Duży kraj — największy w całości w Europie"))),
        MapQuiz(4, "Belarus", "Zaznacz Białoruś — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Białoruś graniczy z Polską na północnym-wschodzie.",
                steps = listOf("Północny-wschód od Polski", "Na południe od Litwy"))),
        MapQuiz(5, "Lithuania", "Zaznacz Litwę — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Litwa graniczy z Polską na północy.",
                steps = listOf("Na północ od Polski", "Jedno z trzech państw bałtyckich"))),
        MapQuiz(6, "Russia", "Zaznacz Rosję (obwód kaliningradzki) — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Rosja graniczy z Polską przez obwód kaliningradzki.",
                steps = listOf("Szukaj enklawy nad Morzem Bałtyckim", "Rosyjski obwód odcięty od Rosji, między Polską a Litwą")))
    )

    /**
     * Static questions for Lesson 4-2: "Kraje Azji" (Asian Countries).
     *
     * Presents map locations for major Asian countries such as China, India, Japan, etc.
     */
    private val geo_4_2: List<Question> = listOf(
        MapQuiz(0, "China", "Gdzie leżą Chiny?", MapRegion.ASIA,
            hint = Hint("Chiny to największy kraj Azji pod względem populacji.",
                steps = listOf("Wschód Azji", "Sąsiad Mongolii, Rosji, Indii i Wietnamu"))),
        MapQuiz(1, "Japan", "Gdzie leży Japonia?", MapRegion.ASIA,
            hint = Hint("Japonia to archipelag wysp na wschodnim wybrzeżu Azji.",
                steps = listOf("Daleki Wschód — wyspy na Oceanie Spokojnym", "Na wschód od Korei i Chin"))),
        MapQuiz(2, "India", "Gdzie leżą Indie?", MapRegion.ASIA,
            hint = Hint("Indie leżą na Półwyspie Indyjskim.",
                steps = listOf("Azja Południowa", "Wielki półwysep wchodzący do Oceanu Indyjskiego"))),
        MapQuiz(3, "South Korea", "Gdzie leży Korea Południowa?", MapRegion.ASIA,
            hint = Hint("Korea Południowa leży na Półwyspie Koreańskim.",
                steps = listOf("Azja Wschodnia", "Południowa część Półwyspu Koreańskiego"))),
        MapQuiz(4, "Mongolia", "Gdzie leży Mongolia?", MapRegion.ASIA,
            hint = Hint("Mongolia leży między Chinami a Rosją.",
                steps = listOf("Azja Środkowa/Wschodnia", "Duży kraj otoczony Chinami od południa i Rosją od północy"))),
        MapQuiz(5, "Kazakhstan", "Gdzie leży Kazachstan?", MapRegion.ASIA,
            hint = Hint("Kazachstan to największy kraj Azji Środkowej.",
                steps = listOf("Azja Środkowa", "Na południe od Rosji, na północ od Morza Kaspijskiego"))),
        MapQuiz(6, "Iran", "Gdzie leży Iran?", MapRegion.ASIA,
            hint = Hint("Iran leży na Bliskim Wschodzie.",
                steps = listOf("Azja Zachodnia — Bliski Wschód", "Między Irakiem a Afganistanem, nad Zatoką Perską"))),
        MapQuiz(7, "Saudi Arabia", "Gdzie leży Arabia Saudyjska?", MapRegion.ASIA,
            hint = Hint("Arabia Saudyjska zajmuje większość Półwyspu Arabskiego.",
                steps = listOf("Półwysep Arabski", "Duży kraj w centrum Bliskiego Wschodu"))),
        MapQuiz(8, "Vietnam", "Gdzie leży Wietnam?", MapRegion.ASIA,
            hint = Hint("Wietnam leży w Azji Południowo-Wschodniej.",
                steps = listOf("Półwysep Indochiński", "Długi, wąski kraj wzdłuż Morza Południowochińskiego"))),
        MapQuiz(9, "Thailand", "Gdzie leży Tajlandia?", MapRegion.ASIA,
            hint = Hint("Tajlandia leży w Azji Południowo-Wschodniej.",
                steps = listOf("Azja Południowo-Wschodnia", "Na południe od Laosu, na zachód od Wietnamu")))
    )

    /**
     * Static questions for Lesson 4-3: "Stolice Europy" (European Capitals).
     *
     * Asks students to identify European countries on the map based on their capital cities.
     */
    private val geo_4_3: List<Question> = listOf(
        MapQuiz(0, "France", "Wskaż kraj, którego stolicą jest Paryż", MapRegion.EUROPE,
            hint = Hint("Paryż to stolica Francji.",
                steps = listOf("Francja leży w zachodniej Europie", "Graniczy z Niemcami, Belgią i Hiszpanią"))),
        MapQuiz(1, "Germany", "Wskaż kraj, którego stolicą jest Berlin", MapRegion.EUROPE,
            hint = Hint("Berlin to stolica Niemiec.",
                steps = listOf("Niemcy leżą w środkowej Europie", "Sąsiad Polski na zachodzie"))),
        MapQuiz(2, "Italy", "Wskaż kraj, którego stolicą jest Rzym", MapRegion.EUROPE,
            hint = Hint("Rzym to stolica Włoch.",
                steps = listOf("Włochy to półwysep w kształcie buta", "Morze Śródziemne — południe Europy"))),
        MapQuiz(3, "Spain", "Wskaż kraj, którego stolicą jest Madryt", MapRegion.EUROPE,
            hint = Hint("Madryt to stolica Hiszpanii.",
                steps = listOf("Półwysep Iberyjski — południe-zachód Europy", "Sąsiad Francji i Portugalii"))),
        MapQuiz(4, "United Kingdom", "Wskaż kraj, którego stolicą jest Londyn", MapRegion.EUROPE,
            hint = Hint("Londyn to stolica Zjednoczonego Królestwa.",
                steps = listOf("Wyspy Brytyjskie — północny-zachód Europy", "Oddzielony od Francji Kanałem La Manche"))),
        MapQuiz(5, "Czechia", "Wskaż kraj, którego stolicą jest Praga", MapRegion.EUROPE,
            hint = Hint("Praga to stolica Czech.",
                steps = listOf("Środkowa Europa bez dostępu do morza", "Sąsiad Polski na południu"))),
        MapQuiz(6, "Hungary", "Wskaż kraj, którego stolicą jest Budapeszt", MapRegion.EUROPE,
            hint = Hint("Budapeszt to stolica Węgier.",
                steps = listOf("Środkowa Europa", "Na południe od Czech i Słowacji"))),
        MapQuiz(7, "Austria", "Wskaż kraj, którego stolicą jest Wiedeń", MapRegion.EUROPE,
            hint = Hint("Wiedeń to stolica Austrii.",
                steps = listOf("Środkowa Europa — Alpy", "Sąsiad Niemiec, Czech, Węgier i Włoch"))),
        MapQuiz(8, "Sweden", "Wskaż kraj, którego stolicą jest Sztokholm", MapRegion.EUROPE,
            hint = Hint("Sztokholm to stolica Szwecji.",
                steps = listOf("Półwysep Skandynawski — północna Europa", "Wschodnia część Skandynawii"))),
        MapQuiz(9, "Greece", "Wskaż kraj, którego stolicą są Ateny", MapRegion.EUROPE,
            hint = Hint("Ateny to stolica Grecji.",
                steps = listOf("Południe Bałkanów", "Wiele wysp na Morzu Egejskim")))
    )

    /**
     * Static questions for Lesson 4-4: "Województwa Polski" (Polish Provinces).
     *
     * Asks students to identify Polish provinces on the map based on their names.
     */
    private val geo_4_4: List<Question> = listOf(
        MapQuiz(4401, "małopolskie", "Wskaż województwo małopolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Małopolskie leży na południu Polski, ze stolicą w Krakowie.")),
        MapQuiz(4402, "mazowieckie", "Wskaż województwo mazowieckie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Mazowieckie to największe województwo, leży w centrum-wschodzie, ze stolicą w Warszawie.")),
        MapQuiz(4403, "pomorskie", "Wskaż województwo pomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Pomorskie leży nad Morzem Bałtyckim, ze stolicą w Gdańsku.")),
        MapQuiz(4404, "śląskie", "Wskaż województwo śląskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Śląskie leży na południu, jest najbardziej zaludnione, ze stolicą w Katowicach.")),
        MapQuiz(4405, "wielkopolskie", "Wskaż województwo wielkopolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Wielkopolskie leży w zachodniej części kraju, ze stolicą w Poznaniu.")),
        MapQuiz(4406, "dolnośląskie", "Wskaż województwo dolnośląskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Dolnośląskie leży na południowym zachodzie, ze stolicą we Wrocławiu.")),
        MapQuiz(4407, "podkarpackie", "Wskaż województwo podkarpackie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Podkarpackie leży na południowym wschodzie, ze stolicą w Rzeszowie.")),
        MapQuiz(4408, "zachodniopomorskie", "Wskaż województwo zachodniopomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Zachodniopomorskie leży na północnym zachodzie, nad Bałtykiem, ze stolicą w Szczecinie.")),
        MapQuiz(4409, "lubelskie", "Wskaż województwo lubelskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Lubelskie leży na wschodzie Polski, ze stolicą w Lublinie.")),
        MapQuiz(4410, "warmińsko-mazurskie", "Wskaż województwo warmińsko-mazurskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Warmińsko-mazurskie to kraina tysiąca jezior na północy, ze stolicą w Olsztynie.")),
        MapQuiz(4411, "podlaskie", "Wskaż województwo podlaskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Podlaskie leży na północnym wschodzie, ze stolicą w Białymstoku.")),
        MapQuiz(4412, "łódzkie", "Wskaż województwo łódzkie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Łódzkie leży w samym centrum Polski, ze stolicą w Łodzi.")),
        MapQuiz(4413, "kujawsko-pomorskie", "Wskaż województwo kujawsko-pomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Kujawsko-pomorskie leży w północnej części centrum, ze stolicami w Bydgoszczy i Toruniu.")),
        MapQuiz(4414, "opolskie", "Wskaż województwo opolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Opolskie to najmniejsze województwo, leży na południowym zachodzie, ze stolicą w Opolu.")),
        MapQuiz(4415, "lubuskie", "Wskaż województwo lubuskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Lubuskie leży na zachodzie, przy granicy z Niemcami, ze stolicami w Gorzowie Wlkp. i Zielonej Górze.")),
        MapQuiz(4416, "świętokrzyskie", "Wskaż województwo świętokrzyskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Świętokrzyskie leży w południowo-środkowej Polsce, ze stolicą w Kielcach."))
    )
    
    /**
     * Static questions for Lesson 3-1: "Wzory kwasów" (Acid Formulas).
     *
     * Covers identifying names and chemical formulas for binary and oxyacids.
     */
    private val chemia_3_1: List<Question> = listOf(
        SelectFromList(
            id = 3101,
            prompt = "Wzór kwasu chlorowodorowego (solnego)",
            options = listOf("HCl", "H₂S", "HF", "HBr"),
            correctIndices = setOf(0),
            hint = Hint(
                mainText = "Kwas chlorowodorowy to kwas beztlenowy. Składa się z wodoru i chloru.",
                boldPart = "HCl",
                sectionTitle = "Kwasy beztlenowe",
                items = listOf("HCl – chlorowodorowy", "H₂S – siarkowodorowy", "HF – fluorowodorowy", "HBr – bromowodorowy")
            )
        ),
        SelectFromList(
            id = 3102,
            prompt = "Wzór kwasu siarkowodorowego",
            options = listOf("H₂SO₄", "H₂S", "H₂SO₃", "HCl"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas siarkowodorowy to H₂S – beztlenowy kwas znany z zapachu zgniłych jaj.",
                boldPart = "H₂S"
            )
        ),
        SelectFromList(
            id = 3103,
            prompt = "Wzór kwasu fluorowodorowego",
            options = listOf("HBr", "HCl", "HF", "H₂S"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "Kwas fluorowodorowy (HF) używany jest do trawienia szkła.",
                boldPart = "HF"
            )
        ),
        SelectFromList(
            id = 3104,
            prompt = "Wzór kwasu bromowodorowego",
            options = listOf("HF", "HCl", "H₂S", "HBr"),
            correctIndices = setOf(3),
            hint = Hint(
                mainText = "Kwas bromowodorowy to HBr – beztlenowy kwas podobny do HCl.",
                boldPart = "HBr"
            )
        ),
        SelectFromList(
            id = 3105,
            prompt = "Wzór kwasu siarkowego(VI)",
            options = listOf("H₂SO₃", "HNO₃", "H₂SO₄", "H₃PO₄"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "Kwas siarkowy(VI) – H₂SO₄ – najważniejszy kwas przemysłowy, silnie żrący.",
                boldPart = "H₂SO₄",
                sectionTitle = "Kwasy tlenowe siarki",
                items = listOf("H₂SO₄ – siarkowy(VI), siarka na +6", "H₂SO₃ – siarkowy(IV), siarka na +4")
            )
        ),
        SelectFromList(
            id = 3106,
            prompt = "Wzór kwasu azotowego(V)",
            options = listOf("H₂SO₄", "HNO₃", "H₃PO₄", "H₂CO₃"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas azotowy(V) to HNO₃. Używany do produkcji nawozów i materiałów wybuchowych.",
                boldPart = "HNO₃"
            )
        ),
        SelectFromList(
            id = 3107,
            prompt = "Wzór kwasu węglowego",
            options = listOf("H₂SO₄", "H₃PO₄", "HNO₃", "H₂CO₃"),
            correctIndices = setOf(3),
            hint = Hint(
                mainText = "Kwas węglowy H₂CO₃ to słaby kwas, powstaje gdy CO₂ rozpuszcza się w wodzie.",
                boldPart = "H₂CO₃"
            )
        ),
        SelectFromList(
            id = 3108,
            prompt = "Wzór kwasu fosforowego(V) (ortofosforowego)",
            options = listOf("H₃PO₄", "H₂SO₄", "H₂CO₃", "HNO₃"),
            correctIndices = setOf(0),
            hint = Hint(
                mainText = "Kwas fosforowy(V) to H₃PO₄. Składnik nawozów i napojów cola.",
                boldPart = "H₃PO₄"
            )
        ),
        SelectFromList(
            id = 3109,
            prompt = "Wzór kwasu siarkowego(IV)",
            options = listOf("H₂SO₄", "H₂SO₃", "HNO₃", "H₃PO₄"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas siarkowy(IV) to H₂SO₃. Powstaje przy spalaniu siarki – przyczyna kwaśnych deszczy.",
                boldPart = "H₂SO₃",
                sectionTitle = "Kwasy tlenowe siarki",
                items = listOf("H₂SO₄ – siarkowy(VI), siarka na +6", "H₂SO₃ – siarkowy(IV), siarka na +4")
            )
        ),
        SelectFromList(
            id = 3110,
            prompt = "Kwas o wzorze H₂SO₄ to…",
            options = listOf("kwas azotowy(V)", "kwas węglowy", "kwas siarkowy(VI)", "kwas fosforowy(V)"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "H₂SO₄ to kwas siarkowy(VI). Liczba (VI) oznacza stopień utlenienia siarki.",
                boldPart = "kwas siarkowy(VI)"
            )
        )
    )

    /**
     * Static questions for Lesson 3-2: "Równania reakcji" (Chemical Equations Balancing).
     *
     * Focuses on balancing chemical equations for synthesis, acid production, and neutralizations.
     */
    private val chemia_3_2: List<Question> = listOf(
        EquationBalance(
            id = 3201,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Dobierz odpowiednie współczynniki stechiometryczne",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("Cl₂", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "H₂ + Cl₂ → 2HCl. Po lewej: 2H i 2Cl. Po prawej: w 2 cząsteczkach HCl też 2H i 2Cl.",
                boldPart = "2HCl",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "Policz atomy H po lewej: 1×H₂ = 2 atomy H",
                    "Policz atomy Cl po lewej: 1×Cl₂ = 2 atomy Cl",
                    "Po prawej HCl ma 1H i 1Cl → potrzeba 2×HCl"
                )
            )
        ),
        EquationBalance(
            id = 3202,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Dobierz odpowiednie współczynniki stechiometryczne",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 2),
                BalanceTerm("O₂", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "2H₂ + O₂ → 2H₂O. Klasyczna reakcja syntezy wody.",
                boldPart = "2H₂O",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "Po prawej 2 cząsteczki H₂O = 4H i 2O",
                    "4H po lewej to 2×H₂",
                    "2O po lewej to 1×O₂"
                )
            )
        ),
        EquationBalance(
            id = 3203,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas siarkowy(VI) powstaje z SO₃ i wody",
            reactants = listOf(
                BalanceTerm("SO₃", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂SO₄", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "SO₃ + H₂O → H₂SO₄. Wszystkie współczynniki wynoszą 1.",
                boldPart = "H₂SO₄"
            )
        ),
        EquationBalance(
            id = 3204,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas siarkowy(IV) powstaje z SO₂ i wody",
            reactants = listOf(
                BalanceTerm("SO₂", fixedCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂SO₃", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "SO₂ + H₂O → H₂SO₃. Kwas siarkowy(IV) odpowiada za kwaśne deszcze.",
                boldPart = "H₂SO₃"
            )
        ),
        EquationBalance(
            id = 3205,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas węglowy powstaje z CO₂ i wody",
            reactants = listOf(
                BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂CO₃", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "CO₂ + H₂O → H₂CO₃. Tak powstaje kwas węglowy w napojach gazowanych.",
                boldPart = "H₂CO₃"
            )
        ),
        EquationBalance(
            id = 3206,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Kwas azotowy(V) z N₂O₅ i wody",
            reactants = listOf(
                BalanceTerm("N₂O₅", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("HNO₃", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "N₂O₅ + H₂O → 2HNO₃. Jedna cząsteczka N₂O₅ zawiera 2 atomy N → 2 cząsteczki HNO₃.",
                boldPart = "2HNO₃",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "N₂O₅ ma 2 atomy azotu",
                    "Każda cząsteczka HNO₃ ma 1 atom azotu",
                    "Potrzeba 2×HNO₃ aby zbilansować azot"
                )
            )
        ),
        EquationBalance(
            id = 3207,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Kwas fosforowy(V) z P₂O₅ i wody",
            reactants = listOf(
                BalanceTerm("P₂O₅", fixedCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 3)
            ),
            products = listOf(
                BalanceTerm("H₃PO₄", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "P₂O₅ + 3H₂O → 2H₃PO₄. Dwa atomy P → 2 cząsteczki H₃PO₄, a to wymaga 3 cząsteczek wody.",
                boldPart = "2H₃PO₄",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "P₂O₅ ma 2 atomy P → potrzeba 2×H₃PO₄",
                    "2×H₃PO₄ ma 6 atomów H → potrzeba 3×H₂O",
                    "Sprawdź O: P₂O₅(5) + 3H₂O(3) = 8 = 2×H₃PO₄(8) ✓"
                )
            )
        ),
        EquationBalance(
            id = 3208,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Reakcja syntezy kwasu siarkowodorowego",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("S", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂S", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "H₂ + S → H₂S. Wszystkie współczynniki wynoszą 1.",
                boldPart = "H₂S"
            )
        )
    )

    // Maps lesson IDs to their static question lists. Add new entries here after
    // declaring the corresponding private val above.
    private val banks: Map<String, List<Question>> = mapOf(
        "mat_3_1" to genericMath,
        "geo_1_1" to geo_1_1,
        "geo_4_1" to geo_4_1,
        "geo_4_2" to geo_4_2,
        "geo_4_3" to geo_4_3,
        "geo_4_4" to geo_4_4,
        "chemia_3_1" to chemia_3_1,
        "chemia_3_2" to chemia_3_2,
    )
}
