package prz.rutedu.app.math

import org.matheclipse.core.eval.ExprEvaluator

actual val mathEngineAvailable: Boolean = true

actual class MathEngine actual constructor() {

    // One ExprEvaluator per instance — not thread-safe, never share across coroutines.
    private val evaluator: ExprEvaluator? = try {
        ExprEvaluator(false, 100)
    } catch (e: Throwable) {
        null
    }

    actual val isAvailable: Boolean get() = evaluator != null

    actual fun areEquivalent(userExpr: String, correctExpr: String): Boolean? {
        val eval = evaluator ?: return null
        return try {
            val result = eval.eval("Simplify(($userExpr)-($correctExpr))")
            result.toString() == "0"
        } catch (e: Throwable) {
            null
        }
    }

    actual fun simplify(expr: String): String? {
        val eval = evaluator ?: return null
        return try {
            eval.eval("Simplify($expr)").toString()
        } catch (e: Throwable) {
            null
        }
    }

    actual fun differentiate(expr: String, variable: String): String? {
        val eval = evaluator ?: return null
        return try {
            eval.eval("D[$expr,$variable]").toString()
        } catch (e: Throwable) {
            null
        }
    }

    actual fun integrate(expr: String, variable: String): String? {
        val eval = evaluator ?: return null
        return try {
            eval.eval("Integrate[$expr,$variable]").toString()
        } catch (e: Throwable) {
            null
        }
    }

    actual fun solve(equation: String, variable: String): String? {
        val eval = evaluator ?: return null
        return try {
            eval.eval("Solve[$equation,$variable]").toString()
        } catch (e: Throwable) {
            null
        }
    }
}
