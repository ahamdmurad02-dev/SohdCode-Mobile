package app.sohdcode.data.auth

import app.sohdcode.core.mapAuthError
import app.sohdcode.domain.model.AuthStatus
import app.sohdcode.domain.model.UserAccount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthRepository(private val client: SupabaseClient) {

    val status: Flow<AuthStatus> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthStatus.Checking
            is SessionStatus.Authenticated -> AuthStatus.SignedIn
            is SessionStatus.NotAuthenticated -> AuthStatus.SignedOut
            else -> AuthStatus.Checking
        }
    }

    fun currentUser(): UserAccount? {
        val user = client.auth.currentUserOrNull() ?: return null
        return UserAccount(id = user.id, email = user.email.orEmpty())
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }.recoverCatching { throw IllegalStateException(mapAuthError(it.message)) }

    suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val result = client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        result?.id?.let { id -> ensureProfile(id, email.trim()) }
        if (client.auth.currentSessionOrNull() == null) {
            "Account created. Confirm your email, then sign in."
        } else {
            "Account created. You are signed in."
        }
    }.recoverCatching { throw IllegalStateException(mapAuthError(it.message)) }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        client.auth.resetPasswordForEmail(email.trim())
    }.recoverCatching { throw IllegalStateException(mapAuthError(it.message)) }

    suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
    }.recoverCatching { throw IllegalStateException(mapAuthError(it.message)) }

    private suspend fun ensureProfile(userId: String, email: String) {
        runCatching {
            client.from("profiles").upsert(
                ProfileUpsert(id = userId, email = email, displayName = email.substringBefore("@"))
            )
        }
    }

    @Serializable
    private data class ProfileUpsert(
        val id: String,
        val email: String,
        @SerialName("display_name") val displayName: String
    )
}
