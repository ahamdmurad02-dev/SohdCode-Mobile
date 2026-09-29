package app.sohdcode.data.supabase

import android.content.Context
import app.sohdcode.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseModule {
    fun create(@Suppress("UNUSED_PARAMETER") context: Context): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth) {
                host = "login"
                scheme = "sohdcode"
                autoLoadFromStorage = true
                alwaysAutoRefresh = true
            }
            install(Postgrest)
        }
    }
}
