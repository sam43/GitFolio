package io.sam43.gitfolio.presentation.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import io.sam43.gitfolio.R
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.SETTINGS_SCREEN
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.USERS_SCREEN


data class BottomNavigationItem(
    @StringRes val title: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavigationItem(
        title = R.string.title_users,
        selectedIcon = R.drawable.menu_users_selected,
        unselectedIcon = R.drawable.menu_users_unselected,
        route = USERS_SCREEN
    ),
    BottomNavigationItem(
        title = R.string.title_settings,
        selectedIcon = R.drawable.menu_settings_selected,
        unselectedIcon = R.drawable.menu_settings_unselected,
        route = SETTINGS_SCREEN
    )
)
class AppNavigation {
    companion object {
        /**
         * Navigates to the given route using the provided NavController.
         *
         * @param navController The NavController instance to use for navigation.
         * @param route The destination route.
         * @param navOptions Optional NavOptions for customizing navigation behavior (e.g., popUpTo).
         * @param navigatorExtras Optional Navigator.Extras for passing specific extras to the navigator.
         */
        const val USERS_SCREEN = "users_screen"
        const val USER_PROFILE_SCREEN = "user_profile_screen"
        const val SETTINGS_SCREEN = "settings_screen"

        fun navigateTo(
            navController: NavController,
            route: String,
            navOptions: NavOptions? = null,
            navigatorExtras: Navigator.Extras? = null
        ) {
            if (navController.currentDestination?.route != route) {
                navController.navigate(route, navOptions, navigatorExtras)
            }
        }

        fun navigateToWithPopUp(
            navController: NavController,
            route: String,
            popUpToRoute: String,
            inclusive: Boolean = false
        ) {
            if (navController.currentDestination?.route != route) {
                navController.navigate(route) {
                    popUpTo(popUpToRoute) {
                        this.inclusive = inclusive
                    }
                    launchSingleTop = true // Optional: Avoid multiple copies of the same destination on top
                }
            }
        }
    }
}