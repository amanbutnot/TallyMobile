package org.prime.tally.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.prime.tally.ui.shared.composables.TallyButton
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
                .fillMaxSize()
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

                // Top Section - Logo and App Name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // App Icon with elevated background
                    Card(
                        modifier = Modifier.size(120.dp),
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

                    Spacer(Modifier.height(24.dp))

                    // App Name
                    Text(
                        text = "Easy Karobar",
                        style = type.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Tagline
                    Text(
                        text = "Simplify Your Business",
                        style = type.titleMedium.copy(
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Manage invoices, track expenses, and grow\nyour business with ease",
                        style = type.bodyMedium.copy(
                            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                // Bottom Section - Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign Up Button
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

                    // Sign In Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Don't have an account?",
                            style = type.bodyMedium.copy(
                                color = colors.onSurfaceVariant
                            )
                        )
                        TextButton(
                            onClick = { nav.push(SignUpScreen) }
                        ) {
                            Text(
                                text = "Sign Up",
                                style = type.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    @Composable
    private fun FeatureItem(emoji: String, label: String) {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = type.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
            )
        }
    }
}