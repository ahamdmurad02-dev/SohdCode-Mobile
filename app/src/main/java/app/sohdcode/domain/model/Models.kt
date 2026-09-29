package app.sohdcode.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UserAccount(val id: String, val email: String)

enum class AuthStatus { Checking, SignedOut, SignedIn }

@Serializable
data class ProjectFile(
    val path: String,
    val content: String,
    val language: String = "javascript"
)

@Serializable
data class CodeProject(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String = "",
    val language: String = "javascript",
    val files: List<ProjectFile> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

enum class SupportedLanguage(val id: String, val displayName: String, val defaultFile: String) {
    JAVASCRIPT("javascript", "JavaScript", "main.js"),
    JSON("json", "JSON", "data.json"),
    MARKDOWN("markdown", "Markdown", "README.md"),
    TEXT("text", "Plain text", "notes.txt"),
    PYTHON("python", "Python", "main.py"),
    KOTLIN("kotlin", "Kotlin", "Main.kt"),
    JAVA("java", "Java", "Main.java");
    companion object {
        fun fromId(id: String): SupportedLanguage =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: JAVASCRIPT
    }
}

enum class AiProviderId(val id: String, val displayName: String) {
    GEMINI("gemini", "Gemini"),
    CLAUDE("claude", "Claude"),
    OPENAI("openai", "OpenAI / ChatGPT-compatible");
    companion object {
        fun fromId(id: String): AiProviderId = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

data class AiChatMessage(val id: String, val role: Role, val content: String) {
    enum class Role { User, Assistant, System }
}

data class ExecutionResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val limited: Boolean = false
)
