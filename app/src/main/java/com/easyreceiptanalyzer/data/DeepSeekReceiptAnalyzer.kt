package com.easyreceiptanalyzer.data

import android.graphics.Bitmap
import android.util.Base64
import com.easyreceiptanalyzer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DeepSeekReceiptAnalyzer {

    // La clave se inyecta en tiempo de compilación desde local.properties:
    //   deepseek.api.key=sk-tu-clave
    // y se expone como BuildConfig.DEEPSEEK_API_KEY (ver build.gradle.kts).
    private val apiKey: String = BuildConfig.DEEPSEEK_API_KEY

    private const val DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions"

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

    suspend fun analyze(bitmap: Bitmap): ParsedReceipt = withContext(Dispatchers.IO) {
        val currentKey = apiKey.trim()
        if (currentKey.isBlank()) {
            throw IllegalStateException(
                "API Key de DeepSeek no configurada. Agrega tu clave en local.properties " +
                        "con la línea: deepseek.api.key=sk-..."
            )
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        val imageUrl = "data:image/jpeg;base64,$base64Image"

        val textContentObj = JSONObject().apply {
            put("type", "text")
            put("text", PROMPT)
        }

        val imageUrlObj = JSONObject().apply {
            put("url", imageUrl)
        }
        val imageContentObj = JSONObject().apply {
            put("type", "image_url")
            put("image_url", imageUrlObj)
        }

        val contentArray = JSONArray().apply {
            put(textContentObj)
            put(imageContentObj)
        }

        val userMessageObj = JSONObject().apply {
            put("role", "user")
            put("content", contentArray)
        }

        val messagesArray = JSONArray().apply {
            put(userMessageObj)
        }

        val requestBody = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", messagesArray)
            put("response_format", JSONObject().put("type", "json_object"))
            put("temperature", 0.1)
        }

        val url = URL(DEEPSEEK_API_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $currentKey")
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 60000

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        if (responseCode == 401) {
            throw IllegalStateException(
                "La API Key de DeepSeek es inválida (HTTP 401). " +
                        "Verifica tu clave en https://platform.deepseek.com/ y actualízala en " +
                        "local.properties como deepseek.api.key=sk-..."
            )
        }

        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw IllegalStateException("Error de DeepSeek API (HTTP $responseCode): $errorText")
        }

        val responseJson = JSONObject(responseText)
        val choices = responseJson.optJSONArray("choices")
            ?: throw IllegalStateException("Respuesta no válida de DeepSeek API")

        val messageObj = choices.getJSONObject(0).optJSONObject("message")
            ?: throw IllegalStateException("Estructura de respuesta no válida")

        val rawJson = messageObj.optString("content", "")
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        if (rawJson.isBlank()) {
            throw IllegalStateException("DeepSeek no devolvió texto en la respuesta.")
        }

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

        ParsedReceipt(
            storeName = json.optString("store", "Desconocido"),
            date = json.optString("date", "2026-01-01"),
            total = json.optDouble("total", 0.0),
            items = items
        )
    }
}