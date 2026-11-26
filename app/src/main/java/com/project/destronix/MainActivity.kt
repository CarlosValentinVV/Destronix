package com.project.destronix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.project.destronix.di.LocalAppContainer
import com.project.destronix.presentation.DestronixApp
import com.project.destronix.ui.theme.DestronixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DestronixApplication).container
        setContent {
            DestronixTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    DestronixApp()
                }
            }
        }
    }
}
