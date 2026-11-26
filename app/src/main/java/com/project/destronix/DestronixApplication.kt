package com.project.destronix

import android.app.Application
import com.project.destronix.di.AppContainer
import com.project.destronix.di.DefaultAppContainer

class DestronixApplication : Application() {
    val container: AppContainer by lazy { DefaultAppContainer(this) }
}
