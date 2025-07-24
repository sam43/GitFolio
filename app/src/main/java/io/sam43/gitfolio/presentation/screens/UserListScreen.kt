@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.sam43.gitfolio.presentation.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.common.AppNavigation
import io.sam43.gitfolio.presentation.common.AppNavigation.Companion.USER_PROFILE_SCREEN
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.common.ErrorScreen
import io.sam43.gitfolio.presentation.common.LoadImageWith
import io.sam43.gitfolio.presentation.viewmodels.UserListViewModel
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.presentation.state.ListUiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun UserListScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    searchQuery: String? = "",
    viewModel: UserListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.isLoading -> CenteredCircularProgressIndicator()
        state.items.isNotEmpty() -> UserList(
            userListState = state,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            searchQuery = searchQuery ?: "",
        ) { user -> navController.navigateToProfile(user) }
        state.error != null -> ErrorScreen(error = state.error ?: ErrorType.UnknownError())
    }
}

private fun NavController.navigateToProfile(user: User) {
    val encodedAvatarUrl = URLEncoder.encode(user.avatarUrl, StandardCharsets.UTF_8.name())
    val encodedDisplayName = URLEncoder.encode(user.login, StandardCharsets.UTF_8.name())
    AppNavigation.navigateTo(
        navController = this,
        route = "${USER_PROFILE_SCREEN}/${user.login}?avatarUrl=$encodedAvatarUrl&displayName=$encodedDisplayName"
    )
}

@Composable
fun UserList(
    userListState: ListUiState<User>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    searchQuery: String,
    onItemClick: (User) -> Unit = {}
) {
    val filteredUsers = remember(userListState.items, searchQuery) {
        if (searchQuery.isBlank()) {
            userListState.items
        } else {
            userListState.items.filter { user ->
                user.login.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    LazyColumn {
        items(filteredUsers, key = { user -> user.login }) { user ->
            UserListItem(
                user = user,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onItemClick(user) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 88.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit = {}
) {
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
                    .clip(RectangleShape)
                    .clickable(onClick = onClick)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "user-avatar-${user.login}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = user.login,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "username-${user.login}"),
                        animatedVisibilityScope = animatedVisibilityScope
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