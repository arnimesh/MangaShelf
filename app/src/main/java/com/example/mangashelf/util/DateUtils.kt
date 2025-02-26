package com.example.mangashelf.util

import android.icu.util.Calendar

object DateUtils {
    fun timestampToYear(timestamp: Long): Int {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp * 1000 // Convert Unix timestamp to milliseconds
        }.get(Calendar.YEAR)
    }
} 