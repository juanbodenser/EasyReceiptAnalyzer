package com.easyreceiptanalyzer.data

import kotlin.math.min

object CategoryKeywords {

    private val keywordMap: Map<String, ProductCategory> = mapOf(
        "LECHE" to ProductCategory.LACTEOS,
        "QUESO" to ProductCategory.LACTEOS,
        "YOGUR" to ProductCategory.LACTEOS,
        "PROTEINA" to ProductCategory.LACTEOS,
        "MANTEQUILLA" to ProductCategory.LACTEOS,

        "POLLO" to ProductCategory.CARNE_PESCADO,
        "VACUNO" to ProductCategory.CARNE_PESCADO,
        "SALCHICHA" to ProductCategory.CARNE_PESCADO,
        "PICADA" to ProductCategory.CARNE_PESCADO,
        "SOLOMILLO" to ProductCategory.CARNE_PESCADO,
        "PESCADO" to ProductCategory.CARNE_PESCADO,
        "LOMO" to ProductCategory.CARNE_PESCADO,
        "AVES" to ProductCategory.CARNE_PESCADO,

        "PLATANO" to ProductCategory.FRUTA_VERDURA,
        "MELON" to ProductCategory.FRUTA_VERDURA,
        "CIRUELA" to ProductCategory.FRUTA_VERDURA,
        "PIMIENTO" to ProductCategory.FRUTA_VERDURA,
        "TOMATE" to ProductCategory.FRUTA_VERDURA,
        "CEBOLLA" to ProductCategory.FRUTA_VERDURA,
        "UVA" to ProductCategory.FRUTA_VERDURA,
        "MANGO" to ProductCategory.FRUTA_VERDURA,
        "PATATA" to ProductCategory.FRUTA_VERDURA,
        "ZANAHORIA" to ProductCategory.FRUTA_VERDURA,

        "AGUA" to ProductCategory.BEBIDAS,
        "COLA" to ProductCategory.BEBIDAS,
        "PELLEGRINO" to ProductCategory.BEBIDAS,
        "CAFE" to ProductCategory.BEBIDAS,
        "BATIDO" to ProductCategory.BEBIDAS,
        "CACAOLAT" to ProductCategory.BEBIDAS,

        "TORTILLA" to ProductCategory.PANADERIA_CEREALES,
        "SANDWICH" to ProductCategory.PANADERIA_CEREALES,
        "PAN" to ProductCategory.PANADERIA_CEREALES,
        "HARINA" to ProductCategory.PANADERIA_CEREALES,

        "BOLSA" to ProductCategory.OTROS,
        "ARENA" to ProductCategory.MASCOTAS,

        "GEL" to ProductCategory.HIGIENE,
        "PAPEL" to ProductCategory.HIGIENE,
        "HIGIENICO" to ProductCategory.HIGIENE
    )

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + min(dp[i - 1][j], min(dp[i][j - 1], dp[i - 1][j - 1]))
                }
            }
        }
        return dp[a.length][b.length]
    }

    fun classify(productName: String): ProductCategory {
        val normalized = productName.uppercase()
            .replace("Á", "A").replace("É", "E").replace("Í", "I")
            .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")

        val words = normalized.split(Regex("[^A-Z]+")).filter { it.length >= 3 }

        for (word in words) {
            keywordMap.entries.firstOrNull { word.contains(it.key) || it.key.contains(word) }
                ?.let { return it.value }
        }

        for (word in words) {
            for ((keyword, category) in keywordMap) {
                val maxDistance = if (keyword.length <= 5) 1 else 2
                if (levenshtein(word, keyword) <= maxDistance) {
                    return category
                }
            }
        }

        return ProductCategory.SIN_CATEGORIZAR
    }
}