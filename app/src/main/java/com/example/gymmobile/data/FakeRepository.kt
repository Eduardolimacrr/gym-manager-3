package com.example.gymmobile.data

import java.util.Calendar

/**
 * Dados fixos do app e as regras que cruzam as duas tabelas.
 *
 * Treinos e exercícios agora moram no banco Room (`AppDatabase`); daqui saem
 * só os exemplos com que o banco nasce na primeira abertura (`seed*`), os
 * textos fixos de perfil e Home, e funções puras que recebem as listas já
 * buscadas do banco.
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

    val recentWorkouts = listOf(
        WorkoutSummary("Pull B", "seg", "48min"),
        WorkoutSummary("Legs", "sáb", "55min"),
        WorkoutSummary("Push A", "qui", "51min"),
    )

    // ----- exercícios -----
    val muscleGroups = listOf("Peito", "Costas", "Ombro", "Bíceps", "Tríceps", "Pernas", "Posterior", "Core")

    // ----- exemplos com que o banco nasce (tabela `exercicios`) -----
    /** Ids fixos para que `seedTemplates` possa apontar para eles. */
    val seedExercises = listOf(
        Exercise(
            1, "Supino reto", "Peito",
            listOf(
                LoadEntry("18 ago", 80.0, 8),
                LoadEntry("14 ago", 75.0, 10),
                LoadEntry("10 ago", 75.0, 8),
                LoadEntry("4 ago", 60.0, 10),
                LoadEntry("28 jul", 55.0, 10),
                LoadEntry("21 jul", 45.0, 12),
            ),
        ),
        Exercise(2, "Supino inclinado", "Peito", listOf(LoadEntry("12 ago", 50.0, 10))),
        Exercise(
            3, "Desenvolvimento halteres", "Ombro",
            listOf(LoadEntry("18 ago", 18.0, 12), LoadEntry("14 ago", 16.0, 12)),
        ),
        Exercise(4, "Elevação lateral", "Ombro", listOf(LoadEntry("18 ago", 8.0, 15))),
        Exercise(5, "Tríceps corda", "Tríceps", listOf(LoadEntry("18 ago", 25.0, 12))),
        Exercise(6, "Puxada frente", "Costas", listOf(LoadEntry("16 ago", 60.0, 10))),
        Exercise(7, "Remada curvada", "Costas", listOf(LoadEntry("16 ago", 50.0, 10))),
        Exercise(8, "Rosca direta", "Bíceps", listOf(LoadEntry("16 ago", 30.0, 10))),
        Exercise(
            9, "Agachamento livre", "Pernas",
            listOf(
                LoadEntry("17 ago", 110.0, 6),
                LoadEntry("12 ago", 100.0, 8),
                LoadEntry("5 ago", 90.0, 8),
                LoadEntry("29 jul", 85.0, 8),
                LoadEntry("22 jul", 70.0, 10),
                LoadEntry("15 jul", 60.0, 10),
            ),
        ),
        Exercise(10, "Leg press", "Pernas"),
        Exercise(11, "Cadeira extensora", "Pernas"),
        Exercise(
            12, "Levantamento terra", "Posterior",
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

    // ----- exemplos com que o banco nasce (tabela `treinos`) -----
    val seedTemplates = listOf(
        // Supino reto, Supino inclinado, Desenvolvimento, Elevação lateral, Tríceps corda
        WorkoutTemplate(1, "Push A", listOf(1, 2, 3, 4, 5)),
        // Puxada frente, Remada curvada, Rosca direta
        WorkoutTemplate(2, "Pull B", listOf(6, 7, 8)),
        // Agachamento, Leg press, Cadeira extensora, Terra
        WorkoutTemplate(3, "Legs", listOf(9, 10, 11, 12), notes = "Aquecer 10 min na bike antes do agachamento."),
        // Supino reto, Agachamento, Remada curvada, Desenvolvimento, Terra
        WorkoutTemplate(4, "Full body", listOf(1, 9, 7, 3, 12)),
    )

    // ----- regras que cruzam as duas tabelas -----
    /** Junta as duas tabelas: os exercícios de um treino, na ordem em que foram escolhidos. */
    fun exercisesOf(template: WorkoutTemplate, allExercises: List<Exercise>): List<Exercise> =
        template.exerciseIds.mapNotNull { id -> allExercises.firstOrNull { it.id == id } }

    /** `Peito · Ombro · Tríceps`: grupos musculares distintos, na ordem dos exercícios. */
    fun muscleGroupsOf(exercises: List<Exercise>): String =
        exercises.map { it.muscleGroup }.distinct().joinToString(" · ")
            .ifEmpty { "Sem exercícios" }

    // ----- treino ativo -----
    const val SETS_PER_EXERCISE = 3

    /** Cada exercício do treino entra com três séries repetindo a última carga registrada. */
    fun activeExercisesFor(exercises: List<Exercise>): List<ActiveExercise> =
        exercises.map { exercise ->
            val last = exercise.lastLoad
            val set = ExerciseSet(weight = last?.weight ?: 0.0, reps = last?.reps ?: 10)
            ActiveExercise(exercise.name, List(SETS_PER_EXERCISE) { set })
        }

    private val monthLabels =
        listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

    /** Hoje no formato do histórico: `5 out`. */
    fun todayLabel(): String {
        val now = Calendar.getInstance()
        return "${now.get(Calendar.DAY_OF_MONTH)} ${monthLabels[now.get(Calendar.MONTH)]}"
    }
}
