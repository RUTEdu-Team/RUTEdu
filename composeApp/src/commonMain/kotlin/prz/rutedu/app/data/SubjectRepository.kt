package prz.rutedu.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.Color
import prz.rutedu.app.data.SubjectRepository.subjects
import prz.rutedu.app.models.Lesson
import prz.rutedu.app.models.Subject
import prz.rutedu.app.models.Topic

/**
 * In-memory, hardcoded source of truth for the entire curriculum tree.
 *
 * The hierarchy is: **Subject -> Topic -> Lesson**.
 * Every [Subject] has a list of [Topic]s; every [Topic] has a list of [Lesson]s.
 * The ordering of entries in each list controls the display order on screen.
 *
 * ## How to add content
 *
 * | What to add     | Where to add it                                                |
 * |-----------------|----------------------------------------------------------------|
 * | New **Subject** | Append a `Subject(...)` to [subjects].                         |
 * | New **Topic**   | Append a `Topic(...)` to its parent [Subject]'s `topics` list. |
 * | New **Lesson**  | Append a `Lesson(...)` to its parent [Topic]'s `lessons` list. |
 *
 * After adding a lesson, also register its questions:
 * - **Static questions** (math, geography): add a private `val` list in `QuestionBank.kt`
 *   and register it in the `banks` map at the bottom of that file.
 * - **Generated questions** (chemistry): add a private generator function in
 *   `ChemistryQuestionGenerator.kt` and register it in the `when` block of `generateFor()`.
 *
 * ## Locking / unlocking
 *
 * Set `isLocked = true` on a [Topic] or [Lesson] to render it as a greyed-out card that
 * the student cannot tap. Change to `false` when the content is ready.
 *
 * ## Colours and icons
 *
 * Use `Color(0xFFRRGGBB)` for full-opacity hex colours.
 * Use `Icons.Default.<Name>` from the `materialIconsExtended` dependency.
 */
object SubjectRepository {

    /**
     * The complete, ordered list of subjects displayed on the home screen.
     *
     * Currently contains: **Matematyka**, **Chemia**, **Geografia**.
     * Append new [Subject] entries here to add more school subjects.
     */
    val subjects: List<Subject> = listOf(

        /**
         * **Mathematics Subject**
         *
         * Includes core high school math topics:
         * - Liczby rzeczywiste (Real Numbers)
         * - Wyrażenia algebraiczne (Algebraic Expressions)
         * - Równania i nierówności (Equations and Inequalities)
         * - Funkcje (Functions)
         * - Ciągi (Sequences)
         */
        Subject(
            id = "matematyka",
            name = "Matematyka",
            progress = 0.25f,
            color = Color(0xFF4A80F0),
            backgroundColor = Color(0xFFEBF1FF),
            icon = Icons.Default.Calculate,
            topics = listOf(
                Topic(
                    id = "mat_1",
                    name = "Liczby rzeczywiste",
                    description = "Podstawowe operacje i zbiory",
                    progress = 0.80f,
                    isLocked = false,
                    color = Color(0xFFF47B20),
                    icon = Icons.Default.Calculate,
                    lessons = listOf(
                        Lesson(
                            id = "mat_1_1",
                            name = "Dodawanie i odejmowanie",
                            description = "Podstawowe działania na liczbach rzeczywistych",
                            progress = 0.80f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Add
                        ),
                        Lesson(
                            id = "mat_1_2",
                            name = "Mnożenie i dzielenie",
                            description = "Reguły znaków i kolejność wykonywania działań",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Close
                        ),
                        Lesson(
                            id = "mat_1_3",
                            name = "Potęgowanie",
                            description = "Potęgi o wykładniku całkowitym",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "mat_1_4",
                            name = "Pierwiastkowanie",
                            description = "Własności pierwiastków",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Roofing
                        ),
                        Lesson(
                            id = "mat_1_5",
                            name = "Logarytmy",
                            description = "Wprowadzenie do logarytmów",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Functions
                        )
                    )
                ),
                Topic(
                    id = "mat_2",
                    name = "Wyrażenia algebraiczne",
                    description = "Wzory skróconego mnożenia",
                    progress = 0.45f,
                    isLocked = false,
                    color = Color(0xFF4A80F0),
                    icon = Icons.Default.Functions,
                    lessons = listOf(
                        Lesson(
                            id = "mat_2_1",
                            name = "Jednomiany i wielomiany",
                            description = "Podstawy algebry",
                            progress = 0.90f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Functions
                        ),
                        Lesson(
                            id = "mat_2_2",
                            name = "Wzory skróconego mnożenia",
                            description = "(a+b)², (a-b)², (a+b)(a-b)",
                            progress = 0.20f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Calculate
                        ),
                        Lesson(
                            id = "mat_2_3",
                            name = "Rozkład na czynniki",
                            description = "Metody faktoryzacji",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.Default.Expand
                        )
                    )
                ),
                Topic(
                    id = "mat_3",
                    name = "Równania i nierówności",
                    description = "Metody rozwiązywania układów",
                    progress = 0.10f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.BarChart,
                    lessons = listOf(
                        Lesson(
                            id = "mat_3_1",
                            name = "Równania liniowe",
                            description = "Równania pierwszego stopnia",
                            progress = 0.30f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.BarChart
                        ),
                        Lesson(
                            id = "mat_3_2",
                            name = "Układy równań",
                            description = "Metoda podstawiania i przeciwnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.TableChart
                        ),
                        Lesson(
                            id = "mat_3_3",
                            name = "Nierówności liniowe",
                            description = "Metoda podstawiania i przeciwnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFEF5350),
                            icon = Icons.Default.Calculate
                        )
                    )
                ),
                Topic(
                    id = "mat_4",
                    name = "Funkcje",
                    description = "Wykresy i własności funkcji",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF7C4DFF),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    lessons = listOf(
                        Lesson(
                            id = "mat_4_1",
                            name = "Funkcja liniowa",
                            description = "Wprowadzenie do funkcji liniowej i jej wykresu y = ax + b",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "mat_4_2",
                            name = "Funkcja kwadratowa",
                            description = "Wykres funkcji kwadratowej (parabola) i jej podstawowe własności",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "mat_4_3",
                            name = "Odczytywanie wykresów funkcji",
                            description = "Interpretacja wykresów funkcji: wartości, miejsca zerowe i przedziały",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "mat_4_4",
                            name = "Własności funkcji",
                            description = "Dziedzina, zbiór wartości, monotoniczność i miejsca zerowe funkcji",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        )
                    )
                ),
                Topic(
                    id = "mat_5",
                    name = "Geometria płaska",
                    description = "Figury, kąty, pola i zależności geometryczne na płaszczyźnie",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF00BFA6),
                    icon = Icons.Default.ChangeHistory,
                    lessons = listOf(
                        Lesson(
                            id = "mat_5_1",
                            name = "Kąty w trójkącie",
                            description = "Suma kątów wewnętrznych trójkąta i podstawowe własności kątów",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF00BFA6),
                            icon = Icons.Default.ChangeHistory
                        ),
                        Lesson(
                            id = "mat_5_2",
                            name = "Czworokąty",
                            description = "Własności prostokąta, kwadratu, równoległoboku i trapezu",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF00BFA6),
                            icon = Icons.Default.Layers
                        ),
                        Lesson(
                            id = "mat_5_3",
                            name = "Okręgi i koła",
                            description = "Elementy okręgu: promień, średnica, cięciwa i własności koła",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF00BFA6),
                            icon = Icons.Default.PieChart
                        ),
                        Lesson(
                            id = "mat_5_4",
                            name = "Pola i obwody figur",
                            description = "Obliczanie pól i obwodów podstawowych figur geometrycznych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF00BFA6),
                            icon = Icons.Default.Straighten
                        ),
                        Lesson(
                            id = "mat_5_5",
                            name = "Twierdzenie Pitagorasa",
                            description = "Zależność a² + b² = c² w trójkącie prostokątnym",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF00BFA6),
                            icon = Icons.Default.Functions
                        )
                    )
                ),

                Topic(
                    id = "mat_6",
                    name = "Ułamki",
                    description = "Operacje na ułamkach zwykłych i dziesiętnych oraz ich zastosowania",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFFFB300),
                    icon = Icons.Default.Layers,
                    lessons = listOf(
                        Lesson(
                            id = "mat_6_1",
                            name = "Zamiana ułamków",
                            description = "Przekształcanie ułamków zwykłych i dziesiętnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFFFB300),
                            icon = Icons.AutoMirrored.Filled.CompareArrows
                        ),
                        Lesson(
                            id = "mat_6_2",
                            name = "Działania na ułamkach",
                            description = "Dodawanie, odejmowanie, mnożenie i dzielenie ułamków",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFFFB300),
                            icon = Icons.Default.Calculate
                        ),
                        Lesson(
                            id = "mat_6_3",
                            name = "Porównywanie ułamków",
                            description = "Porównywanie wartości ułamków zwykłych i dziesiętnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFFFB300),
                            icon = Icons.AutoMirrored.Filled.CompareArrows
                        ),
                        Lesson(
                            id = "mat_6_4",
                            name = "Ułamki dziesiętne",
                            description = "Działania na liczbach dziesiętnych i ich zastosowania",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFFFB300),
                            icon = Icons.Default.Calculate
                        )
                    )
                ),

                Topic(
                    id = "mat_7",
                    name = "Procenty",
                    description = "Obliczenia procentowe i ich zastosowanie w życiu codziennym",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFEF5350),
                    icon = Icons.Default.Percent,
                    lessons = listOf(
                        Lesson(
                            id = "mat_7_1",
                            name = "Procent liczby",
                            description = "Obliczanie procentu danej liczby",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFEF5350),
                            icon = Icons.Default.Percent
                        ),
                        Lesson(
                            id = "mat_7_2",
                            name = "Zniżki i podwyżki",
                            description = "Zmiany procentowe w cenach i wartościach",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFEF5350),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "mat_7_3",
                            name = "Zadania tekstowe",
                            description = "Zastosowanie procentów w problemach praktycznych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFEF5350),
                            icon = Icons.Default.Functions
                        )
                    )
                ),

                Topic(
                    id = "mat_8",
                    name = "Proporcje i skala",
                    description = "Zależności proporcjonalne i praca ze skalą oraz mapami",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF42A5F5),
                    icon = Icons.Default.Straighten,
                    lessons = listOf(
                        Lesson(
                            id = "mat_8_1",
                            name = "Proporcje",
                            description = "Rozwiązywanie proporcji prostych i odwrotnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF42A5F5),
                            icon = Icons.AutoMirrored.Filled.CompareArrows
                        ),
                        Lesson(
                            id = "mat_8_2",
                            name = "Skala",
                            description = "Odczytywanie i stosowanie skali na mapach i planach",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF42A5F5),
                            icon = Icons.Default.Map
                        ),
                        Lesson(
                            id = "mat_8_3",
                            name = "Mapa i odległości",
                            description = "Obliczanie rzeczywistych odległości na mapie",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF42A5F5),
                            icon = Icons.Default.Explore
                        )
                    )
                ),

                Topic(
                    id = "mat_9",
                    name = "Geometria przestrzenna",
                    description = "Bryły, ich własności oraz obliczenia objętości i pól powierzchni",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFAB47BC),
                    icon = Icons.Default.ViewInAr,
                    lessons = listOf(
                        Lesson(
                            id = "mat_9_1",
                            name = "Graniastosłupy",
                            description = "Własności i elementy graniastosłupów",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFAB47BC),
                            icon = Icons.Default.ViewInAr
                        ),
                        Lesson(
                            id = "mat_9_2",
                            name = "Ostrosłupy",
                            description = "Własności i elementy ostrosłupów",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFAB47BC),
                            icon = Icons.Default.ChangeHistory
                        ),
                        Lesson(
                            id = "mat_9_3",
                            name = "Bryły obrotowe",
                            description = "Walec, stożek i kula oraz ich własności",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFAB47BC),
                            icon = Icons.Default.PieChart
                        ),
                        Lesson(
                            id = "mat_9_4",
                            name = "Pola brył",
                            description = "Obliczanie pól powierzchni brył",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFAB47BC),
                            icon = Icons.Default.Straighten
                        ),
                        Lesson(
                            id = "mat_9_5",
                            name = "Objętość brył",
                            description = "Obliczanie objętości brył geometrycznych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFAB47BC),
                            icon = Icons.Default.Layers
                        )
                    )
                ),

                Topic(
                    id = "mat_10",
                    name = "Układ współrzędnych",
                    description = "Punkty, wykresy i geometria analityczna na płaszczyźnie",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF26C6DA),
                    icon = Icons.Default.Timeline,
                    lessons = listOf(
                        Lesson(
                            id = "mat_10_1",
                            name = "Układ współrzędnych",
                            description = "Wprowadzenie do osi X i Y",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF26C6DA),
                            icon = Icons.Default.Timeline
                        ),
                        Lesson(
                            id = "mat_10_2",
                            name = "Odczytywanie punktów",
                            description = "Współrzędne punktów na płaszczyźnie",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF26C6DA),
                            icon = Icons.Default.Place
                        ),
                        Lesson(
                            id = "mat_10_3",
                            name = "Rysowanie figur",
                            description = "Rysowanie prostych i figur w układzie współrzędnych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF26C6DA),
                            icon = Icons.Default.Layers
                        ),
                        Lesson(
                            id = "mat_10_4",
                            name = "Odległość punktów",
                            description = "Obliczanie odległości między punktami",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF26C6DA),
                            icon = Icons.Default.Straighten
                        )
                    )
                ),

                Topic(
                    id = "mat_11",
                    name = "Statystyka i prawdopodobieństwo",
                    description = "Średnia, mediana, wykresy i prawdopodobieństwo.",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.BarChart,
                    lessons = listOf(
                        Lesson(
                            id = "mat_11_1",
                            name = "Średnia arytmetyczna",
                            description = "Obliczanie średniej arytmetycznej danych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Calculate
                        ),
                        Lesson(
                            id = "mat_11_2",
                            name = "Mediana i dominanta",
                            description = "Wyznaczanie mediany i dominanty w zbiorach danych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Timeline
                        ),
                        Lesson(
                            id = "mat_11_3",
                            name = "Wykresy i diagramy",
                            description = "Odczytywanie i tworzenie wykresów oraz diagramów",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.BarChart
                        ),
                        Lesson(
                            id = "mat_11_4",
                            name = "Prawdopodobieństwo prostych zdarzeń",
                            description = "Obliczanie prawdopodobieństwa prostych zdarzeń losowych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Percent
                        )
                    )
                ),

                Topic(
                    id = "mat_12",
                    name = "Liczby całkowite",
                    description = "Liczby dodatnie i ujemne oraz działania.",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Remove,
                    lessons = listOf(
                        Lesson(
                            id = "mat_12_1",
                            name = "Liczby dodatnie i ujemne",
                            description = "Wprowadzenie do liczb całkowitych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.AutoMirrored.Filled.CompareArrows
                        ),
                        Lesson(
                            id = "mat_12_2",
                            name = "Dodawanie i odejmowanie",
                            description = "Działania na liczbach całkowitych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Add
                        ),
                        Lesson(
                            id = "mat_12_3",
                            name = "Mnożenie i dzielenie",
                            description = "Działania na liczbach całkowitych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Close
                        )
                    )
                )
            )
        ),

        /**
         * **Chemistry Subject**
         *
         * Includes organic and inorganic chemistry topics:
         * - Układ okresowy (Periodic Table)
         * - Tlenki, wodorotlenki, kwasy, sole (Oxides, Hydroxides, Acids, Salts)
         * - Dysocjacja elektrolityczna (Electrolytic Dissociation)
         * - Węglowodory (Hydrocarbons)
         * - Pochodne węglowodorów (Hydrocarbon Derivatives)
         */
        Subject(
            id = "chemia",
            name = "Chemia",
            progress = 0.60f,
            color = Color(0xFFF47B20),
            backgroundColor = Color(0xFFFFF0E8),
            icon = Icons.Default.Science,
            topics = listOf(
                Topic(
                    id = "chemia_1",
                    name = "Atomy i cząsteczki",
                    description = "Budowa materii",
                    progress = 0.80f,
                    isLocked = false,
                    color = Color(0xFFF47B20),
                    icon = Icons.Default.Science,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_1_1",
                            name = "Budowa atomu",
                            description = "Proton, neutron, elektron",
                            progress = 1.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Science
                        ),
                        Lesson(
                            id = "chemia_1_2",
                            name = "Wskazywanie atomów w układzie okresowym",
                            description = "Kowalencyjne i jonowe",
                            progress = 0.60f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Bolt
                        ),
                        Lesson(
                            id = "chemia_1_4",
                            name = "Elektrony w atomach",
                            description = "Ile elektronów ma dany pierwiastek?",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Science
                        ),
                        Lesson(
                            id = "chemia_1_3",
                            name = "Cząsteczki i wzory",
                            description = "Wzory sumaryczne i strukturalne",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Science
                        )
                    )
                ),
                Topic(
                    id = "chemia_2",
                    name = "Układ okresowy",
                    description = "Pierwiastki chemiczne",
                    progress = 0.45f,
                    isLocked = false,
                    color = Color(0xFF4A80F0),
                    icon = Icons.Default.TableChart,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_2_1",
                            name = "Grupy i okresy",
                            description = "Budowa układu okresowego",
                            progress = 0.45f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.TableChart
                        ),
                        Lesson(
                            id = "chemia_2_2",
                            name = "Właściwości pierwiastków",
                            description = "Metale i niemetale",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.TableChart
                        )
                    )
                ),
                Topic(
                    id = "chemia_3",
                    name = "Związki nieorganiczne",
                    description = "Kwasy, zasady, sole i tlenki",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Science,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_3_1",
                            name = "Wzory kwasów",
                            description = "Rozpoznaj wzory kwasów beztlenowych i tlenowych",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Science
                        ),
                        Lesson(
                            id = "chemia_3_2",
                            name = "Równania reakcji",
                            description = "Bilansuj reakcje kwasów, metali, spalania i rozkładu",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Bolt
                        ),
                        Lesson(
                            id = "chemia_3_3",
                            name = "Wodorotlenki",
                            description = "Wzory zasad i reakcje otrzymywania",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Science
                        ),
                        Lesson(
                            id = "chemia_3_4",
                            name = "Sole",
                            description = "Wzory i nazwy soli, powiązanie z kwasami",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Science
                        )
                    )
                ),
                Topic(
                    id = "chemia_4",
                    name = "Kwasy i zasady",
                    description = "pH i dysocjacja",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Science,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_4_1",
                            name = "Skala pH",
                            description = "Odczyn kwasowy, zasadowy i obojętny",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.BarChart
                        ),
                        Lesson(
                            id = "chemia_4_2",
                            name = "Dysocjacja elektrolityczna",
                            description = "Jony kwasów, zasad i soli",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Bolt
                        )
                    )
                ),
                Topic(
                    id = "chemia_6",
                    name = "Tlenki",
                    description = "Tlenki zasadowe, kwasowe i amfoteryczne",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF7C4DFF),
                    icon = Icons.Default.Science,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_6_1",
                            name = "Rodzaje tlenków",
                            description = "Wzory, nazwy i klasyfikacja tlenków + równania tworzenia",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.Default.Science
                        )
                    )
                ),
                Topic(
                    id = "chemia_5",
                    name = "Chemia organiczna",
                    description = "Węglowodory i pochodne",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFF47B20),
                    icon = Icons.Default.Science,
                    lessons = listOf(
                        Lesson(
                            id = "chemia_5_1",
                            name = "Węglowodory",
                            description = "Alkany, alkeny i alkiny",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Science
                        ),
                        Lesson(
                            id = "chemia_5_2",
                            name = "Pochodne węglowodorów",
                            description = "Alkohole, kwasy karboksylowe i estry",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.Default.Science
                        )
                    )
                )
            )
        ),

        /**
         * **Algebra Subject**
         *
         * Focused on advanced algebraic calculations and analysis:
         * - Przekształcenia wyrażeń (Expression Transformations, Derivatives, Integrals)
         * - Równania liniowe i kwadratowe (Linear and Quadratic Equations)
         */
        Subject(
            id = "algebra",
            name = "Algebra",
            progress = 0.0f,
            color = Color(0xFF7C4DFF),
            backgroundColor = Color(0xFFF0EBFF),
            icon = Icons.Default.Functions,
            topics = listOf(
                Topic(
                    id = "algebra_1",
                    name = "Przekształcenia wyrażeń",
                    description = "Upraszczanie, pochodne, całki",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF7C4DFF),
                    icon = Icons.Default.Functions,
                    lessons = listOf(
                        Lesson(
                            id = "algebra_1_1",
                            name = "Upraszczanie wyrażeń",
                            description = "Rozwiń i uprość wyrażenia algebraiczne",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.Default.Functions
                        ),
                        Lesson(
                            id = "algebra_1_2",
                            name = "Pochodne",
                            description = "Oblicz pochodną funkcji wielomianowej",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        ),
                        Lesson(
                            id = "algebra_1_3",
                            name = "Całki nieoznaczone",
                            description = "Oblicz całkę nieoznaczoną wielomianu",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        )
                    )
                ),
                Topic(
                    id = "algebra_2",
                    name = "Równania",
                    description = "Liniowe i kwadratowe",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFF47B20),
                    icon = Icons.Default.Calculate,
                    lessons = listOf(
                        Lesson(
                            id = "algebra_2_1",
                            name = "Równania liniowe",
                            description = "Znajdź x w równaniu liniowym",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Calculate
                        ),
                        Lesson(
                            id = "algebra_2_2",
                            name = "Równania kwadratowe",
                            description = "Wyznacz pierwiastki równania kwadratowego",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Calculate
                        )
                    )
                )
            )
        ),

        /**
         * **Geography Subject**
         *
         * Covers physical and political geography topics:
         * - Kontynenty i oceany (Continents and Oceans)
         * - Klimat i pogoda (Climate and Weather)
         * - Ludność świata (World Population)
         * - Kraje i stolice (Countries and Capitals map quizzes)
         */
        Subject(
            id = "geografia",
            name = "Geografia",
            progress = 0.35f,
            color = Color(0xFF3DBD7D),
            backgroundColor = Color(0xFFE8F8F0),
            icon = Icons.Default.Public,
            topics = listOf(
                Topic(
                    id = "geo_1",
                    name = "Kontynenty i oceany",
                    description = "Podstawy geografii fizycznej",
                    progress = 0.70f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Public,
                    lessons = listOf(
                        Lesson(
                            id = "geo_1_1",
                            name = "Lądy i oceany świata",
                            description = "Rozmieszczenie kontynentów",
                            progress = 0.70f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Public
                        ),
                        Lesson(
                            id = "geo_1_2",
                            name = "Ukształtowanie terenu",
                            description = "Góry, niziny, wyżyny",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Landscape
                        )
                    )
                ),
                Topic(
                    id = "geo_2",
                    name = "Klimat i pogoda",
                    description = "Strefy klimatyczne",
                    progress = 0.30f,
                    isLocked = true,
                    color = Color(0xFF4A80F0),
                    icon = Icons.Default.Cloud
                ),
                Topic(
                    id = "geo_3",
                    name = "Ludność świata",
                    description = "Demografia i migracje",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFFF47B20),
                    icon = Icons.Default.Public
                ),
                Topic(
                    id = "geo_4",
                    name = "Kraje i stolice",
                    description = "Wskazuj kraje na mapie",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Map,
                    lessons = listOf(
                        Lesson(
                            id = "geo_4_1",
                            name = "Sąsiedzi Polski",
                            description = "7 krajów graniczących z Polską",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Place
                        ),
                        Lesson(
                            id = "geo_4_2",
                            name = "Kraje Azji",
                            description = "Największe i najważniejsze kraje Azji",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF4A80F0),
                            icon = Icons.Default.Public
                        ),
                        Lesson(
                            id = "geo_4_3",
                            name = "Stolice Europy",
                            description = "Wskaż kraj po nazwie stolicy",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFFF47B20),
                            icon = Icons.Default.Explore
                        ),
                        Lesson(
                            id = "geo_4_4",
                            name = "Województwa Polski",
                            description = "Zaznacz województwa na mapie Polski",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF959955),
                            icon = Icons.Default.Map
                        )
                    )
                )
            )
        )
    )

    /**
     * Finds a [Subject] by its unique string [id].
     *
     * @param id The subject identifier (e.g. `"matematyka"`).
     * @return The matching [Subject], or `null` if no subject has that id.
     */
    fun getById(id: String): Subject? =
        subjects.find { it.id == id }

    /**
     * Finds a [Topic] by its parent subject id and topic id.
     *
     * @param subjectId The subject identifier (e.g. `"chemia"`).
     * @param topicId   The topic identifier (e.g. `"chemia_3"`).
     * @return The matching [Topic], or `null` if not found.
     */
    fun getTopicById(subjectId: String, topicId: String): Topic? =
        getById(subjectId)?.topics?.find { it.id == topicId }
}
