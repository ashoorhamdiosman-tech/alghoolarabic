package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReferenceScreen
import com.example.ui.screens.SchoolsScreen
import com.example.ui.screens.SupervisionScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldTheme
import com.example.ui.theme.MintContainer
import com.example.ui.viewmodel.AppViewModel

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Mandated RTL Layout Direction for Arabic UI
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmeraldTheme {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AppViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val navItems = listOf(
        NavItem("المؤشرات", Icons.Default.Dashboard, Icons.Default.Dashboard, "tab_dashboard"),
        NavItem("المدارس", Icons.Default.School, Icons.Default.School, "tab_schools"),
        NavItem("المتابعة", Icons.Default.Assignment, Icons.Default.Assignment, "tab_supervision"),
        NavItem("المرجعية", Icons.Default.MenuBook, Icons.Default.MenuBook, "tab_reference")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val selected = currentTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = MintContainer,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    0 -> DashboardScreen(viewModel = viewModel)
                    1 -> SchoolsScreen(viewModel = viewModel)
                    2 -> SupervisionScreen(viewModel = viewModel)
                    3 -> ReferenceScreen(viewModel = viewModel)
                }
            }
        }
    }
}
