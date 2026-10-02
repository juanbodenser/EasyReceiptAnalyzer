package com.easyreceiptanalyzer.data

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import org.json.JSONObject

object GeminiReceiptAnalyzer {

    private const val PROMPT = """
        Analiza esta imagen de un ticket de compra español. Devuelve EXCLUSIVAMENTE un
        objeto JSON válido, sin texto adicional, sin markdown, sin backticks, con esta
        estructura exacta:
        {
          "store": "nombre de la tienda",
          "date": "YYYY-MM-DD",
          "total": 12.34,
          "items": [
            { "name": "nombre del producto", "price": 1.23 }
          ]
        }
        Si la imagen no es un ticket de compra (por ejemplo, un justificante bancario
        o un recibo de pago de tasas), usa "store": "No es un ticket de compra" y
        "items": [].
        No inventes productos ni precios que no aparezcan en la imagen.
    """

    suspend fun analyze(bitmap: Bitmap): ParsedReceipt {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.8-flash")

        val prompt = content {
            image(bitmap)
            text(PROMPT)
        }

        val response = model.generateContent(prompt)
        val rawJson = response.text
            ?.trim()
            ?.removePrefix("```json")
            ?.removePrefix("```")
            ?.removeSuffix("```")
            ?.trim()
            ?: throw IllegalStateException("Gemini no devolvió texto en la respuesta.")

        val json = JSONObject(rawJson)
        val itemsArray = json.optJSONArray("items")
        val items = mutableListOf<ParsedReceiptItem>()
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                items.add(
                    ParsedReceiptItem(
                        name = itemObj.optString("name", "Desconocido"),
                        price = itemObj.optDouble("price", 0.0)
                    )
                )
            }
        }

        return ParsedReceipt(
            storeName = json.optString("store", "Desconocido"),
            date = json.optString("date", "2026-01-01"),
            total = json.optDouble("total", 0.0),
            items = items
        )
    }
}
