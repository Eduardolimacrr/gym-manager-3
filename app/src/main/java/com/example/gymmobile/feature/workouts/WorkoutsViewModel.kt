package com.example.gymmobile.feature.workouts

import androidx.lifecycle.ViewModel
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.data.WorkoutTemplate

class WorkoutsViewModel : ViewModel() {
    /** A própria `mutableStateListOf` do repositório: um treino salvo ou removido aparece aqui na hora. */
    val templates: List<WorkoutTemplate> = FakeRepository.templates

    fun muscleGroupsOf(template: WorkoutTemplate): String = FakeRepository.muscleGroupsOf(template)

    fun exerciseCountOf(template: WorkoutTemplate): Int = FakeRepository.exercisesOf(template).size

    fun remove(template: WorkoutTemplate) = FakeRepository.removeTemplate(template.id)
}
