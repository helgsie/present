package `is`.hi.present.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import `is`.hi.present.core.theme.PresentTheme
import `is`.hi.present.app.navigation.AppNavGraphNav3

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PresentTheme {
                AppNavGraphNav3()
            }
        }
    }
}