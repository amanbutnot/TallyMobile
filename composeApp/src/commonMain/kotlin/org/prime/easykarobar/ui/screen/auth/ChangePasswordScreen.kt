package org.prime.easykarobar.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.BuildKonfig
import org.prime.easykarobar.business.viewmodel.AuthViewModel
import org.prime.easykarobar.ui.screen.auth.OnBoardingScreen
import org.prime.easykarobar.ui.screen.easymart.EasyMartScreen
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

data class ChangePasswordScreen(val number: String) : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        val nav = LocalNavigator.currentOrThrow

        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val validateState by authViewModel.resetState

        if (validateState.isLoading) {
            TallyLoadingDialog(
                text = "Validating your mobile number"
            )
        }

        if (validateState.error != null) {
            TallyResultDialog(
                message = validateState.message ?: "Error Occurred",
                onDone = { authViewModel.clearValidateMessage() },
                isSuccess = validateState.success,
                confirmText = "Ok"
            )
        }

        TallyScaffold(
            title = "Change Password",
            onBack = { nav.pop() },
            showEditIcon = false,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
                contentAlignment = Alignment.Center
            )
            {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Numbers,
                            contentDescription = "Number icon",
                            tint = colors.primary,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Forgot Password?",
                            style = type.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Enter your new password.",
                            style = type.bodyMedium.copy(color = colors.onSurfaceVariant)
                        )

                        Spacer(Modifier.height(32.dp))


                        TallyTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Enter your password",
                            isPassword = true,
                            isNumber = false,
                            label = "Password",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))


                        TallyTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Enter your password again",
                            isPassword = true,
                            isNumber = false,
                            label = "Confirm Password",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        TallyButton(
                            label = "Change Password",
                            onClick = {
                                    authViewModel.resetPassword(
                                        password = password, id = number,
                                        onSuccess = {
                                            if (BuildKonfig.STORE_ID.isNotEmpty()) {
                                                nav.replaceAll(EasyMartScreen)
                                            } else {
                                                nav.replaceAll(OnBoardingScreen)
                                            }
                                        })

                            },
                            backgroundColor = colors.primary,
                            enabled = (password == confirmPassword) && password.isNotEmpty() && confirmPassword.isNotEmpty(),
                            contentColor = colors.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }

            }
        }


    }
}
