package com.elfrikiamv.minegocio_puntodeventa.viewmodel.login

// AuthViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    // Instancia de FirebaseAuth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    // Función para iniciar sesión
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // Función para registrar usuario
    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // verificar si el usuario está autenticado al abrir la app
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // cerrar sesion
    fun signOut(
        onSuccess: (String) -> Unit,
        onLoading: (String) -> Unit
    ) {
        if (isLoading.value) {
            onLoading("Procesando...")
            return
        } else {
            _isLoading.value = true
            auth.signOut()
            onSuccess("Sesión cerrada")
            _isLoading.value = false
            //return
        }
    }

    // obtener datos del usuario
    fun getUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            _email.value = user.email.toString()
        } else {
            _email.value = "Usuario no autenticado"
        }
    }
}