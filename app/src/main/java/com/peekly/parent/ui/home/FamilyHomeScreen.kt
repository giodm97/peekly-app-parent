package com.peekly.parent.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peekly.parent.data.ChildDto
import com.peekly.parent.data.DigestDto
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.ui.common.ChildAvatar
import com.peekly.parent.ui.common.avatarStyles
import com.peekly.parent.ui.dashboard.ChildSummary
import com.peekly.parent.ui.dashboard.DashboardUiState
import com.peekly.parent.ui.dashboard.DashboardViewModel
import com.peekly.parent.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FamilyHomeScreen(
    onChildTap: (Long, String) -> Unit,
    onAddChild: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: DashboardViewModel = viewModel(viewModelStoreOwner = activity)
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { FamilyHeader(onAddChild = onAddChild) }

        when (val s = state) {
            is DashboardUiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WarmPrimary)
                }
            }
            is DashboardUiState.Error -> item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.message, color = WarmWarn)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
                    ) { Text("Retry") }
                }
            }
            is DashboardUiState.Success -> {
                val children = s.children
                val totalWarnings = children.sumOf { it.latestDigest?.details?.warnings?.size ?: 0 }

                item { FamilySummaryCard(children) }

                if (totalWarnings > 0) {
                    item { FamilyAlertBanner(totalWarnings, onOpenActivity) }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your kids",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmInk
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WarmPrimary)
                                .clickable(onClick = onAddChild),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add child", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                items(children) { summary ->
                    KidCard(summary = summary, onTap = { onChildTap(summary.child.id, summary.child.name) })
                }

                item { QuickActionsSection(onOpenActivity) }
            }
        }
    }
}

@Composable
private fun FamilyHeader(onAddChild: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Good morning ☀️",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarmMuted
            )
            Text(
                "Family today",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = WarmSurface,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, WarmHairline)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = WarmInkSoft, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun FamilySummaryCard(children: List<ChildSummary>) {
    val totalSeconds = children.sumOf { c ->
        c.latestDigest?.details?.secondaryApps?.sumOf { it.durationSeconds } ?: 0L
    }
    val totalH = totalSeconds / 3600
    val totalM = (totalSeconds % 3600) / 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFF9970), WarmPrimary)))
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 90.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Group, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(13.dp))
                Text(
                    "Family · ${children.size} kids",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Row(
                modifier = Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(totalH.toString(), fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 58.sp)
                Text("h", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(bottom = 8.dp))
                Text(totalM.toString(), fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 58.sp, modifier = Modifier.padding(start = 4.dp))
                Text("m", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(bottom = 8.dp))
            }
            Text(
                "Combined screen time today",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                children.forEach { summary ->
                    ChildAvatarStacked(summary.child)
                }
            }
        }
    }
}

@Composable
private fun ChildAvatarStacked(child: ChildDto) {
    val context = LocalContext.current
    var avatarIndex by remember { mutableStateOf(0) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(child.id) {
        avatarIndex = ParentDataStore.getAvatarIndex(context, child.id)
        photoUri = ParentDataStore.getPhotoUri(context, child.id)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(WarmSurface)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        ChildAvatar(name = child.name, styleIndex = avatarIndex, photoUri = photoUri, size = 30.dp)
    }
}

@Composable
private fun FamilyAlertBanner(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ForbiddenBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = WarmSurface) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = ForbiddenFg, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$count apps need your attention",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ForbiddenFg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Above-age apps detected across your family",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA05A42)
            )
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = WarmMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun KidCard(summary: ChildSummary, onTap: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var avatarIndex by remember { mutableStateOf(0) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(summary.child.id) {
        avatarIndex = ParentDataStore.getAvatarIndex(context, summary.child.id)
        photoUri = ParentDataStore.getPhotoUri(context, summary.child.id)
    }

    val digest = summary.latestDigest
    val secondaryApps = digest?.details?.secondaryApps ?: emptyList()
    val totalSeconds = secondaryApps.sumOf { it.durationSeconds }
    val totalH = totalSeconds / 3600
    val totalM = (totalSeconds % 3600) / 60
    val totalFmt = if (totalH > 0) "${totalH}h ${totalM}m" else "${totalM}m"
    val warnings = digest?.details?.warnings?.size ?: 0
    val statusText = digest?.digestDate?.let { "Last peek: $it" } ?: "No data yet"
    val topApps = secondaryApps.sortedByDescending { it.durationSeconds }.take(3)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = WarmSurface,
        border = BorderStroke(1.dp, WarmHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTap)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChildAvatar(
                    name = summary.child.name,
                    styleIndex = avatarIndex,
                    photoUri = photoUri,
                    size = 44.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            summary.child.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmInk
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WarmSurfaceAlt
                        ) {
                            Text(
                                "${summary.child.age}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmMuted
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(WarmGreen))
                        Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = WarmMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(totalFmt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = WarmInk)
                }
            }

            if (topApps.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    topApps.forEach { app ->
                        val weight = (app.durationSeconds.toFloat() / totalSeconds.coerceAtLeast(1)).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .fillMaxHeight()
                                .background(catColor(app.appPackage)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (weight > 0.22f) {
                                Text(
                                    app.appName,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(0.1f)
                            .fillMaxHeight()
                            .background(WarmHairline)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = WarmMuted)
                    Text(digest?.generatedAt?.let { formatTime(it) } ?: "—", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = WarmMuted)
                }
                if (warnings > 0) {
                    Surface(shape = RoundedCornerShape(999.dp), color = ForbiddenBg) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(12.dp), tint = ForbiddenFg)
                            Text("$warnings flagged", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = ForbiddenFg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(onOpenActivity: () -> Unit) {
    Text(
        "Quick actions",
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = WarmInk
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickCard(
                modifier = Modifier.weight(1f),
                iconBg = WarmPrimarySoft, iconTint = WarmPrimary,
                icon = Icons.Outlined.AutoAwesome,
                label = "AI daily recap",
                onClick = onOpenActivity
            )
            QuickCard(
                modifier = Modifier.weight(1f),
                iconBg = WarmPurpleBg, iconTint = WarmPurple,
                icon = Icons.Outlined.Bedtime,
                label = "Pause devices",
                onClick = {}
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickCard(
                modifier = Modifier.weight(1f),
                iconBg = WarmGreenBg, iconTint = WarmGreen,
                icon = Icons.Outlined.School,
                label = "Focus hours",
                onClick = {}
            )
            QuickCard(
                modifier = Modifier.weight(1f),
                iconBg = Color(0xFFFADDD3), iconTint = WarmWarn,
                icon = Icons.Outlined.LocationOn,
                label = "See on map",
                onClick = {}
            )
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier = Modifier,
    iconBg: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = WarmSurface,
        border = BorderStroke(1.dp, WarmHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = iconBg) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = WarmInk, lineHeight = 16.sp)
        }
    }
}

internal fun catColor(category: String): Color = when (category) {
    "social media", "messaging"   -> Color(0xFFFF7B54)
    "short video", "video platform" -> Color(0xFFE86D4E)
    "gaming"                      -> Color(0xFFD88A3D)
    "music"                       -> Color(0xFF8B6DC0)
    "browser"                     -> Color(0xFF7CA8D8)
    else                          -> Color(0xFF9A826F)
}

private fun formatTime(raw: String): String = try {
    java.time.OffsetDateTime.parse(raw)
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))
} catch (_: Exception) { raw }
