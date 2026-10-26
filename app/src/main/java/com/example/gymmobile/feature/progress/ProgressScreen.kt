package com.example.gymmobile.feature.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymmobile.data.Exercise
import com.example.gymmobile.data.formatWeight
import com.example.gymmobile.ui.components.ConfirmRemoveDialog
import com.example.gymmobile.ui.components.GymTextField
import com.example.gymmobile.ui.components.PrimaryButton
import com.example.gymmobile.ui.components.SectionLabel
import com.example.gymmobile.ui.components.SelectableChip
import com.example.gymmobile.ui.components.SquareIconButton
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymType

@Composable
fun ProgressScreen(
    onOpenExercise: (Int) -> Unit,
    viewModel: ProgressViewModel = viewModel(),
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    // Exercício esperando confirmação de remoção; `null` = diálogo fechado.
    var pendingRemoval by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar() }
    val exercises = viewModel.exercises ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GymColors.Bg)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Progresso", style = GymType.display19)
                SquareIconButton(
                    icon = if (showForm) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = if (showForm) "Fechar formulário" else "Novo exercício",
                    onClick = { showForm = !showForm },
                    accent = !showForm,
                )
            }
            Text(
                text = "${exercises.size} exercícios · toque para ver, segure para remover",
                style = GymType.body12.copy(color = GymColors.TextMuted),
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp),
            )
        }
        if (showForm) {
            item {
                NewExerciseForm(
                    muscleGroups = viewModel.muscleGroups,
                    onSubmit = viewModel::add,
                    onAdded = { showForm = false },
                )
                Spacer(Modifier.height(18.dp))
            }
        }
        items(items = exercises, key = { it.id }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClick = { onOpenExercise(exercise.id) },
                onLongClick = { pendingRemoval = exercise },
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    pendingRemoval?.let { exercise ->
        ConfirmRemoveDialog(
            title = "Remover ${exercise.name}?",
            message = "O histórico de cargas é apagado e o exercício sai dos treinos que o usam.",
            onConfirm = {
                viewModel.remove(exercise)
                pendingRemoval = null
            },
            onDismiss = { pendingRemoval = null },
        )
    }
}

/** Antigo `.exercise-list-row`, agora um `Card`: toque abre o detalhe, toque longo pede remoção. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GymShape.card)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = GymShape.card,
        colors = CardDefaults.cardColors(containerColor = GymColors.Surface),
        border = BorderStroke(0.5.dp, GymColors.BorderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Largura fixa de seis barras, para os nomes ficarem alinhados mesmo sem histórico.
            Box(modifier = Modifier.width(34.dp)) {
                Sparkline(bars = exercise.bars)
            }
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
                text = exercise.prWeight?.let { "${formatWeight(it)}kg" } ?: "—",
                style = GymType.mono12.copy(color = GymColors.TextSecondary),
            )
        }
    }
}

/** Formulário de novo exercício. O estado dos campos é da tela; a validação é do ViewModel. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewExerciseForm(
    muscleGroups: List<String>,
    onSubmit: (String, String?, String, String) -> ProgressViewModel.FormErrors,
    onAdded: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var muscleGroup by rememberSaveable { mutableStateOf<String?>(null) }
    var weight by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf(ProgressViewModel.FormErrors()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GymShape.card)
            .background(GymColors.Surface2)
            .padding(14.dp),
    ) {
        SectionLabel("Novo exercício")
        GymTextField(
            value = name,
            onValueChange = {
                name = it
                errors = errors.copy(name = null)
            },
            label = "Nome",
            placeholder = "Ex: Crucifixo",
            error = errors.name,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = errors.muscleGroup ?: "Grupo muscular",
            style = GymType.body12.copy(
                color = if (errors.muscleGroup != null) GymColors.Danger else GymColors.TextSecondary,
            ),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            muscleGroups.forEach { group ->
                SelectableChip(
                    text = group,
                    selected = group == muscleGroup,
                    onClick = {
                        muscleGroup = group
                        errors = errors.copy(muscleGroup = null)
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Primeira carga (opcional)",
            style = GymType.body12.copy(color = GymColors.TextSecondary),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GymTextField(
                value = weight,
                onValueChange = {
                    weight = it
                    errors = errors.copy(weight = null)
                },
                label = "Kg",
                error = errors.weight,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
            GymTextField(
                value = reps,
                onValueChange = {
                    reps = it
                    errors = errors.copy(reps = null)
                },
                label = "Reps",
                error = errors.reps,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = "Adicionar exercício",
            icon = Icons.Filled.Add,
            onClick = {
                errors = onSubmit(name, muscleGroup, weight, reps)
                if (errors.isValid) onAdded()
            },
        )
    }
}
