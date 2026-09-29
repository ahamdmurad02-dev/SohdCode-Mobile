package app.sohdcode.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.sohdcode.data.auth.AuthRepository
import app.sohdcode.data.settings.AppSettings
import app.sohdcode.data.settings.SettingsRepository
import app.sohdcode.data.settings.ThemeOption
import app.sohdcode.domain.model.AiProviderId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val auth: AuthRepository
) : ViewModel() {
    val state: StateFlow<AppSettings> = settings.state.stateIn(
        viewModelScope, SharingStarted.Eagerly, settings.snapshot()
    )
    fun email(): String = auth.currentUser()?.email.orEmpty()
    fun userId(): String = auth.currentUser()?.id.orEmpty()
    fun setTheme(option: ThemeOption) = settings.setTheme(option)
    fun setProvider(id: AiProviderId) = settings.setProvider(id)
    fun saveModels(gemini: String, claude: String, openAi: String, base: String) {
        settings.setModels(gemini, claude, openAi, base)
    }
    fun saveKey(provider: AiProviderId, value: String) = settings.saveApiKey(provider, value)
    fun signOut() { viewModelScope.launch { auth.signOut() } }
    class Factory(
        private val settings: SettingsRepository,
        private val auth: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(settings, auth) as T
    }
}
