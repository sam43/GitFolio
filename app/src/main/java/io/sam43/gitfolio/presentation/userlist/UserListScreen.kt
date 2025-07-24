package io.sam43.gitfolio.presentation.userlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.common.AppNavigation
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.common.ErrorScreen
import io.sam43.gitfolio.presentation.common.LoadImageWith

import io.sam43.gitfolio.presentation.userlist.UserListContract.Event
import io.sam43.gitfolio.presentation.userlist.UserListContract.Effect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserListScreen(navController: NavController, viewModel: UserListViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.ShowErrorToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.error != null && state.users.isEmpty()) {
                ErrorScreen(error = state.error) { viewModel.setEvent(Event.FetchUsers) }
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.users) { user ->
                        UserListItem(user) {
                            navController.navigate(
                                AppNavigation.USER_PROFILE_SCREEN + "/" + user.login + "?avatarUrl=" + user.avatarUrl + "&displayName=" + user.login
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                    if (state.isLoading) {
                        item {
                            CenteredCircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    if (state.canLoadMore && !state.isLoading && listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.users.lastIndex) {
                        viewModel.setEvent(Event.LoadMoreUsers)
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User, onClick: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(user) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        user.avatarUrl.LoadImageWith(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
        )
        Column {
            Text(
                text = user.login,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.login,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}