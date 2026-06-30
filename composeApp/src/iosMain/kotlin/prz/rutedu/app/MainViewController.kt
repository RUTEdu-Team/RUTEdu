package prz.rutedu.app

import androidx.compose.ui.window.ComposeUIViewController
import prz.rutedu.app.database.DriverFactory

/**
 * iOS entry point for the app.
 *
 * Creates the platform [DriverFactory] and passes it to [App], which manages the driver
 * lifecycle internally - including showing [DatabaseErrorScreen] on open failure.
 */
fun MainViewController() = ComposeUIViewController {
    App(DriverFactory())
}