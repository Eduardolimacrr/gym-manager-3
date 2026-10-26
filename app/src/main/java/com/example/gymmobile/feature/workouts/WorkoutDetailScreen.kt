package com.example.gymmobile.feature.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.FakeRepository
import com.example.gymmobile.ui.components.GymTopBar
import com.example.gymmobile.ui.components.PrimaryButton
import com.example.gymmobile.ui.components.SectionLabel
import com.example.gymmobile.ui.components.SquareIconButton
import com.example.gymmobile.ui.components.StatCard
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymType

/**
 * Detalhe de um treino, buscado no banco pelo id que veio na rota. Vai além de
 * reexibir o item: cruza os ids do treino com a tabela de exercícios, calcula
 * duração e volume estimados a partir das últimas cargas, cada exercício abre o
 * próprio detalhe e o lápis abre a edição.
 */
@Composable
fun WorkoutDetailScreen(
    templateId: Int,
    onBack: () -> Unit,
    onOpenExercise: (Int) -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    viewModel: WorkoutDetailViewModel = viewModel(
        factory = WorkoutDetailViewModel.factory(templateId)
    ),
) {
    LaunchedEffect(Unit) { viewModel.carregar() }
    if (!viewModel.loaded) return
    val template = viewModel.template

    Scaffold(
        topBar = {
            GymTopBar(title = template?.name ?: "Treino", onBack = onBack) {
                if (template != null) {
                    SquareIconButton(
                        icon = Icons.Outlined.Edit,
                        contentDescription = "Editar treino",
                        onClick = onEdit,
                        modifier = Modifier.padding(end = 14.dp),
                    )
                }
            }
        },
        containerColor = GymColors.Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        if (template == null) {
            Text(
                text = "Este treino não existe mais.",
                style = GymType.body14.copy(color = GymColors.TextMuted),
                modifier = Modifier.padding(innerPadding).padding(18.dp),
            )
            return@Scaffold
        }

        val exercises = viewModel.exercises
        val minutes = WorkoutDetailViewModel.estimatedMinutes(exercises.size)
        val (volume, volumeUnit) = WorkoutDetailViewModel.formatVolume(
            WorkoutDetailViewModel.estimatedVolumeKg(exercises)
        )

        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp),
        ) {
            item {
                Text(
                    text = viewModel.muscleGroups(),
                    style = GymType.body13.copy(color = GymColors.TextSecondary),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        label = "Duração estimada",
                        value = "~$minutes",
                        unit = "min",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Volume estimado",
                        value = volume,
                        unit = volumeUnit,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = "Calculado com a última carga de cada exercício × " +
                        "${FakeRepository.SETS_PER_EXERCISE} séries.",
                    style = GymType.body11.copy(color = GymColors.TextMuted),
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (template.notes.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Observações")
                    Text(text = template.notes, style = GymType.body13)
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("Exercícios · ${exercises.size}")
                if (exercises.isEmpty()) {
                    Text(
                        text = "Nenhum exercício neste treino.",
                        style = GymType.body13.copy(color = GymColors.TextMuted),
                    )
                }
            }
            items(items = exercises, key = { it.id }) { exercise ->
                ExerciseRow(exercise = exercise, onClick = { onOpenExercise(exercise.id) })
                Spacer(Modifier.height(8.dp))
            }
            item {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "Iniciar treino", onClick = onStart, icon = Icons.Filled.PlayArrow)
            }
        }
    }
}

/** Um exercício do treino, com a última carga vinda da lista de Progresso. */
@Composable
private fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = GymShape.card,
        colors = CardDefaults.cardColors(containerColor = GymColors.Surface),
        border = BorderStroke(0.5.dp, GymColors.BorderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = GymType.body14.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    text = exercise.muscleGroup,
                    style = GymType.body12.copy(color = GymColors.TextMuted),
                )
            }
            Text(
                text = exercise.lastLoad?.label ?: "sem registro",
                style = GymType.mono12.copy(color = GymColors.TextSecondary),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GymColors.TextMuted,
                modifier = Modifier.padding(start = 6.dp).size(18.dp),
            )
        }
    }
}
