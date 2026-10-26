package com.example.gymmobile.feature.workouts

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
import com.example.gymmobile.data.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    /** `null` até a primeira busca terminar, para não piscar "Nenhum treino ainda". */
    var templates by mutableStateOf<List<WorkoutTemplate>?>(null)
        private set

    /** Também vem do banco: os cards mostram grupos musculares e quantidade de exercícios. */
    private var exercises by mutableStateOf<List<Exercise>>(emptyList())

    /** Chamada pelo `LaunchedEffect(Unit)` da tela, toda vez que ela abre. */
    suspend fun carregar() {
        withContext(Dispatchers.IO) {
            try {
                exercises = db.exerciseDao().buscarTodos()
                templates = db.workoutTemplateDao().buscarTodos()
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao buscar os treinos", e)
            }
        }
    }

    fun muscleGroupsOf(template: WorkoutTemplate): String =
        FakeRepository.muscleGroupsOf(FakeRepository.exercisesOf(template, exercises))

    fun exerciseCountOf(template: WorkoutTemplate): Int = FakeRepository.exercisesOf(template, exercises).size

    fun remove(template: WorkoutTemplate) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.workoutTemplateDao().deletar(template)
                carregar()
            } catch (e: Exception) {
                Log.e(DB_TAG, "Erro ao remover o treino ${template.id}", e)
            }
        }
    }
}
