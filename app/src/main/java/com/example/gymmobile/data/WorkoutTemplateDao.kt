package com.example.gymmobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkoutTemplateDao {

    @Insert
    suspend fun inserir(template: WorkoutTemplate)

    /** Na ordem em que foram criados, como a lista em memória do Trabalho 2. */
    @Query("SELECT * FROM treinos ORDER BY id")
    suspend fun buscarTodos(): List<WorkoutTemplate>

    /** `null` se o treino foi removido. */
    @Query("SELECT * FROM treinos WHERE id = :id")
    suspend fun buscarPorId(id: Int): WorkoutTemplate?

    @Update
    suspend fun atualizar(template: WorkoutTemplate)

    @Delete
    suspend fun deletar(template: WorkoutTemplate)
}
