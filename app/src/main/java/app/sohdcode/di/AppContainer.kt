package app.sohdcode.di

import android.content.Context
import app.sohdcode.data.ai.AiService
import app.sohdcode.data.auth.AuthRepository
import app.sohdcode.data.project.ProjectRepository
import app.sohdcode.data.settings.SettingsRepository
import app.sohdcode.data.supabase.SupabaseModule
import app.sohdcode.domain.execution.CodeRuntime
import app.sohdcode.domain.execution.SandboxedCodeRuntime

class AppContainer(context: Context) {
    val settings: SettingsRepository = SettingsRepository(context.applicationContext)
    val supabase = SupabaseModule.create(context.applicationContext)
    val auth: AuthRepository = AuthRepository(supabase)
    val projects: ProjectRepository = ProjectRepository(supabase)
    val ai: AiService = AiService(settings)
    val runtime: CodeRuntime = SandboxedCodeRuntime()
}
