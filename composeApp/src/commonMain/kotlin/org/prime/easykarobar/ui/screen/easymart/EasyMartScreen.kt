package org.prime.easykarobar.ui.screen.easymart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.prime.easykarobar.BuildKonfig
import org.prime.easykarobar.business.viewmodel.AuthViewModel
import org.prime.easykarobar.ui.screen.auth.VerifyOtpScreen
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.dmsSplash

object EasyMartScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val focusManager = LocalFocusManager.current
        val colors = MaterialTheme.colorScheme

        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val validateState by authViewModel.validateState

        var phoneNumber by remember { mutableStateOf("") }

        if (validateState.isLoading) {
            TallyLoadingDialog(text = "Processing...")
        }

        if (validateState.error != null) {
            TallyResultDialog(
                message = validateState.message ?: "Error Occurred",
                onDone = { authViewModel.clearValidateMessage() },
                isSuccess = validateState.success,
                confirmText = "Ok"
            )
        }

        val topColor = try {
            Color(BuildKonfig.SPLASH_TOP_COLOR.removePrefix("#").toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            colors.primary
        }
        val bottomColor = try {
            Color(BuildKonfig.SPLASH_BOTTOM_COLOR.removePrefix("#").toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            colors.primaryContainer
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface)
        ) {
            // Top Background Gradient with Curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(bottomStart = 64.dp, bottomEnd = 64.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(topColor, bottomColor)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = BuildKonfig.STORE_NAME,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Powered by Prime Solutions",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Light
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Image(
                        painter = painterResource(Res.drawable.dmsSplash),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .aspectRatio(1.2f),
                        contentScale = ContentScale.Fit
                    )
                }

                // Login Card Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in to access your business reports and manage your account.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.onSurfaceVariant,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter Mobile Number") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = topColor
                                )
                            },
                            prefix = {
                                Text(
                                    text = "+91 ",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = topColor,
                                focusedLabelColor = topColor,
                                unfocusedBorderColor = colors.outlineVariant,
                                cursorColor = topColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.length == 10) {
                                    val otp = (100_000..999_999).random()
                                    authViewModel.validateMobile(username = phoneNumber) {
                                        authViewModel.sendOtp(
                                            number = phoneNumber,
                                            message = "Your login OTP is $otp. Please do not share it with anyone.",
                                            onSuccess = {
                                                navigator.push(VerifyOtpScreen(otp.toString(), phoneNumber, isForgot = false))
                                            })
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = phoneNumber.length == 10,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = topColor,
                                contentColor = Color.White,
                                disabledContainerColor = topColor.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                "Get OTP",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Footer
                Text(
                    text = buildAnnotatedString {
                        append("By continuing, you agree to our ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = topColor)) {
                            append("Terms")
                        }
                        append(" & ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = topColor)) {
                            append("Privacy Policy")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
