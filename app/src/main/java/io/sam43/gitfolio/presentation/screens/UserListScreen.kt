package io.sam43.gitfolio.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.viewmodels.UserListState
import io.sam43.gitfolio.presentation.viewmodels.UserListViewModel

@Composable
fun UserListScreen(
    modifier: Modifier = Modifier,
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(value.users) { user ->
                    Text(
                        text = user.login,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
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