package com.example.gymmobile.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymType

/** Confirmação antes de tirar um item de uma das listas. */
@Composable
fun ConfirmRemoveDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = GymType.display19) },
        text = { Text(message, style = GymType.body14.copy(color = GymColors.TextSecondary)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remover", style = GymType.body14.copy(color = GymColors.Danger))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = GymType.body14.copy(color = GymColors.TextSecondary))
            }
        },
        shape = GymShape.cardLarge,
        containerColor = GymColors.Surface,
    )
}
