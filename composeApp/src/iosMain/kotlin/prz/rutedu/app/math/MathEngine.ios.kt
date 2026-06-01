package prz.rutedu.app.math

actual val mathEngineAvailable: Boolean = false

actual class MathEngine actual constructor() {
    actual val isAvailable: Boolean = false
    actual fun areEquivalent(userExpr: String, correctExpr: String): Boolean? = null
    actual fun simplify(expr: String): String? = null
    actual fun differentiate(expr: String, variable: String): String? = null
    actual fun integrate(expr: String, variable: String): String? = null
    actual fun solve(equation: String, variable: String): String? = null
}
