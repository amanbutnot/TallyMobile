package org.prime.easykarobar.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.AuthViewModel
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

object SignUpScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var number by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }

        val viewModel: AuthViewModel = viewModel { AuthViewModel() }
        val state by viewModel.authState
        val nav = LocalNavigator.currentOrThrow

        if (state.isLoading) {
            TallyLoadingDialog("Creating your account")
        }

        if (state.error != null) {
            TallyResultDialog(
                state.error ?: "Unexpected Error",
                onDone = { viewModel.clearError() },
                isSuccess = state.success,
                confirmText = "Try Again"
            )
        }
        TallyScaffold(
            title = "Sign Up",
            onBack = { nav.pop() },
            showEditIcon = false,
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize().padding(paddingValues)
                    .background(colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Create Account",
                            style = type.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        Text(
                            text = "Join Easy Karobar today",
                            style = type.bodySmall.copy(
                                color = colors.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        // Name fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TallyTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                placeholder = "First name",
                                isPassword = false,
                                isNumber = false,
                                label = "First Name",
                                modifier = Modifier.weight(1f)
                            )

                            TallyTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                placeholder = "Last name",
                                isPassword = false,
                                isNumber = false,
                                label = "Last Name",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        TallyTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "you@example.com",
                            isPassword = false,
                            isNumber = false,
                            label = "Email",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TallyTextField(
                                value = number,
                                onValueChange = { number = it },
                                placeholder = "Phone number",
                                isPassword = false,
                                isNumber = true,
                                label = "Phone",
                                modifier = Modifier.weight(1.2f)
                            )

                            TallyTextField(
                                value = city,
                                onValueChange = { city = it },
                                placeholder = "Your city",
                                isPassword = false,
                                isNumber = false,
                                label = "City",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Gender Selection
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gender",
                                style = type.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectableGroup(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Male", "Female", "Others").forEach { option ->
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .selectable(
                                                selected = (gender == option),
                                                onClick = { gender = option }
                                            )
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        RadioButton(
                                            selected = (gender == option),
                                            onClick = { gender = option },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colors.primary,
                                                unselectedColor = colors.onSurfaceVariant
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = option,
                                            style = type.bodySmall.copy(
                                                fontWeight = if (gender == option) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (gender == option) colors.primary else colors.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TallyTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Create password",
                            isPassword = true,
                            isNumber = false,
                            label = "Password",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        TallyTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Confirm password",
                            isPassword = true,
                            isNumber = false,
                            label = "Confirm Password",
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

                        val isFormValid = firstName.isNotEmpty() &&
                                lastName.isNotEmpty() &&
                                email.isNotEmpty() &&
                                number.isNotEmpty() &&
                                gender.isNotEmpty() &&
                                city.isNotEmpty() &&
                                password.isNotEmpty() &&
                                confirmPassword.isNotEmpty() &&
                                password == confirmPassword

                        TallyButton(
                            label = "Create Account",
                            onClick = {
                                // Handle sign up
                            },
                            backgroundColor = colors.primary,
                            contentColor = colors.onPrimary,
                            enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = colors.outlineVariant
                        )

                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = type.bodySmall.copy(
                                    color = colors.onSurfaceVariant
                                )
                            )
                            TextButton(
                                onClick = { nav.replace(LoginScreen) }
                            ) {
                                Text(
                                    text = "Sign In",
                                    style = type.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}