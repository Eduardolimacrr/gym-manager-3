package com.example.gymmobile.feature.progress

import androidx.lifecycle.ViewModel
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository

class ProgressViewModel : ViewModel() {

    /** Erro de cada campo do formulário de novo exercício; tudo `null` = válido. */
    data class FormErrors(
        val name: String? = null,
        val muscleGroup: String? = null,
        val weight: String? = null,
        val reps: String? = null,
    ) {
        val isValid: Boolean get() = name == null && muscleGroup == null && weight == null && reps == null
    }

    /** A própria `mutableStateListOf` do repositório. */
    val exercises: List<Exercise> = FakeRepository.exercises
    val muscleGroups: List<String> = FakeRepository.muscleGroups

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
                exercises.any { it.name.equals(name.trim(), ignoreCase = true) } -> "Esse exercício já está na lista"
                else -> null
            },
            muscleGroup = if (muscleGroup == null) "Escolha um grupo muscular" else null,
            weight = if (hasLoad && (weight == null || weight <= 0)) "Carga inválida" else null,
            reps = if (hasLoad && (reps == null || reps <= 0)) "Reps inválidas" else null,
        )
        if (errors.isValid && muscleGroup != null) {
            FakeRepository.addExercise(
                name = name,
                muscleGroup = muscleGroup,
                weight = if (hasLoad) weight else null,
                reps = if (hasLoad) reps else null,
            )
        }
        return errors
    }

    fun remove(exercise: Exercise) = FakeRepository.removeExercise(exercise.id)
}
