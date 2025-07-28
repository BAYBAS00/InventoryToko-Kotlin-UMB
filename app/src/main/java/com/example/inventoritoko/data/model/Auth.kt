package com.example.inventoritoko.data.model

import com.google.gson.annotations.SerializedName


data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

data class AuthResponse(
    val message: String,
    val error: Boolean,
    @SerializedName("loginResult") // Map ke properti 'loginResult' dari JSON
    val loginResult: LoginResult? = null
)

data class LoginResult(
    val name: String,
    val token: String // Token sekarang ada di dalam LoginResult
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class ErrorResponse(
    val message: String,
    val detail: List<String>? = null
)

