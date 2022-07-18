package com.saboon.defter.utils

import java.text.SimpleDateFormat
import java.util.*

class DateTimeConverter {
    var calendar: Calendar = Calendar.getInstance()

    fun getTime(timeInMillis: Long, stringFormat:String): String {
        calendar.timeInMillis = timeInMillis
        return SimpleDateFormat(stringFormat).format(calendar.time)
    }

    fun getCurrentTime(): Long{
        return calendar.timeInMillis
    }

    fun getTimeForClock(hour: Int, minute: Int): Long{
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return calendar.timeInMillis
    }
}