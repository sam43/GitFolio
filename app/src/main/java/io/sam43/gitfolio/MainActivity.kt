package io.sam43.gitfolio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.sam43.gitfolio.presentation.common.AppNavigation
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.SETTINGS_SCREEN
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.USERS_SCREEN
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.USER_PROFILE_SCREEN
import io.sam43.gitfolio.presentation.common.NetworkUiEvent
import io.sam43.gitfolio.presentation.common.bottomNavItems
import io.sam43.gitfolio.presentation.common.theme.GitFolioTheme
import io.sam43.gitfolio.presentation.screens.GithubProfileScreen
import io.sam43.gitfolio.presentation.screens.SearchBox
import io.sam43.gitfolio.presentation.screens.SettingsScreen
import io.sam43.gitfolio.presentation.screens.UserListScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appInstance: App
        get() = applicationContext as App
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            appInstance.networkUiEvents.collectLatest { event ->
                when (event) {
                    is NetworkUiEvent.ShowToast -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        setContent {
            GitFolioTheme {
                val navController = rememberNavController()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentScreenTitle = bottomNavItems.find { it.route == currentRoute }?.title
                    ?: R.string.empty_string

                val selectedItemIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }
                    .takeIf { it >= 0 } ?: 0
              
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (getString(currentScreenTitle).isNotEmpty()) TopAppBar(
                            title = { Text(
                                text = getString(currentScreenTitle),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1
                                ) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            tonalElevation = 4.dp,
                        ) {
                            bottomNavItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItemIndex == index,
                                    onClick = {
                                        AppNavigation.navigateTo(
                                            navController = navController,
                                            route = item.route,
                                            navOptions = androidx.navigation.navOptions {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = getString(item.title),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if(index == selectedItemIndex) FontWeight.Bold else FontWeight.Normal,
                                            textDecoration = if(index == selectedItemIndex) TextDecoration.Underline else TextDecoration.None,
                                            ) },
                                    icon = {
                                        val iconResId = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else {
                                            item.unselectedIcon
                                        }
                                        Icon(
                                            painter = painterResource(id = iconResId),
                                            contentDescription = getString(item.title),
                                            tint = if (index == selectedItemIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }
                    }
                    ) { innerPadding -> AppMain(Modifier.padding(innerPadding), navController) }
            }
        }
    }
}

@Composable
fun LandingScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBox(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        UserListScreen(navController)
    }
}

@Composable
fun AppMain(modifier: Modifier, navController: NavHostController) {
    BackHandler(enabled = true) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }
    NavHost(
        navController = navController,
        startDestination = bottomNavItems.firstOrNull()?.route ?: USERS_SCREEN,
        modifier = modifier
    ) {
        composable(USERS_SCREEN) {
            LandingScreen(navController = navController)
        }
        composable("${USER_PROFILE_SCREEN}/{user_name}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("user_name") ?: ""
            GithubProfileScreen(navController, userName)
        }
        composable(SETTINGS_SCREEN) {
            SettingsScreen(navController)
        }
    }
}