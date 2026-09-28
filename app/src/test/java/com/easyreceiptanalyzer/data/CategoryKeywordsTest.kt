package com.easyreceiptanalyzer.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryKeywordsTest {

    @Test
    fun testSnacks() {
        assertEquals(ProductCategory.SNACKS, CategoryKeywords.classify("PATATAS FRITAS LAYS"))
        assertEquals(ProductCategory.SNACKS, CategoryKeywords.classify("CHOCOLATE NEGRO"))
        assertEquals(ProductCategory.SNACKS, CategoryKeywords.classify("GALLETAS MARIA"))
        assertEquals(ProductCategory.SNACKS, CategoryKeywords.classify("CACAHUETES SALADOS"))
    }

    @Test
    fun testDespensa() {
        assertEquals(ProductCategory.DESPENSA, CategoryKeywords.classify("ARROZ REDONDO"))
        assertEquals(ProductCategory.DESPENSA, CategoryKeywords.classify("ACEITE OLIVA 1L"))
        assertEquals(ProductCategory.DESPENSA, CategoryKeywords.classify("LENTEJAS PARDINAS"))
        assertEquals(ProductCategory.DESPENSA, CategoryKeywords.classify("ATUN EN ACEITE"))
        assertEquals(ProductCategory.DESPENSA, CategoryKeywords.classify("ESPAGUETIS Nº5"))
    }

    @Test
    fun testHuevosEnLacteos() {
        assertEquals(ProductCategory.LACTEOS, CategoryKeywords.classify("HUEVOS FRESCOS DOCENA"))
        assertEquals(ProductCategory.LACTEOS, CategoryKeywords.classify("HUEVOS CAMPEROS"))
    }

    @Test
    fun testPatatasNoSonSnacks() {
        // Las patatas crudas deben seguir siendo FRUTA_VERDURA
        assertEquals(ProductCategory.FRUTA_VERDURA, CategoryKeywords.classify("PATATAS"))
        assertEquals(ProductCategory.FRUTA_VERDURA, CategoryKeywords.classify("PATATAS 2KG"))
    }

    @Test
    fun testCategoriasExistentes() {
        // Comprobación de que no hemos roto lo que ya funcionaba
        assertEquals(ProductCategory.LACTEOS, CategoryKeywords.classify("LECHE ENTERA"))
        assertEquals(ProductCategory.CARNE_PESCADO, CategoryKeywords.classify("POLLO PECHUGA"))
        assertEquals(ProductCategory.FRUTA_VERDURA, CategoryKeywords.classify("TOMATE"))
        assertEquals(ProductCategory.BEBIDAS, CategoryKeywords.classify("AGUA MINERAL"))
        assertEquals(ProductCategory.PANADERIA_CEREALES, CategoryKeywords.classify("PAN DE MOLDE"))
    }
}