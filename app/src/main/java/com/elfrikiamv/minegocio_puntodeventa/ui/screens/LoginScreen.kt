package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// LoginScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    // Verificar si el usuario ya está autenticado
    if (authViewModel.isUserLoggedIn()) {
        // Navegar directamente a la pantalla principal
        navController.navigate("main") {
            popUpTo("login") { inclusive = true }
        }
    }

    // Estado para el correo electrónico
    var email by remember { mutableStateOf("") }
    // Estado para la contraseña
    var password by remember { mutableStateOf("") }
    // Estado para mostrar mensajes de error
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        // No colocamos nada en el topBar, así que el Top App Bar no se mostrará
        topBar = {
            TopAppBar(
                title = { Text("Login") }
            )
        },
        content = { padding ->

            // Interfaz de usuario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo de texto para el correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Campo de texto para la contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Botón para iniciar sesión
                Button(
                    onClick = {
                        authViewModel.signIn(email, password) { success, error ->
                            if (success) {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                errorMessage = "Error al iniciar sesión: $error"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Sesión")
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Botón para ir a la pantalla de registro
                TextButton(
                    onClick = {
                        navController.navigate("register")
                    }
                ) {
                    Text("Crear Cuenta")
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Mostrar mensaje de error si existe
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}