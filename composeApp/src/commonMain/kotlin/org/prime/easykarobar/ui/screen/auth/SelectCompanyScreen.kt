package org.prime.easykarobar.ui.screen.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.AuthViewModel
import org.prime.easykarobar.data.model.CompanyList
import org.prime.easykarobar.data.model.LoginRequest
import org.prime.easykarobar.ui.screen.startup.GoogleDriveDownloadScreen
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold

data class SelectCompanyScreen(val username: String, val passwd: String, val list: CompanyList) :
    Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewModel: AuthViewModel = viewModel { AuthViewModel() }
        val state by viewModel.authState
        val scope = rememberCoroutineScope()

        if (state.isLoading) {
            TallyLoadingDialog("Logging you in")
        }

        if (state.error != null) {
            TallyResultDialog(
                state.error ?: "Unexpected Error",
                onDone = {
                    viewModel.clearError()
                    nav.pop()
                },
                isSuccess = state.success,
                confirmText = "Try Again"
            )
        }
        TallyScaffold(title = "Select Company", onBack = { nav.pop() }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                // Subtitle header
                Text(
                    text = "Choose your company to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list.CompanyDetails) { company ->
                        EnhancedCompanyCard(
                            companyName = company.CompanyName,
                            onClick = {
                                scope.launch {
                                    viewModel.userLogin(
                                        LoginRequest(
                                            Username = username,
                                            Password = passwd,
                                            CompanyID = company.CompanyID
                                        ),
                                        onSuccess = {

                                            nav.push(GoogleDriveDownloadScreen)
                                        },
                                        onListSuccess = {}
                                    )
                                }

                                // Handle company selection
                                // nav.push(NextScreen(username, passwd, company))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedCompanyCard(
    companyName: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100)
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 4f,
        animationSpec = tween(100)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isPressed)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Company Info - Full Width Clean Layout
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                // Subtle divider line
                Spacer(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }

            // Stylish Arrow Container
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Select company",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}