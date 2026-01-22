package org.prime.easykarobar.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.ui.shared.composables.TallyButton
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.splashImage

object OnBoardingScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        val nav = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier
                .fillMaxSize().navigationBarsPadding()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryContainer.copy(alpha = 0.3f),
                            colors.background,
                            colors.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(40.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(28.dp),

                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.splashImage),
                                contentDescription = "Easy Karobar Logo",
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = "Simplify Your Business",
                        style = type.displayLarge.copy(
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Manage invoices, track expenses, and grow your business with ease",
                        style = type.titleLarge.copy(
                            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,

                        ),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TallyButton(
                        label = "Login",
                        onClick = { nav.push(LoginScreen) },
                        backgroundColor = colors.primary,
                        contentColor = colors.onPrimary,
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(Modifier.height(12.dp))
//
//                    // Sign In Option
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            text = "Don't have an account?",
//                            style = type.bodyMedium.copy(
//                                color = colors.onSurfaceVariant
//                            )
//                        )
//                        TextButton(
//                            onClick = { nav.push(SignUpScreen) }
//                        ) {
//                            Text(
//                                text = "Sign Up",
//                                style = type.bodyMedium.copy(
//                                    fontWeight = FontWeight.Bold,
//                                    color = colors.primary
//                                )
//                            )
//                        }
//                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

}