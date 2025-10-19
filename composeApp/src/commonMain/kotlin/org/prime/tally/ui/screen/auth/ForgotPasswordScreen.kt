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
import androidx.compose.material.icons.filled.Email
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.composables.TallyTextField

object ForgotPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var email by remember { mutableStateOf("") }
        val nav = LocalNavigator.currentOrThrow

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
                            Icons.Default.Email,
                            contentDescription = "Email icon",
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
                            text = "Enter your email address and we’ll send you a link to reset your password.",
                            style = type.bodyMedium.copy(color = colors.onSurfaceVariant)
                        )

                        Spacer(Modifier.height(32.dp))


                        TallyTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email",
                            isPassword = false,
                            isNumber = false,
                            label = "Email",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        TallyButton(
                            label = "Get Otp",
                            onClick = { /* TODO */ },
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
