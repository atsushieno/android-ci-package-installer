package dev.atsushieno.cipackageinstaller

import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.mutableDoubleStateOf

// progress ranges from 0.0 to 1.0.
class DownloadStatus(val label: String, var progress: MutableDoubleState = mutableDoubleStateOf(0.0))
