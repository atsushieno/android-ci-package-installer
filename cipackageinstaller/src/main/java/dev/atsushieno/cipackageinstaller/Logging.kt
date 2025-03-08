package dev.atsushieno.cipackageinstaller

import android.util.Log
import androidx.compose.runtime.mutableStateListOf

// Collects log entries that should be shown to the user
class Logger {
    class LogEntry(val text: String, val artifact: ApplicationArtifact? = null)

    val logs = mutableStateListOf<LogEntry>()

    fun logError(message: String, ex: Exception? = null, artifact: ApplicationArtifact? = null) {
        logs.add(LogEntry(message, artifact))
        if (ex != null)
            Log.e(AppModel.LOG_TAG, ex.stackTraceToString())
        else
            Log.e(AppModel.LOG_TAG, message)
    }

    fun logInfo(message: String, artifact: ApplicationArtifact? = null) {
        logs.add(LogEntry(message, artifact))
        Log.i(AppModel.LOG_TAG, message)
    }
}
