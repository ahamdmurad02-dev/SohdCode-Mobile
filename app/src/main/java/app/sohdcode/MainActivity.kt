package app.sohdcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.sohdcode.ui.SohdCodeRoot
import app.sohdcode.ui.theme.SohdCodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SohdCodeApp).container
        setContent {
            SohdCodeTheme {
                SohdCodeRoot(container = container)
            }
        }
    }
}
