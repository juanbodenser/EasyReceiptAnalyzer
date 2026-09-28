package com.easyreceiptanalyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ProductCategoryOverride

@Database(
    entities = [
        ReceiptEntity::class,
        ItemEntity::class,
        ProductCategoryOverride::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun productCategoryDao(): ProductCategoryDao
    abstract fun analysisDao(): AnalysisDao
}
