package `is`.hi.present.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.auth.AuthViewModel

@Composable
fun LoginTest(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome! You are logged in.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.signOut(context) {
                onLogout()
            }
        }
        ) {
            Text("Sign Out")
        }
    }
}
