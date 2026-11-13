package com.mhmmad.periodcalculator.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "periods", indices = [Index(value = ["startEpochDay"], unique = true)])
data class PeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startEpochDay: Long,
    val endEpochDay: Long?,
    val flowIntensity: Int,
    val note: String
)

@Entity(tableName = "daily_entries")
data class DailyEntryEntity(
    @PrimaryKey val epochDay: Long,
    val cramps: Int = 0,
    val headache: Int = 0,
    val bloating: Int = 0,
    val fatigue: Int = 0,
    val mood: Int = 3,
    val sleepHours: Float = 0f,
    val notes: String = ""
)

@Dao
interface PeriodDao {
    @Query("SELECT * FROM periods ORDER BY startEpochDay DESC")
    fun periods(): Flow<List<PeriodEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(period: PeriodEntity)
    @Query("DELETE FROM periods WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM periods") suspend fun deleteAll()
}
@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_entries ORDER BY epochDay DESC")
    fun entries(): Flow<List<DailyEntryEntity>>
    @Upsert suspend fun save(entry: DailyEntryEntity)
    @Query("DELETE FROM daily_entries WHERE epochDay = :day") suspend fun delete(day: Long)
    @Query("DELETE FROM daily_entries") suspend fun deleteAll()
}

@Database(entities = [PeriodEntity::class, DailyEntryEntity::class], version = 1, exportSchema = true)
abstract class PeriodDatabase : RoomDatabase() {
    abstract fun periods(): PeriodDao
    abstract fun daily(): DailyDao
    companion object {
        @Volatile private var instance: PeriodDatabase? = null
        fun get(context: Context): PeriodDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, PeriodDatabase::class.java, "period-local.db")
                .build().also { instance = it }
        }
    }
}

private val Context.periodDataStore by preferencesDataStore(name = "period_preferences")
data class UserPreferences(
    val onboarded: Boolean = false,
    val cycleLength: Int = 28,
    val bleedingLength: Int = 5,
    val theme: String = "light",
    val reminder: Boolean = false,
    val lock: Boolean = false,
    val periodReminder: Boolean = false
)

class PreferenceRepository(private val context: Context) {
    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val cycle = intPreferencesKey("cycle_length")
        val bleeding = intPreferencesKey("bleeding_length")
        val theme = stringPreferencesKey("theme")
        val reminder = booleanPreferencesKey("reminder")
        val lock = booleanPreferencesKey("lock")
        val periodReminder = booleanPreferencesKey("period_reminder")
    }
    val settings: Flow<UserPreferences> = context.periodDataStore.data.map {
        UserPreferences(it[Keys.onboarded] ?: false, it[Keys.cycle] ?: 28,
            it[Keys.bleeding] ?: 5, it[Keys.theme] ?: "light", it[Keys.reminder] ?: false, it[Keys.lock] ?: false, it[Keys.periodReminder] ?: false)
    }
    suspend fun finishSetup(cycle: Int, bleeding: Int) = context.periodDataStore.edit {
        it[Keys.onboarded] = true; it[Keys.cycle] = cycle.coerceIn(15, 90); it[Keys.bleeding] = bleeding.coerceIn(1, 14)
    }
    suspend fun setTheme(value: String) = context.periodDataStore.edit { it[Keys.theme] = value }
    suspend fun setReminder(value: Boolean) = context.periodDataStore.edit { it[Keys.reminder] = value }
    suspend fun setPeriodReminder(value: Boolean) = context.periodDataStore.edit { it[Keys.periodReminder] = value }
    suspend fun setLock(value: Boolean) = context.periodDataStore.edit { it[Keys.lock] = value }
    suspend fun clear() = context.periodDataStore.edit { it.clear() }
}

class PeriodRepository(context: Context) {
    private val db = PeriodDatabase.get(context)
    val periods = db.periods().periods()
    val entries = db.daily().entries()
    suspend fun savePeriod(p: PeriodEntity) = db.periods().save(p)
    suspend fun deletePeriod(id: Long) = db.periods().delete(id)
    suspend fun saveDay(e: DailyEntryEntity) = db.daily().save(e)
    suspend fun deleteDay(day: Long) = db.daily().delete(day)
    suspend fun clear() { db.periods().deleteAll(); db.daily().deleteAll() }
}
