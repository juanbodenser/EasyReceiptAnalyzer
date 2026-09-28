package com.easyreceiptanalyzer.data

import java.util.regex.Pattern

object ReceiptParsingUtils {
    val pricePattern: Pattern = Pattern.compile("(\\d+[,.]\\d{1,2})")
    val weightTokenPattern: Pattern = Pattern.compile("(?i)\\d+[.,]?\\d*\\s*kg")
    private val datePattern = Pattern.compile("(\\d{2})[-/](\\d{2})[-/](\\d{2,4})")

    fun lastPrice(line: String): Double? {
        val matcher = pricePattern.matcher(line)
        var last: String? = null
        while (matcher.find()) last = matcher.group(1)
        return last?.replace(",", ".")?.toDoubleOrNull()
    }

    fun extractDate(line: String): String? {
        val m = datePattern.matcher(line)
        if (!m.find()) return null
        val day = m.group(1) ?: return null
        val month = m.group(2) ?: return null
        var year = m.group(3) ?: return null
        if (year.length == 2) year = "20$year"
        return "$year-$month-$day"
    }

    fun stripGhostText(name: String): String {
        val cutIndex = name.indexOfFirst { it.isLowerCase() }
        var result = if (cutIndex >= 0) name.substring(0, cutIndex) else name
        result = result.replace(Regex("(?<=[A-ZÁÉÍÓÚÑ])\\d+$"), "")
        return result.trim()
    }
}
