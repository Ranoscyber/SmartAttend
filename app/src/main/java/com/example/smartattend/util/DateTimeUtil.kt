package com.example.smartattend.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtil {

    fun todayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }

    fun currentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date())
    }

    fun currentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun isLate(
        currentTime: String,
        lateAfterTime: String
    ): Boolean {
        return currentTime > lateAfterTime
    }
}