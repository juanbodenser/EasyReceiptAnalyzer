package com.easyreceiptanalyzer.data

object ReceiptParserRouter {
    private val parsers: List<StoreReceiptParser> = listOf(MercadonaParser, DiaParser)

    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val parser = parsers.firstOrNull { it.matches(lines) }
        return parser?.parse(lines) ?: fallback(lines)
    }

    private fun fallback(lines: List<String>): ParsedReceipt {
        var date = ""
        var total = 0.0
        for (line in lines) {
            val upper = line.uppercase()
            if (date.isEmpty()) ReceiptParsingUtils.extractDate(line)?.let { date = it }
            if (upper.contains("TOTAL") || upper.contains("IMPORTE")) {
                ReceiptParsingUtils.lastPrice(line)?.let { total = it }
            }
        }
        if (date.isEmpty()) date = "2026-01-01"
        return ParsedReceipt("Desconocido", date, total, emptyList())
    }
}
