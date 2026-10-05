package com.easyreceiptanalyzer.data.bank

import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BankStatementParser {

    /**
     * Parsea un archivo TXT de extracto bancario de CaixaBank.
     * Devuelve los movimientos del extracto.
     *
     * @param inputStream El stream del archivo TXT.
     * @return Lista de movimientos parseados.
     */
    fun parse(inputStream: InputStream): List<BankMovement> {
        // Leer el archivo como Latin-1 (ISO-8859-1) para que los caracteres
        // especiales (µ, etc.) se lean correctamente.
        val text = inputStream.bufferedReader(Charsets.ISO_8859_1).readText()
        return parseText(text)
    }

    /**
     * Parsea el texto completo. Útil para tests.
     */
    fun parseText(text: String): List<BankMovement> {
        val lines = text.lines()
        val movements = mutableListOf<BankMovement>()

        var pendingMovement: BankMovement? = null

        for (line in lines) {
            when {
                line.startsWith("22") -> {
                    // Si había un movimiento pendiente sin línea 2301, lo descartamos
                    // (no debería pasar, pero por seguridad).
                    pendingMovement = parseMovementLine(line)
                }
                line.startsWith("2301") -> {
                    val movement = pendingMovement
                    if (movement != null) {
                        val (concept, conceptDate) = extractConceptAndDate(line)
                        if (concept.isNotBlank()) {
                            val finalDate = conceptDate ?: movement.date
                            val suspicious = isSuspiciousConcept(concept)
                            movements.add(
                                movement.copy(
                                    date = finalDate,
                                    concept = concept,
                                    rawConcept = line,
                                    isSuspicious = suspicious
                                )
                            )
                        }
                    }
                    pendingMovement = null
                }
                // Ignoramos las líneas 2302, 2303, 2305 y cualquier otra.
            }
        }

        return movements
    }

    /**
     * Parsea una línea que empieza por "22" (movimiento).
     * Devuelve un BankMovement con date, amountCents e isExpense.
     * El campo concept se rellena después con la línea 2301.
     */
    private fun parseMovementLine(line: String): BankMovement? {
        if (line.length < 42) return null

        return try {
            // Fecha de operación: posiciones 16-21 (YYMMDD)
            val dateStr = line.substring(16, 22)
            val date = parseDate(dateStr) ?: return null

            // Código de operación: posiciones 22-27
            val operationCode = line.substring(22, 28)

            // Importe: posiciones 28-41 (14 dígitos, en céntimos)
            val amountStr = line.substring(28, 42).trim()
            val amountCents = amountStr.toLongOrNull() ?: return null

            // Determinamos si es gasto o ingreso:
            // - Códigos que empiezan por "02" son ingresos (nómina, cajero, etc.)
            // - "120402" es ingreso con tarjeta
            // - Todo lo demás es gasto (compras, transferencias, adeudos, préstamos, etc.)
            val isExpense = when {
                operationCode.startsWith("02") -> false
                operationCode == "120402" -> false
                else -> true
            }

            BankMovement(
                date = date,
                concept = "",
                rawConcept = "",
                amountCents = amountCents,
                isExpense = isExpense
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extrae el concepto limpio y la fecha de la línea 2301.
     *
     * Ejemplo de entrada: "2301Fecha de operación: 20-09-2026        Klarna*Optica Bas"
     * Resultado: Pair("Klarna*Optica Bas", 1726786800000L)
     *
     * Si la línea no tiene el prefijo "Fecha de operación: DD-MM-YYYY", la fecha devuelta es null.
     */
    private fun extractConceptAndDate(line: String): Pair<String, Long?> {
        // Quitamos el código "2301" del principio
        var content = line.removePrefix("2301").trim()

        // Regex tolerante a caracteres raros de Latin-1 (µ en lugar de ó, etc.)
        // Captura la fecha entre "Fecha de operación:" y el concepto.
        val prefixRegex = Regex("^Fecha de operaci.{0,2}n:\\s*(\\d{2})-(\\d{2})-(\\d{4})\\s+")
        val match = prefixRegex.find(content)

        var date: Long? = null
        if (match != null) {
            val day = match.groupValues[1].toIntOrNull()
            val month = match.groupValues[2].toIntOrNull()
            val year = match.groupValues[3].toIntOrNull()

            if (day != null && month != null && year != null) {
                val cal = Calendar.getInstance()
                cal.clear()
                cal.set(year, month - 1, day, 0, 0, 0)
                date = cal.timeInMillis
            }

            // Quitamos el prefijo de fecha del contenido
            content = content.removePrefix(match.value).trim()
        }

        return content to date
    }

    /**
     * Convierte una fecha en formato "YYMMDD" a timestamp en milisegundos.
     */
    private fun parseDate(yyMMdd: String): Long? {
        if (yyMMdd.length != 6) return null
        return try {
            val year = yyMMdd.substring(0, 2).toInt() + 2000
            val month = yyMMdd.substring(2, 4).toInt()
            val day = yyMMdd.substring(4, 6).toInt()

            val cal = Calendar.getInstance()
            cal.clear()
            cal.set(year, month - 1, day, 0, 0, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            null
        }
    }

    private fun isSuspiciousConcept(concept: String): Boolean {
        val upper = concept.uppercase()
        return upper.contains("BIZUM ENVIADO") ||
                upper.contains("BIZUM RECIBIDO") ||
                upper.contains("REINT.CAJERO") ||
                upper.contains("REINTEGRO") ||
                upper.contains("TRANSFERENCIA") ||
                upper.contains("TRASPASO") ||
                upper.contains("DEVOLUCION") ||
                upper.contains("ABONO")
    }
}
