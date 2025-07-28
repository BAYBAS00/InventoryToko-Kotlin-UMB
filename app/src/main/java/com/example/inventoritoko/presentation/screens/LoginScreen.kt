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
import com.example.inventoritoko.data.model.LoginRequest
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.AuthViewModel
import com.example.inventoritoko.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginState by authViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthViewModel.AuthResult.Success -> {
                Toast.makeText(
                    context,
                    (loginState as AuthViewModel.AuthResult.Success).message,
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(Screen.ProductList.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                authViewModel.clearLoginState()
            }
            is AuthViewModel.AuthResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (loginState as AuthViewModel.AuthResult.Error).message,
                        duration = SnackbarDuration.Long // Durasi lebih lama untuk error
                    )
                }
                authViewModel.clearLoginState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 64.dp, bottom = 32.dp) // Tambahkan padding atas untuk judul
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
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { authViewModel.login(LoginRequest(email, password)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = loginState !is AuthViewModel.AuthResult.Loading
                ) {
                    if (loginState is AuthViewModel.AuthResult.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Login")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Tidak punya akun? Register")
                }
                TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
                    Text("Lupa Password?")
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.ime.asPaddingValues())
                .padding(WindowInsets.navigationBars.asPaddingValues())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(rememberNavController())
}