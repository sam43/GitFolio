package io.sam43.gitfolio.presentation.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.viewmodels.UserListState
import io.sam43.gitfolio.presentation.viewmodels.UserListViewModel

@Composable
fun UserListScreen(
    navController: NavController,
    viewModel: UserListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState()
    UserListView(state.value)
}

@Composable
fun UserListView(value: UserListState) {
    when {
        value.isLoading -> {
            CenteredCircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        value.error != null -> {
            Text(text = "Error: ${value.error}")
        }
        else -> {
            UserList(users = value.users)
        }
    }
}
@Composable
fun UserList(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserListItem(user = user)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 88.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "${user.login} avatar",
            contentScale = ContentScale.Inside,
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
    UserListView(
        UserListState(
            users = dummyList(),
            isLoading = false,
            error = null
        )
    )
}