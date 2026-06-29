package prz.rutedu.app

import kotlin.test.Test
import kotlin.test.assertEquals
import prz.rutedu.app.locale.GeneratorLocalizer

/**
 * Common test suite containing example and base assertions for unit testing.
 *
 * Use this class as a starting point to write business logic tests that run
 * on both Android and iOS targets without platform-specific configurations.
 */
class ComposeAppCommonTest {

    /**
     * Placeholder example test to verify the testing environment.
     *
     * Ensures that standard Kotlin test assertions work correctly within the KMP setup.
     */
    @Test
    fun testGeneratorLocalizerFallback() {
        // Since we are not in a full Compose app context, initialize might fail if resources aren't packed for unit tests,
        // but we can test that t(key) returns the key itself as a fallback if not initialized.
        val result = GeneratorLocalizer.t("Non-existent key")
        assertEquals("Non-existent key", result)
    }

    @Test
    fun testGeneratorLocalizerFormatting() {
        val formatted = GeneratorLocalizer.t("Test {param}", "param" to "value")
        assertEquals("Test value", formatted)
    }
}