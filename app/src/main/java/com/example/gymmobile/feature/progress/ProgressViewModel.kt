package com.example.gymmobile.feature.progress

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gymmobile.data.AppDatabase
import com.example.gymmobile.data.DB_TAG
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.data.LoadEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    /** Erro de cada campo do formulário de novo exercício; tudo `null` = válido. */
    data class FormErrors(
        val name: String? = null,
        val muscleGroup: String? = null,
        val weight: String? = null,
        val reps: String? = null,
    ) {
        val isValid: Boolean get() = name == null && muscleGroup == null && weight == null && reps == null
    }

    private val db = AppDatabase.getDatabase(application)

    /** `null` até a primeira busca terminar. */
    var exercises by mutableStateOf<List<Exercise>?>(null)
        private set
    val muscleGroups: List<String> = FakeRepository.muscleGroups

    /** Chamada pelo `LaunchedEffect(Unit)` da tela, toda vez que ela abre. */
    suspend fun carregar() {
        withContext(Dispatchers.IO) {
            try {
                exercises = db.exerciseDao().buscarTodos()
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao buscar os exercícios", e)
            }
        }
    }

    /**
     * Valida e, se estiver tudo certo, adiciona. A carga inicial é opcional,
     * mas carga e reps andam juntas: preencher só uma das duas é erro.
     */
    fun add(name: String, muscleGroup: String?, rawWeight: String, rawReps: String): FormErrors {
        val weight = rawWeight.replace(',', '.').toDoubleOrNull()
        val reps = rawReps.toIntOrNull()
        val hasLoad = rawWeight.isNotBlank() || rawReps.isNotBlank()

        val errors = FormErrors(
            name = when {
                name.isBlank() -> "Informe o nome do exercício"
                exercises.orEmpty().any { it.name.equals(name.trim(), ignoreCase = true) } -> "Esse exercício já está na lista"
                else -> null
            },
            muscleGroup = if (muscleGroup == null) "Escolha um grupo muscular" else null,
            weight = if (hasLoad && (weight == null || weight <= 0)) "Carga inválida" else null,
            reps = if (hasLoad && (reps == null || reps <= 0)) "Reps inválidas" else null,
        )
        if (errors.isValid && muscleGroup != null) {
            val firstLoad =
                if (hasLoad && weight != null && reps != null) LoadEntry(FakeRepository.todayLabel(), weight, reps)
                else null
            val exercise = Exercise(name = name.trim(), muscleGroup = muscleGroup, history = listOfNotNull(firstLoad))
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.exerciseDao().inserir(exercise)
                    carregar()
                } catch (e: Exception) {
                    Log.e(DB_TAG, "Erro ao inserir o exercício", e)
                }
            }
        }
        return errors
    }

    /** Apaga o exercício e também o tira dos treinos que o usavam. */
    fun remove(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.exerciseDao().deletar(exercise)
                db.workoutTemplateDao().buscarTodos()
                    .filter { exercise.id in it.exerciseIds }
                    .forEach { db.workoutTemplateDao().atualizar(it.copy(exerciseIds = it.exerciseIds - exercise.id)) }
                carregar()
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao remover o exercício ${exercise.id}", e)
            }
        }
    }
}
