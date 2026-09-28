package com.easyreceiptanalyzer.data

import kotlin.math.min

object CategoryKeywords {

    private val keywordMap: Map<String, ProductCategory> = mapOf(

        // ─────────────────────────────────────────────────────────────
        // SNACKS — Van PRIMERO para que "PATATAS FRITAS" no caiga en FRUTA_VERDURA
        // ─────────────────────────────────────────────────────────────
        "FRITAS" to ProductCategory.SNACKS,
        "CHIPS" to ProductCategory.SNACKS,
        "LAYS" to ProductCategory.SNACKS,
        "PATATILLA" to ProductCategory.SNACKS,
        "CHOCOLATE" to ProductCategory.SNACKS,
        "GALLETA" to ProductCategory.SNACKS,
        "BOLACHA" to ProductCategory.SNACKS,
        "CACAO" to ProductCategory.SNACKS,
        "PALOMITA" to ProductCategory.SNACKS,
        "PIPAS" to ProductCategory.SNACKS,
        "ALMENDRA" to ProductCategory.SNACKS,
        "AVELLANA" to ProductCategory.SNACKS,
        "PISTACHO" to ProductCategory.SNACKS,
        "CACAHUETE" to ProductCategory.SNACKS,
        "SNACK" to ProductCategory.SNACKS,
        "APERITIVO" to ProductCategory.SNACKS,
        "BOLLERIA" to ProductCategory.SNACKS,
        "CROISSANT" to ProductCategory.SNACKS,
        "DONUT" to ProductCategory.SNACKS,
        "MAGDALENA" to ProductCategory.SNACKS,
        "CHUCHERIA" to ProductCategory.SNACKS,
        "GOLOSINA" to ProductCategory.SNACKS,
        "CARAMELO" to ProductCategory.SNACKS,
        "HELADO" to ProductCategory.SNACKS,

        // ─────────────────────────────────────────────────────────────
        // DESPENSA — Arroz, pasta, legumbres, conservas, aceite, salsas
        // ─────────────────────────────────────────────────────────────
        "ARROZ" to ProductCategory.DESPENSA,
        "PASTA" to ProductCategory.DESPENSA,
        "ESPAGUETI" to ProductCategory.DESPENSA,
        "MACARRON" to ProductCategory.DESPENSA,
        "TALLARIN" to ProductCategory.DESPENSA,
        "LENTEJA" to ProductCategory.DESPENSA,
        "GARBANZO" to ProductCategory.DESPENSA,
        "ALUBIA" to ProductCategory.DESPENSA,
        "JUDIA" to ProductCategory.DESPENSA,
        "ACEITE" to ProductCategory.DESPENSA,
        "VINAGRE" to ProductCategory.DESPENSA,
        "AZUCAR" to ProductCategory.DESPENSA,
        "ATUN" to ProductCategory.DESPENSA,
        "SARDINA" to ProductCategory.DESPENSA,
        "CONSERVA" to ProductCategory.DESPENSA,
        "SALSA" to ProductCategory.DESPENSA,
        "MAYONESA" to ProductCategory.DESPENSA,
        "KETCHUP" to ProductCategory.DESPENSA,
        "MOSTAZA" to ProductCategory.DESPENSA,
        "CALDO" to ProductCategory.DESPENSA,
        "ESPECIA" to ProductCategory.DESPENSA,
        "LEVADURA" to ProductCategory.DESPENSA,
        "HARINA" to ProductCategory.DESPENSA,
        "SAL" to ProductCategory.DESPENSA,
        "ACEITUNA" to ProductCategory.DESPENSA,
        "PICKLE" to ProductCategory.DESPENSA,

        // ─────────────────────────────────────────────────────────────
        // LACTEOS — Incluye huevos por decisión de diseño
        // ─────────────────────────────────────────────────────────────
        "LECHE" to ProductCategory.LACTEOS,
        "QUESO" to ProductCategory.LACTEOS,
        "YOGUR" to ProductCategory.LACTEOS,
        "MANTEQUILLA" to ProductCategory.LACTEOS,
        "NATA" to ProductCategory.LACTEOS,
        "REQUESON" to ProductCategory.LACTEOS,
        "HUEVO" to ProductCategory.LACTEOS,
        "PROTEINA" to ProductCategory.LACTEOS,

        // ─────────────────────────────────────────────────────────────
        // CARNE Y PESCADO
        // ─────────────────────────────────────────────────────────────
        "POLLO" to ProductCategory.CARNE_PESCADO,
        "VACUNO" to ProductCategory.CARNE_PESCADO,
        "SALCHICHA" to ProductCategory.CARNE_PESCADO,
        "PICADA" to ProductCategory.CARNE_PESCADO,
        "SOLOMILLO" to ProductCategory.CARNE_PESCADO,
        "PESCADO" to ProductCategory.CARNE_PESCADO,
        "LOMO" to ProductCategory.CARNE_PESCADO,
        "AVES" to ProductCategory.CARNE_PESCADO,
        "CERDO" to ProductCategory.CARNE_PESCADO,
        "TERNERA" to ProductCategory.CARNE_PESCADO,
        "JAMON" to ProductCategory.CARNE_PESCADO,
        "CHORIZO" to ProductCategory.CARNE_PESCADO,
        "SALAMI" to ProductCategory.CARNE_PESCADO,
        "MERLUZA" to ProductCategory.CARNE_PESCADO,
        "SALMON" to ProductCategory.CARNE_PESCADO,
        "GAMBAS" to ProductCategory.CARNE_PESCADO,
        "MARISCO" to ProductCategory.CARNE_PESCADO,

        // ─────────────────────────────────────────────────────────────
        // FRUTA Y VERDURA
        // ─────────────────────────────────────────────────────────────
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
        "MANZANA" to ProductCategory.FRUTA_VERDURA,
        "PERA" to ProductCategory.FRUTA_VERDURA,
        "NARANJA" to ProductCategory.FRUTA_VERDURA,
        "LIMON" to ProductCategory.FRUTA_VERDURA,
        "LECHUGA" to ProductCategory.FRUTA_VERDURA,
        "AJO" to ProductCategory.FRUTA_VERDURA,
        "CALABACIN" to ProductCategory.FRUTA_VERDURA,
        "BERENJENA" to ProductCategory.FRUTA_VERDURA,
        "PEPINO" to ProductCategory.FRUTA_VERDURA,
        "FRESA" to ProductCategory.FRUTA_VERDURA,
        "SANDIA" to ProductCategory.FRUTA_VERDURA,
        "KIWI" to ProductCategory.FRUTA_VERDURA,
        "BROCOLI" to ProductCategory.FRUTA_VERDURA,
        "COLIFLOR" to ProductCategory.FRUTA_VERDURA,
        "ESPINACA" to ProductCategory.FRUTA_VERDURA,
        "ACELGA" to ProductCategory.FRUTA_VERDURA,

        // ─────────────────────────────────────────────────────────────
        // BEBIDAS
        // ─────────────────────────────────────────────────────────────
        "AGUA" to ProductCategory.BEBIDAS,
        "COLA" to ProductCategory.BEBIDAS,
        "PELLEGRINO" to ProductCategory.BEBIDAS,
        "CAFE" to ProductCategory.BEBIDAS,
        "BATIDO" to ProductCategory.BEBIDAS,
        "CACAOLAT" to ProductCategory.BEBIDAS,
        "CERVEZA" to ProductCategory.BEBIDAS,
        "VINO" to ProductCategory.BEBIDAS,
        "REFRESCO" to ProductCategory.BEBIDAS,
        "ZUMO" to ProductCategory.BEBIDAS,
        "INFUSION" to ProductCategory.BEBIDAS,
        "TE" to ProductCategory.BEBIDAS,
        "TONICA" to ProductCategory.BEBIDAS,
        "SIDRA" to ProductCategory.BEBIDAS,
        "CAVA" to ProductCategory.BEBIDAS,

        // ─────────────────────────────────────────────────────────────
        // PANADERIA Y CEREALES
        // ─────────────────────────────────────────────────────────────
        "PAN" to ProductCategory.PANADERIA_CEREALES,
        "TORTILLA" to ProductCategory.PANADERIA_CEREALES,
        "SANDWICH" to ProductCategory.PANADERIA_CEREALES,
        "CEREAL" to ProductCategory.PANADERIA_CEREALES,
        "AVENA" to ProductCategory.PANADERIA_CEREALES,
        "BARRA" to ProductCategory.PANADERIA_CEREALES,
        "MOLDE" to ProductCategory.PANADERIA_CEREALES,
        "BAGUETTE" to ProductCategory.PANADERIA_CEREALES,

        // ─────────────────────────────────────────────────────────────
        // CONGELADOS — Palabras simples; la detección real requiere más lógica
        // ─────────────────────────────────────────────────────────────
        "CONGELADO" to ProductCategory.CONGELADOS,
        "PIZZA" to ProductCategory.CONGELADOS,
        "NUGGET" to ProductCategory.CONGELADOS,
        "CROQUETA" to ProductCategory.CONGELADOS,
        "SAN JACOBO" to ProductCategory.CONGELADOS,
        "VARITAS" to ProductCategory.CONGELADOS,

        // ─────────────────────────────────────────────────────────────
        // LIMPIEZA
        // ─────────────────────────────────────────────────────────────
        "DETERGENTE" to ProductCategory.LIMPIEZA,
        "SUAVIZANTE" to ProductCategory.LIMPIEZA,
        "FREGONA" to ProductCategory.LIMPIEZA,
        "BAYETA" to ProductCategory.LIMPIEZA,
        "LAVAVAJILLAS" to ProductCategory.LIMPIEZA,
        "LEJIA" to ProductCategory.LIMPIEZA,
        "AMONIACO" to ProductCategory.LIMPIEZA,
        "LIMPIACRISTALES" to ProductCategory.LIMPIEZA,
        "ESTROPAJO" to ProductCategory.LIMPIEZA,

        // ─────────────────────────────────────────────────────────────
        // HIGIENE
        // ─────────────────────────────────────────────────────────────
        "GEL" to ProductCategory.HIGIENE,
        "PAPEL" to ProductCategory.HIGIENE,
        "HIGIENICO" to ProductCategory.HIGIENE,
        "CHAMPU" to ProductCategory.HIGIENE,
        "JABON" to ProductCategory.HIGIENE,
        "DENTIFRICO" to ProductCategory.HIGIENE,
        "CEPILLO" to ProductCategory.HIGIENE,
        "DESODORANTE" to ProductCategory.HIGIENE,
        "COMPRESA" to ProductCategory.HIGIENE,
        "PANAL" to ProductCategory.HIGIENE,
        "TOALLITA" to ProductCategory.HIGIENE,
        "CREMA" to ProductCategory.HIGIENE,

        // ─────────────────────────────────────────────────────────────
        // MASCOTAS
        // ─────────────────────────────────────────────────────────────
        "ARENA" to ProductCategory.MASCOTAS,
        "PIENSO" to ProductCategory.MASCOTAS,
        "COMIDA PERRO" to ProductCategory.MASCOTAS,
        "COMIDA GATO" to ProductCategory.MASCOTAS,

        // ─────────────────────────────────────────────────────────────
        // OTROS — Genéricos al final para no robar coincidencias
        // ─────────────────────────────────────────────────────────────
        "BOLSA" to ProductCategory.OTROS
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

        // Capa 1: coincidencia directa (contains en ambos sentidos)
        for (word in words) {
            keywordMap.entries.firstOrNull { word.contains(it.key) || it.key.contains(word) }
                ?.let { return it.value }
        }

        // Capa 2: tolerancia a errores OCR con Levenshtein
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