package com.elfrikiamv.minegocio_puntodeventa.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    // Contenido de la pantalla de inicio
    Text(text = "Home screen", modifier = modifier)
}