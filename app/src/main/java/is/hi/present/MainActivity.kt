package `is`.hi.present

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `is`.hi.present.navigation.AppNavGraphNav3
import `is`.hi.present.ui.theme.PresentTheme

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