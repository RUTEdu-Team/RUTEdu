package prz.rutedu.app.locale

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.models.ELEMENTS
import prz.rutedu.app.data.QuestionBank

object GeneratorLocalizer {
    private var mathTranslations: Map<String, String> = emptyMap()
    private var chemTranslations: Map<String, String> = emptyMap()
    private var geoTranslations: Map<String, String> = emptyMap()
    private val elementNames = mutableMapOf<Int, String>()

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initialize() {
        loadForLanguage(getCurrentLanguage())
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadForLanguage(lang: String) {
        val resolvedLang = if (lang in QuestionBank.SUPPORTED_QUESTION_LANGS) lang else "en"
        
        mathTranslations = loadSubjectTranslations("math", resolvedLang)
        chemTranslations = loadSubjectTranslations("chem", resolvedLang)
        geoTranslations = loadSubjectTranslations("geo", resolvedLang)
        
        // Cache element names for the current language
        try {
            loadElementNames()
        } catch (_: Exception) {}
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadSubjectTranslations(subject: String, lang: String): Map<String, String> {
        return try {
            val bytes = Res.readBytes("files/generator/$subject/translations_$lang.json")
            val jsonText = bytes.decodeToString()
            Json.decodeFromString(jsonText)
        } catch (e: Exception) {
            if (lang != "en") {
                try {
                    val bytes = Res.readBytes("files/generator/$subject/translations_en.json")
                    val jsonText = bytes.decodeToString()
                    Json.decodeFromString(jsonText)
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        }
    }

    private suspend fun loadElementNames() {
        for (z in 1..118) {
            val res = getElementNameRes(z)
            if (res != Res.string.empty) {
                elementNames[z] = getString(res)
            }
        }
    }

    fun t(key: String): String {
        return mathTranslations[key] ?: chemTranslations[key] ?: geoTranslations[key] ?: key
    }

    fun t(key: String, vararg args: Pair<String, String>): String {
        var text = t(key)
        for ((placeholder, value) in args) {
            text = text.replace("{$placeholder}", value)
        }
        return text
    }

    fun getElementName(atomicNumber: Int): String {
        return elementNames[atomicNumber] ?: ELEMENTS.firstOrNull { it.atomicNumber == atomicNumber }?.name ?: ""
    }

    private fun getElementNameRes(atomicNumber: Int): StringResource {
        return when (atomicNumber) {
            1 -> Res.string.element_1
            2 -> Res.string.element_2
            3 -> Res.string.element_3
            4 -> Res.string.element_4
            5 -> Res.string.element_5
            6 -> Res.string.element_6
            7 -> Res.string.element_7
            8 -> Res.string.element_8
            9 -> Res.string.element_9
            10 -> Res.string.element_10
            11 -> Res.string.element_11
            12 -> Res.string.element_12
            13 -> Res.string.element_13
            14 -> Res.string.element_14
            15 -> Res.string.element_15
            16 -> Res.string.element_16
            17 -> Res.string.element_17
            18 -> Res.string.element_18
            19 -> Res.string.element_19
            20 -> Res.string.element_20
            21 -> Res.string.element_21
            22 -> Res.string.element_22
            23 -> Res.string.element_23
            24 -> Res.string.element_24
            25 -> Res.string.element_25
            26 -> Res.string.element_26
            27 -> Res.string.element_27
            28 -> Res.string.element_28
            29 -> Res.string.element_29
            30 -> Res.string.element_30
            31 -> Res.string.element_31
            32 -> Res.string.element_32
            33 -> Res.string.element_33
            34 -> Res.string.element_34
            35 -> Res.string.element_35
            36 -> Res.string.element_36
            37 -> Res.string.element_37
            38 -> Res.string.element_38
            39 -> Res.string.element_39
            40 -> Res.string.element_40
            41 -> Res.string.element_41
            42 -> Res.string.element_42
            43 -> Res.string.element_43
            44 -> Res.string.element_44
            45 -> Res.string.element_45
            46 -> Res.string.element_46
            47 -> Res.string.element_47
            48 -> Res.string.element_48
            49 -> Res.string.element_49
            50 -> Res.string.element_50
            51 -> Res.string.element_51
            52 -> Res.string.element_52
            53 -> Res.string.element_53
            54 -> Res.string.element_54
            55 -> Res.string.element_55
            56 -> Res.string.element_56
            57 -> Res.string.element_57
            58 -> Res.string.element_58
            59 -> Res.string.element_59
            60 -> Res.string.element_60
            61 -> Res.string.element_61
            62 -> Res.string.element_62
            63 -> Res.string.element_63
            64 -> Res.string.element_64
            65 -> Res.string.element_65
            66 -> Res.string.element_66
            67 -> Res.string.element_67
            68 -> Res.string.element_68
            69 -> Res.string.element_69
            70 -> Res.string.element_70
            71 -> Res.string.element_71
            72 -> Res.string.element_72
            73 -> Res.string.element_73
            74 -> Res.string.element_74
            75 -> Res.string.element_75
            76 -> Res.string.element_76
            77 -> Res.string.element_77
            78 -> Res.string.element_78
            79 -> Res.string.element_79
            80 -> Res.string.element_80
            81 -> Res.string.element_81
            82 -> Res.string.element_82
            83 -> Res.string.element_83
            84 -> Res.string.element_84
            85 -> Res.string.element_85
            86 -> Res.string.element_86
            87 -> Res.string.element_87
            88 -> Res.string.element_88
            89 -> Res.string.element_89
            90 -> Res.string.element_90
            91 -> Res.string.element_91
            92 -> Res.string.element_92
            93 -> Res.string.element_93
            94 -> Res.string.element_94
            95 -> Res.string.element_95
            96 -> Res.string.element_96
            97 -> Res.string.element_97
            98 -> Res.string.element_98
            99 -> Res.string.element_99
            100 -> Res.string.element_100
            101 -> Res.string.element_101
            102 -> Res.string.element_102
            103 -> Res.string.element_103
            104 -> Res.string.element_104
            105 -> Res.string.element_105
            106 -> Res.string.element_106
            107 -> Res.string.element_107
            108 -> Res.string.element_108
            109 -> Res.string.element_109
            110 -> Res.string.element_110
            111 -> Res.string.element_111
            112 -> Res.string.element_112
            113 -> Res.string.element_113
            114 -> Res.string.element_114
            115 -> Res.string.element_115
            116 -> Res.string.element_116
            117 -> Res.string.element_117
            118 -> Res.string.element_118
            else -> Res.string.empty
        }
    }
}
