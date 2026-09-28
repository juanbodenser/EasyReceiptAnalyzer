package com.easyreceiptanalyzer.data

object MercadonaParser : StoreReceiptParser {

    override fun matches(lines: List<String>): Boolean =
        lines.take(10).any { it.uppercase().contains("MERCADONA") }

    override fun parse(lines: List<String>): ParsedReceipt {
        val storeName = "Mercadona"
        var date = ""
        var total = 0.0
        val items = mutableListOf<ParsedReceiptItem>()

        var importeTotal: Double? = null
        for (rawLine in lines) {
            val upperLine = rawLine.uppercase()
            if (upperLine.contains("IMPORTE") && !upperLine.contains("SIMPLIFICADA")) {
                ReceiptParsingUtils.lastPrice(rawLine)?.let { if (it >= 1.0) importeTotal = it }
            }
        }

        val pricePattern = ReceiptParsingUtils.pricePattern
        val weightTokenPattern = ReceiptParsingUtils.weightTokenPattern
        val textLinesWithoutPrice = mutableListOf<String>()
        var itemsSectionStarted = false

        for (line in lines) {
            val lineUpper = line.uppercase()

            if (lineUpper.contains("DESCRIPCIÓN") || lineUpper.contains("P. UNIT")) {
                itemsSectionStarted = true
                continue
            }

            if (line.trim().matches(Regex("^\\d{1,2}\\s*%.*"))) {
                continue
            }

            if (lineUpper.contains("BASE IMPONIBLE") || lineUpper.contains("CUOTA") ||
                lineUpper.contains("IVA") || lineUpper.contains("RECARGO") ||
                lineUpper.contains("AUT:") || lineUpper.contains("AID:") || lineUpper.contains("ARC:") ||
                lineUpper.contains("TARJ") || lineUpper.contains("VISA") ||
                lineUpper.contains("****") || lineUpper.contains("VERIFICADO") ||
                lineUpper.contains("FACTURA SIMPLIFICADA") || lineUpper.contains("TELEFONO") ||
                lineUpper.contains("OP:") || lineUpper.contains("BANCARIA") ||
                lineUpper.contains("NIF") || lineUpper.contains("CIF")) {
                continue
            }

            if (date.isEmpty()) {
                ReceiptParsingUtils.extractDate(line)?.let { date = it }
            }

            if (lineUpper.contains("ENTREGA") || lineUpper.contains("DEVOLUCI")) {
                break // fin de la sección de productos; no usamos el importe de esta línea como total
            }

            if (lineUpper.contains("TOTAL") || lineUpper.contains("EUR") || lineUpper.contains("IMPORTE")) {
                val lastFoundPrice = ReceiptParsingUtils.lastPrice(line)
                if (lineUpper.contains("TOTAL") && !lineUpper.contains("SIMPLIFICADA")) {
                    lastFoundPrice?.let { total = it }
                    break
                }
                if (lastFoundPrice != null && lastFoundPrice > total) total = lastFoundPrice
                continue
            }

            if (lineUpper.contains("KG")) {
                val price = ReceiptParsingUtils.lastPrice(line) ?: 0.0
                val weightMatcher = weightTokenPattern.matcher(line)
                val cutIndex = if (weightMatcher.find()) weightMatcher.start() else line.length
                var candidateName = line.substring(0, cutIndex)
                    .replace(Regex("^[0-9]+\\s+"), "")
                    .replace(Regex("[€Xx*@#]"), "")
                    .trim()

                if (candidateName.length <= 2 && itemsSectionStarted && textLinesWithoutPrice.isNotEmpty()) {
                    val fallback = textLinesWithoutPrice.last()
                    if (fallback.length > 2 && !fallback.contains(Regex("[0-9]"))) {
                        candidateName = fallback
                        textLinesWithoutPrice.removeAt(textLinesWithoutPrice.lastIndex)
                    }
                }
                candidateName = ReceiptParsingUtils.stripGhostText(candidateName)
                if (candidateName.length > 2 && price > 0.0) {
                    items.add(ParsedReceiptItem(candidateName, price))
                }
                continue
            }

            val priceMatcher = pricePattern.matcher(line)
            var matchedPriceStr: String? = null
            while (priceMatcher.find()) matchedPriceStr = priceMatcher.group(1)

            if (matchedPriceStr != null) {
                val price = matchedPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                var namePart = line.replace(matchedPriceStr, "")
                    .replace(Regex("^[0-9]+\\s+"), "")
                    .replace(Regex("[€Xx*@#]"), "")
                    .trim()

                if (namePart.length <= 2 && textLinesWithoutPrice.isNotEmpty()) {
                    val candidate = textLinesWithoutPrice.last()
                    if (candidate.length > 2 && !candidate.contains(Regex("[0-9]"))) {
                        namePart = candidate
                        textLinesWithoutPrice.removeAt(textLinesWithoutPrice.lastIndex)
                    }
                }
                namePart = ReceiptParsingUtils.stripGhostText(namePart)
                val nameUpper = namePart.uppercase()

                if (nameUpper.isNotEmpty() && nameUpper != "TOTAL" && nameUpper != "IMPORTE" &&
                    namePart.length > 2 && !nameUpper.matches(Regex("^[0-9]+$")) &&
                    !nameUpper.matches(Regex("^[A-Z0-9]{1,2}$"))) {
                    items.add(ParsedReceiptItem(namePart, price))
                }
            } else if (itemsSectionStarted) {
                val clean = line.replace(Regex("^[0-9]+\\s+"), "").replace(Regex("[€Xx*@#]"), "").trim()
                if (clean.length > 2 && !lineUpper.contains("MERCADONA") && !lineUpper.contains("MADRID")) {
                    textLinesWithoutPrice.add(clean)
                }
            }
        }

        var finalDate = date
        if (finalDate.isEmpty()) finalDate = "2026-01-01"
        if (total == 0.0 && items.isNotEmpty()) total = items.sumOf { it.price }

        for (orphan in textLinesWithoutPrice) {
            val cleaned = ReceiptParsingUtils.stripGhostText(orphan)
            if (cleaned.length in 3..40 && !cleaned.uppercase().contains(Regex("[0-9]{3,}"))) {
                items.add(ParsedReceiptItem(cleaned, 0.0))
            }
        }

        if (total == 0.0 && items.isNotEmpty()) total = items.sumOf { it.price }
        importeTotal?.let { total = it }

        return ParsedReceipt(storeName, finalDate, total, items)
    }
}
