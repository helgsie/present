package `is`.hi.present

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import `is`.hi.present.navigation.AppNavGraphNav3
import `is`.hi.present.ui.theme.PresentTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkToken by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /*deepLinkToken = extractToken(intent)
        * (startJoinToken = deepLinkToken)*/

        setContent {
            PresentTheme {
                AppNavGraphNav3()
            }
        }
    }

    /*override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkToken = extractToken(intent)
    }

    private fun extractToken(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (data.scheme != "https") return null
        if (data.host != "benevolent-blini-5869c9.netlify.app") return null
        if (data.path != "/join") return null
        return data.getQueryParameter("token")
    }*/
}