package com.example.gymmobile.navigation

/** Todas as rotas do app, num lugar só. As com `{argumento}` têm uma função que monta o caminho. */
object Rotas {
    // Abas da barra inferior
    const val HOME = "home"
    const val WORKOUTS = "workouts"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    // Tela cheia, sem barra inferior
    const val CREATE = "create"

    const val ARG_TEMPLATE_ID = "templateId"
    const val ARG_EXERCISE_ID = "exerciseId"

    const val ACTIVE = "active/{$ARG_TEMPLATE_ID}"
    const val WORKOUT_DETAIL = "workouts/{$ARG_TEMPLATE_ID}"
    const val PROGRESS_DETAIL = "progress/{$ARG_EXERCISE_ID}"
    const val EDIT_WORKOUT = "edit/{$ARG_TEMPLATE_ID}"

    fun active(templateId: Int) = "active/$templateId"
    fun workoutDetail(templateId: Int) = "workouts/$templateId"
    fun progressDetail(exerciseId: Int) = "progress/$exerciseId"
    fun editWorkout(templateId: Int) = "edit/$templateId"

    /** Rotas que mostram a barra inferior. */
    val TAB_ROUTES = listOf(HOME, WORKOUTS, PROGRESS, PROFILE)
}
