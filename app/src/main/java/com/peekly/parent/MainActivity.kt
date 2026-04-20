package com.peekly.parent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.peekly.parent.data.FcmTokenRequest
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.network.ApiClient
import com.peekly.parent.ui.addchild.AddChildScreen
import com.peekly.parent.ui.dashboard.DashboardScreen
import com.peekly.parent.ui.digest.DigestDetailScreen
import com.peekly.parent.ui.home.FamilyHomeScreen
import com.peekly.parent.ui.navigation.Routes
import com.peekly.parent.ui.onboarding.OnboardingScreen
import com.peekly.parent.ui.pairing.PairingCodeScreen
import com.peekly.parent.ui.theme.PeeklyParentTheme
import com.peekly.parent.ui.theme.WarmBg
import com.peekly.parent.ui.theme.WarmMuted
import com.peekly.parent.ui.theme.WarmPrimary
import com.peekly.parent.ui.theme.WarmSurface
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CHILD_NAME = "child_name"
        const val EXTRA_CHILD_ID   = "child_id"
    }

    @Suppress("InvalidFragmentVersionForActivityResult")
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val currentIntent = mutableStateOf<android.content.Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        refreshFcmToken()
        currentIntent.value = intent

        setContent {
            PeeklyParentTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PeeklyNavHost(currentIntent.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent.value = intent
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun refreshFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            android.util.Log.d("PEEKLY_FCM", token)
            lifecycleScope.launch {
                val parentSub = ParentDataStore.getOrCreateParentSub(this@MainActivity)
                ParentDataStore.storeFcmToken(this@MainActivity, token)
                runCatching {
                    ApiClient.instance.registerFcmToken(FcmTokenRequest(parentSub, token))
                }
            }
        }
    }
}

@Composable
private fun PeeklyNavHost(activeIntent: android.content.Intent?) {
    val context = LocalContext.current
    val deepLinkChildId   = activeIntent?.getLongExtra(MainActivity.EXTRA_CHILD_ID, -1L)?.takeIf { it != -1L }
    val deepLinkChildName = activeIntent?.getStringExtra(MainActivity.EXTRA_CHILD_NAME) ?: "Child"

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (ParentDataStore.isOnboardingDone(context)) Routes.MAIN else Routes.ONBOARDING
    }

    if (startDestination == null) return

    val navController = rememberNavController()
    val childNames = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(deepLinkChildId) {
        if (deepLinkChildId != null) {
            navController.navigate(Routes.childDash(deepLinkChildId, deepLinkChildName)) {
                popUpTo(Routes.MAIN) { inclusive = false }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination!!) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(navController = navController)
        }

        composable(Routes.CHILD_DASH) {
            val childId   = it.arguments?.getString("childId")?.toLongOrNull() ?: return@composable
            val childName = it.arguments?.getString("childName") ?: "Child"
            DashboardScreen(
                childId    = childId,
                childName  = childName,
                onBack     = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.ADD_CHILD) {
            AddChildScreen(
                onChildCreated = { childId ->
                    navController.navigate(Routes.pairing(childId)) {
                        popUpTo(Routes.ADD_CHILD) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PAIRING) {
            val childId = it.arguments?.getString("childId")?.toLongOrNull() ?: return@composable
            PairingCodeScreen(
                childId   = childId,
                childName = childNames[childId] ?: "your child",
                onBack    = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DIGEST) {
            val childId   = it.arguments?.getString("childId")?.toLongOrNull() ?: return@composable
            val childName = it.arguments?.getString("childName") ?: "Child"
            DigestDetailScreen(
                childId   = childId,
                childName = childName,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Activity("Activity", Icons.Outlined.QueryStats),
    Settings("Settings", Icons.Outlined.Settings)
}

@Composable
private fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        containerColor = WarmBg,
        bottomBar = {
            PeeklyBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                MainTab.Home -> FamilyHomeScreen(
                    onChildTap = { childId, childName ->
                        navController.navigate(Routes.childDash(childId, childName))
                    },
                    onAddChild = { navController.navigate(Routes.ADD_CHILD) },
                    onOpenActivity = { selectedTab = MainTab.Activity }
                )
                MainTab.Activity -> ActivityPlaceholder()
                MainTab.Settings -> SettingsPlaceholder()
            }
        }
    }
}

@Composable
private fun PeeklyBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), clip = false)
                .clip(RoundedCornerShape(32.dp))
                .background(WarmSurface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) WarmPrimary else Color.Transparent)
                        .clickable { onSelect(tab) }
                        .padding(horizontal = if (isSelected) 18.dp else 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) Color.White else WarmMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    if (isSelected) {
                        Text(
                            text = tab.label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Activity — coming soon", color = WarmMuted, fontSize = 15.sp)
    }
}

@Composable
private fun SettingsPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings — coming soon", color = WarmMuted, fontSize = 15.sp)
    }
}
