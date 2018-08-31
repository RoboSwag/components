package ru.touchin.roboswag.components.extensions

import android.content.Context
import android.content.Intent

fun Context.safeStartActivity(intent: Intent, flags: Int = 0): Boolean =
        if (packageManager.resolveActivity(intent, 0) != null) {
            startActivity(intent)
            true
        } else {
            false
        }
