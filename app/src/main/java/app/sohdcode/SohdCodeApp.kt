package app.sohdcode

import android.app.Application
import app.sohdcode.di.AppContainer

class SohdCodeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
