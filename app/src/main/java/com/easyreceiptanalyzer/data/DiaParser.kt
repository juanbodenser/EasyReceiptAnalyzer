package com.easyreceiptanalyzer.data

object DiaParser : StoreReceiptParser {

    override fun matches(lines: List<String>): Boolean =
        lines.take(6).any {
            val u = it.uppercase()
            u.contains("GRUPO DIA") || u.contains("CLUBDIA")
        }

    override fun parse(lines: List<String>): ParsedReceipt {
        var date = ""
        var importeTotal: Double? = null
        var totalAPagar: Double? = null
        val items = mutableListOf<ParsedReceiptItem>()
        val textLinesWithoutPrice = mutableListOf<String>()

        var itemsSectionStarted = false
        var itemsSectionEnded = false
        val pricePattern = ReceiptParsingUtils.pricePattern

        for (line in lines) {
            val lineUpper = line.uppercase()

            if (date.isEmpty()) {
                ReceiptParsingUtils.extractDate(line)?.let { date = it }
            }

            if (lineUpper.contains("IMPORTE") && lineUpper.contains("EUROS")) {
                ReceiptParsingUtils.lastPrice(line)?.let { importeTotal = it }
            }
            if (lineUpper.contains("TOTAL A PAGAR")) {
                ReceiptParsingUtils.lastPrice(line)?.let { totalAPagar = it }
            }

            if (lineUpper.contains("DESCRIPCION ARTICULO") || lineUpper.contains("PVP/UNIT") ||
                lineUpper.contains("CANTIDAD")) {
                itemsSectionStarted = true
                continue
            }

            if (lineUpper.contains("TOTAL COMPRA")) {
                itemsSectionEnded = true
                continue
            }

            if (!itemsSectionStarted || itemsSectionEnded) continue

            if (lineUpper.contains("DESGLOSES") || lineUpper.contains("TIPO IVA") ||
                lineUpper.contains("BASE") || lineUpper.contains("CUOTA") ||
                lineUpper.matches(Regex("^\\([A-C]\\).*"))) {
                continue
            }

            if (lineUpper.contains("KG")) {
                val price = ReceiptParsingUtils.lastPrice(line)
                if (price != null && textLinesWithoutPrice.isNotEmpty()) {
                    val name = ReceiptParsingUtils.stripGhostText(
                        textLinesWithoutPrice.removeAt(textLinesWithoutPrice.lastIndex)
                    )
                    if (name.length > 2) items.add(ParsedReceiptItem(name, price))
                }
                continue
            }

            val priceMatcher = pricePattern.matcher(line)
            var matchedPriceStr: String? = null
            while (priceMatcher.find()) matchedPriceStr = priceMatcher.group(1)

            if (matchedPriceStr != null) {
                val price = matchedPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                var namePart = line.replace(matchedPriceStr, "")
                    .replace(Regex("\\s*[ABC]\\s*$"), "")
                    .trim()
                namePart = ReceiptParsingUtils.stripGhostText(namePart)
                if (namePart.length > 2) {
                    items.add(ParsedReceiptItem(namePart, price))
                }
            } else {
                val clean = line.trim()
                if (clean.length > 2) textLinesWithoutPrice.add(clean)
            }
        }

        var finalDate = date
        if (finalDate.isEmpty()) finalDate = "2026-01-01"
        val total = importeTotal ?: totalAPagar ?: items.sumOf { it.price }

        for (orphan in textLinesWithoutPrice) {
            val cleaned = ReceiptParsingUtils.stripGhostText(orphan)
            if (cleaned.length in 3..40) {
                items.add(ParsedReceiptItem(cleaned, 0.0))
            }
        }

        return ParsedReceipt("Dia", finalDate, total, items)
    }
}
