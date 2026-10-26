package com.example.gymmobile.feature.create

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymmobile.data.AppDatabase
import com.example.gymmobile.data.DB_TAG
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Formulário de Novo treino e de Editar treino. `templateId == 0` = treino novo. */
class CreateWorkoutViewModel(
    application: Application,
    private val templateId: Int,
) : AndroidViewModel(application) {

    data class UiState(
        val name: String = "",
        val notes: String = "",
        /** Ids dos exercícios escolhidos, na ordem de escolha. */
        val picked: List<Int> = emptyList(),
        /**
         * Na edição, `false` até o treino chegar do banco: um formulário ainda vazio
         * não pode ser salvo por cima do treino nem apagar o que a pessoa já digitou.
         */
        val loaded: Boolean = true,
    )

    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(UiState(loaded = templateId == 0))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val isEditing: Boolean get() = templateId != 0

    /**
     * Catálogo = a tabela de exercícios: um exercício novo em Progresso já aparece aqui.
     * `StateFlow`, não `mutableStateOf`: é preenchido pelo `init`, em outra thread, enquanto a
     * tela ainda está sendo composta, e um estado do Compose criado nessa hora perde a escrita.
     */
    private val _catalog = MutableStateFlow<List<Exercise>>(emptyList())
    val catalog: StateFlow<List<Exercise>> = _catalog.asStateFlow()

    init {
        // Uma vez só, no init: buscar de novo ao girar a tela apagaria o que já foi digitado.
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _catalog.value = db.exerciseDao().buscarTodos()
                if (isEditing) {
                    db.workoutTemplateDao().buscarPorId(templateId)?.let { template ->
                        _uiState.value = UiState(template.name, template.notes, template.exerciseIds)
                    }
                }
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao abrir o formulário do treino $templateId", e)
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    /** `toggleChip` do protótipo: alterna, preservando a ordem de escolha. */
    fun toggleExercise(id: Int) {
        _uiState.update { state ->
            val picked =
                if (id in state.picked) state.picked - id
                else state.picked + id
            state.copy(picked = picked)
        }
    }

    /** Escrito na thread de IO quando a gravação falha e lido no toque, na principal. */
    @Volatile
    private var saving = false

    /**
     * Nome em branco vira "Treino sem nome", como no `saveWorkout()` do protótipo.
     * [onSaved] só roda depois da gravação, para a tela anterior já encontrar o treino no banco.
     */
    fun save(onSaved: () -> Unit) {
        // O botão fica ativo durante a gravação: um segundo toque gravaria de novo e voltaria duas telas.
        if (saving) return
        saving = true
        val state = _uiState.value
        val template = WorkoutTemplate(
            id = templateId,
            name = state.name.trim().ifBlank { "Treino sem nome" },
            exerciseIds = state.picked,
            notes = state.notes.trim(),
        )
        // Fora do viewModelScope: se a tela fechar no meio, a gravação não é cancelada junto.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isEditing) db.workoutTemplateDao().atualizar(template)
                else db.workoutTemplateDao().inserir(template)
                withContext(Dispatchers.Main) { onSaved() }
            } catch (e: Exception) {
                // Fica no formulário, com o que foi digitado, e libera para tentar de novo.
                Log.e(DB_TAG, "Erro ao salvar o treino", e)
                saving = false
            }
        }
    }

    companion object {
        fun factory(templateId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { CreateWorkoutViewModel(this[APPLICATION_KEY]!!, templateId) }
        }
    }
}
