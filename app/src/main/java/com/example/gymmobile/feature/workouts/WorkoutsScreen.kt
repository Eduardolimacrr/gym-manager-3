package com.example.gymmobile.feature.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymmobile.data.WorkoutTemplate
import com.example.gymmobile.ui.components.ConfirmRemoveDialog
import com.example.gymmobile.ui.components.GhostButton
import com.example.gymmobile.ui.components.SquareIconButton
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymType

@Composable
fun WorkoutsScreen(
    onOpenTemplate: (Int) -> Unit,
    onStartTemplate: (Int) -> Unit,
    onCreate: () -> Unit,
    viewModel: WorkoutsViewModel = viewModel(),
) {
    // Treino esperando confirmação de remoção; `null` = diálogo fechado.
    var pendingRemoval by remember { mutableStateOf<WorkoutTemplate?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar() }
    val templates = viewModel.templates ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GymColors.Bg)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp),
    ) {
        item {
            Text(
                text = "Meus treinos",
                style = GymType.display19,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Toque num treino para ver os detalhes",
                style = GymType.body12.copy(color = GymColors.TextMuted),
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp),
            )
        }
        if (templates.isEmpty()) {
            item {
                Text(
                    text = "Nenhum treino ainda. Crie o primeiro abaixo.",
                    style = GymType.body13.copy(color = GymColors.TextMuted),
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
        items(items = templates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                muscleGroups = viewModel.muscleGroupsOf(template),
                exerciseCount = viewModel.exerciseCountOf(template),
                onClick = { onOpenTemplate(template.id) },
                onRemove = { pendingRemoval = template },
                onPlay = { onStartTemplate(template.id) },
            )
            Spacer(Modifier.height(10.dp))
        }
        item {
            Spacer(Modifier.height(6.dp))
            GhostButton(text = "Novo treino", onClick = onCreate, icon = Icons.Filled.Add)
        }
    }

    pendingRemoval?.let { template ->
        ConfirmRemoveDialog(
            title = "Remover ${template.name}?",
            message = "O treino sai da lista. Os exercícios e o histórico de cargas continuam em Progresso.",
            onConfirm = {
                viewModel.remove(template)
                pendingRemoval = null
            },
            onDismiss = { pendingRemoval = null },
        )
    }
}

/** `.template-card`, agora um `Card` clicável que abre o detalhe. */
@Composable
private fun TemplateCard(
    template: WorkoutTemplate,
    muscleGroups: String,
    exerciseCount: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onPlay: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = GymShape.card,
        colors = CardDefaults.cardColors(containerColor = GymColors.Surface),
        border = BorderStroke(0.5.dp, GymColors.BorderColor),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(text = template.name, style = GymType.display16)
                Text(
                    text = muscleGroups,
                    style = GymType.body12.copy(color = GymColors.TextSecondary),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = if (exerciseCount == 1) "1 exercício" else "$exerciseCount exercícios",
                    style = GymType.mono11.copy(color = GymColors.TextMuted),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SquareIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Remover ${template.name}",
                    onClick = onRemove,
                    size = 32.dp,
                    shape = GymShape.miniButton,
                    container = GymColors.Surface2,
                )
                SquareIconButton(
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = "Iniciar ${template.name}",
                    onClick = onPlay,
                    accent = true,
                    size = 32.dp,
                    shape = GymShape.miniButton,
                )
            }
        }
    }
}
