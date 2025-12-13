package com.elfrikiamv.minegocio_puntodeventa.ui.screens.login

// LoginScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.login.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    // Estado para el correo electrónico
    var email by remember { mutableStateOf("") }
    // Estado para la contraseña
    var password by remember { mutableStateOf("") }
    // Estado para los mensajes de error
    var errorMessage by remember { mutableStateOf("") }
    // Estado para manejar el progreso de autenticación
    var isLoading by remember { mutableStateOf(false) }
    // Estado para manejar el progreso de la verificación de autenticación inicial
    var isCheckingAuth by remember { mutableStateOf(true) }

    // Verificar si el usuario ya está autenticado al cargar la pantalla
    LaunchedEffect(Unit) {
        // Simular la verificación inicial y mostrar el CircularProgressIndicator
        isCheckingAuth = true
        if (authViewModel.isUserLoggedIn()) {
            // Si ya está autenticado, navegar directamente a la pantalla principal
            navController.navigate(Screen.Main.route) {
                popUpTo(0) // Elimina todas las pantallas de la pila
            }
        }
        isCheckingAuth = false // Finalizar la verificación inicial
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión") }
            )
        },
        content = { padding ->

            // Interfaz de usuario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostrar CircularProgressIndicator durante la verificación inicial de autenticación
                if (isCheckingAuth) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                } else {
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

                    // Mostrar el indicador de carga durante la autenticación
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    } else {
                        // Botón para iniciar sesión
                        Button(
                            onClick = {
                                // Iniciar el proceso de inicio de sesión
                                isLoading = true
                                authViewModel.signIn(email, password) { success, error ->
                                    isLoading = false // Desactivar el estado de carga
                                    if (success) {
                                        // Navegar a la pantalla principal al iniciar sesión correctamente
                                        navController.navigate(Screen.Main.route) {
                                            popUpTo(0) // Elimina todas las pantallas de la pila
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón para ir a la pantalla de registro
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.Register.route)
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
        }
    )
}