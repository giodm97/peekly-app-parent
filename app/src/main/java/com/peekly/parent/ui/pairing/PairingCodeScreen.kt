package com.peekly.parent.ui.pairing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peekly.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingCodeScreen(
    childId: Long,
    childName: String,
    onBack: () -> Unit,
    viewModel: PairingViewModel = viewModel()
) {
    LaunchedEffect(childId) { viewModel.generateCode(childId) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            TopAppBar(
                title = { Text("Link ${childName}'s device", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = state) {
                is PairingUiState.Loading -> CircularProgressIndicator(color = PurplePrimary)

                is PairingUiState.Error -> {
                    Text(s.message, color = RedAccent)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.generateCode(childId) },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Retry") }
                }

                is PairingUiState.Success -> {
                    Text(
                        "Open the Peekly app on ${childName}'s phone and enter this code",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(PurpleContainer, CardBackground)))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                s.pairing.pairingCode,
                                fontFamily    = FontFamily.Monospace,
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 48.sp,
                                letterSpacing = 8.sp,
                                color         = TextWhite
                            )
                            Text(
                                "Expires at ${s.pairing.expiresAt.take(16).replace("T", " ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { viewModel.generateCode(childId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate new code")
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
