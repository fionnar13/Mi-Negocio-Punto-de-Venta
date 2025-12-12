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

    /*val user = Firebase.auth.currentUser
    user?.let {
        // Name, email address, and profile photo Url
        val name = it.displayName
        val email = it.email
        val photoUrl = it.photoUrl

        // Check if user's email is verified
        val emailVerified = it.isEmailVerified

        // The user's ID, unique to the Firebase project. Do NOT use this value to
        // authenticate with your backend server, if you have one. Use
        // FirebaseUser.getIdToken() instead.
        val uid = it.uid
    }*/

    // funcion para obtener informacion del usuario

    /*fun getUserInfo(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                //auth.createUserWithEmailAndPassword(email, password).await()


                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }

    }*/

    /*fun getUserInfo(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    onResult(true, user.email)
                } else {
                    onResult(false, "Usuario no autenticado")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }*/

}