# Keep all Symja / MathEclipse classes (uses heavy reflection internally)
-keep class org.matheclipse.** { *; }
-dontwarn org.matheclipse.**

# Keep ANTLR runtime (Symja uses it for expression parsing)
-keep class org.antlr.** { *; }
-dontwarn org.antlr.**

# Keep Apache Commons Math / Hipparchus (Symja numerical backends)
-keep class org.apache.commons.math3.** { *; }
-dontwarn org.apache.commons.math3.**
-keep class org.hipparchus.** { *; }
-dontwarn org.hipparchus.**

# Keep Apache Commons Logging (pulled in transitively)
-dontwarn org.apache.commons.logging.**

# Compose + Material3 — keep lambdas used in recomposition
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# SQLDelight
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**
