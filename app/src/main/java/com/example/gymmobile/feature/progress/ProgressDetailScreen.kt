package com.example.gymmobile.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymmobile.data.formatWeight
import com.example.gymmobile.ui.components.GymTextField
import com.example.gymmobile.ui.components.GymTopBar
import com.example.gymmobile.ui.components.HistoryRow
import com.example.gymmobile.ui.components.PrimaryButton
import com.example.gymmobile.ui.components.SectionLabel
import com.example.gymmobile.ui.components.StatCard
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymType
import kotlin.math.roundToInt

/**
 * Detalhe de um exercício, buscado no banco pelo id que veio na rota. Além de
 * mostrar o item, deixa registrar uma carga nova ali mesmo — o exercício é
 * atualizado no banco e recorde, gráfico, 1RM e histórico são recalculados — e
 * lista os treinos (da outra tabela) que usam o exercício.
 */
@Composable
fun ProgressDetailScreen(
    exerciseId: Int,
    onBack: () -> Unit,
    viewModel: ProgressDetailViewModel = viewModel(
        factory = ProgressDetailViewModel.factory(exerciseId)
    ),
) {
    val focusManager = LocalFocusManager.current
    var weight by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf(ProgressDetailViewModel.LoadErrors()) }

    LaunchedEffect(Unit) { viewModel.carregar() }
    if (!viewModel.loaded) return
    val exercise = viewModel.exercise

    Scaffold(
        topBar = { GymTopBar(title = exercise?.name ?: "Exercício", onBack = onBack) },
        containerColor = GymColors.Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        if (exercise == null) {
            Text(
                text = "Este exercício não existe mais.",
                style = GymType.body14.copy(color = GymColors.TextMuted),
                modifier = Modifier.padding(innerPadding).padding(18.dp),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 18.dp)
        ) {
            Text(
                text = exercise.muscleGroup,
                style = GymType.body13.copy(color = GymColors.TextSecondary),
                modifier = Modifier.padding(bottom = 14.dp),
            )

            // `.detail-hero`
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = exercise.prWeight?.let { "${formatWeight(it)}kg" } ?: "—",
                    style = GymType.display44.copy(color = GymColors.Chalk),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (exercise.prWeight != null) "recorde pessoal" else "sem cargas registradas",
                    style = GymType.body13.copy(color = GymColors.TextSecondary),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            if (exercise.bars.size >= 2) {
                BarChart(bars = exercise.bars)
            } else {
                Text(
                    text = "Registre pelo menos duas cargas para ver o gráfico.",
                    style = GymType.body12.copy(color = GymColors.TextMuted),
                )
            }
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Última carga",
                    value = exercise.lastLoad?.let { formatWeight(it.weight) } ?: "—",
                    unit = exercise.lastLoad?.let { "kg" },
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "1RM estimado",
                    value = exercise.estimatedOneRepMax?.let { it.roundToInt().toString() } ?: "—",
                    unit = exercise.estimatedOneRepMax?.let { "kg" },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "1RM pela fórmula de Epley: carga × (1 + reps ÷ 30), no melhor registro.",
                style = GymType.body11.copy(color = GymColors.TextMuted),
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel("Registrar carga de hoje")
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
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                text = "Registrar",
                icon = Icons.Filled.Add,
                onClick = {
                    errors = viewModel.registerLoad(weight, reps)
                    if (errors.isValid) {
                        weight = ""
                        reps = ""
                        focusManager.clearFocus()
                    }
                },
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel("Usado nos treinos")
            Text(
                text = viewModel.usedIn.joinToString(" · ") { it.name }
                    .ifEmpty { "Nenhum treino usa este exercício." },
                style = GymType.body13,
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel("Histórico · ${exercise.history.size}")
            exercise.history.forEachIndexed { index, entry ->
                HistoryRow(
                    left = entry.date,
                    right = entry.label,
                    showDivider = index != exercise.history.lastIndex,
                )
            }
        }
    }
}
