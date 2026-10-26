package com.example.gymmobile.feature.home

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
import com.example.gymmobile.data.WorkoutSummary
import com.example.gymmobile.data.WorkoutTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val greetingName: String,
        val streakValue: String,
        val weekVolume: String,
        val recent: List<WorkoutSummary>,
    )

    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(
        UiState(
            greetingName = FakeRepository.userName,
            streakValue = FakeRepository.streakDays.toString(),
            weekVolume = FakeRepository.weekVolume,
            recent = FakeRepository.recentWorkouts,
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * O app não tem agenda de treinos: o "Treino de hoje" é o primeiro de Meus treinos.
     * `null` = nenhum treino salvo, e o cartão some.
     */
    var today by mutableStateOf<WorkoutTemplate?>(null)
        private set

    /** Os exercícios de [today], na ordem do treino. */
    var todayExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set

    /** Chamada pelo `LaunchedEffect(Unit)` da tela: apagar ou editar o treino muda o cartão ao voltar. */
    suspend fun carregar() {
        withContext(Dispatchers.IO) {
            try {
                val first = db.workoutTemplateDao().buscarTodos().firstOrNull()
                todayExercises = first?.let { FakeRepository.exercisesOf(it, db.exerciseDao().buscarTodos()) }.orEmpty()
                today = first
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao buscar o treino de hoje", e)
            }
        }
    }
}
