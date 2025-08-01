// ResetPasswordScreen.kt
package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventoritoko.data.model.ResetPasswordRequest
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.AuthViewModel
import com.example.inventoritoko.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch // Tetap diperlukan untuk Snackbar error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val resetPasswordState by authViewModel.resetPasswordState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is AuthViewModel.AuthResult.Success -> {
                // Menggunakan Toast untuk pesan sukses
                Toast.makeText(
                    context,
                    (resetPasswordState as AuthViewModel.AuthResult.Success).message,
                    Toast.LENGTH_LONG
                ).show()

                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                authViewModel.clearResetPasswordState()
            }
            is AuthViewModel.AuthResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (resetPasswordState as AuthViewModel.AuthResult.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
                authViewModel.clearResetPasswordState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) } // SnackbarHost tetap ada untuk pesan error
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 64.dp,
                        bottom = 32.dp
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Reset Token") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password Baru") },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (newPasswordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (newPasswordVisible) "Hide new password" else "Show new password"
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Password Baru") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password"
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (email.isNotBlank() && token.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()) {
                            authViewModel.resetPassword(
                                ResetPasswordRequest(
                                    email,
                                    token,
                                    newPassword,
                                    confirmPassword
                                )
                            )
                        } else {
                            Toast.makeText(context, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = resetPasswordState !is AuthViewModel.AuthResult.Loading
                ) {
                    if (resetPasswordState is AuthViewModel.AuthResult.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Reset Password")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Kembali ke Login")
                }
            }
        }
        // SnackbarHost tetap ada untuk error, jadi tidak perlu dihapus dari Box
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewResetPasswordScreen() {
    ResetPasswordScreen(rememberNavController())
}
