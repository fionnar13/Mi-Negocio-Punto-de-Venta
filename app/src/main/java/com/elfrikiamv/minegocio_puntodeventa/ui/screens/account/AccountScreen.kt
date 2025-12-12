package com.elfrikiamv.minegocio_puntodeventa.ui.screens.account

// AccountScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.login.AuthViewModel

@Composable
fun AccountScreen(viewModel: AuthViewModel) {

    val isLoading by viewModel.isLoading.collectAsState()

    val email by viewModel.email.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserInfo()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Correo: $email")
    }
}