package com.example.gymmobile.feature.create

import androidx.lifecycle.ViewModel
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateWorkoutViewModel : ViewModel() {

    data class UiState(
        val name: String = "",
        val notes: String = "",
        /** Ids dos exercícios escolhidos, na ordem de escolha. */
        val picked: List<String> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Catálogo = a lista de exercícios de Progresso: um exercício novo lá já aparece aqui. */
    val catalog: List<Exercise> = FakeRepository.exercises

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    /** `toggleChip` do protótipo: alterna, preservando a ordem de escolha. */
    fun toggleExercise(id: String) {
        _uiState.update { state ->
            val picked =
                if (id in state.picked) state.picked - id
                else state.picked + id
            state.copy(picked = picked)
        }
    }

    fun save() {
        val state = _uiState.value
        FakeRepository.addTemplate(name = state.name, exerciseIds = state.picked, notes = state.notes)
    }
}
