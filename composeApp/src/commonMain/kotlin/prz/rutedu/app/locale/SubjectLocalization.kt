package prz.rutedu.app.locale

import org.jetbrains.compose.resources.StringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.models.Subject
import prz.rutedu.app.models.Topic
import prz.rutedu.app.models.Lesson

/**
 * Resolves the string resource associated with this subject by its unique id.
 */
fun Subject.getNameRes(): StringResource {
    return when (id) {
        "matematyka" -> Res.string.subject_matematyka
        "chemia"     -> Res.string.subject_chemia
        "algebra"    -> Res.string.subject_algebra
        "geografia"  -> Res.string.subject_geografia
        else         -> Res.string.empty
    }
}

/**
 * Resolves the string resource associated with this topic's name by its unique id.
 */
fun Topic.getNameRes(): StringResource {
    return when (id) {
        "mat_1"     -> Res.string.topic_mat_1
        "mat_2"     -> Res.string.topic_mat_2
        "mat_3"     -> Res.string.topic_mat_3
        "mat_4"     -> Res.string.topic_mat_4
        "mat_5"     -> Res.string.topic_mat_5
        "mat_6"     -> Res.string.topic_mat_6
        "mat_7"     -> Res.string.topic_mat_7
        "mat_8"     -> Res.string.topic_mat_8
        "mat_9"     -> Res.string.topic_mat_9
        "mat_10"    -> Res.string.topic_mat_10
        "mat_11"    -> Res.string.topic_mat_11
        "mat_12"    -> Res.string.topic_mat_12
        "chemia_1"  -> Res.string.topic_chemia_1
        "chemia_2"  -> Res.string.topic_chemia_2
        "chemia_3"  -> Res.string.topic_chemia_3
        "chemia_4"  -> Res.string.topic_chemia_4
        "chemia_5"  -> Res.string.topic_chemia_5
        "chemia_6"  -> Res.string.topic_chemia_6
        "algebra_1" -> Res.string.topic_algebra_1
        "algebra_2" -> Res.string.topic_algebra_2
        "geo_1"     -> Res.string.topic_geo_1
        "geo_2"     -> Res.string.topic_geo_2
        "geo_3"     -> Res.string.topic_geo_3
        "geo_4"     -> Res.string.topic_geo_4
        else        -> Res.string.empty
    }
}

/**
 * Resolves the string resource associated with this topic's description by its unique id.
 */
fun Topic.getDescriptionRes(): StringResource {
    return when (id) {
        "mat_1"     -> Res.string.topic_desc_mat_1
        "mat_2"     -> Res.string.topic_desc_mat_2
        "mat_3"     -> Res.string.topic_desc_mat_3
        "mat_4"     -> Res.string.topic_desc_mat_4
        "mat_5"     -> Res.string.topic_desc_mat_5
        "mat_6"     -> Res.string.topic_desc_mat_6
        "mat_7"     -> Res.string.topic_desc_mat_7
        "mat_8"     -> Res.string.topic_desc_mat_8
        "mat_9"     -> Res.string.topic_desc_mat_9
        "mat_10"    -> Res.string.topic_desc_mat_10
        "mat_11"    -> Res.string.topic_desc_mat_11
        "mat_12"    -> Res.string.topic_desc_mat_12
        "chemia_1"  -> Res.string.topic_desc_chemia_1
        "chemia_2"  -> Res.string.topic_desc_chemia_2
        "chemia_3"  -> Res.string.topic_desc_chemia_3
        "chemia_4"  -> Res.string.topic_desc_chemia_4
        "chemia_5"  -> Res.string.topic_desc_chemia_5
        "chemia_6"  -> Res.string.topic_desc_chemia_6
        "algebra_1" -> Res.string.topic_desc_algebra_1
        "algebra_2" -> Res.string.topic_desc_algebra_2
        "geo_1"     -> Res.string.topic_desc_geo_1
        "geo_2"     -> Res.string.topic_desc_geo_2
        "geo_3"     -> Res.string.topic_desc_geo_3
        "geo_4"     -> Res.string.topic_desc_geo_4
        else        -> Res.string.empty
    }
}

/**
 * Resolves the string resource associated with this lesson's name by its unique id.
 */
fun Lesson.getNameRes(): StringResource {
    return when (id) {
        "mat_1_1"    -> Res.string.lesson_mat_1_1
        "mat_1_2"    -> Res.string.lesson_mat_1_2
        "mat_1_3"    -> Res.string.lesson_mat_1_3
        "mat_1_4"    -> Res.string.lesson_mat_1_4
        "mat_1_5"    -> Res.string.lesson_mat_1_5
        "mat_2_1"    -> Res.string.lesson_mat_2_1
        "mat_2_2"    -> Res.string.lesson_mat_2_2
        "mat_2_3"    -> Res.string.lesson_mat_2_3
        "mat_3_1"    -> Res.string.lesson_mat_3_1
        "mat_3_2"    -> Res.string.lesson_mat_3_2
        "mat_3_3"    -> Res.string.lesson_mat_3_3
        "mat_4_1"    -> Res.string.lesson_mat_4_1
        "mat_4_2"    -> Res.string.lesson_mat_4_2
        "mat_4_3"    -> Res.string.lesson_mat_4_3
        "mat_4_4"    -> Res.string.lesson_mat_4_4
        "mat_5_1"    -> Res.string.lesson_mat_5_1
        "mat_5_2"    -> Res.string.lesson_mat_5_2
        "mat_5_3"    -> Res.string.lesson_mat_5_3
        "mat_5_4"    -> Res.string.lesson_mat_5_4
        "mat_5_5"    -> Res.string.lesson_mat_5_5
        "mat_6_1"    -> Res.string.lesson_mat_6_1
        "mat_6_2"    -> Res.string.lesson_mat_6_2
        "mat_6_3"    -> Res.string.lesson_mat_6_3
        "mat_6_4"    -> Res.string.lesson_mat_6_4
        "mat_7_1"    -> Res.string.lesson_mat_7_1
        "mat_7_2"    -> Res.string.lesson_mat_7_2
        "mat_7_3"    -> Res.string.lesson_mat_7_3
        "mat_8_1"    -> Res.string.lesson_mat_8_1
        "mat_8_2"    -> Res.string.lesson_mat_8_2
        "mat_8_3"    -> Res.string.lesson_mat_8_3
        "mat_9_1"    -> Res.string.lesson_mat_9_1
        "mat_9_2"    -> Res.string.lesson_mat_9_2
        "mat_9_3"    -> Res.string.lesson_mat_9_3
        "mat_9_4"    -> Res.string.lesson_mat_9_4
        "mat_9_5"    -> Res.string.lesson_mat_9_5
        "mat_10_1"   -> Res.string.lesson_mat_10_1
        "mat_10_2"   -> Res.string.lesson_mat_10_2
        "mat_10_3"   -> Res.string.lesson_mat_10_3
        "mat_10_4"   -> Res.string.lesson_mat_10_4
        "mat_11_1"   -> Res.string.lesson_mat_11_1
        "mat_11_2"   -> Res.string.lesson_mat_11_2
        "mat_11_3"   -> Res.string.lesson_mat_11_3
        "mat_11_4"   -> Res.string.lesson_mat_11_4
        "mat_12_1"   -> Res.string.lesson_mat_12_1
        "mat_12_2"   -> Res.string.lesson_mat_12_2
        "mat_12_3"   -> Res.string.lesson_mat_12_3
        "chemia_1_1" -> Res.string.lesson_chemia_1_1
        "chemia_1_2" -> Res.string.lesson_chemia_1_2
        "chemia_1_3" -> Res.string.lesson_chemia_1_3
        "chemia_1_4" -> Res.string.lesson_chemia_1_4
        "chemia_2_1" -> Res.string.lesson_chemia_2_1
        "chemia_2_2" -> Res.string.lesson_chemia_2_2
        "chemia_3_1" -> Res.string.lesson_chemia_3_1
        "chemia_3_2" -> Res.string.lesson_chemia_3_2
        "chemia_3_3" -> Res.string.lesson_chemia_3_3
        "chemia_3_4" -> Res.string.lesson_chemia_3_4
        "chemia_4_1" -> Res.string.lesson_chemia_4_1
        "chemia_4_2" -> Res.string.lesson_chemia_4_2
        "chemia_5_1" -> Res.string.lesson_chemia_5_1
        "chemia_5_2" -> Res.string.lesson_chemia_5_2
        "chemia_6_1" -> Res.string.lesson_chemia_6_1
        "algebra_1_1"-> Res.string.lesson_algebra_1_1
        "algebra_1_2"-> Res.string.lesson_algebra_1_2
        "algebra_1_3"-> Res.string.lesson_algebra_1_3
        "algebra_2_1"-> Res.string.lesson_algebra_2_1
        "algebra_2_2"-> Res.string.lesson_algebra_2_2
        "geo_1_1"    -> Res.string.lesson_geo_1_1
        "geo_1_2"    -> Res.string.lesson_geo_1_2
        "geo_4_1"    -> Res.string.lesson_geo_4_1
        "geo_4_2"    -> Res.string.lesson_geo_4_2
        "geo_4_3"    -> Res.string.lesson_geo_4_3
        "geo_4_4"    -> Res.string.lesson_geo_4_4
        else         -> Res.string.empty
    }
}

/**
 * Resolves the string resource associated with this lesson's description by its unique id.
 */
fun Lesson.getDescriptionRes(): StringResource {
    return when (id) {
        "mat_1_1"    -> Res.string.lesson_desc_mat_1_1
        "mat_1_2"    -> Res.string.lesson_desc_mat_1_2
        "mat_1_3"    -> Res.string.lesson_desc_mat_1_3
        "mat_1_4"    -> Res.string.lesson_desc_mat_1_4
        "mat_1_5"    -> Res.string.lesson_desc_mat_1_5
        "mat_2_1"    -> Res.string.lesson_desc_mat_2_1
        "mat_2_2"    -> Res.string.lesson_desc_mat_2_2
        "mat_2_3"    -> Res.string.lesson_desc_mat_2_3
        "mat_3_1"    -> Res.string.lesson_desc_mat_3_1
        "mat_3_2"    -> Res.string.lesson_desc_mat_3_2
        "mat_3_3"    -> Res.string.lesson_desc_mat_3_3
        "mat_4_1"    -> Res.string.lesson_desc_mat_4_1
        "mat_4_2"    -> Res.string.lesson_desc_mat_4_2
        "mat_4_3"    -> Res.string.lesson_desc_mat_4_3
        "mat_4_4"    -> Res.string.lesson_desc_mat_4_4
        "mat_5_1"    -> Res.string.lesson_desc_mat_5_1
        "mat_5_2"    -> Res.string.lesson_desc_mat_5_2
        "mat_5_3"    -> Res.string.lesson_desc_mat_5_3
        "mat_5_4"    -> Res.string.lesson_desc_mat_5_4
        "mat_5_5"    -> Res.string.lesson_desc_mat_5_5
        "mat_6_1"    -> Res.string.lesson_desc_mat_6_1
        "mat_6_2"    -> Res.string.lesson_desc_mat_6_2
        "mat_6_3"    -> Res.string.lesson_desc_mat_6_3
        "mat_6_4"    -> Res.string.lesson_desc_mat_6_4
        "mat_7_1"    -> Res.string.lesson_desc_mat_7_1
        "mat_7_2"    -> Res.string.lesson_desc_mat_7_2
        "mat_7_3"    -> Res.string.lesson_desc_mat_7_3
        "mat_8_1"    -> Res.string.lesson_desc_mat_8_1
        "mat_8_2"    -> Res.string.lesson_desc_mat_8_2
        "mat_8_3"    -> Res.string.lesson_desc_mat_8_3
        "mat_9_1"    -> Res.string.lesson_desc_mat_9_1
        "mat_9_2"    -> Res.string.lesson_desc_mat_9_2
        "mat_9_3"    -> Res.string.lesson_desc_mat_9_3
        "mat_9_4"    -> Res.string.lesson_desc_mat_9_4
        "mat_9_5"    -> Res.string.lesson_desc_mat_9_5
        "mat_10_1"   -> Res.string.lesson_desc_mat_10_1
        "mat_10_2"   -> Res.string.lesson_desc_mat_10_2
        "mat_10_3"   -> Res.string.lesson_desc_mat_10_3
        "mat_10_4"   -> Res.string.lesson_desc_mat_10_4
        "mat_11_1"   -> Res.string.lesson_desc_mat_11_1
        "mat_11_2"   -> Res.string.lesson_desc_mat_11_2
        "mat_11_3"   -> Res.string.lesson_desc_mat_11_3
        "mat_11_4"   -> Res.string.lesson_desc_mat_11_4
        "mat_12_1"   -> Res.string.lesson_desc_mat_12_1
        "mat_12_2"   -> Res.string.lesson_desc_mat_12_2
        "mat_12_3"   -> Res.string.lesson_desc_mat_12_3
        "chemia_1_1" -> Res.string.lesson_desc_chemia_1_1
        "chemia_1_2" -> Res.string.lesson_desc_chemia_1_2
        "chemia_1_3" -> Res.string.lesson_desc_chemia_1_3
        "chemia_1_4" -> Res.string.lesson_desc_chemia_1_4
        "chemia_2_1" -> Res.string.lesson_desc_chemia_2_1
        "chemia_2_2" -> Res.string.lesson_desc_chemia_2_2
        "chemia_3_1" -> Res.string.lesson_desc_chemia_3_1
        "chemia_3_2" -> Res.string.lesson_desc_chemia_3_2
        "chemia_3_3" -> Res.string.lesson_desc_chemia_3_3
        "chemia_3_4" -> Res.string.lesson_desc_chemia_3_4
        "chemia_4_1" -> Res.string.lesson_desc_chemia_4_1
        "chemia_4_2" -> Res.string.lesson_desc_chemia_4_2
        "chemia_5_1" -> Res.string.lesson_desc_chemia_5_1
        "chemia_5_2" -> Res.string.lesson_desc_chemia_5_2
        "chemia_6_1" -> Res.string.lesson_desc_chemia_6_1
        "algebra_1_1"-> Res.string.lesson_desc_algebra_1_1
        "algebra_1_2"-> Res.string.lesson_desc_algebra_1_2
        "algebra_1_3"-> Res.string.lesson_desc_algebra_1_3
        "algebra_2_1"-> Res.string.lesson_desc_algebra_2_1
        "algebra_2_2"-> Res.string.lesson_desc_algebra_2_2
        "geo_1_1"    -> Res.string.lesson_desc_geo_1_1
        "geo_1_2"    -> Res.string.lesson_desc_geo_1_2
        "geo_4_1"    -> Res.string.lesson_desc_geo_4_1
        "geo_4_2"    -> Res.string.lesson_desc_geo_4_2
        "geo_4_3"    -> Res.string.lesson_desc_geo_4_3
        "geo_4_4"    -> Res.string.lesson_desc_geo_4_4
        else         -> Res.string.empty
    }
}
