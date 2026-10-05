package com.example.gymmobile.feature.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymmobile.ui.components.GymTextField
import com.example.gymmobile.ui.components.GymTopBar
import com.example.gymmobile.ui.components.PrimaryButton
import com.example.gymmobile.ui.components.SelectableChip
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymTheme
import com.example.gymmobile.ui.theme.GymType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateWorkoutScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val catalog = viewModel.catalog

    Scaffold(
        topBar = { GymTopBar(title = "Novo treino", onBack = onBack) },
        containerColor = GymColors.Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 18.dp)
        ) {
            GymTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Nome do treino",
                placeholder = "Ex: Push A, Upper, Dia de perna",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))
            FieldLabel("Adicionar exercícios")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                catalog.forEach { exercise ->
                    SelectableChip(
                        text = exercise.name,
                        selected = exercise.id in state.picked,
                        onClick = { viewModel.toggleExercise(exercise.id) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("Selecionados")
            val picked = state.picked.mapNotNull { id -> catalog.firstOrNull { it.id == id } }
            if (picked.isEmpty()) {
                Text(
                    text = "Nenhum exercício selecionado ainda.",
                    style = GymType.body13.copy(color = GymColors.TextMuted),
                )
            } else {
                picked.forEach { exercise ->
                    PickedRow(name = exercise.name, onRemove = { viewModel.toggleExercise(exercise.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            GymTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Observações (opcional)",
                placeholder = "Ex: aquecer antes, descanso maior no supino",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Salvar treino",
                onClick = {
                    viewModel.save()
                    onSaved()
                },
            )
        }
    }
}

/** `.field-label`. */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = GymType.sectionLabel,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

/** `.picked-row`. */
@Composable
private fun PickedRow(name: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GymShape.iconButton)
            .background(GymColors.Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = name, style = GymType.body13)
        Text(
            text = "remover",
            style = GymType.body13.copy(color = GymColors.TextMuted),
            modifier = Modifier.clickable(onClick = onRemove),
        )
    }
}

@Preview(widthDp = 390, heightDp = 760)
@Composable
private fun CreateWorkoutScreenPreview() {
    GymTheme { CreateWorkoutScreen(onBack = {}, onSaved = {}) }
}
