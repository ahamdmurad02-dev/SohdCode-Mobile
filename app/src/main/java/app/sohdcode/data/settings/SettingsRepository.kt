package app.sohdcode.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.sohdcode.domain.model.AiProviderId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val theme: ThemeOption = ThemeOption.System,
    val provider: AiProviderId = AiProviderId.GEMINI,
    val geminiModel: String = "gemini-2.0-flash",
    val claudeModel: String = "claude-sonnet-4-5",
    val openAiModel: String = "gpt-4o-mini",
    val openAiBaseUrl: String = "https://api.openai.com/v1",
    val hasGeminiKey: Boolean = false,
    val hasClaudeKey: Boolean = false,
    val hasOpenAiKey: Boolean = false
)

enum class ThemeOption { System, Light, Dark }

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = createSecurePrefs(context)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun snapshot(): AppSettings = _state.value
    fun setTheme(option: ThemeOption) { prefs.edit().putString(KEY_THEME, option.name).apply(); publish() }
    fun setProvider(id: AiProviderId) { prefs.edit().putString(KEY_PROVIDER, id.id).apply(); publish() }
    fun setModels(gemini: String, claude: String, openAi: String, openAiBase: String) {
        prefs.edit()
            .putString(KEY_GEMINI_MODEL, gemini.trim())
            .putString(KEY_CLAUDE_MODEL, claude.trim())
            .putString(KEY_OPENAI_MODEL, openAi.trim())
            .putString(KEY_OPENAI_BASE, openAiBase.trim().ifBlank { "https://api.openai.com/v1" })
            .apply()
        publish()
    }
    fun saveApiKey(provider: AiProviderId, raw: String) {
        val key = raw.trim()
        val prefKey = keyName(provider)
        if (key.isBlank()) prefs.edit().remove(prefKey).apply() else prefs.edit().putString(prefKey, key).apply()
        publish()
    }
    fun apiKey(provider: AiProviderId): String? = prefs.getString(keyName(provider), null)?.takeIf { it.isNotBlank() }

    private fun read(): AppSettings {
        val theme = runCatching { ThemeOption.valueOf(prefs.getString(KEY_THEME, ThemeOption.System.name)!!) }.getOrDefault(ThemeOption.System)
        return AppSettings(
            theme = theme,
            provider = AiProviderId.fromId(prefs.getString(KEY_PROVIDER, AiProviderId.GEMINI.id)!!),
            geminiModel = prefs.getString(KEY_GEMINI_MODEL, "gemini-2.0-flash") ?: "gemini-2.0-flash",
            claudeModel = prefs.getString(KEY_CLAUDE_MODEL, "claude-sonnet-4-5") ?: "claude-sonnet-4-5",
            openAiModel = prefs.getString(KEY_OPENAI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini",
            openAiBaseUrl = prefs.getString(KEY_OPENAI_BASE, "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
            hasGeminiKey = !prefs.getString(KEY_GEMINI, null).isNullOrBlank(),
            hasClaudeKey = !prefs.getString(KEY_CLAUDE, null).isNullOrBlank(),
            hasOpenAiKey = !prefs.getString(KEY_OPENAI, null).isNullOrBlank()
        )
    }
    private fun publish() { _state.value = read() }
    private fun keyName(provider: AiProviderId) = when (provider) {
        AiProviderId.GEMINI -> KEY_GEMINI
        AiProviderId.CLAUDE -> KEY_CLAUDE
        AiProviderId.OPENAI -> KEY_OPENAI
    }
    private fun createSecurePrefs(context: Context): SharedPreferences {
        return runCatching {
            val master = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context, "sohdcode_secure_settings", master,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse { context.getSharedPreferences("sohdcode_settings_fallback", Context.MODE_PRIVATE) }
    }
    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_GEMINI = "api_gemini"
        private const val KEY_CLAUDE = "api_claude"
        private const val KEY_OPENAI = "api_openai"
        private const val KEY_GEMINI_MODEL = "model_gemini"
        private const val KEY_CLAUDE_MODEL = "model_claude"
        private const val KEY_OPENAI_MODEL = "model_openai"
        private const val KEY_OPENAI_BASE = "openai_base"
    }
}
