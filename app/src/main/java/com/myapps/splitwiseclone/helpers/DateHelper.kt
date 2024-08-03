package com.myapps.splitwiseclone.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateHelper {
    companion object{
        fun convertDateStringToMillis(dateString: String): Long {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date: Date = dateFormat.parse(dateString)!!
            return date.time
        }
    }
}