package app.sohdcode.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sohdcode.ui.components.PrimaryAction
import app.sohdcode.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    email: String,
    userId: String,
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(title = "Account") {
                Text(email.ifBlank { "Signed in" }, style = MaterialTheme.typography.titleMedium)
                Text("User ID", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(userId.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall)
                PrimaryAction("Open settings", onClick = onSettings)
                PrimaryAction("Sign out", onClick = onSignOut)
            }
        }
    }
}
