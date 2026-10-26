package com.example.gymmobile.feature.progress

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
import com.example.gymmobile.data.LoadEntry
import com.example.gymmobile.data.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressDetailViewModel(
    application: Application,
    private val exerciseId: Int,
) : AndroidViewModel(application) {

    data class LoadErrors(val weight: String? = null, val reps: String? = null) {
        val isValid: Boolean get() = weight == null && reps == null
    }

    private val db = AppDatabase.getDatabase(application)

    /** Separa "ainda buscando" de "buscou e o exercício não existe" (`exercise == null`). */
    var loaded by mutableStateOf(false)
        private set

    var exercise by mutableStateOf<Exercise?>(null)
        private set

    /** Os treinos que usam este exercício — dado que vem da outra tabela. */
    var usedIn by mutableStateOf<List<WorkoutTemplate>>(emptyList())
        private set

    /** A rota traz só o id; o exercício vem do banco por `buscarPorId`. */
    suspend fun carregar() {
        withContext(Dispatchers.IO) {
            try {
                usedIn = db.workoutTemplateDao().buscarTodos().filter { exerciseId in it.exerciseIds }
                exercise = db.exerciseDao().buscarPorId(exerciseId)
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao buscar o exercício $exerciseId", e)
            }
            loaded = true
        }
    }

    fun registerLoad(rawWeight: String, rawReps: String): LoadErrors {
        val weight = rawWeight.replace(',', '.').toDoubleOrNull()
        val reps = rawReps.toIntOrNull()
        val errors = LoadErrors(
            weight = if (weight == null || weight <= 0) "Carga inválida" else null,
            reps = if (reps == null || reps <= 0) "Reps inválidas" else null,
        )
        val current = exercise
        if (weight != null && reps != null && errors.isValid && current != null) {
            // A carga nova entra no topo do histórico e a linha inteira é regravada com `atualizar`.
            val updated = current.copy(history = listOf(LoadEntry(FakeRepository.todayLabel(), weight, reps)) + current.history)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.exerciseDao().atualizar(updated)
                    carregar()
                } catch (e: Exception) {
                    Log.e(DB_TAG, "Erro ao registrar carga no exercício $exerciseId", e)
                }
            }
        }
        return errors
    }

    companion object {
        fun factory(exerciseId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProgressDetailViewModel(this[APPLICATION_KEY]!!, exerciseId) }
        }
    }
}
