package com.easyreceiptanalyzer.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Insert
import androidx.room.Query

@Entity(tableName = "product_category_overrides")
data class ProductCategoryOverride(
    @PrimaryKey val normalizedName: String,
    val category: String
)

@Dao
interface ProductCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOverride(override: ProductCategoryOverride)

    @Query("SELECT * FROM product_category_overrides WHERE normalizedName = :name")
    suspend fun getOverride(name: String): ProductCategoryOverride?

    @Query("SELECT * FROM product_category_overrides")
    suspend fun getAllOverrides(): List<ProductCategoryOverride>
}
