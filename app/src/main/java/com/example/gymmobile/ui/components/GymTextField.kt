package com.example.gymmobile.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymShape
import com.example.gymmobile.ui.theme.GymType

/**
 * `OutlinedTextField` com as cores de `.text-input`: superfície escura, borda
 * fina que vira laranja com o foco. `error` não nulo pinta o campo de
 * `Danger` e mostra a mensagem logo abaixo.
 */
@Composable
fun GymTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        // Estilos sem cor, para valerem as cores de foco/erro definidas em `colors`.
        label = { Text(label, style = GymType.body13.copy(color = Color.Unspecified)) },
        placeholder = placeholder?.let { { Text(it, style = GymType.body15.copy(color = Color.Unspecified)) } },
        supportingText = error?.let { { Text(it, style = GymType.body12.copy(color = Color.Unspecified)) } },
        isError = error != null,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = GymType.body15,
        shape = GymShape.button,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = GymColors.TextPrimary,
            unfocusedTextColor = GymColors.TextPrimary,
            focusedContainerColor = GymColors.Surface,
            unfocusedContainerColor = GymColors.Surface,
            errorContainerColor = GymColors.Surface,
            cursorColor = GymColors.Accent,
            focusedBorderColor = GymColors.Accent,
            unfocusedBorderColor = GymColors.BorderColor,
            errorBorderColor = GymColors.Danger,
            focusedLabelColor = GymColors.AccentText,
            unfocusedLabelColor = GymColors.TextMuted,
            errorLabelColor = GymColors.Danger,
            focusedPlaceholderColor = GymColors.TextMuted,
            unfocusedPlaceholderColor = GymColors.TextMuted,
            errorSupportingTextColor = GymColors.Danger,
            errorCursorColor = GymColors.Danger,
        ),
    )
}
