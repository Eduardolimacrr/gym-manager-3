package com.example.gymmobile.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gymmobile.ui.theme.GymColors
import com.example.gymmobile.ui.theme.GymType

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Rotas.HOME, "Início", Icons.Outlined.Home),
    Tab(Rotas.WORKOUTS, "Treinos", Icons.Outlined.FitnessCenter),
    Tab(Rotas.PROGRESS, "Progresso", Icons.AutoMirrored.Outlined.ShowChart),
    Tab(Rotas.PROFILE, "Perfil", Icons.Outlined.Person),
)

/** `NavigationBar` do Material com as cores de `.bottom-nav`. Renderizada apenas nas rotas-aba — ver `GymApp`. */
@Composable
fun GymBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = GymColors.BorderColor)
        NavigationBar(containerColor = GymColors.Bg, tonalElevation = 0.dp) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { onNavigate(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(21.dp)) },
                    // Sem cor no estilo, para valer a cor de selecionado/não selecionado do item.
                    label = { Text(tab.label, style = GymType.body11.copy(color = Color.Unspecified)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GymColors.AccentText,
                        selectedTextColor = GymColors.AccentText,
                        indicatorColor = GymColors.AccentDim,
                        unselectedIconColor = GymColors.TextMuted,
                        unselectedTextColor = GymColors.TextMuted,
                    ),
                )
            }
        }
    }
}
