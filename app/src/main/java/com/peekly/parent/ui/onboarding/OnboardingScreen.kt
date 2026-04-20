package com.peekly.parent.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(val icon: ImageVector, val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(Icons.Outlined.Visibility,         "Welcome to Peekly",    "Stay connected to your child's digital world — gently and privately. No spying, just awareness."),
    OnboardingPage(Icons.Outlined.NotificationsActive, "How it works",         "Pair Peekly with your child's device in seconds. Every evening you receive a digest of their app usage."),
    OnboardingPage(Icons.Outlined.Lock,                "Your privacy promise", "Peekly never reads messages, photos, or search queries. Only app names and time spent — nothing more.")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLast = pagerState.currentPage == pages.lastIndex

    fun complete() { scope.launch { ParentDataStore.setOnboardingDone(context); onFinish() } }

    Scaffold(containerColor = NavyBackground) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TextButton(
                onClick = { complete() },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) { Text("Skip", color = TextMuted) }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(bottom = 148.dp)
            ) { index -> OnboardingPageContent(pages[index]) }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PagerDots(count = pages.size, current = pagerState.currentPage)

                Button(
                    onClick = {
                        if (isLast) complete()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text(
                        if (isLast) "Get started" else "Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = PurpleContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(page.icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = PurpleLight)
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(page.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextWhite, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(page.body, style = MaterialTheme.typography.bodyLarge, color = TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PagerDots(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { i ->
            val width by animateDpAsState(targetValue = if (i == current) 24.dp else 8.dp, label = "dot")
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (i == current) PurplePrimary else CardBorder)
            )
        }
    }
}
