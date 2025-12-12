package com.elfrikiamv.minegocio_puntodeventa.ui.screens.account

// AccountScreen.kt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.login.AuthViewModel

@Composable
fun AccountScreen(viewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        //horizontalAlignment = Alignment.CenterHorizontally
        //verticalArrangement = Arrangement.Center,
    ) {
        /*Text(text = "Hola: ${viewModel.currentUser.value?.displayName}")
        Text(text = "Correo: ${viewModel.currentUser.value?.email}")*/
    }
}