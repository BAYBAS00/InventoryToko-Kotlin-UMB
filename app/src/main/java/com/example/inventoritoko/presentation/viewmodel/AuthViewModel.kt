package com.example.inventoritoko.presentation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoritoko.data.api.RetrofitClient
import com.example.inventoritoko.data.api.dataStore
import com.example.inventoritoko.data.model.*
import com.example.inventoritoko.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class AuthViewModel(private val context: Context) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val loginState: StateFlow<AuthResult> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val registerState: StateFlow<AuthResult> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val forgotPasswordState: StateFlow<AuthResult> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val resetPasswordState: StateFlow<AuthResult> = _resetPasswordState.asStateFlow()

    private val apiService = RetrofitClient.getApiService(context)
    private val gson = Gson()

    fun login(request: LoginRequest) {
        _loginState.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.login(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.loginResult?.token?.let { token ->
                        context.dataStore.edit { preferences ->
                            preferences[stringPreferencesKey(Constants.AUTH_TOKEN)] = token
                        }
                    }
                    _loginState.value = AuthResult.Success(authResponse?.message ?: "Login successful")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = parseAndFormatErrorMessage(errorBodyString)
                    _loginState.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = parseAndFormatErrorMessage(e.message)
                _loginState.value = AuthResult.Error(errorMessage)
            }
        }
    }

    fun register(request: RegisterRequest) {
        _registerState.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.register(request)
                if (response.isSuccessful) {
                    _registerState.value = AuthResult.Success(response.body()?.message ?: "Registration successful")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = parseAndFormatErrorMessage(errorBodyString)
                    _registerState.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = parseAndFormatErrorMessage(e.message)
                _registerState.value = AuthResult.Error(errorMessage)
            }
        }
    }

    fun forgotPassword(request: ForgotPasswordRequest) {
        _forgotPasswordState.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.forgotPassword(request)
                if (response.isSuccessful) {
                    _forgotPasswordState.value = AuthResult.Success(response.body()?.message ?: "Password reset email sent")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = parseAndFormatErrorMessage(errorBodyString)
                    _forgotPasswordState.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = parseAndFormatErrorMessage(e.message)
                _forgotPasswordState.value = AuthResult.Error(errorMessage)
            }
        }
    }

    fun resetPassword(request: ResetPasswordRequest) {
        _resetPasswordState.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.resetPassword(request)
                if (response.isSuccessful) {
                    _resetPasswordState.value = AuthResult.Success(response.body()?.message ?: "Password has been reset")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = parseAndFormatErrorMessage(errorBodyString)
                    _resetPasswordState.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = parseAndFormatErrorMessage(e.message)
                _resetPasswordState.value = AuthResult.Error(errorMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(Constants.AUTH_TOKEN)) // Hapus token
            }
            // Opsional: Reset semua state autentikasi ke Idle setelah logout
            _loginState.value = AuthResult.Idle
            _registerState.value = AuthResult.Idle
            _forgotPasswordState.value = AuthResult.Idle
            _resetPasswordState.value = AuthResult.Idle
        }
    }

    fun clearLoginState() {
        _loginState.value = AuthResult.Idle
    }

    fun clearRegisterState() {
        _registerState.value = AuthResult.Idle
    }
    fun clearForgotPasswordState() {
        _forgotPasswordState.value = AuthResult.Idle
    }
    fun clearResetPasswordState() {
        _resetPasswordState.value = AuthResult.Idle
    }

    private fun parseAndFormatErrorMessage(errorString: String?): String {
        return errorString?.let {
            try {
                val errorResponse = gson.fromJson(it, ErrorResponse::class.java)
                val message = errorResponse.message ?: "Terjadi kesalahan."
                val details = errorResponse.detail?.joinToString(", ")
                if (!details.isNullOrBlank()) {
                    "$message. Detail: $details"
                } else {
                    message
                }
            } catch (e: JsonSyntaxException) {
                // Bukan JSON, atau formatnya tidak sesuai yang diharapkan
                "Terjadi kesalahan: $it" // Kembali ke string mentah jika parsing gagal
            } catch (e: Exception) {
                // Pengecualian lain selama parsing
                "Terjadi kesalahan tidak dikenal: ${e.message}"
            }
        } ?: "Terjadi kesalahan tidak dikenal."
    }

    sealed class AuthResult {
        object Idle : AuthResult()
        object Loading : AuthResult()
        data class Success(val message: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}