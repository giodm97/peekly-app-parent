package com.peekly.parent.ui.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.peekly.parent.data.DigestDetailsDto
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.data.SecondaryAppDto
import com.peekly.parent.ui.common.ChildAvatar
import com.peekly.parent.ui.home.catColor
import com.peekly.parent.ui.theme.*

@Composable
fun DashboardScreen(
    childId: Long,
    childName: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: DashboardViewModel = viewModel(viewModelStoreOwner = activity)
    val state by viewModel.state.collectAsState()

    val allChildren = (state as? DashboardUiState.Success)?.children ?: emptyList()
    val summary = allChildren.find { it.child.id == childId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            ChildDashHeader(childName = summary?.child?.name ?: childName, onBack = onBack)
        }

        if (allChildren.size > 1) {
            item {
                ChildSwitcherRow(
                    allChildren = allChildren,
                    activeId = childId,
                    onSwitch = { id, name -> onNavigate(com.peekly.parent.ui.navigation.Routes.childDash(id, name)) }
                )
            }
        }

        if (summary == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    if (state is DashboardUiState.Loading) {
                        CircularProgressIndicator(color = WarmPrimary)
                    } else {
                        Text("No data for this child.", color = WarmMuted)
                    }
                }
            }
            return@LazyColumn
        }

        val digest = summary.latestDigest
        val details = digest?.details

        item { ChildSummaryCard(childName = summary.child.name, digest = digest, details = details) }

        if (!details?.warnings.isNullOrEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ForbiddenBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = ForbiddenFg, modifier = Modifier.size(16.dp))
                    Text(
                        "${details!!.warnings.size} app${if (details.warnings.size > 1) "s" else ""} above ${summary.child.name}'s age (${summary.child.age})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ForbiddenFg
                    )
                }
            }
        }

        if (details != null) {
            val cats = buildCategoryTotals(details)
            if (cats.isNotEmpty()) {
                item {
                    SectionHeader("By category")
                }
                item {
                    CategoryGrid(cats)
                }
            }

            val topApps = details.secondaryApps.sortedByDescending { it.durationSeconds }.take(4)
            if (topApps.isNotEmpty()) {
                item { SectionHeader("Most used today") }
                items(topApps) { app ->
                    AppRow(app = app, childAge = summary.child.age)
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No digest available yet.", color = WarmMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ChildDashHeader(childName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = WarmSurface, border = BorderStroke(1.dp, WarmHairline), shadowElevation = 1.dp) {
            Box(modifier = Modifier.size(42.dp).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = WarmInkSoft, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            "Here's $childName's day",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(shape = RoundedCornerShape(14.dp), color = WarmSurface, border = BorderStroke(1.dp, WarmHairline), shadowElevation = 1.dp) {
            Box(modifier = Modifier.size(42.dp).clickable { }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = WarmInkSoft, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ChildSwitcherRow(
    allChildren: List<ChildSummary>,
    activeId: Long,
    onSwitch: (Long, String) -> Unit
) {
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(allChildren) { s ->
            val isActive = s.child.id == activeId
            var avatarIndex by remember(s.child.id) { mutableStateOf(0) }
            var photoUri by remember(s.child.id) { mutableStateOf<String?>(null) }
            LaunchedEffect(s.child.id) {
                avatarIndex = ParentDataStore.getAvatarIndex(context, s.child.id)
                photoUri = ParentDataStore.getPhotoUri(context, s.child.id)
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isActive) WarmInk else WarmSurface,
                border = BorderStroke(1.5.dp, if (isActive) WarmInk else WarmHairline)
            ) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = !isActive) { onSwitch(s.child.id, s.child.name) }
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.White.copy(alpha = 0.2f) else WarmPrimarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        ChildAvatar(name = s.child.name, styleIndex = avatarIndex, photoUri = photoUri, size = 30.dp)
                    }
                    Column {
                        Text(s.child.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isActive) Color.White else WarmInk)
                        Text("${s.child.age} yrs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isActive) Color.White.copy(alpha = 0.7f) else WarmMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildSummaryCard(childName: String, digest: com.peekly.parent.data.DigestDto?, details: DigestDetailsDto?) {
    val totalSeconds = details?.secondaryApps?.sumOf { it.durationSeconds } ?: 0L
    val totalH = totalSeconds / 3600
    val totalM = (totalSeconds % 3600) / 60
    val dateLabel = digest?.digestDate ?: "today"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFF9970), WarmPrimary)))
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 56.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(13.dp))
                Text(dateLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
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
            Text("Total screen time today", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 22.dp, top = 20.dp, bottom = 10.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = WarmInk
    )
}

@Composable
private fun CategoryGrid(cats: List<Pair<String, Long>>) {
    val maxSecs = cats.maxOfOrNull { it.second } ?: 1L
    Column(
        modifier = Modifier.padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (cat, secs) ->
                    val (bg, fg) = catStyle(cat)
                    val mins = secs / 60
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = WarmSurface,
                        border = BorderStroke(1.dp, WarmHairline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Surface(shape = RoundedCornerShape(12.dp), color = bg) {
                                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                    Icon(catIcon(cat), contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(catDisplayName(cat), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WarmMuted)
                            Text(
                                if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmInk
                            )
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(WarmHairline)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(secs.toFloat() / maxSecs)
                                        .fillMaxHeight()
                                        .background(fg)
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AppRow(app: SecondaryAppDto, childAge: Int) {
    val (bg, fg) = catStyle(app.appPackage)
    val mins = app.durationSeconds / 60
    val timeFmt = if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        color = WarmSurface,
        border = BorderStroke(1.dp, WarmHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = catColor(app.appPackage)) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(catIcon(app.appPackage), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(app.appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = WarmInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(catDisplayName(app.appPackage), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = WarmMuted)
            }
            Text(timeFmt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = WarmInk)
        }
    }
}

private fun buildCategoryTotals(details: DigestDetailsDto): List<Pair<String, Long>> {
    return details.secondaryApps
        .groupBy { it.appPackage }
        .map { (cat, apps) -> cat to apps.sumOf { it.durationSeconds } }
        .sortedByDescending { it.second }
        .take(4)
}

private fun catStyle(category: String): Pair<Color, Color> = when (category) {
    "social media", "messaging"     -> Pair(WarmPrimarySoft, WarmPrimary)
    "short video", "video platform" -> Pair(Color(0xFFFADDD3), WarmWarn)
    "gaming"                        -> Pair(Color(0xFFFFF0D6), Color(0xFFD88A3D))
    "music"                         -> Pair(WarmPurpleBg, WarmPurple)
    "browser", "email", "maps"      -> Pair(WarmGreenBg, WarmGreen)
    else                            -> Pair(Color(0xFFE4D9CC), Color(0xFF7A5C4A))
}

private fun catDisplayName(cat: String): String = when (cat) {
    "social media"                  -> "Social"
    "short video", "video platform" -> "Video"
    "gaming"                        -> "Gaming"
    "messaging"                     -> "Chat"
    "music"                         -> "Music"
    "browser"                       -> "Browser"
    "email"                         -> "Email"
    else                            -> "Other"
}

private fun catIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    "social media"                  -> Icons.Outlined.People
    "messaging"                     -> Icons.Outlined.Chat
    "short video", "video platform" -> Icons.Outlined.PlayCircle
    "gaming"                        -> Icons.Outlined.SportsEsports
    "music"                         -> Icons.Outlined.MusicNote
    "browser"                       -> Icons.Outlined.Language
    "email"                         -> Icons.Outlined.Email
    else                            -> Icons.Outlined.Apps
}
