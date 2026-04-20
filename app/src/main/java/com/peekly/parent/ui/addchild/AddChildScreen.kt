package com.peekly.parent.ui.addchild

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peekly.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(
    onChildCreated: (childId: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: AddChildViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var age  by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AddChildUiState.Success) {
            onChildCreated((state as AddChildUiState.Success).childId)
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add child", color = TextWhite) },
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(PurpleContainer, CardBackground))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = PurpleLight
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Who are you watching over?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Add your child's name and age to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Child's name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor     = PurplePrimary,
                    unfocusedBorderColor   = CardBorder,
                    focusedLabelColor      = PurplePrimary,
                    unfocusedLabelColor    = TextMuted,
                    cursorColor            = PurplePrimary,
                    focusedTextColor       = TextWhite,
                    unfocusedTextColor     = TextWhite,
                    focusedContainerColor  = CardBackground,
                    unfocusedContainerColor = CardBackground
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor     = PurplePrimary,
                    unfocusedBorderColor   = CardBorder,
                    focusedLabelColor      = PurplePrimary,
                    unfocusedLabelColor    = TextMuted,
                    cursorColor            = PurplePrimary,
                    focusedTextColor       = TextWhite,
                    unfocusedTextColor     = TextWhite,
                    focusedContainerColor  = CardBackground,
                    unfocusedContainerColor = CardBackground
                )
            )

            if (state is AddChildUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text((state as AddChildUiState.Error).message, color = RedAccent, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val parsedAge = age.toIntOrNull() ?: return@Button
                    if (name.isBlank()) return@Button
                    viewModel.createChild(name.trim(), parsedAge)
                },
                enabled = state !is AddChildUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (state is AddChildUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = TextWhite)
                } else {
                    Text("Add child", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
