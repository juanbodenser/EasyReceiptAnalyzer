package com.easyreceiptanalyzer.data

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.util.Log

object OcrTextExtractor {

    private data class OcrLine(val text: String, val box: Rect)

    suspend fun extractText(context: Context, imageUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val rawText = reconstructRowsFromLayout(visionText)
                        Log.d("OCR_RAW", rawText)
                        continuation.resume(cleanOcrText(rawText))
                    }
                    .addOnFailureListener { e -> continuation.resumeWithException(e) }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    private fun reconstructRowsFromLayout(visionText: Text): String {
        val allLines = mutableListOf<OcrLine>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                allLines.add(OcrLine(line.text, box))
            }
        }
        if (allLines.isEmpty()) return ""

        allLines.sortBy { it.box.top }
        val avgHeight = allLines.map { it.box.height() }.average()
        val yTolerance = avgHeight * 0.6

        val rows = mutableListOf<MutableList<OcrLine>>()
        for (line in allLines) {
            val centerY = (line.box.top + line.box.bottom) / 2.0
            val row = rows.find { r ->
                val rowCenterY = r.map { (it.box.top + it.box.bottom) / 2.0 }.average()
                abs(rowCenterY - centerY) < yTolerance
            }
            if (row != null) row.add(line) else rows.add(mutableListOf(line))
        }

        return rows.joinToString("\n") { row ->
            row.sortedBy { it.box.left }.joinToString("   ") { it.text }
        }
    }

    private fun cleanOcrText(rawText: String): String {
        return rawText
            .lines()
            .map { line ->
                line.trim()
                    .replace(Regex("[\\u0000-\\u001F\\u007F-\\u009F]"), "")
                    .replace(Regex("\\s+"), " ")
                    .replace(Regex("[^\\p{L}\\p{N}\\s.,€%:/-]"), "")
                    .trim()
            }
            .filter { it.isNotEmpty() && it.length > 1 }
            .joinToString("\n")
    }
}