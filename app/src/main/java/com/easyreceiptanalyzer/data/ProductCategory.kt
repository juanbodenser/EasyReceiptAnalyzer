package com.easyreceiptanalyzer.data

enum class ProductCategory(val displayName: String) {
    LACTEOS("Lácteos"),
    CARNE_PESCADO("Carne y Pescado"),
    FRUTA_VERDURA("Fruta y Verdura"),
    BEBIDAS("Bebidas"),
    PANADERIA_CEREALES("Panadería y Cereales"),
    DESPENSA("Despensa"),           // ← NUEVA: arroz, pasta, legumbres, conservas
    SNACKS("Snacks"),               // ← NUEVA: patatas, frutos secos, chocolate, galletas
    LIMPIEZA("Limpieza"),
    HIGIENE("Higiene"),
    CONGELADOS("Congelados"),
    MASCOTAS("Mascotas"),
    OTROS("Otros"),
    SIN_CATEGORIZAR("Sin categorizar")
}