package com.saboon.defter.utils

import java.util.*

class IDGenerator {


    fun generateMomentPhotoID(date: Long): String {
        val randomID = UUID.randomUUID().toString()
        return DateTimeConverter().getTime(date, "dd_MM_yyyy_HH:mm:ss") + "_" + randomID
    }

    fun generateMomentID(date: Long, sender: String): String{
        val randomID = UUID.randomUUID().toString()
        return DateTimeConverter().getTime(date,"dd_MM_yyyy_HH:mm:ss") + "_" + sender  + "_" + randomID
    }

}