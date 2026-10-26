package com.example.gymmobile.feature.workouts

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymmobile.data.AppDatabase
import com.example.gymmobile.data.DB_TAG
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.data.WorkoutTemplate
import com.example.gymmobile.data.formatWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutDetailViewModel(
    application: Application,
    private val templateId: Int,
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    /** Separa "ainda buscando" de "buscou e o treino não existe" (`template == null`). */
    var loaded by mutableStateOf(false)
        private set

    var template by mutableStateOf<WorkoutTemplate?>(null)
        private set

    /** Os exercícios do treino, na ordem dele. */
    var exercises by mutableStateOf<List<Exercise>>(emptyList())
        private set

    /**
     * A rota traz só o id; o treino vem do banco por `buscarPorId`. Roda toda
     * vez que a tela abre, então voltar de uma edição ou de um registro de carga
     * já mostra os dados novos.
     */
    suspend fun carregar() {
        withContext(Dispatchers.IO) {
            try {
                val found = db.workoutTemplateDao().buscarPorId(templateId)
                exercises = found?.let { FakeRepository.exercisesOf(it, db.exerciseDao().buscarTodos()) }.orEmpty()
                template = found
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao buscar o treino $templateId", e)
            }
            loaded = true
        }
    }

    fun muscleGroups(): String = FakeRepository.muscleGroupsOf(exercises)

    companion object {
        /** ~40s executando a série + 60s de descanso, o mesmo do Treino ativo. */
        private const val SECONDS_PER_SET = 100

        fun factory(templateId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { WorkoutDetailViewModel(this[APPLICATION_KEY]!!, templateId) }
        }

        fun estimatedMinutes(exerciseCount: Int): Int =
            exerciseCount * FakeRepository.SETS_PER_EXERCISE * SECONDS_PER_SET / 60

        /** Soma de `última carga × reps × séries`. Exercício sem registro conta zero. */
        fun estimatedVolumeKg(exercises: List<Exercise>): Double =
            exercises.sumOf { exercise ->
                val last = exercise.lastLoad ?: return@sumOf 0.0
                last.weight * last.reps * FakeRepository.SETS_PER_EXERCISE
            }

        /** 5328.0 -> ("5,3", "t"); 640.0 -> ("640", "kg"). */
        fun formatVolume(kg: Double): Pair<String, String> =
            if (kg >= 1000) "%.1f".format(kg / 1000).replace('.', ',') to "t"
            else formatWeight(kg) to "kg"
    }
}
