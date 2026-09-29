package app.sohdcode.core

fun mapAuthError(raw: String?): String {
    val text = raw.orEmpty().lowercase()
    return when {
        text.contains("invalid login") || text.contains("invalid credentials") ->
            "Incorrect email or password."
        text.contains("user already registered") || text.contains("already registered") ->
            "An account with this email already exists."
        text.contains("password") && (text.contains("weak") || text.contains("least")) ->
            "Password is too weak. Use at least 8 characters."
        text.contains("email not confirmed") ->
            "Check your inbox and confirm your email before signing in."
        text.contains("rate limit") || text.contains("too many") ->
            "Too many attempts. Wait a moment and try again."
        text.contains("network") || text.contains("unable to resolve") || text.contains("timeout") ->
            "Network failure. Check your connection and try again."
        text.contains("session") && text.contains("expired") ->
            "Your session expired. Please sign in again."
        raw.isNullOrBlank() -> "Something went wrong. Please try again."
        else -> raw
    }
}

fun mapNetworkError(raw: Throwable): String {
    val text = raw.message.orEmpty().lowercase()
    return when {
        text.contains("unable to resolve") || text.contains("failed to connect") ->
            "Cannot reach the server. Check your internet connection."
        text.contains("timeout") -> "The request timed out. Try again."
        text.contains("401") || text.contains("unauthorized") ->
            "Session expired or API key rejected. Sign in again or check Settings."
        text.contains("429") -> "The provider rate-limited this request. Wait and retry."
        else -> raw.message ?: "Unexpected error."
    }
}
