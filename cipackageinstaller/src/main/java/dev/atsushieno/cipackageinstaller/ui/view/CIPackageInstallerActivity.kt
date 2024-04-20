package dev.atsushieno.cipackageinstaller.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.graphics.drawable.IconCompat

abstract class CIPackageInstallerActivity : ComponentActivity() {
    companion object {
        var mainActivityClass: Class<*>? = null
        lateinit var notificationIcon: IconCompat
    }
    init {
        mainActivityClass = this::class.java
    }

    abstract fun createNotificationIcon(): IconCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CIPackageInstallerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TopLevelNavHost()
                }
            }
        }
    }
}
