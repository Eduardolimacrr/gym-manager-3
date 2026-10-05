package com.example.gymmobile.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.Calendar

/**
 * Fonte de dados única do app, em memória.
 *
 * É um `object` para que o estado sobreviva à troca de telas: um treino
 * salvo em Novo treino precisa aparecer imediatamente em Meus treinos, e uma
 * carga registrada no detalhe do exercício precisa mudar o volume do treino
 * que o usa. As duas listas são `mutableStateListOf`: qualquer tela que as lê
 * é redesenhada quando um item entra, sai ou é trocado.
 */
object FakeRepository {

    // ----- perfil -----
    const val userName = "Lucas"
    const val userFullName = "Lucas Costa"
    const val userInitials = "LC"
    const val userSubtitle = "Treinando há 8 meses"

    // ----- home -----
    const val streakDays = 4
    const val weekVolume = "12,4"
    const val todayTag = "Push · dia 3"
    const val todayTitle = "Peito, ombro e tríceps"
    const val todaySubtitle = "5 exercícios · ~25 min"

    /** Template aberto pelo botão "Iniciar treino" da Home. */
    const val todayTemplateId = "push-a"

    val recentWorkouts = listOf(
        WorkoutSummary("Pull B", "seg", "48min"),
        WorkoutSummary("Legs", "sáb", "55min"),
        WorkoutSummary("Push A", "qui", "51min"),
    )

    // ----- exercícios (lista de Progresso) -----
    val muscleGroups = listOf("Peito", "Costas", "Ombro", "Bíceps", "Tríceps", "Pernas", "Posterior", "Core")

    val exercises: SnapshotStateList<Exercise> = mutableStateListOf(
        Exercise(
            "supino-reto", "Supino reto", "Peito",
            listOf(
                LoadEntry("18 ago", 80.0, 8),
                LoadEntry("14 ago", 75.0, 10),
                LoadEntry("10 ago", 75.0, 8),
                LoadEntry("4 ago", 60.0, 10),
                LoadEntry("28 jul", 55.0, 10),
                LoadEntry("21 jul", 45.0, 12),
            ),
        ),
        Exercise("supino-inclinado", "Supino inclinado", "Peito", listOf(LoadEntry("12 ago", 50.0, 10))),
        Exercise(
            "desenvolvimento-halteres", "Desenvolvimento halteres", "Ombro",
            listOf(LoadEntry("18 ago", 18.0, 12), LoadEntry("14 ago", 16.0, 12)),
        ),
        Exercise("elevacao-lateral", "Elevação lateral", "Ombro", listOf(LoadEntry("18 ago", 8.0, 15))),
        Exercise("triceps-corda", "Tríceps corda", "Tríceps", listOf(LoadEntry("18 ago", 25.0, 12))),
        Exercise("puxada-frente", "Puxada frente", "Costas", listOf(LoadEntry("16 ago", 60.0, 10))),
        Exercise("remada-curvada", "Remada curvada", "Costas", listOf(LoadEntry("16 ago", 50.0, 10))),
        Exercise("rosca-direta", "Rosca direta", "Bíceps", listOf(LoadEntry("16 ago", 30.0, 10))),
        Exercise(
            "agachamento-livre", "Agachamento livre", "Pernas",
            listOf(
                LoadEntry("17 ago", 110.0, 6),
                LoadEntry("12 ago", 100.0, 8),
                LoadEntry("5 ago", 90.0, 8),
                LoadEntry("29 jul", 85.0, 8),
                LoadEntry("22 jul", 70.0, 10),
                LoadEntry("15 jul", 60.0, 10),
            ),
        ),
        Exercise("leg-press", "Leg press", "Pernas"),
        Exercise("cadeira-extensora", "Cadeira extensora", "Pernas"),
        Exercise(
            "levantamento-terra", "Levantamento terra", "Posterior",
            listOf(
                LoadEntry("15 ago", 140.0, 4),
                LoadEntry("8 ago", 130.0, 5),
                LoadEntry("1 ago", 120.0, 5),
                LoadEntry("25 jul", 110.0, 6),
                LoadEntry("18 jul", 100.0, 6),
                LoadEntry("11 jul", 90.0, 8),
            ),
        ),
    )

    fun exerciseById(id: String): Exercise? = exercises.firstOrNull { it.id == id }

    /** `weight`/`reps` nulos = exercício entra sem nenhuma carga registrada. */
    fun addExercise(name: String, muscleGroup: String, weight: Double?, reps: Int?) {
        val firstLoad = if (weight != null && reps != null) LoadEntry(todayLabel(), weight, reps) else null
        exercises += Exercise(
            id = "exercise-${System.currentTimeMillis()}",
            name = name.trim(),
            muscleGroup = muscleGroup,
            history = listOfNotNull(firstLoad),
        )
    }

    /** Remove o exercício e também o tira dos treinos que o usavam. */
    fun removeExercise(id: String) {
        exercises.removeAll { it.id == id }
        for (index in templates.indices) {
            val template = templates[index]
            if (id in template.exerciseIds) {
                templates[index] = template.copy(exerciseIds = template.exerciseIds - id)
            }
        }
    }

    /** Registra uma carga com a data de hoje no topo do histórico. */
    fun addLoad(exerciseId: String, weight: Double, reps: Int) {
        val index = exercises.indexOfFirst { it.id == exerciseId }
        if (index == -1) return
        val exercise = exercises[index]
        exercises[index] = exercise.copy(
            history = listOf(LoadEntry(todayLabel(), weight, reps)) + exercise.history
        )
    }

    // ----- treinos (lista de Meus treinos) -----
    val templates: SnapshotStateList<WorkoutTemplate> = mutableStateListOf(
        WorkoutTemplate(
            "push-a", "Push A",
            listOf("supino-reto", "supino-inclinado", "desenvolvimento-halteres", "elevacao-lateral", "triceps-corda"),
        ),
        WorkoutTemplate("pull-b", "Pull B", listOf("puxada-frente", "remada-curvada", "rosca-direta")),
        WorkoutTemplate(
            "legs", "Legs",
            listOf("agachamento-livre", "leg-press", "cadeira-extensora", "levantamento-terra"),
            notes = "Aquecer 10 min na bike antes do agachamento.",
        ),
        WorkoutTemplate(
            "full-body", "Full body",
            listOf("supino-reto", "agachamento-livre", "remada-curvada", "desenvolvimento-halteres", "levantamento-terra"),
        ),
    )

    fun templateById(id: String): WorkoutTemplate? = templates.firstOrNull { it.id == id }

    /** Nome em branco vira "Treino sem nome", como no `saveWorkout()` do protótipo. */
    fun addTemplate(name: String, exerciseIds: List<String>, notes: String) {
        templates += WorkoutTemplate(
            id = "custom-${System.currentTimeMillis()}",
            name = name.trim().ifBlank { "Treino sem nome" },
            exerciseIds = exerciseIds,
            notes = notes.trim(),
        )
    }

    fun removeTemplate(id: String) {
        templates.removeAll { it.id == id }
    }

    /** Junta as duas listas: os exercícios de um treino, na ordem em que foram escolhidos. */
    fun exercisesOf(template: WorkoutTemplate): List<Exercise> =
        template.exerciseIds.mapNotNull(::exerciseById)

    /** `Peito · Ombro · Tríceps`: grupos musculares distintos, na ordem dos exercícios. */
    fun muscleGroupsOf(template: WorkoutTemplate): String =
        exercisesOf(template).map { it.muscleGroup }.distinct().joinToString(" · ")
            .ifEmpty { "Sem exercícios" }

    fun templatesUsing(exerciseId: String): List<WorkoutTemplate> =
        templates.filter { exerciseId in it.exerciseIds }

    // ----- treino ativo -----
    const val SETS_PER_EXERCISE = 3

    /** Cada exercício do treino entra com três séries repetindo a última carga registrada. */
    fun activeExercisesFor(templateId: String): List<ActiveExercise> {
        val template = templateById(templateId) ?: return emptyList()
        return exercisesOf(template).map { exercise ->
            val last = exercise.lastLoad
            val set = ExerciseSet(weight = last?.weight ?: 0.0, reps = last?.reps ?: 10)
            ActiveExercise(exercise.name, List(SETS_PER_EXERCISE) { set })
        }
    }

    private val monthLabels =
        listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

    /** Hoje no formato do histórico: `5 out`. */
    private fun todayLabel(): String {
        val now = Calendar.getInstance()
        return "${now.get(Calendar.DAY_OF_MONTH)} ${monthLabels[now.get(Calendar.MONTH)]}"
    }
}
