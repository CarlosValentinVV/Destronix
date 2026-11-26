package com.project.destronix.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Abre enlaces externos (por ejemplo, tráilers de YouTube) con la app del sistema. */
fun Context.openExternalUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
