package com.easyreceiptanalyzer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlyBalanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(balance: MonthlyBalanceEntity)

    @Query("SELECT * FROM monthly_balances ORDER BY yearMonth DESC")
    suspend fun getAll(): List<MonthlyBalanceEntity>

    @Query("SELECT * FROM monthly_balances WHERE yearMonth = :yearMonth")
    suspend fun getByMonth(yearMonth: String): MonthlyBalanceEntity?

    @Query("SELECT SUM(balanceCents) FROM monthly_balances")
    suspend fun getTotalSavings(): Long?

    @Query("DELETE FROM monthly_balances WHERE yearMonth = :yearMonth")
    suspend fun deleteByMonth(yearMonth: String)
}
