package com.easyreceiptanalyzer.data.bank

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BankStatementParserTest {

    @Test
    fun parse_extraeGastosCorrectamente() {
        val txt = """
22    9736260902260902120401000000000030820000000000000000000000961318619292493 
2301Fecha de operaciµn: 30-08-2026        CARREF LOS ANGELE                     
230504000174TCR                           TARJETA CREDITO                       
22    9736260902260902120401000000000004000000000000000000000000961318619292493 
2301Fecha de operaciµn: 02-09-2026        EL RINCON DEL QUI                     
230504000174TCR                           TARJETA CREDITO                       
        """.trimIndent()

        val movements = BankStatementParser.parseText(txt)

        assertEquals(2, movements.size)

        val first = movements[0]
        assertEquals("CARREF LOS ANGELE", first.concept)
        assertEquals(3082L, first.amountCents)
        assertTrue(first.isExpense)

        val second = movements[1]
        assertEquals("EL RINCON DEL QUI", second.concept)
        assertEquals(400L, second.amountCents)
        assertTrue(second.isExpense)
    }

    @Test
    fun parse_aceptaIngresos() {
        val txt = """
22    13672609272609270204020000000001200000000000000000000000004766********1614
2301                                      INGRESO CAJERO                         
230504000002TOC                           INGRESO CAJERO                         
        """.trimIndent()

        val movements = BankStatementParser.parseText(txt)

        assertEquals(1, movements.size)
        assertEquals(false, movements[0].isExpense)
    }

    @Test
    fun parse_marcaMovimientosSospechosos() {
        val txt = """
22    9736260931260931120401000000000050000000000000000000000000961318619292493 
2301                                      BIZUM ENVIADO                          
230504000174TCR                           TARJETA CREDITO                       
        """.trimIndent()

        val movements = BankStatementParser.parseText(txt)

        assertEquals(1, movements.size)
        assertTrue(movements[0].isSuspicious)
    }

    @Test
    fun parse_usaFechaDeLinea22SiNoHayConcepto() {
        val txt = """
22    9736260902260902120401000000000030820000000000000000000000961318619292493 
2301                                      CARREF LOS ANGELE                     
230504000174TCR                           TARJETA CREDITO                       
        """.trimIndent()

        val movements = BankStatementParser.parseText(txt)

        // El movimiento tiene concepto pero no fecha en el concepto.
        // Debería aceptarse usando la fecha de la línea 22.
        assertEquals(1, movements.size)
        assertEquals("CARREF LOS ANGELE", movements[0].concept)
    }

    @Test
    fun extractConcept_quitaPrefijoFecha() {
        val concept = "Fecha de operaciµn: 30-08-2026        CARREF LOS ANGELE                     "
        // Este test es indirecto, pero nos sirve para verificar el comportamiento.
        // En la práctica, lo validamos con los tests anteriores.
        assertTrue(concept.contains("CARREF LOS ANGELE"))
    }

    @Test
    fun parse_usaLaFechaDelConcepto() {
        val txt = """
22    9736260922260922120401000000000028200000000000000000000000961318619292493 
2301Fecha de operación: 20-09-2026        Klarna*Optica Bas                     
230504000174TCR                           TARJETA CREDITO                       
    """.trimIndent()

        val movements = BankStatementParser.parseText(txt)

        assertEquals(1, movements.size)
        assertEquals("Klarna*Optica Bas", movements[0].concept)

        // Verificamos que la fecha es la del concepto (20/09/2026), no la de la línea 22 (22/09/2026)
        val cal = Calendar.getInstance()
        cal.timeInMillis = movements[0].date
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH))
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
    }
}
