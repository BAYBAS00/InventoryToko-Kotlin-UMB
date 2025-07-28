// ForgotPasswordScreen.kt
package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventoritoko.data.model.ForgotPasswordRequest
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.AuthViewModel
import com.example.inventoritoko.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch // Tetap diperlukan untuk Snackbar error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var email by remember { mutableStateOf("") }

    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(forgotPasswordState) {
        when (forgotPasswordState) {
            is AuthViewModel.AuthResult.Success -> {
                // Menggunakan Toast untuk pesan sukses
                Toast.makeText(
                    context,
                    (forgotPasswordState as AuthViewModel.AuthResult.Success).message,
                    Toast.LENGTH_LONG
                ).show()

                navController.navigate(Screen.ResetPassword.route)
                authViewModel.clearForgotPasswordState()
            }
            is AuthViewModel.AuthResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (forgotPasswordState as AuthViewModel.AuthResult.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
                authViewModel.clearForgotPasswordState()
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
                    text = "Forgot Password",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 64.dp,
                        bottom = 32.dp
                    )
                )
                Text(
                    text = "Masukkan email Anda untuk mendapatkan token reset password.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            authViewModel.forgotPassword(ForgotPasswordRequest(email))
                        } else {
                            Toast.makeText(context, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = forgotPasswordState !is AuthViewModel.AuthResult.Loading
                ) {
                    if (forgotPasswordState is AuthViewModel.AuthResult.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Kirim Reset Token")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Kembali ke Login")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgotPasswordScreen() {
    ForgotPasswordScreen(rememberNavController())
}
