package com.example.gymmobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

/**
 * Um treino salvo — item da lista de Meus treinos, uma linha da tabela `treinos`.
 *
 * Guarda só os ids dos exercícios: nome, grupo muscular e cargas vêm da
 * tabela de exercícios, para que as duas listas nunca discordem.
 */
@Entity(tableName = "treinos")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val exerciseIds: List<Int>,
    val notes: String = "",
)

/** Um registro de carga no histórico de um exercício: `18 ago` / `80kg × 8`. */
data class LoadEntry(
    val date: String,
    val weight: Double,
    val reps: Int,
) {
    val label: String get() = "${formatWeight(weight)}kg × $reps"
}

/**
 * Um exercício acompanhado — item da lista de Progresso e catálogo do Novo treino,
 * uma linha da tabela `exercicios`.
 */
@Entity(tableName = "exercicios")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val muscleGroup: String,
    /** Mais recente primeiro. */
    val history: List<LoadEntry> = emptyList(),
) {
    val lastLoad: LoadEntry? get() = history.firstOrNull()

    /** Recorde pessoal: a maior carga já registrada. */
    val prWeight: Double? get() = history.maxOfOrNull { it.weight }

    /** 1RM estimado pela fórmula de Epley, `carga × (1 + reps / 30)`, no melhor registro. */
    val estimatedOneRepMax: Double?
        get() = history.maxOfOrNull { it.weight * (1 + it.reps / 30.0) }

    /**
     * Alturas de 0 a 100 das últimas seis cargas, da mais antiga para a mais
     * recente — a mesma escala que `BarChart` e `Sparkline` já desenhavam.
     */
    val bars: List<Int>
        get() {
            val recent = history.take(6).reversed()
            val max = recent.maxOfOrNull { it.weight }?.takeIf { it > 0 } ?: return emptyList()
            return recent.map { (it.weight / max * 100).roundToInt().coerceAtLeast(8) }
        }
}

/** Uma série: carga, repetições e se já foi concluída. */
data class ExerciseSet(
    val weight: Double,
    val reps: Int,
    val done: Boolean = false,
)

/** Um exercício dentro do treino em execução. */
data class ActiveExercise(
    val name: String,
    val sets: List<ExerciseSet>,
)

/** Um treino concluído, listado em "Últimos treinos". */
data class WorkoutSummary(
    val name: String,
    val day: String,
    val duration: String,
)

/** 60.0 -> "60"; 62.5 -> "62,5" (vírgula, como se escreve em pt-BR). */
fun formatWeight(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else "%.1f".format(value).replace('.', ',')
