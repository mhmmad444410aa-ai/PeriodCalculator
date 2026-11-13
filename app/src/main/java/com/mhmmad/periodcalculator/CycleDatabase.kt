package com.mhmmad.periodcalculator

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "periods")
data class PeriodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String,
    val endDate: String,
    val flowLevel: Int = 2,
    val notes: String = ""
)

@Entity(tableName = "symptoms")
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mood: Int,
    val cramps: Boolean,
    val headache: Boolean,
    val fatigue: Boolean,
    val notes: String
)

@Dao
interface CycleDao {
    @Query("SELECT * FROM periods ORDER BY startDate DESC")
    fun observePeriods(): Flow<List<PeriodEntry>>

    @Query("SELECT * FROM symptoms ORDER BY date DESC")
    fun observeSymptoms(): Flow<List<SymptomEntry>>

    @Insert suspend fun addPeriod(entry: PeriodEntry)
    @Insert suspend fun addSymptom(entry: SymptomEntry)
    @Delete suspend fun deletePeriod(entry: PeriodEntry)
    @Delete suspend fun deleteSymptom(entry: SymptomEntry)
    @Query("DELETE FROM periods") suspend fun clearPeriods()
    @Query("DELETE FROM symptoms") suspend fun clearSymptoms()
}

@Database(entities = [PeriodEntry::class, SymptomEntry::class], version = 1, exportSchema = false)
abstract class CycleDatabase : RoomDatabase() {
    abstract fun dao(): CycleDao

    companion object {
        @Volatile private var instance: CycleDatabase? = null
        fun get(context: Context): CycleDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CycleDatabase::class.java,
                "cycle-private.db"
            ).build().also { instance = it }
        }
    }
}
