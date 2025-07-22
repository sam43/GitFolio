@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.sam43.gitfolio.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.common.AppNavigation
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.USER_PROFILE_SCREEN
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.common.ErrorScreen
import io.sam43.gitfolio.presentation.common.LoadImageWith
import io.sam43.gitfolio.presentation.viewmodels.UserListState
import io.sam43.gitfolio.presentation.viewmodels.UserListViewModel

@Composable
fun UserListScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    viewModel: UserListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    if (state.isLoading && state.error.isNullOrEmpty()) {
        CenteredCircularProgressIndicator()
    } else if (!state.isLoading && state.error.isNullOrEmpty()) {
        UserListView(state, sharedTransitionScope) { user ->
            // Pass user data through navigation arguments
            AppNavigation.navigateTo(
                navController = navController, 
                route = "${USER_PROFILE_SCREEN}/${user.login}?avatarUrl=${user.avatarUrl}&displayName=${user.login}"
            )
        }
    } else {
        ErrorScreen(errorText = state.error ?: "")
    }
}

@Composable
fun UserListView(
    value: UserListState,
    sharedTransitionScope: SharedTransitionScope,
    onItemClick: (User) -> Unit
) {
    UserList(userListState = value, sharedTransitionScope = sharedTransitionScope) { onItemClick(it) }
}
// In your UserListScreen.kt where UserList is defined
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserList(
    userListState: UserListState, // Assuming this is your data state
    sharedTransitionScope: SharedTransitionScope,
    onItemClick: (User) -> Unit = {} // Added onItemClick based on your project code
) {
    AnimatedContent(
        targetState = userListState, // Or whatever drives the list updates
        transitionSpec = {
            // This transitionSpec is for how the UserList itself animates when its content changes
            fadeIn(animationSpec = tween(durationMillis = 1500)) togetherWith fadeOut(animationSpec = tween(durationMillis = 1500))
        },
        label = "UserListAnimation" // Good practice to add a label
    ) { targetState -> // targetState is the current UserListState

        // THIS `this` is the AnimatedVisibilityScope from the AnimatedContent above
        val lazyColumnAnimatedVisibilityScope = this

        LazyColumn {
            items(targetState.users, key = { user -> user.id }) { user -> // Add a key to items for better performance
                UserListItem(
                    user = user,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = lazyColumnAnimatedVisibilityScope, // Pass the scope here
                    onClick = { onItemClick(user) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 88.dp, end = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserListItem(
    user: User,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope, // Pass this from the parent AnimatedContent
    onClick: () -> Unit = {}
) {
    // No need for AnimatedContent here if the item itself isn't animating its own content changes
    // based on the 'user' object changing TO a NEW user object for the SAME list item instance.
    // The shared element transition is about this item animating to/from another screen.

    with(sharedTransitionScope) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            user.avatarUrl.LoadImageWith(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "user-avatar-${user.login}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        renderInOverlayDuringTransition = true,
                        zIndexInOverlay = 0f
                    )
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = user.login,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "username-${user.login}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        renderInOverlayDuringTransition = true,
                        zIndexInOverlay = 0f
                    )
            )
        }
    }
}


@Composable
fun UserListItemPreview(user: User, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        user.avatarUrl.LoadImageWith(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = user.login,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUserListScreen() {
    fun dummyList(): List<User> {
        val dummyList = mutableListOf<User>()
        for (i in 1..20) {
            dummyList.add(User(i,"user$i","https://example.com/avatar$i.jpg", "https://github.com/user$i", "type$i"))
        }
        return dummyList
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(dummyList()) { user ->
            UserListItemPreview(user = user) { }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 88.dp, end = 8.dp)
            )
        }
    }
}