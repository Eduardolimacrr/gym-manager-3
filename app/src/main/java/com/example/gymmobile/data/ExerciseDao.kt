package com.example.gymmobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {

    @Insert
    suspend fun inserir(exercise: Exercise)

    @Query("SELECT * FROM exercicios ORDER BY id")
    suspend fun buscarTodos(): List<Exercise>

    /** `null` se o exercício foi removido. */
    @Query("SELECT * FROM exercicios WHERE id = :id")
    suspend fun buscarPorId(id: Int): Exercise?

    @Update
    suspend fun atualizar(exercise: Exercise)

    @Delete
    suspend fun deletar(exercise: Exercise)
}
