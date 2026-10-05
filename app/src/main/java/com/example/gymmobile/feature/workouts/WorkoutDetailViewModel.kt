package com.example.gymmobile.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.data.WorkoutTemplate
import com.example.gymmobile.data.formatWeight

class WorkoutDetailViewModel(private val templateId: String) : ViewModel() {

    /**
     * Getter, não cópia: é lido durante a composição, então registrar uma
     * carga no detalhe do exercício e voltar já mostra o volume novo.
     */
    val template: WorkoutTemplate? get() = FakeRepository.templateById(templateId)

    fun exercisesOf(template: WorkoutTemplate): List<Exercise> = FakeRepository.exercisesOf(template)

    fun muscleGroupsOf(template: WorkoutTemplate): String = FakeRepository.muscleGroupsOf(template)

    companion object {
        /** ~40s executando a série + 60s de descanso, o mesmo do Treino ativo. */
        private const val SECONDS_PER_SET = 100

        fun factory(templateId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { WorkoutDetailViewModel(templateId) }
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
