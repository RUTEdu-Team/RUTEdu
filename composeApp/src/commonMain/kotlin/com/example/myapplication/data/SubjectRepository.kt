package com.example.myapplication.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import com.example.myapplication.models.Lesson
import com.example.myapplication.models.Subject
import com.example.myapplication.models.Topic

/**
 * ════════════════════════════════════════════════════════
 *  HOW TO ADD DATA
 * ════════════════════════════════════════════════════════
 *
 *  ▸ New SUBJECT  → add Subject(...) to [subjects] list
 *  ▸ New TOPIC    → add Topic(...) to its Subject's `topics` list
 *  ▸ New LESSON   → add Lesson(...) to its Topic's `lessons` list
 *
 *  All IDs must be unique (used for navigation).
 *  Icons: Icons.Default.<Name> from materialIconsExtended.
 *  Colors: Color(0xFFRRGGBB) — full opacity hex.
 *  progress: Float from 0f (not started) to 1f (100% done).
 *  isLocked: true → grayed-out card, not clickable.
 * ════════════════════════════════════════════════════════
 */
object SubjectRepository {

    val subjects: List<Subject> = listOf(

        // ── MATEMATYKA ────────────────────────────────────
        Subject(
            id = "matematyka",
            name = "Matematyka",
            lessonCount = 10,
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
                            icon = Icons.Default.TrendingUp
                        ),
                        Lesson(
                            id = "mat_1_4",
                            name = "Pierwiastkowanie",
                            description = "Własności pierwiastków",
                            progress = 0.0f,
                            isLocked = true,
                            color = Color(0xFF9E9E9E),
                            icon = Icons.Default.Lock
                        ),
                        Lesson(
                            id = "mat_1_5",
                            name = "Logarytmy",
                            description = "Wprowadzenie do logarytmów",
                            progress = 0.0f,
                            isLocked = true,
                            color = Color(0xFF9E9E9E),
                            icon = Icons.Default.Lock
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
                            isLocked = true,
                            color = Color(0xFF9E9E9E),
                            icon = Icons.Default.Lock
                        )
                    )
                ),
                Topic(
                    id = "mat_3",
                    name = "Równania i nierówności",
                    description = "Metody rozwiązywania układów",
                    progress = 0.10f,
                    isLocked = true,
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
                            isLocked = true,
                            color = Color(0xFF9E9E9E),
                            icon = Icons.Default.Lock
                        )
                    )
                ),
                Topic(
                    id = "mat_4",
                    name = "Funkcje",
                    description = "Wykresy i własności",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF7C4DFF),
                    icon = Icons.Default.TrendingUp,
                    lessons = listOf(
                        Lesson(
                            id = "mat_4_1",
                            name = "Funkcja kwadratowa",
                            description = "Parabola — wykres i obliczanie wartości",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF7C4DFF),
                            icon = Icons.Default.TrendingUp
                        )
                    )
                ),
                Topic(
                    id = "mat_5",
                    name = "Geometria płaska",
                    description = "Trójkąty i okręgi",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.BarChart,
                    lessons = listOf(
                        Lesson(
                            id = "mat_5_1",
                            name = "Kąty w trójkącie",
                            description = "Suma kątów wewnętrznych trójkąta",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.BarChart
                        )
                    )
                )
            )
        ),

        // ── CHEMIA ────────────────────────────────────────
        Subject(
            id = "chemia",
            name = "Chemia",
            lessonCount = 15,
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

        // ── GEOGRAFIA ─────────────────────────────────────
        Subject(
            id = "geografia",
            name = "Geografia",
            lessonCount = 8,
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
                            isLocked = true,
                            color = Color(0xFF9E9E9E),
                            icon = Icons.Default.Lock
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
                    isLocked = true,
                    color = Color(0xFF9E9E9E),
                    icon = Icons.Default.Lock
                ),
                Topic(
                    id = "geo_4",
                    name = "Kraje i stolice",
                    description = "Wskazuj kraje na mapie",
                    progress = 0.0f,
                    isLocked = false,
                    color = Color(0xFF3DBD7D),
                    icon = Icons.Default.Public,
                    lessons = listOf(
                        Lesson(
                            id = "geo_4_1",
                            name = "Sąsiedzi Polski",
                            description = "7 krajów graniczących z Polską",
                            progress = 0.0f,
                            isLocked = false,
                            color = Color(0xFF3DBD7D),
                            icon = Icons.Default.Public
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
                            icon = Icons.Default.Public
                        )
                    )
                )
            )
        )
    )

    fun getById(id: String): Subject? =
        subjects.find { it.id == id }

    fun getTopicById(subjectId: String, topicId: String): Topic? =
        getById(subjectId)?.topics?.find { it.id == topicId }
}
