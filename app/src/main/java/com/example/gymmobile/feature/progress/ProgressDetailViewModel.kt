package com.example.gymmobile.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.data.WorkoutTemplate

class ProgressDetailViewModel(private val exerciseId: String) : ViewModel() {

    data class LoadErrors(val weight: String? = null, val reps: String? = null) {
        val isValid: Boolean get() = weight == null && reps == null
    }

    /**
     * Getters, não cópias: são lidos durante a composição, então registrar uma
     * carga redesenha recorde, gráfico e histórico na mesma hora.
     */
    val exercise: Exercise? get() = FakeRepository.exerciseById(exerciseId)

    /** Os treinos que usam este exercício — dado que vem da outra lista. */
    val usedIn: List<WorkoutTemplate> get() = FakeRepository.templatesUsing(exerciseId)

    fun registerLoad(rawWeight: String, rawReps: String): LoadErrors {
        val weight = rawWeight.replace(',', '.').toDoubleOrNull()
        val reps = rawReps.toIntOrNull()
        val errors = LoadErrors(
            weight = if (weight == null || weight <= 0) "Carga inválida" else null,
            reps = if (reps == null || reps <= 0) "Reps inválidas" else null,
        )
        if (weight != null && reps != null && errors.isValid) {
            FakeRepository.addLoad(exerciseId, weight, reps)
        }
        return errors
    }

    companion object {
        fun factory(exerciseId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProgressDetailViewModel(exerciseId) }
        }
    }
}
