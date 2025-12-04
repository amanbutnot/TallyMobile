package org.prime.tally.ui.screen.auth


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
import org.prime.tally.business.viewmodel.AuthViewModel
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.composables.TallyTextField

object ForgotPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var number by remember { mutableStateOf("") }
        val nav = LocalNavigator.currentOrThrow

        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val validateState by authViewModel.validateState

        TallyScaffold(
            title = "Forgot Password",
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
                            text = "Enter your mobile number and we’ll send you an otp to reset your password.",
                            style = type.bodyMedium.copy(color = colors.onSurfaceVariant)
                        )

                        Spacer(Modifier.height(32.dp))


                        TallyTextField(
                            value = number,
                            onValueChange = { number = it },
                            placeholder = "Enter your mobile number",
                            isPassword = false,
                            isNumber = true,
                            label = "Mobile Number",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        TallyButton(
                            label = "Get Otp",
                            onClick = {
                                authViewModel.validateMobile(username = number) {
                                    nav.push(VerifyOtpScreen)
                                }
                            },
                            backgroundColor = colors.primary,
                            contentColor = colors.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }

            }
        }


    }
}
