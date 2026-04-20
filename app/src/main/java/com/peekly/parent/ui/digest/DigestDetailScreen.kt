package com.peekly.parent.ui.digest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.peekly.parent.data.DigestDetailsDto
import com.peekly.parent.data.DigestDto
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.ui.common.AvatarPickerSheet
import com.peekly.parent.ui.common.ChildAvatar
import com.peekly.parent.ui.common.ScreenTimeRing
import com.peekly.parent.ui.common.avatarStyles
import com.peekly.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigestDetailScreen(
    childId: Long,
    childName: String,
    onBack: () -> Unit,
    viewModel: DigestDetailViewModel = viewModel()
) {
    LaunchedEffect(childId) { viewModel.load(childId) }
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = NavyBackground,
        topBar = {
            MediumTopAppBar(
                title = { Text("$childName's Peeks", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor         = NavyBackground,
                    scrolledContainerColor = NavyBackground,
                    titleContentColor      = TextWhite,
                    navigationIconContentColor = TextWhite
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        when (val s = state) {
            is DigestUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PurplePrimary) }

            is DigestUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(s.message, modifier = Modifier.padding(24.dp), color = RedAccent) }

            is DigestUiState.Success -> {
                val hiddenIds = remember { mutableStateOf(emptySet<Long>()) }
                val visible = s.digests.filter { it.id !in hiddenIds.value }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { HeroSection(childId, childName, visible.firstOrNull()) }

                    if (visible.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No peeks yet. Check back after the nightly digest runs.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                "History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                        itemsIndexed(visible) { _, digest ->
                            DigestCard(
                                digest = digest,
                                onHide = { hiddenIds.value = hiddenIds.value + digest.id }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroSection(childId: Long, childName: String, latestDigest: DigestDto?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var avatarIndex by remember { mutableStateOf(0) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(childId) {
        avatarIndex = ParentDataStore.getAvatarIndex(context, childId)
        photoUri = ParentDataStore.getPhotoUri(context, childId)
    }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val uriStr = uri.toString()
            photoUri = uriStr
            scope.launch { ParentDataStore.setPhotoUri(context, childId, uriStr) }
        }
    }

    val ringColor = avatarStyles.getOrElse(avatarIndex) { avatarStyles[0] }.foreground
    val screenTimeRating = latestDigest?.details?.screenTimeRating

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(listOf(PurpleContainer, CardBackground))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(ringColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    ChildAvatar(
                        name = childName,
                        styleIndex = avatarIndex,
                        photoUri = photoUri,
                        size = 72.dp,
                        onClick = { showPicker = true }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(childName, style = MaterialTheme.typography.titleLarge, color = TextWhite, fontWeight = FontWeight.Bold)
                Text("Tap avatar to change photo", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                if (latestDigest != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Last peek: ${formatDate(latestDigest.digestDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PurpleLight
                    )
                }
            }

            if (screenTimeRating != null) {
                ScreenTimeRing(rating = screenTimeRating, size = 64.dp, strokeWidth = 6.dp)
            }
        }
    }

    if (showPicker) {
        AvatarPickerSheet(
            childName = childName,
            currentIndex = avatarIndex,
            photoUri = photoUri,
            onPickStyle = { index ->
                avatarIndex = index
                photoUri = null
                scope.launch {
                    ParentDataStore.setAvatarIndex(context, childId, index)
                    ParentDataStore.setPhotoUri(context, childId, null)
                }
            },
            onPickPhoto = {
                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun DigestCard(digest: DigestDto, onHide: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = PurpleLight
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDate(digest.digestDate),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        digest.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hide") },
                            onClick = { menuExpanded = false; onHide() }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = CardBorder)
                    Text(digest.content, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                    DigestExpandedDetail(digest)
                }
            }
        }
    }
}

@Composable
private fun DigestExpandedDetail(digest: DigestDto) {
    val details = digest.details ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTimeRatingBadge(details.screenTimeRating)
        if (details.warnings.isNotEmpty()) WarningsDetail(details)
        if (details.secondaryApps.isNotEmpty()) AppsDetail(details)
        Text(
            "Generated ${formatDateTime(digest.generatedAt)}",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim
        )
    }
}

@Composable
private fun ScreenTimeRatingBadge(rating: String) {
    val (label, color, container) = when (rating.lowercase()) {
        "low"    -> Triple("Low screen time",    RatingLow,    RatingLowContainer)
        "medium" -> Triple("Medium screen time", RatingMedium, RatingMediumContainer)
        "high"   -> Triple("High screen time",   RatingHigh,   RatingHighContainer)
        else     -> Triple(rating,               PurplePrimary, PurpleContainer)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = container) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WarningsDetail(details: DigestDetailsDto) {
    Surface(shape = RoundedCornerShape(12.dp), color = RedContainer, border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp))
                Text("Age-restricted apps", style = MaterialTheme.typography.labelMedium, color = RedAccent, fontWeight = FontWeight.SemiBold)
            }
            details.warnings.forEach { w ->
                Text(
                    "• ${w.appName} — recommended age ${w.minAge}+",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AppsDetail(details: DigestDetailsDto) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceHighlight, border = BorderStroke(1.dp, CardBorder)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Apps used", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
            details.secondaryApps.forEach { app ->
                val h = app.durationSeconds / 3600
                val m = (app.durationSeconds % 3600) / 60
                val dur = if (h > 0) "${h}h ${m}m" else "${m}m"
                Text(
                    "• ${app.appName ?: app.appPackage}: $dur",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatDate(raw: String): String = try {
    java.time.LocalDate.parse(raw)
        .format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"))
} catch (_: Exception) { raw }

private fun formatDateTime(raw: String): String = try {
    java.time.OffsetDateTime.parse(raw)
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (_: Exception) { raw }
