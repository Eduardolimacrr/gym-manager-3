package com.example.gymmobile.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymType

/**
 * `TopAppBar` do Material com a cara do protótipo: fundo `Bg`, título em
 * Oswald e o botão quadrado de voltar no lugar do `IconButton` redondo.
 *
 * Sem insets próprios: o `Scaffold` de `GymApp` já desconta a status bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = GymType.display19,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            SquareIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                onClick = onBack,
                modifier = Modifier.padding(start = 14.dp, end = 4.dp),
            )
        },
        actions = actions,
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GymColors.Bg,
            titleContentColor = GymColors.TextPrimary,
            actionIconContentColor = GymColors.TextPrimary,
        ),
    )
}
