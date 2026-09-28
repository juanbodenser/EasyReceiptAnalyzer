package com.easyreceiptanalyzer.data

object ProductCategorizer {

    private fun normalize(name: String): String =
        name.uppercase()
            .replace("Á", "A").replace("É", "E").replace("Í", "I")
            .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")
            .trim()

    suspend fun categorize(productName: String, dao: ProductCategoryDao): ProductCategory {
        val normalized = normalize(productName)

        dao.getOverride(normalized)?.let {
            return ProductCategory.valueOf(it.category)
        }

        return CategoryKeywords.classify(productName)
    }

    suspend fun saveCorrection(productName: String, category: ProductCategory, dao: ProductCategoryDao) {
        dao.saveOverride(ProductCategoryOverride(normalize(productName), category.name))
    }
}