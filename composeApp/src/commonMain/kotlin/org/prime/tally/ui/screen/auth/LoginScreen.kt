package org.prime.tally.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.prime.tally.business.viewmodel.AuthViewModel
import org.prime.tally.data.model.LoginRequest
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.screen.startup.GoogleDriveDownloadScreen
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyResultDialog
import org.prime.tally.ui.shared.composables.TallyScaffold
import org.prime.tally.ui.shared.composables.TallyTextField
import tallymobile.composeapp.generated.resources.Res
import tallymobile.composeapp.generated.resources.splashImage

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography

        var email by remember { mutableStateOf(SharedPrefs.LoginInfo.get() ?: "") }
        var password by remember { mutableStateOf("") }
        val viewModel: AuthViewModel = viewModel { AuthViewModel() }
        val state by viewModel.authState

        val nav = LocalNavigator.currentOrThrow
        val urlHandler = LocalUriHandler.current

        if (state.isLoading) {
            TallyLoadingDialog("Logging you in")
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
            title = "Login",
            onBack = { nav.replaceAll(OnBoardingScreen) },
            showEditIcon = false,
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize().padding(paddingValues).navigationBarsPadding().verticalScroll(
                        rememberScrollState()
                    )
                    .background(colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            )


            {
                Image(
                    painterResource(Res.drawable.splashImage),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(200.dp)
                )
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

                        Text(
                            text = "Welcome Back",
                            style = type.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Your finances, simplified — sign in to continue",
                            style = type.bodyMedium.copy(color = colors.onSurfaceVariant)
                        )

                        Spacer(Modifier.height(32.dp))


                        TallyTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email or Mobile Number",
                            isPassword = false,
                            isNumber = false,
                            label = "Email/Mobile Number",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        TallyTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Enter your password",
                            isPassword = true,
                            isNumber = false,
                            label = "Password",
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                nav.push(ForgotPasswordScreen)
                            }, modifier = Modifier
                                .align(Alignment.End)

                        ) {
                            Text(
                                text = "Forgot password?",
                                fontSize = 14.sp,
                                color = colors.primary,

                                )
                        }

                        TallyButton(
                            label = "Login",
                            onClick = {
                                viewModel.userLogin(
                                    LoginRequest(
                                        Username = email,
                                        Password = password
                                    ),
                                    onSuccess = {
                                        nav.push(GoogleDriveDownloadScreen)
                                    }, onListSuccess = { companyList ->
                                        SharedPrefs.LoginData.save(
                                            SharedPrefs.LoginDataModel(
                                                username = email,
                                                password = password,
                                                list = companyList,
                                            )
                                        )
                                        nav.push(SelectCompanyScreen(email, password, companyList))
                                    }
                                )
                            },
                            backgroundColor = colors.primary,
                            contentColor = colors.onPrimary,
                            enabled = !(email.isEmpty() || password.isEmpty()),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        TallyButton(
                            label = "Support Ticket",
                            onClick = { urlHandler.openUri("http://easykarobar.in/support-ticket.php") },
                            backgroundColor = Color.Transparent,
                            contentColor = colors.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

            }
        }
    }
}
