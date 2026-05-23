package prz.rutedu.app.math

/**
 * Compile-time flag: true on Android (symja available), false on iOS (stub).
 * Use this instead of constructing MathEngine() just to check availability —
 * avoids triggering the slow ExprEvaluator initialization on the main thread.
 */
expect val mathEngineAvailable: Boolean

/**
 * Platform-specific computer algebra engine wrapping symja on Android.
 *
 * Android actual: wraps org.matheclipse.core.eval.ExprEvaluator.
 * iOS actual: stub — all methods return null, isAvailable = false.
 *
 * Thread safety: ExprEvaluator is NOT thread-safe. Always construct a new MathEngine()
 * per call site and never share instances across coroutines. Construction cost:
 * ~500–2000 ms on first use (class loading). Always call from Dispatchers.Default.
 */
expect class MathEngine() {

    /** false on iOS (stub); true on Android after successful initialization. */
    val isAvailable: Boolean

    /**
     * Checks whether [userExpr] is mathematically equivalent to [correctExpr].
     * Uses Simplify(userExpr - correctExpr) == 0.
     * Returns null on error or when the platform stub is active.
     */
    fun areEquivalent(userExpr: String, correctExpr: String): Boolean?

    /**
     * Returns the canonical simplified form of [expr], or null on failure.
     * Example: "x^2+2*x+1" -> "(1+x)^2"
     */
    fun simplify(expr: String): String?

    /**
     * Differentiates [expr] with respect to [variable].
     * Example: expr="x^3", variable="x" -> "3*x^2"
     */
    fun differentiate(expr: String, variable: String = "x"): String?

    /**
     * Indefinite integral of [expr] with respect to [variable].
     * Example: expr="2*x", variable="x" -> "x^2"
     */
    fun integrate(expr: String, variable: String = "x"): String?

    /**
     * Solves [equation] for [variable]. Returns raw Symja result string or null.
     * Example: equation="2*x+4==0", variable="x" -> "{{x->-2}}"
     */
    fun solve(equation: String, variable: String = "x"): String?
}
