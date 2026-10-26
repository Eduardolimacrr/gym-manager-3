package com.example.gymmobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

/** Tag única de log das operações de banco: filtre por `GymDb` no Logcat. */
const val DB_TAG = "GymDb"

@Database(entities = [WorkoutTemplate::class, Exercise::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym.db",
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

/**
 * Uma coluna do SQLite só guarda tipos simples, então as duas listas das
 * entidades viram texto: ids como `1,2,5` e o histórico como `18 ago;80.0;8|14 ago;75.0;10`.
 */
class Converters {

    @TypeConverter
    fun idsToText(ids: List<Int>): String = ids.joinToString(",")

    @TypeConverter
    fun textToIds(text: String): List<Int> = text.split(",").mapNotNull { it.toIntOrNull() }

    @TypeConverter
    fun historyToText(history: List<LoadEntry>): String =
        history.joinToString("|") { "${it.date};${it.weight};${it.reps}" }

    @TypeConverter
    fun textToHistory(text: String): List<LoadEntry> =
        if (text.isEmpty()) emptyList()
        else text.split("|").map { entry ->
            val (date, weight, reps) = entry.split(";")
            LoadEntry(date, weight.toDouble(), reps.toInt())
        }
}

/**
 * Na primeira vez que o app abre, o banco nasce com os treinos e exercícios de
 * exemplo do Trabalho 2. O insert é feito ali mesmo, durante a criação das
 * tabelas, para que a primeira busca de qualquer tela já os encontre.
 */
private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val converters = Converters()
        FakeRepository.seedExercises.forEach {
            db.execSQL(
                "INSERT INTO exercicios (id, name, muscleGroup, history) VALUES (?, ?, ?, ?)",
                arrayOf(it.id, it.name, it.muscleGroup, converters.historyToText(it.history)),
            )
        }
        FakeRepository.seedTemplates.forEach {
            db.execSQL(
                "INSERT INTO treinos (id, name, exerciseIds, notes) VALUES (?, ?, ?, ?)",
                arrayOf(it.id, it.name, converters.idsToText(it.exerciseIds), it.notes),
            )
        }
    }
}
